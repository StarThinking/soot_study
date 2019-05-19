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

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.lang.Exception;

public class Test {
        private String confClass = null;
        private String confGetFunc = null;
        private String paraName = null;
        private CallGraph cg = null;
        private Set<String> defSet = new HashSet<String>();

        public Test(String classPath, String className) {
                configureSoot(classPath);
                    
                SootClass sootClass = Scene.v().loadClassAndSupport(className);
                sootClass.setApplicationClass();
                Scene.v().loadNecessaryClasses();
                CHATransformer.v().transform();
		cg = Scene.v().getCallGraph();
	}

        public void findUses(SootMethod method) {
                Body body = method.getActiveBody();
                for (Unit unit : body.getUnits()) {
                        for (ValueBox valueBox : unit.getUseBoxes()) {
                                if (defSet.contains(valueBox.getValue().toString())) {
                                        System.out.println("The following unit uses def with value " 
                                                + valueBox + ": " + unit);
                                        System.out.println("LOC: " + method + ":" 
                                                + unit.getTags().get(0));
                                        
                                        // try to new definition
                                        List<ValueBox> defBoxes = unit.getDefBoxes();
                                        if (!defBoxes.isEmpty()) {
                                                for (ValueBox box : defBoxes) {
                                                        System.out.println("New defBox: " +
                                                                box);
                                                        System.out.println("New defBox value: " + box.getValue().toString());
                                                }
                                        }

                                        // org.apache.hadoop.hdfs


                                        break;
                                }
                        }
                }
        }

	public void edgeAnalysis() {
		SootMethod sm = Scene.v().getSootClass(confClass).getMethodByName(confGetFunc);
		// get edges
		Iterator<Edge> edges = cg.edgesInto(sm); 
                while (edges.hasNext()) {
                        Edge e = edges.next();
                        Unit unit = e.srcUnit();
                        SootMethod method = e.src();
                        if (unit.toString().contains(paraName)) { // unit filter
                                System.out.println("---------------------------------");
                                System.out.println("Unit: " + unit);
                                System.out.println("Of method: " + method);
                                List<ValueBox> defBoxes = unit.getDefBoxes();
                                for (ValueBox box : defBoxes) {
                                        System.out.println("DefBox: " + box);
                                        defSet.add(box.getValue().toString());
                                        //System.out.println("defBox getValue(): " + box.getValue());
                                        findUses(method);
                                }
                                System.out.println("---------------------------------");
                        }
		}
	}

	private void configureSoot(String classpath) {
		Options.v().set_whole_program(true);  // process whole program
		Options.v().set_allow_phantom_refs(true); // load phantom references
		Options.v().set_prepend_classpath(true); // prepend class path
		Options.v().set_src_prec(Options.src_prec_class); // process only .class files
		ArrayList<String> list = new ArrayList<>();
		list.add(classpath);
		//list.add("/mnt/git/hadoop-3.1.2-src/hadoop-common-project/hadoop-common/target/classes");
		Options.v().set_process_dir(list); // process all .class files in directory
		Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
		Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
		Options.v().set_keep_line_number(true);
	}
	
        public static void main(String[] args) {
                String classPath = args[0];
                String className = args[1];
		Test t = new Test(classPath, className);

		t.confClass = "org.apache.hadoop.conf.Configuration";
		t.confGetFunc = "getInt";
		t.paraName = "\"dfs.replication\"";

		t.edgeAnalysis();
                //List<SootMethod> list = Scene.v().getSootClass("org.apache.hadoop.hdfs.server.blockmanagement.ProvidedStorageMap").getMethods();
                //for (SootMethod sm : list)
                //        System.out.println("method: " + sm);
	}
}
