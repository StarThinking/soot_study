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
    private static final String junitTestKey1 = "Lorg/junit/AfterClass";
    private static final String junitTestKey2 = "Lorg/junit/BeforeClass";
    private static final String junitTestKey3 = "Lorg/junit/Test";
    private static final int exploreDepth = 7;
    private static final int occurance = 1;
    private static List<String> excludePackagesList = new ArrayList<String>();
    private int junitTests = 0;
    private List<String> processDirs = null;
    private List<String> clusterKeys = null;
    private List<StartAfterStop> sasList = null;
    // raw data by for each method
    private Map<String, Map<String, Integer>> clusterKeyStatByMethod = new HashMap<String, Map<String, Integer>>();
   
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

    public JunitTestAnalysis(List<String> processDirs, List<String> clusterKeys, List<StartAfterStop> sasList) {
        this.processDirs = processDirs;
	this.clusterKeys = clusterKeys;
	this.sasList = sasList;
    }

    public void start() {	
	if (processDirs == null || clusterKeys == null) {
	    System.out.println("processDirs or clusterKeys is null, exit.");
	    System.exit(1);
	}

	// NO CLASSPATH
          String classPath = "/root/hbase-2.2.1/hbase-server/target/hbase-server-2.2.1-tests.jar";
          classPath += ":/root/hbase-2.2.1/hbase-server/target/hbase-server-2.2.1.jar";
/*        String classPath = "/root/hbase-2.2.1/hbase-rsgroup/target/hbase-rsgroup-2.2.1-sources.jar"; 
classPath += ":/root/hbase-2.2.1/hbase-rsgroup/target/hbase-rsgroup-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-rsgroup/target/hbase-rsgroup-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-rsgroup/target/hbase-rsgroup-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-examples/target/hbase-examples-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-examples/target/hbase-examples-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-examples/target/hbase-examples-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-examples/target/hbase-examples-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-annotations/target/hbase-annotations-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-annotations/target/hbase-annotations-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-common/target/hbase-common-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-common/target/hbase-common-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-common/target/hbase-common-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-common/target/hbase-common-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop-compat/target/hbase-hadoop-compat-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop-compat/target/hbase-hadoop-compat-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop-compat/target/hbase-hadoop-compat-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop-compat/target/hbase-hadoop-compat-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol-shaded/target/hbase-protocol-shaded-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol-shaded/target/hbase-protocol-shaded-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol-shaded/target/hbase-protocol-shaded-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol-shaded/target/hbase-protocol-shaded-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol-shaded/target/original-hbase-protocol-shaded-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-it/target/hbase-it-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-it/target/hbase-it-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol/target/hbase-protocol-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol/target/hbase-protocol-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol/target/hbase-protocol-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-protocol/target/hbase-protocol-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-client/target/hbase-client-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-client/target/hbase-client-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-client/target/hbase-client-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-client/target/hbase-client-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-thrift/target/hbase-thrift-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-thrift/target/hbase-thrift-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-thrift/target/hbase-thrift-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-thrift/target/hbase-thrift-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-checkstyle/target/hbase-checkstyle-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-checkstyle/target/hbase-checkstyle-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics/target/hbase-metrics-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics/target/hbase-metrics-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics/target/hbase-metrics-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics/target/hbase-metrics-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop2-compat/target/hbase-hadoop2-compat-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop2-compat/target/hbase-hadoop2-compat-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop2-compat/target/hbase-hadoop2-compat-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-hadoop2-compat/target/hbase-hadoop2-compat-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-external-blockcache/target/hbase-external-blockcache-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-external-blockcache/target/hbase-external-blockcache-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-external-blockcache/target/hbase-external-blockcache-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-external-blockcache/target/hbase-external-blockcache-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-procedure/target/hbase-procedure-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-procedure/target/hbase-procedure-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-procedure/target/hbase-procedure-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-procedure/target/hbase-procedure-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-replication/target/hbase-replication-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-replication/target/hbase-replication-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-replication/target/hbase-replication-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-replication/target/hbase-replication-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-rest/target/hbase-rest-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-rest/target/hbase-rest-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-rest/target/hbase-rest-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-rest/target/hbase-rest-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-archetypes/hbase-client-project/target/hbase-client-project-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-archetypes/hbase-client-project/target/hbase-client-project-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-archetypes/hbase-shaded-client-project/target/hbase-shaded-client-project-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-archetypes/hbase-shaded-client-project/target/hbase-shaded-client-project-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-http/target/hbase-http-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-http/target/hbase-http-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-http/target/hbase-http-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-http/target/hbase-http-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-testing-util/target/hbase-testing-util-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-testing-util/target/hbase-testing-util-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics-api/target/hbase-metrics-api-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics-api/target/hbase-metrics-api-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics-api/target/hbase-metrics-api-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-metrics-api/target/hbase-metrics-api-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-resource-bundle/target/hbase-resource-bundle-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-endpoint/target/hbase-endpoint-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-endpoint/target/hbase-endpoint-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-endpoint/target/hbase-endpoint-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-endpoint/target/hbase-endpoint-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-zookeeper/target/hbase-zookeeper-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-zookeeper/target/hbase-zookeeper-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-mapreduce/target/hbase-mapreduce-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-mapreduce/target/hbase-mapreduce-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-mapreduce/target/hbase-mapreduce-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-mapreduce/target/hbase-mapreduce-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-shell/target/hbase-shell-2.2.1-test-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-shell/target/hbase-shell-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-shell/target/hbase-shell-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shell/target/hbase-shell-2.2.1-sources.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-client-byo-hadoop/target/original-hbase-shaded-client-byo-hadoop-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-client-byo-hadoop/target/hbase-shaded-client-byo-hadoop-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-testing-util-tester/target/hbase-shaded-testing-util-tester-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-testing-util-tester/target/hbase-shaded-testing-util-tester-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-client/target/original-hbase-shaded-client-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-client/target/hbase-shaded-client-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-mapreduce/target/original-hbase-shaded-mapreduce-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-mapreduce/target/hbase-shaded-mapreduce-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-testing-util/target/original-hbase-shaded-testing-util-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-shaded/hbase-shaded-testing-util/target/hbase-shaded-testing-util-2.2.1.jar";
classPath += ":/root/hbase-2.2.1/hbase-server/target/hbase-server-2.2.1-tests.jar";
classPath += ":/root/hbase-2.2.1/hbase-server/target/hbase-server-2.2.1.jar";
*/
        Options.v().set_whole_program(true);  // process whole program
        Options.v().set_allow_phantom_refs(true); // load phantom references
        Options.v().set_prepend_classpath(true); // prepend class path
        Options.v().set_src_prec(Options.src_prec_class); // process only .class files
        Options.v().set_process_dir(processDirs); // process all .class files in directory
        Options.v().set_no_bodies_for_excluded(true);
	Options.v().set_exclude(excludePackagesList);
        //
        Options.v().set_prepend_classpath(true); // prepend class path
        Options.v().set_soot_classpath(classPath);
        
        Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
        Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
        Options.v().set_keep_line_number(true);
        Scene.v().loadNecessaryClasses();
        CHATransformer.v().transform();
        cg = Scene.v().getCallGraph();

	List<SootClass> sootClassList = Scene.v().getClasses(1);
	for (SootClass sootClass : sootClassList) {
	    for (SootMethod sootMethod : sootClass.getMethods()) {
		boolean isJunitTest = false;
    	        for (Tag tag : sootMethod.getTags()) {
    	            if (tag instanceof VisibilityAnnotationTag) {
    	                VisibilityAnnotationTag vaTag = (VisibilityAnnotationTag) tag;
	    	        for (AnnotationTag aTag : vaTag.getAnnotations()) {
	    		    if (aTag.getType().contains(junitTestKey1) || aTag.getType().contains(junitTestKey2) || aTag.getType().contains(junitTestKey3)) {
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
                    // create cluster key stat for this method
		    Map<String, Integer> clusterKeyStat = new HashMap<String, Integer>();
		    for (String key : clusterKeys) {
			clusterKeyStat.put(key, 0);
		    }

                    if (sasList != null) {
		        // IMPORTANT: clear up for every round
		        for (StartAfterStop sas : sasList) {
			    sas.hasStopped = false;
			    sas.startAfterStop = false;
			    if (sas.stopKey.equals("")) {
			        sas.hasStopped = true;
			    }
		        }
                    }

                    if (sootMethod.toString().contains("TestClientClusterMetrics")) {
                    // fo through the method
		    goThroughMethod(clusterKeyStat, sootMethod, 0);
                    }
		    // combine the result of this method to global stat
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
	    
        System.out.println("num of classes: " + sootClassList.size());
        System.out.println("num of junitTests: " + junitTests);
    }

    private void goThroughMethod(Map<String, Integer> clusterKeyStat, 
				SootMethod sootMethod, int level) {
	String levelPrefix = "[" + level + "] ";
	//System.out.println(levelPrefix + sootMethod);
	Body b = sootMethod.retrieveActiveBody(); 
	UnitGraph graph = new ExceptionalUnitGraph(b);
	
	for (Unit unit : graph) {
	    System.out.println(levelPrefix + "unit:" + unit);
	    for (String key : clusterKeyStat.keySet()) { 
		if (unit.toString().contains(key)) {
		    clusterKeyStat.put(key, clusterKeyStat.get(key) + 1);
		}
	    }

            // start after stop analysis
	    if (sasList != null) {
	        for (StartAfterStop sas : sasList) { 
	    	    if (unit.toString().contains(sas.stopKey)) {
		        sas.hasStopped = true;
		    }
	            if (unit.toString().contains(sas.startKey) && sas.hasStopped == true) {
	                sas.startAfterStop = true;
                        System.out.println("[true] " + levelPrefix + "unit:" + unit);
		    }
	        }
	    }

	    if (unit instanceof InvokeStmt) {
		InvokeStmt invokeStmt = (InvokeStmt) unit;
		System.out.println(levelPrefix + "InvokeStmt:" + invokeStmt);
		try {
		    SootMethod invokedMethod = invokeStmt.getInvokeExpr().getMethod();
		    if (level <= exploreDepth) {
		        goThroughMethod(clusterKeyStat, invokedMethod, level + 1);
		    }
		} catch (Exception e) {
		    System.out.println(levelPrefix + "touched the boundary:");
		    System.out.println(levelPrefix + " " + e);
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
			//System.out.println(method);
			num ++;
		 	break;
		    }
		}
		if (found) {
		    break;
		}
	    }
	}
	return num;
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
		   // System.out.println(method);
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

    // Helper Class
    static class StartAfterStop {
	public String stopKey = null;
	public String startKey = null;
	public Set<String> methodSet = null;
	public Boolean hasStopped = false; // always set to false for each method iteration
	public Boolean startAfterStop = false; // always set to false for each method iteration

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
