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

        public static void init(String procDir, String componentClass) {
                String classPath = "/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-client-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-httpfs-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-nfs-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/hdfs/hadoop-hdfs-nfs-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-common-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-nfs-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/common/hadoop-kms-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/client/hadoop-client-api-3.1.2.jar";
                classPath += ":/root/hadoop-3.1.2-src/hadoop-dist/target/hadoop-3.1.2/share/hadoop/client/hadoop-client-runtime-3.1.2.jar";

		Options.v().set_whole_program(true);  // process whole program
		Options.v().set_allow_phantom_refs(true); // load phantom references
		Options.v().set_prepend_classpath(true); // prepend class path
		Options.v().set_src_prec(Options.src_prec_class); // process only .class files
		ArrayList<String> procDirList = new ArrayList<String>();
		procDirList.add(procDir);
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

	public static int edgeAnalysis(String functionName, List<Type> parameterTypes, Set<String> paraSet) {
                SootMethod sm = null;
                if (functionName != "" && parameterTypes != null) {
		        sm = Scene.v().getSootClass(confClass).getMethod(functionName, parameterTypes);
                        System.out.println("edgeAnalysis for function " + functionName + " " + parameterTypes);
                } else if (functionName != "" && parameterTypes == null) {
		        sm = Scene.v().getSootClass(confClass).getMethodByName(functionName);
                        System.out.println("edgeAnalysis for function " + functionName);
                } else {
                        System.out.println("wrong arguments for edgeAnalysis!");
                        return 1;
                }

		// get edges
		Iterator<Edge> edges = cg.edgesInto(sm); 
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
                                //System.out.println(twoArguments);
                               
                                String firstArgument = "";
                                String secondArgument = "";
                                if (twoArguments.contains(",")) { // with default value
                                        firstArgument = twoArguments.substring(0, twoArguments.indexOf(","));
                                        secondArgument = twoArguments.substring(twoArguments.indexOf(",")+1).trim();
                                        //System.out.println("firstArgument = " + firstArgument + " secondArgument = " + secondArgument);
                                } else { // without default value
                                        firstArgument = twoArguments;
                                        //System.out.println("firstArgument = " + firstArgument);
                                }
                                paraSet.add(firstArgument);
                        }
		}
                return 0;
	}

        public static void main(String[] args) {
                String procDir = args[0];
                String componentClass = args[1];
                String[] functionsWithType = {"getInt", "getInts", "getLong", "getLongBytes", "getHexDigits", "getFloat",
                                              "getDouble", "getBoolean"};
                RefType stringType = RefType.v("java.lang.String");
                Map<String, Set<String>> funcMap = new HashMap<String, Set<String>>();
                
                // init once and perform analysises for different functions
                ComponentAnalysis.init(procDir, componentClass);

                // get(java.lang.String)
                Set<String> paraSetGet1 = new HashSet<String>();
                funcMap.put("get1", paraSetGet1);
                ComponentAnalysis.edgeAnalysis("get", new ArrayList<Type>(Arrays.asList(stringType)), paraSetGet1);
                
                // get(java.lang.String, java.lang.String)
                Set<String> paraSetGet2 = new HashSet<String>();
                funcMap.put("get2", paraSetGet2);
                ComponentAnalysis.edgeAnalysis("get", new ArrayList<Type>(Arrays.asList(stringType, stringType)), paraSetGet2);

                for ( String func : functionsWithType ) {
                        Set<String> paraSet = new HashSet<String>();
                        funcMap.put(func, paraSet);
                        ComponentAnalysis.edgeAnalysis(func, null, paraSet);
                }
               
                System.out.println("Results: parameters statically used on component " + componentClass);
                for (String key : funcMap.keySet()) {
                        Set<String> paraSet = funcMap.get(key);
                        System.out.println(paraSet.size() + " parameters read from function " + key + ":");
                        if ( paraSet != null) {
                                for ( String p : paraSet ) {
                                        System.out.println(p);
                                }
                        }
                        System.out.println("");
                }
        }
}
