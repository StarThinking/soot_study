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

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.*;
import java.lang.Exception;

public class ComponentAnalysis {
    private static String confClass = "org.apache.hadoop.conf.Configuration";
    private static CallGraph cg = null;
    private static ArrayList<String> procDirList = new ArrayList<String>();
    private static String classPath = ".";

    private static void loadClassPath(String classPathPath) {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(classPathPath));
	    String buffer = "";
	    while ((buffer = reader.readLine()) != null) {
	        if (!buffer.equals("null"))
	            classPath += ":" + buffer;
            }
            reader.close();
	} catch(Exception e) {
 	    e.printStackTrace();
	}
    }
    
    private static void loadProcDirList(String procDirListPath) {
	try {
	    BufferedReader reader = new BufferedReader(new FileReader(procDirListPath));
	    String buffer = "";
	    while ((buffer = reader.readLine()) != null) {
	        if (!buffer.equals("null"))
	            procDirList.add(buffer);
            }
            reader.close();
	} catch(Exception e) {
 	    e.printStackTrace();
	}
    }

    public static void init(String componentClass) {
        Options.v().set_whole_program(true);  // process whole program
    	Options.v().set_allow_phantom_refs(true); // load phantom references
        Options.v().set_prepend_classpath(true); // prepend class path
        Options.v().set_src_prec(Options.src_prec_class); // process only .class files
        Options.v().set_process_dir(procDirList); // process all .class files in directory
    	Options.v().set_soot_classpath(classPath);
        Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
    	Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
        Options.v().set_keep_line_number(true);
                    
        SootClass sootClass = Scene.v().loadClassAndSupport(componentClass);
        sootClass.setApplicationClass();
        Scene.v().loadNecessaryClasses();
        CHATransformer.v().transform();
    	cg = Scene.v().getCallGraph();
    }

    public static void edgeAnalysis(SootMethod getMethod) {
        // Into edges
	Iterator<Edge> edges = cg.edgesInto(getMethod); 
        while (edges.hasNext()) {
            Edge e = edges.next();
            Unit unit = e.srcUnit();
            SootMethod method = e.src();
            if (unit.toString().contains("dfs.")) { // pattern of parameters
                //System.out.println("LOC: " + method + ":" + unit.getTags().get(0));
                String raw = unit.toString();
                int fristLeft = raw.indexOf("(");
                int secondLeft = raw.indexOf("(", fristLeft+1);
                String twoArguments = raw.substring(secondLeft+1, raw.indexOf(")", secondLeft+1));
                               
                String firstArgument = "";
                String secondArgument = "";
                if (twoArguments.contains(",")) { // with default value
                    firstArgument = twoArguments.substring(0, twoArguments.indexOf(","));
                    secondArgument = twoArguments.substring(twoArguments.indexOf(",")+1).trim();
                } else { // without default value
                    firstArgument = twoArguments;
                }
                if (secondArgument.equals("")) {
                    System.out.println(firstArgument.replaceAll("^\"|\"$", ""));
                } else {
                    if (getMethod.getName().equals("Boolean")) {
                        Boolean boolSecondArgument = (secondArgument.equals("1"))? true : false;
                        // remove "parameter"
			System.out.println((firstArgument.replaceAll("^\"|\"$", "") + " " + boolSecondArgument));
                    } else {
                        // remove "parameter"
                        System.out.println((firstArgument.replaceAll("^\"|\"$", "") + " " + secondArgument));
                    }
                }
            }
    	}
        return;
    }

    public static void main(String[] args) {
	if (args.length != 4) {
	    System.out.println("Wrong arguments: [procDirListPath] [classPathPath] [selectedMethod] [componentClass]");
	    System.exit(-1);
	}
	String procDirListPath = args[0];
	String classPathPath = args[1];
	String selectedMethod = args[2];
        String componentClass = args[3];
        
	loadClassPath(classPathPath);
	loadProcDirList(procDirListPath);
        /*System.out.println("classPath = " + classPath);	
        for (String s : procDirList ) {
	    System.out.println("procDir = " + s);
	}	
        System.exit(0);*/     

        // init once and perform analysises for different functions
        ComponentAnalysis.init(componentClass);

        for (SootMethod getMethod : Scene.v().getSootClass(confClass).getMethods()) {
            //if (getMethod.isPublic() && getMethod.getName().startsWith("get")) {
            if (getMethod.isPublic() && selectedMethod.equals(getMethod.getName())) {
                //System.out.println(getMethod.getName());
            	ComponentAnalysis.edgeAnalysis(getMethod);
            }
        }
    }
}
