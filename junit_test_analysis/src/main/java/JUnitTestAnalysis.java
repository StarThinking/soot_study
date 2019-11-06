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

public abstract class JUnitTestAnalysis {
    private String clusterInvolvedKey;
    private CallGraph cg = null;
    private static final String junitTestKey = "Lorg/junit/Test";
    private static final int exploreDepth = 3;
    
    protected ArrayList<String> processDirs = new ArrayList<String>();

    public JUnitTestAnalysis(String key) {
	this.clusterInvolvedKey = key;
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
        Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
        Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
        Options.v().set_keep_line_number(true);
            
        //SootClass sootClass = Scene.v().loadClassAndSupport(componentClass);
        //sootClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        CHATransformer.v().transform();
        cg = Scene.v().getCallGraph();

	int numOfJunitTests = 0;
	int numOfClusterJunitTests = 0;

	List<SootClass> sootClassList = Scene.v().getClasses(1);
	for (SootClass sootClass : sootClassList) {
	    // tmp
	    //if ( !sootClass.getName().contains("TestXAttrConfigFlag") )
	    //if (!sootClass.getName().contains("TestBlockScanner"))
	    //if (!sootClass.getName().contains("org.apache.hadoop.hdfs.TestRead"))
		//continue;
    	   
	    for (SootMethod sootMethod : sootClass.getMethods()) {
		boolean isJunitTest = false;
    	        for (Tag tag : sootMethod.getTags()) {
    	            if (tag instanceof VisibilityAnnotationTag) {
    	                VisibilityAnnotationTag vaTag = (VisibilityAnnotationTag) tag;
	    	        for (AnnotationTag aTag : vaTag.getAnnotations()) {
	    		    if (aTag.getType().contains(junitTestKey)) {
	    		        numOfJunitTests ++;
				isJunitTest = true;
				break;
	    		    }
	    	        }
		    }	
		    if (isJunitTest)
			break;
    	        }

		if (isJunitTest) {
		    if (goThroughMethod(sootMethod, 0)) { 
			System.out.println(sootMethod + " is a cluster junit test!");
			numOfClusterJunitTests ++;
		    } else {
			System.out.println(sootMethod + " is a junit test!");
		    }
		    System.out.println("");
		}
	    }
	}
	    
        System.out.println("num of classes: " + sootClassList.size());
	System.out.println("numOfJunitTests: " + numOfJunitTests);
	System.out.println("numOfClusterJunitTests: " + numOfClusterJunitTests);
    }

    private boolean goThroughMethod(SootMethod sootMethod, int level) {
	boolean finished = false;
	String levelPrefix = "[" + level + "] ";
	System.out.println(levelPrefix + sootMethod);
	Body b = sootMethod.retrieveActiveBody(); 
	UnitGraph graph = new ExceptionalUnitGraph(b);
	
	for (Unit unit : graph) {
	    System.out.println(levelPrefix + "unit:" + unit);
	    if (unit instanceof InvokeStmt) {
		InvokeStmt invokeStmt = (InvokeStmt) unit;
		System.out.println(levelPrefix + "InvokeStmt:" + invokeStmt);
		try {
		    SootMethod invokedMethod = invokeStmt.getInvokeExpr().getMethod();
		    if (level <= exploreDepth)
		        finished = goThroughMethod(invokedMethod, level+1);
		} catch (Exception e) {
		    System.out.println(levelPrefix + "touched the boundary");
		} 
	    } else {
		if (unit.toString().contains(clusterInvolvedKey)) {
		    System.out.println(levelPrefix + "clusterInvolvedKey matches!");
		    finished = true;
		}
	    }
	    if (finished)
		break;
	}
	return finished;
    }
    
    public static void main(String[] args) {
	//HdfsAnalysis hdfsAnalysis = new HdfsAnalysis("MiniDFS");
	//hdfsAnalysis.start();
	
        YarnAnalysis yarnAnalysis = new YarnAnalysis("MiniYARN");
	yarnAnalysis.start();
    }
}
