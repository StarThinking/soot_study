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

public abstract class JunitTestAnalysis {
    private Map<String, Set<String>> stats;
    private CallGraph cg = null;
    private static final String junitTestKey = "Lorg/junit/Test";
    private static final int exploreDepth = 3;
    
    protected ArrayList<String> processDirs = new ArrayList<String>();
    private static ArrayList<String> excludePackagesList = new ArrayList<String>();

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

    public JunitTestAnalysis(String[] keys) {
        stats = new HashMap<String, Set<String>>();
	for (String key : keys) {
	    stats.put(key, new HashSet<String>());
	}
    }

    protected abstract void initProcessDir();

    public void start() {	
	initProcessDir();

	// NO CLASSPATH 
	//String classPath = "/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-3.1.2-tests.jar";
        //Options.v().set_soot_classpath(classPath);
        Options.v().set_whole_program(true);  // process whole program
        Options.v().set_allow_phantom_refs(true); // load phantom references
        Options.v().set_prepend_classpath(true); // prepend class path
        Options.v().set_src_prec(Options.src_prec_class); // process only .class files
        Options.v().set_process_dir(processDirs); // process all .class files in directory
        Options.v().set_no_bodies_for_excluded(true);
	Options.v().set_exclude(excludePackagesList);
        Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
        Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
        Options.v().set_keep_line_number(true);
            
        //SootClass sootClass = Scene.v().loadClassAndSupport(componentClass);
        //sootClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        CHATransformer.v().transform();
        cg = Scene.v().getCallGraph();

	int junitTests = 0;
	int clusterInvolvedTests = 0;
	int rebootInvolvedTests = 0;

	List<SootClass> sootClassList = Scene.v().getClasses(1);
	for (SootClass sootClass : sootClassList) {
	    for (SootMethod sootMethod : sootClass.getMethods()) {
		boolean isJunitTest = false;
    	        for (Tag tag : sootMethod.getTags()) {
    	            if (tag instanceof VisibilityAnnotationTag) {
    	                VisibilityAnnotationTag vaTag = (VisibilityAnnotationTag) tag;
	    	        for (AnnotationTag aTag : vaTag.getAnnotations()) {
	    		    if (aTag.getType().contains(junitTestKey)) {
				isJunitTest = true;
				break;
	    		    }
	    	        }
		    }	
		    if (isJunitTest)
			break;
    	        }

		if (isJunitTest) {
	            junitTests ++;
		    Map<String, Boolean> keyMap = new HashMap<String, Boolean>();
		    for (String key : stats.keySet()) {
			keyMap.put(key, false);
		    }
		    
		    goThroughMethod(keyMap, sootMethod, 0);
		   
		    // merge the result for this method with the global stats
	 	    for (String key : keyMap.keySet()) {
			if (keyMap.get(key) == true) {
			    stats.get(key).add(sootMethod.toString());
			    //System.out.println(sootMethod);
			}
		    }
		}
	    }
	}
	    
        System.out.println("num of classes: " + sootClassList.size());
        System.out.println("num of junitTests: " + junitTests);
	for (String key : stats.keySet()) {
	    System.out.println(key + ": " + stats.get(key).size());
	}
    }

    private void goThroughMethod(Map<String, Boolean> keyMap, SootMethod sootMethod, int level) {
	boolean clusterInvolved = false;
	boolean rebootInvolved = false;
	String levelPrefix = "[" + level + "] ";
	//System.out.println(levelPrefix + sootMethod);
	Body b = sootMethod.retrieveActiveBody(); 
	UnitGraph graph = new ExceptionalUnitGraph(b);
	
	for (Unit unit : graph) {
	    //System.out.println(levelPrefix + "unit:" + unit);
	    if (unit instanceof InvokeStmt) {
		InvokeStmt invokeStmt = (InvokeStmt) unit;
		//System.out.println(levelPrefix + "InvokeStmt:" + invokeStmt);
		try {
		    SootMethod invokedMethod = invokeStmt.getInvokeExpr().getMethod();
		    if (level <= exploreDepth) {
		        goThroughMethod(keyMap, invokedMethod, level+1);
		    }
		} catch (Exception e) {
		    //System.out.println(levelPrefix + "touched the boundary");
		} 
	    } else {
		for (String key : keyMap.keySet()) { 
		    if (unit.toString().contains(key)) {
			keyMap.put(key, true);
		    }
		}
	    }
	}
	return;
    }
}
