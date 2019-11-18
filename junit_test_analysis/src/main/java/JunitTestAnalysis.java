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
    private List<String> processDirs = null;
    private List<String> keys = null;
    private Map<String, Map<String, Integer>> keyStatByMethod = new HashMap<String, Map<String, Integer>>();
    private CallGraph cg = null;
    private static final String junitTestKey = "Lorg/junit/Test";
    private static final int exploreDepth = 3;
    private int junitTests = 0;
    private static List<String> excludePackagesList = new ArrayList<String>();

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

    public JunitTestAnalysis(List<String> processDirs, List<String> keys) {
        this.processDirs = processDirs;
	this.keys = keys;
    }

    public void start() {	
	if (processDirs == null) {
	    System.out.println("processDirs is null, exit.");
	    System.exit(1);
	}

	// NO CLASSPATH 
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
		    Map<String, Integer> keyStat = new HashMap<String, Integer>();
		    for (String key : keys) {
			keyStat.put(key, 0);
		    }
		    goThroughMethod(keyStat, sootMethod, 0);
		    // add the result of this method to global stat
		    keyStatByMethod.put(sootMethod.toString(), keyStat);
		}
	    }
	}
	    
        System.out.println("num of classes: " + sootClassList.size());
        System.out.println("num of junitTests: " + junitTests);
    }

    private void goThroughMethod(Map<String, Integer> keyStat, SootMethod sootMethod, int level) {
	boolean clusterInvolved = false;
	boolean rebootInvolved = false;
	String levelPrefix = "[" + level + "] ";
	//System.out.println(levelPrefix + sootMethod);
	Body b = sootMethod.retrieveActiveBody(); 
	UnitGraph graph = new ExceptionalUnitGraph(b);
	
	for (Unit unit : graph) {
	    //System.out.println(levelPrefix + "unit:" + unit);
	    for (String key : keyStat.keySet()) { 
		if (unit.toString().contains(key)) {
		    keyStat.put(key, keyStat.get(key)+1);
		}
	    }
	    
	    if (unit instanceof InvokeStmt) {
		InvokeStmt invokeStmt = (InvokeStmt) unit;
		//System.out.println(levelPrefix + "InvokeStmt:" + invokeStmt);
		try {
		    SootMethod invokedMethod = invokeStmt.getInvokeExpr().getMethod();
		    if (level <= exploreDepth) {
		        goThroughMethod(keyStat, invokedMethod, level+1);
		    }
		} catch (Exception e) {
		    //System.out.println(levelPrefix + "touched the boundary");
		} 
	    } 
	}
	return;
    }

    public Map<String, Map<String, Integer>> getKeyStatByMethod() {
	return keyStatByMethod;
    }
}
