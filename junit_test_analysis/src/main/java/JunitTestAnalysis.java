import soot.*;
import soot.jimple.*; 
import soot.jimple.AssignStmt;
import soot.jimple.IfStmt;
import soot.jimple.ThisRef;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.FlowSet;
import soot.jimple.toolkits.callgraph.Targets;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.*;
import soot.jimple.toolkits.annotation.callgraph.CallGraphGrapher;
import soot.tagkit.LineNumberTag;
import soot.jimple.internal.InvokeExprBox;
import soot.jimple.internal.RValueBox;
import soot.jimple.internal.*;

import soot.util.*;
import soot.Type;
import soot.tagkit.*;
import soot.toolkits.graph.*;

import java.util.*;
import java.lang.Exception;

public class JunitTestAnalysis implements AnalysisUtil {
    private CallGraph cg = null;
    //private static final String junitTestKey = "Lorg/junit/AfterClass";
    //private static final String junitTestKey = "Lorg/junit/BeforeClass";
    private static final String junitTestKey = "Lorg/junit/Test";
    private static final int exploreDepth = 8;
    private static final int occurance = 1;
    private static List<String> excludePackagesList = new ArrayList<String>();
    private int junitTests = 0;
    private String classPath = null;
    private List<String> processDirs = null;
    private List<String> clusterKeys = null;
    private List<StartAfterStop> sasList = null;
    /* raw data for each method */
    private Map<String, Map<String, Integer>> clusterKeyStatByMethod = new HashMap<String, Map<String, Integer>>();
   
    private SootClass miniHBaseClusterClass = null;

    static { 
	excludePackagesList.add("java."); 
	excludePackagesList.add("android."); 
	excludePackagesList.add("javax."); 
	excludePackagesList.add("android.support."); 
	excludePackagesList.add("sun."); 
	excludePackagesList.add("com.google."); 
	excludePackagesList.add("com.android."); 
	excludePackagesList.add("dalvik."); 
	excludePackagesList.add("org.junit."); 
    }

    public JunitTestAnalysis(String classPath, List<String> processDirs, List<String> clusterKeys, List<StartAfterStop> sasList) {
        this.classPath = classPath;
        this.processDirs = processDirs;
	this.clusterKeys = clusterKeys;
	this.sasList = sasList;
    }

    public void start() {	
	if (processDirs == null || clusterKeys == null) {
	    System.out.println("processDirs or clusterKeys is null, exit.");
	    System.exit(1);
	}

        Options.v().set_whole_program(true);  // process whole program
        Options.v().set_allow_phantom_refs(true); // load phantom references
        Options.v().set_prepend_classpath(true); // prepend class path
        Options.v().set_src_prec(Options.src_prec_class); // process only .class files
        Options.v().set_process_dir(processDirs); // process all .class files in directory
        Options.v().set_no_bodies_for_excluded(true);
	Options.v().set_exclude(excludePackagesList);
        Options.v().set_soot_classpath(classPath); // classpath
        
        Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
        Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
        Options.v().set_keep_line_number(true);
        Scene.v().loadNecessaryClasses();
        CHATransformer.v().transform();
        cg = Scene.v().getCallGraph();

	List<SootClass> sootClassList = Scene.v().getClasses(1);
	for (SootClass sootClass : sootClassList) {
            if (sootClass.getName().equals("org.apache.hadoop.hbase.MiniHBaseCluster")) {
                myPrint("yes:" + sootClass.getName());
                miniHBaseClusterClass = sootClass;
            }
	    for (SootMethod sootMethod : sootClass.getMethods()) {
		boolean isJunitTest = false;
    	        for (Tag tag : sootMethod.getTags()) {
    	            if (tag instanceof VisibilityAnnotationTag) {
    	                VisibilityAnnotationTag vaTag = (VisibilityAnnotationTag) tag;
	    	        for (AnnotationTag aTag : vaTag.getAnnotations()) {
			    if (aTag.getType().contains(junitTestKey)) {
				isJunitTest = true;
	            		junitTests ++;
				break;
	    		    }
	    	        }
		    }	
		    if (isJunitTest)
			break;
    	        }

		if (isJunitTest) {
                    /* create cluster key stat for this method */
		    Map<String, Integer> clusterKeyStat = new HashMap<String, Integer>();
		    for (String key : clusterKeys) {
			clusterKeyStat.put(key, 0);
		    }

                    if (sasList != null) {
		        /* IMPORTANT: clear up for every round */
		        for (StartAfterStop sas : sasList) {
			    sas.hasStopped = false;
			    sas.startAfterStop = false;
			    if (sas.stopKey.equals("")) {
			        sas.hasStopped = true;
			    }
		        }
                    }

                    //if (sootMethod.toString().contains("TestClientClusterMetrics")) {
                    // go through the method
		    goThroughMethod(clusterKeyStat, sootMethod, 0);
                    //}
                    
		    /* combine the result of this method to global stat */
		    clusterKeyStatByMethod.put(sootMethod.toString(), clusterKeyStat);
                    if (sasList != null) {
		        for (StartAfterStop sas : sasList) {
		            if (sas.startAfterStop == true) {
			        sas.methodSet.add(sootMethod.toString());
		            }
		        }
                    }
		}
	    }
	}
	    
        System.out.println("exploreDepth: " + exploreDepth);
        System.out.println("num of classes: " + sootClassList.size());
        System.out.println("num of junitTests: " + junitTests);
    }

    private void goThroughMethod(Map<String, Integer> clusterKeyStat, 
				SootMethod sootMethod, int level) {
	String levelPrefix = "[" + level + "] ";
	myPrint(levelPrefix + sootMethod);
        Body b = null;
        try {
	    b = sootMethod.retrieveActiveBody(); 
        } catch (Exception e) {
            if (e.toString().contains("org.apache.hadoop.hbase.HBaseCluster")
                    && e.toString().contains("No method source set")) {
                SootMethod concreteMethod = miniHBaseClusterClass.getMethodByName(sootMethod.getName()); 
                if (concreteMethod != null) {
                    try {
                        /* concern: even though hasActiveBody() = false, retrieveActiveBody() still works.*/
                        b = concreteMethod.retrieveActiveBody();
                        System.out.println(levelPrefix + "abstract method replacement succeed: " + concreteMethod 
                                + " replaced " + sootMethod);
                        sootMethod = concreteMethod;
                    } catch (Exception ee) {
                        System.out.println("Error: still cannot fetch active body. exit");
                        System.exit(1);
                    }
                } else {
                    System.out.println("Error: abstract method replacement failed. exit");
                    System.exit(1);
                }
            } else {
                throw e;
            }
        } 

        UnitGraph graph = new ExceptionalUnitGraph(b);
	for (Unit unit : graph) {
	    myPrint(levelPrefix + "unit:" + unit);

	    /* cluster key analysis */
            for (String key : clusterKeyStat.keySet()) { 
		if (unit.toString().contains(key)) {
		    clusterKeyStat.put(key, clusterKeyStat.get(key) + 1);
		}
	    }

            /* start after stop analysis */
	    if (sasList != null) {
	        for (StartAfterStop sas : sasList) { 
	    	    //if (unit.toString().contains(sas.stopKey) ) {
	    	    if (unit.toString().contains(sas.stopKey) && sas.hasStopped != true) {
		        sas.hasStopped = true;
                        myPrint(levelPrefix + "unit:" + unit);
		    }
	            if (unit.toString().contains(sas.startKey) && sas.hasStopped == true && sas.startAfterStop != true) {
	                sas.startAfterStop = true;
                        myPrint("");
                        myPrint("!!!!![start after stop] " + levelPrefix + "unit:" + unit);
                        myPrint("");
		    }
	        }
	    }

            /* recursive control */
	    if (unit instanceof Stmt) {
                Stmt stmt = (Stmt) unit;
                if (stmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = stmt.getInvokeExpr(); 
                    myPrint(levelPrefix + "This Stmt contains InvokeExpr " + invokeExpr);
                    try {
                        SootMethod invokedMethod = invokeExpr.getMethod();
                        if (level <= exploreDepth) {
                            goThroughMethod(clusterKeyStat, invokedMethod, level + 1);
                        }
                    } catch (Exception e) {
                        myPrint(levelPrefix + "touched the boundary:");
                        myPrint(levelPrefix + " " + e);
                    }
                }
            }
	}
	return;
    }

    private int methodsInvolvingCluster(Set<String> methodSet, Map<String, Set<String>> methodSetByClusterKey) {
	int num = 0;
	for (String method : methodSet) {
	    boolean found = false;
	    for (String key : methodSetByClusterKey.keySet()) {
		Set<String> setOfKey = methodSetByClusterKey.get(key);
		for (String m : setOfKey) {
		    if (method == m) {
			found = true;
			num ++;
		 	break;
		    }
		}
                /* break early */
		if (found) {
		    break;
		}
	    }
	}
	return num;
    }

    private void myPrint(String s) {
        //System.out.println(s);
        ;
    }

    @Override
    public void analysis() {
        Map<String, Set<String>> methodSetByClusterKey = new HashMap<String, Set<String>>();
        for (String key : clusterKeys) {
            methodSetByClusterKey.put(key, new HashSet<String>());
        }

        for (String method : clusterKeyStatByMethod.keySet()) {
            Map<String, Integer> clusterKeyStat =  clusterKeyStatByMethod.get(method);
            for (String key : clusterKeyStat.keySet()) {
                if (clusterKeyStat.get(key) >= occurance) {
                    methodSetByClusterKey.get(key).add(method);
                }
            }
        }

        for (String key : clusterKeys) {
            System.out.println("num of methods involving " + key + " : " + methodSetByClusterKey.get(key).size());
        }

        if (sasList != null) {
	    for (StartAfterStop sas : sasList) {
	        System.out.println("num of " + sas + " : " + sas.methodSet.size());
                System.out.println("num of " + sas + " (wrt cluster keys) : " 
                                  + methodsInvolvingCluster(sas.methodSet, methodSetByClusterKey));
	    }
        }
    }

    /* Helper Class */
    static class StartAfterStop {
	private String stopKey = null;
	private String startKey = null;
	private Set<String> methodSet = null;
	private Boolean hasStopped = false; // always set to false for each method iteration
	private Boolean startAfterStop = false; // always set to false for each method iteration

	public StartAfterStop(String stopKey, String startKey) {
	    this.stopKey = stopKey;
	    this.startKey = startKey;
	    this.methodSet = new HashSet<String>();
	}

	@Override
	public String toString() {
	    return stopKey != "" ? stopKey + " --> " + startKey : "solely " + startKey;
	}
    }
}
