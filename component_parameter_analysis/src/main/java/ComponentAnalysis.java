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

import java.util.*;
import java.lang.Exception;

public class ComponentAnalysis {
    private static String confClass = "org.apache.hadoop.conf.Configuration";
    private static CallGraph cg = null;
    private static ArrayList<String> procDirList = new ArrayList<String>();
    private static String classPath = ".";

    static {	
        procDirList.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/classes");
        procDirList.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-client/target/classes");
        procDirList.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/target/classes");
        procDirList.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-native-client/target/classes");
        procDirList.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-nfs/target/classes");
        procDirList.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-rbf/target/classes");

        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-client-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-httpfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-native-client-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-nfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-rbf-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-common-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-nfs-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-kms-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/client/hadoop-client-api-3.1.2.jar";
        classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/client/hadoop-client-runtime-3.1.2.jar";
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
                    if (getMethod.getName().contains("Boolean")) {
                        Boolean boolSecondArgument = (secondArgument.equals("1"))? true : false;
                        System.out.println((firstArgument.replaceAll("^\"|\"$", "") + " " + boolSecondArgument));
                    } else {
                        System.out.println((firstArgument.replaceAll("^\"|\"$", "") + " " + secondArgument));
                    }
                }
            }
    	}
        return;
    }

    public static void main(String[] args) {
        String componentClass = args[0];

        //String[] functionsWithType = {"getInt", "getInts", "getLong", "getLongBytes", "getHexDigits", "getFloat",
        //                              "getDouble", "getBoolean"};
        //RefType stringType = RefType.v("java.lang.String");
                
        // init once and perform analysises for different functions
        ComponentAnalysis.init(componentClass);

        for (SootMethod getMethod : Scene.v().getSootClass(confClass).getMethods()) {
            if (getMethod.isPublic() && getMethod.getName().startsWith("get")) {
            	ComponentAnalysis.edgeAnalysis(getMethod);
            }
        }
    }
}
