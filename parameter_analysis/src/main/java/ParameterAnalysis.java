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

public class ParameterAnalysis {
        private String confClass = null;
        private String confGetFunc = null;
        private String paraName = null;
        private CallGraph cg = null;
        private Set<String> useSet = new HashSet<String>();

        public ParameterAnalysis(String procDir, String className, String paraName, String confClass, String confGetFunc) {
                this.paraName = paraName;
                this.confClass = confClass;
                this.confGetFunc = confGetFunc;

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
                    
                SootClass sootClass = Scene.v().loadClassAndSupport(className);
                sootClass.setApplicationClass();
                Scene.v().loadNecessaryClasses();
                CHATransformer.v().transform();
		cg = Scene.v().getCallGraph();
	}

        private void printWithTab(String content, int level) {
                int l = 0;
                String tab = "";

                while (l < level) {
                        tab += "\t";     
                        l ++;
                }
//                System.out.println(tab + content);
        }

        public void findUsesInClass(String className, String defBoxValue, int tabLevel) {
                printWithTab("findUsesInClass: className = " + className + ", defBoxValue = " + defBoxValue, tabLevel);
                
                SootClass sootClass = Scene.v().loadClassAndSupport(className);
                sootClass.setApplicationClass();
                Scene.v().loadNecessaryClasses();

                printWithTab("---------------------------------", tabLevel);
                for (SootMethod method : sootClass.getMethods()) {
                        Set<String> defSet4Method = new HashSet<String>();
                        defSet4Method.add(defBoxValue);
                        findUsesInMethod(method, defSet4Method, tabLevel+1);
                }
                printWithTab("---------------------------------", tabLevel);
        }

        public void findUsesInMethod(SootMethod method, Set<String> defSet4Method, int tabLevel) {
                printWithTab("findUsesInMethod: " + method, tabLevel);
                Body body = null;
                try {
                        body = method.getActiveBody();
                } catch (Exception e) {
                        //System.out.println(e);
                }

                if (body == null)
                        return;

                for (Unit unit : body.getUnits()) {
                        //if (tabLevel >=2)
                        //        System.out.println("unit: " + unit);
                        for (ValueBox useBox : unit.getUseBoxes()) {
                                if (defSet4Method.contains(useBox.getValue().toString())) {
                                        printWithTab("", tabLevel);
                                        printWithTab("Expression uses definition(" + useBox + "): " + unit, tabLevel);
                                        printWithTab("LOC: " + method + ":" + unit.getTags().get(0), tabLevel);
                                        
                                        if (unit instanceof AssignStmt) {
                                                printWithTab("@@@@@@" + unit + " is instanceof AssignStmt", tabLevel);
                                                // add new definition
                                                List<ValueBox> defBoxes = unit.getDefBoxes();
                                                for (ValueBox box : defBoxes) {
                                                        defSet4Method.add(box.getValue().toString());
                                                        printWithTab("New Definition Value added: " + box.getValue(), tabLevel);
                                                        // if variable is assigned to hadoop object
                                                        // then let us continue to explore all methods of that class 
                                                        if (box.getValue().toString().contains("org.apache.hadoop")) { 
                                                                int begin = box.getValue().toString().indexOf("org.apache.hadoop");
                                                                int end = box.getValue().toString().indexOf(":");
                                                                String className = box.getValue().toString().substring(begin, end);
                                                                findUsesInClass(className, box.getValue().toString(), tabLevel);
                                                        }
                                                }
                                        }  else if (unit instanceof InvokeStmt) {
                                                InvokeStmt stmt = (InvokeStmt) unit;
                                                InvokeExpr expr = stmt.getInvokeExpr();

                                                if (expr instanceof InterfaceInvokeExpr) {
                                                        printWithTab("!!!!!!" + expr + " is instanceof InterfaceInvokeExpr", tabLevel);
                                                        useSet.add(method + ":" + unit.getTags().get(0) + " " + unit);
                                                        continue;
                                                }

                                                printWithTab("######" + stmt + " is instanceof InvokeStmt", tabLevel);
                                                SootMethod methodInvoked = expr.getMethod();
                                                
                                                // find arg index in current expression
                                                int argCount = expr.getArgCount();
                                                int index;
                                                for (index=0; index<argCount; index++) {
                                                        if (expr.getArg(index).toString() == useBox.getValue().toString()) {
                                                                printWithTab("the index of " + useBox.getValue().toString() +
                                                                            " is " + index, tabLevel);
                                                                break;
                                                        }
                                                }

                                                if (index == argCount) {
                                                        System.out.println("Error: can't find arguement!!");
                                                        return;
                                                }
                                                
                                                // find arg in the invoked method signature
                                                printWithTab("methodInvoked: " + methodInvoked, tabLevel);
                                                Body _body = null;
                                                try {
                                                        _body = methodInvoked.getActiveBody();     
                                                } catch (Exception e) {
                                                        continue; // next statement of the current method
                                                }
                                                
                                                if (_body != null) {
                                                        Local var = _body.getParameterLocal(index);
                                                        printWithTab("Local Var at index " + index + " = " + var.getName(), tabLevel);
                                                        Set<String> _defSet4Method = new HashSet<String>();
                                                        _defSet4Method.add(var.getName());
                                                        findUsesInMethod(methodInvoked, _defSet4Method, tabLevel+1);
                                                }
                                        } else {
                                                printWithTab("!!!!!!" + unit + " is NOT instanceof AssignStmt and InvokeStmt", tabLevel);
                                                useSet.add(method + ":" + unit.getTags().get(0) + " " + unit);
                                        }
                                }
                        }
                }
                //System.out.println("defSet4Method size = " + defSet4Method.size());
        }

	public void edgeAnalysis() {
		SootMethod sm = Scene.v().getSootClass(confClass).getMethodByName(confGetFunc);
		// get edges
		Iterator<Edge> edges = cg.edgesInto(sm); 
                int tabLevel = 0;
                while (edges.hasNext()) {
                        Edge e = edges.next();
                        Unit unit = e.srcUnit();
                        SootMethod method = e.src();
                        /*if (unit.toString().contains("dfs.")) {
                                System.out.println(unit.toString());
                        }*/
                        if (unit.toString().contains(paraName)) { // unit filter
                                printWithTab("---------------------------------", tabLevel);
                                printWithTab("Statement: " + unit, tabLevel);
                                printWithTab("Method: " + method, tabLevel);
                                List<ValueBox> defBoxes = unit.getDefBoxes();
                                for (ValueBox box : defBoxes) {
                                        printWithTab("Definition: " + box, tabLevel);
                                        Set<String> defSet4Method = new HashSet<String>();
                                        defSet4Method.add(box.getValue().toString());
                                        printWithTab("New Definition Value added: " + box.getValue(), tabLevel);
                                        findUsesInMethod(method, defSet4Method, tabLevel+1); // tabLevel = 1
                                }
                        }
		}
	}

        public Set<String> getUseSet() {
                return this.useSet;
        }

        public static void main(String[] args) {
                String procDir = args[0];
                String className = args[1];
                String para = args[2];
                String paraName = "\"" + para + "\"";
                String confClass = "org.apache.hadoop.conf.Configuration";
                String confGetFunc = "getInt";

                System.out.println("main");
                ParameterAnalysis analysis = new ParameterAnalysis(procDir, className, paraName, confClass, confGetFunc);

                System.out.println("Start analysis for parameter " + para);
		analysis.edgeAnalysis();

                List<String> sortedResList = new ArrayList<String>(analysis.getUseSet());
                Collections.sort(sortedResList);
                Iterator<String> it = sortedResList.iterator();	
                System.out.println("-------Results of parameter " + para + " for class " + className + "--------");
                System.out.println("Number: " + sortedResList.size());
                while(it.hasNext()){
                      System.out.println(it.next());
                }
        }
}
