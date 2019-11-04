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

public class JUnitTestAnalysis {
    private static CallGraph cg = null;
    private static final String clusterInvolvedKey = "MiniDFS";
    private static final String junitTestKey = "Lorg/junit/Test";

    public static void start() {

/*        String classPath = "/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-client-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-httpfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-nfs-3.1.2.jar";

        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-common-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-nfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-kms-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/client/hadoop-client-api-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/client/hadoop-client-runtime-3.1.2.jar";
*/
        ArrayList<String> processDirs = new ArrayList<String>();
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-native-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-nfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-rbf/target/test-classes");
        
//        Options.v().set_soot_classpath(classPath);
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
        //ReachableMethods methods = Scene.v().getReachableMethods();
        //System.out.println("size of methods = " + methods.size());
        Chain<SootClass> classChain = Scene.v().getClasses();
        System.out.println("size of classes = " + classChain.size());
        Iterator<SootClass> it = classChain.iterator();
        //final String junitType = "Test";
	int numOfJunitTests = 0;
	int numOfClusterJunitTests = 0;
        while (it.hasNext()) {
     	    SootClass sootClass = it.next();
	    boolean testFuncExistInClass = false;
	    List<SootMethod> junitTestMethods = new ArrayList<SootMethod>();
	    List<SootMethod> clusterInvolvedMethods = new ArrayList<SootMethod>();
	    List<SootMethod> junitClusterTests = new ArrayList<SootMethod>();
	    // tmp
	    if ( !sootClass.getName().contains("TestXAttrConfigFlag") )
		continue;
	    //if (!sootClass.getName().contains("TestBlockScanner"))
    	   
	    for (SootMethod sootMethod : sootClass.getMethods()) {
		boolean isJunitTest = false;
    	        for (Tag tag : sootMethod.getTags()) {
    	            if (tag instanceof VisibilityAnnotationTag) {
    	                VisibilityAnnotationTag vaTag = (VisibilityAnnotationTag) tag;
	    	        for (AnnotationTag aTag : vaTag.getAnnotations()) {
	    		    if (aTag.getType().contains(junitTestKey)) {
				junitTestMethods.add(sootMethod);
	    		        numOfJunitTests ++;
				testFuncExistInClass = true;	
				isJunitTest = true;
	    		    }
	    	        }
		    }	
    	        }
		if (isJunitTest)
		    goThroughMethod(sootMethod);
	    }
	    
	System.out.println("numOfJunitTests: " + numOfJunitTests);
    }

    private void goThroughMethod(SootMethod sootMethod) {
	Body b = sootMethod.retrieveActiveBody(); 
	UnitGraph graph = new ExceptionalUnitGraph(b);
	for (Unit unit : graph) {
	    if (unit instanceof InvokeStmt) {
		System.out.println("InvokeStmt:" + unit);
	    }
	    //if (unit.toString().contains(clusterInvolvedKey)) {
	    //	System.out.println("clusterInvolvedKey matches!");
	    //	return;
	    //}
	}	
    }
   
    public static void main(String[] args) {
        // init once and perform analysises for different functions
        JUnitTestAnalysis.start();
    }
}
	    // intra analysis for all functions in this class
	    /*if (testFuncExistInClass) {
		for (SootMethod sootMethod : sootClass.getMethods()) { 
		    Body b = sootMethod.retrieveActiveBody(); 
		    UnitGraph graph = new ExceptionalUnitGraph(b);
		    for (Unit unit : graph) {
		        if (unit.toString().contains(clusterInvolvedKey)) {
			    clusterInvolvedMethods.add(sootMethod);
			    break;
			}
		    }
		}
	    }	
	    
            System.out.println(sootClass);
            
	    System.out.println("clusterInvolvedMethods:");
	    for (SootMethod m : clusterInvolvedMethods)
		System.out.println(m);

            System.out.println("junitTestMethods:");
	    for (SootMethod m : junitTestMethods)
 	        System.out.println(m);
	    
	    for (SootMethod junitTest : junitTestMethods) {
		Body b = junitTest.retrieveActiveBody();
		UnitGraph graph = new ExceptionalUnitGraph(b);
		boolean found = false;
	        for (Unit unit : graph) {
		    for (SootMethod clusterInvolvedMethod : clusterInvolvedMethods) {
		        if (unit.toString().contains(clusterInvolvedMethod.toString())) {
			    found = true;
			    break; 
			}
		    }
		    if (found)
			break;
	 	}
		if (found)
		    System.out.println("yes: " + junitTest);
	    }
    	}*/
