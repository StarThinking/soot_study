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
import java.util.Iterator;
import java.util.List;
import java.lang.Exception;

public class Test {
	ArrayList<String> historyLocList = new ArrayList<String>();
	ArrayList<String> history = new ArrayList<String>();
	ArrayList<Value> definedVariables = new ArrayList<Value>();
	ArrayList<SootMethod> invokeFuncs = new ArrayList<SootMethod>();

	String confClass = null;
	String confFunc = null;
	String paraName = null;

	CallGraph cg = null;
	SootMethod smMain = null;
	SootMethod sm = null;

	public Test(String classPath, String className) {
		configureSoot(classPath);

		SootClass sootClass = Scene.v().loadClassAndSupport(className);
		sootClass.setApplicationClass();
		Scene.v().loadNecessaryClasses();

		CHATransformer.v().transform();

		cg = Scene.v().getCallGraph();
		smMain = Scene.v().getMainMethod();
	}

	public boolean checkIfUsed(String valueBoxString) {
		if (valueBoxString.contains(confClass) && valueBoxString.contains(confFunc) && valueBoxString.contains(paraName)) {
                        System.out.println("valueBoxString " + valueBoxString + " returns true in branch1");
			return true;
		}
		for (Value v : definedVariables) {
			if (valueBoxString.contains(v.toString())) {
                                System.out.println("valueBoxString " + valueBoxString + " returns true in branch2");
				return true;
			}
		}
		return false;
	}
	public void recursiveCheck(SootMethod sourceMethod, Unit unit) {
		for (ValueBox valueBox : unit.getUseBoxes()) {
			String valueBoxString = valueBox.getValue().toString();
			if (checkIfUsed(valueBoxString)) {
				String historyString = sourceMethod + "@Line" + unit.getTags().get(0);
				if (!history.contains(historyString)) {
					System.out.println("----------------------");
					System.out.println(unit);
					System.out.println(historyString);
					try {
						Value definedValue = unit.getDefBoxes().get(0).getValue();
						System.out.println("Define:" + definedValue);
						definedVariables.add(definedValue);
					}
					catch (Exception e) {
                                                System.out.println(e);
					}
					history.add(historyString);
				}
			}
		}
	}

	public void findConfLocation() {
		sm = Scene.v().getSootClass(confClass).getMethodByName(confFunc);
		Iterator sources = new Sources(cg.edgesInto(sm));
		while (sources.hasNext()) {
			historyLocList.clear();
			SootMethod sourceMethod = (SootMethod)sources.next();
			Body sourceBody = sourceMethod.getActiveBody();
			definedVariables.clear();
			invokeFuncs.clear();
			//firstRoundCheck in the Body sourceBody, String confClass, String confFunc, String paraName);
			for (Unit unit : sourceBody.getUnits()) {
				recursiveCheck(sourceMethod, unit);
			}
		}
	}

	public void findUsedVariables(Value definedValue, PatchingChain<Unit> sourceUnits) {
		for (Unit u : sourceUnits) {
			for (ValueBox v : u.getUseBoxes()) {
				if (v.getValue().equals(definedValue)) {
					System.out.println("The defined value is used by " + u.getDefBoxes().get(0).getValue());
					System.out.println(u);
					return;
				}
			}
		}
		System.out.println("The defined value is not used by other variables.");
		return ;
	}

	public void findLocalVariable() {
	}

	public void findFunctionCall(SootMethod sMethod) {
		Iterator targets = new Targets(cg.edgesOutOf(sMethod));
		while(targets.hasNext()) {
			SootMethod target = (SootMethod)targets.next();
			String targetName = target.toString();
			if (history.contains(targetName)) {
				continue;
			}
			history.add(targetName);
			try {
				List<ValueBox> localList = target.getActiveBody().getUseBoxes();
				//for (ValueBox v : localList) {
				//	if (v.toString().contains(className) && v.toString().contains(field)) {
				//		System.out.println(className + "|" + field + " is used by " + targetName);
				//	}
				//}
				findFunctionCall(target);
			}
			catch(Exception e) {
				//System.out.println(target.getSignature() + " cannot be reachable.");
				continue;
			}
		}
	}

	public void findClassField(String className, String field) {
		Chain<SootClass> app = Scene.v().getApplicationClasses();
		for (SootClass ac : app) {
			List<SootMethod> sootMethods = ac.getMethods();
			for (SootMethod sm : sootMethods) {
				if (sm.hasActiveBody()) {
					List<ValueBox> useBoxes = sm.getActiveBody().getUseBoxes();
					for (ValueBox v : useBoxes) {
						if (v.toString().contains(className) && v.toString().contains(field)) {
							if (sm.toString().contains("access$200")) {
								Iterator sources = new Sources(cg.edgesInto(sm));
								while (sources.hasNext()) {
									sm = (SootMethod)sources.next();
									System.out.println(className + "|" + field + " is used by " + sm);
								}
							}
							else {
								System.out.println(className + "|" + field + " is used by " + sm);
							}
						}
					}
				}
			}
		}
	}

	public static void main(String[] args) {
		Test t = new Test(args[0], args[1]);

		t.confClass = "org.apache.hadoop.conf.Configuration";
		t.confFunc = "getInt";
		t.paraName = "\"dfs.replication\"";

		t.findConfLocation();
		//t.findFunctionCall();
		t.findClassField("org.apache.hadoop.hdfs.server.blockmanagement.ProvidedStorageMap", "defaultReplication");
		t.findClassField("org.apache.hadoop.hdfs.server.blockmanagement.BlockManager", "defaultReplication");
	}

	public void configureSoot(String classpath) {
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
}
