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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.lang.Exception;

public class Test {
  ArrayList<String> historyLocList = new ArrayList<String>();
  //ArrayList<SootMethod> history = new ArrayList<SootMethod>();
  ArrayList<Value> classFields = new ArrayList<Value>();
  ArrayList<String> finalResults = new ArrayList<String>();

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

  public boolean checkIfUsed(Value value, ArrayList<Value> checkedVariables, ArrayList<SootMethod> checkedMethods) {
    String valueString = value.toString();
    if (valueString.contains(confClass) && valueString.contains(confFunc)) { 
      if (valueString.contains(paraName)) {
        System.out.println("DEBUG1|" + valueString);
        //System.out.println(valueString);
        return true;
      }
      else {
        return false;
      }
    }

    if (checkedMethods != null && value instanceof InvokeStmt) {
      SootMethod valueMethod = ((InvokeStmt)value).getInvokeExpr().getMethod();
      for (SootMethod checkedMethod : checkedMethods) {
        if (checkedMethod.equals(value)) {
          System.out.println("DEBUG2|" + valueString);
          return true;
        }
      }
    }

    //System.out.println(valueString);
    for (Value v : checkedVariables) {
      //XXX: not perfect
      if (v.toString().contains(valueString)) {
        System.out.println("DEBUG3|" + valueString);
        return true;
      }
    }
    return false;
  }

  public void checkRefPos(InvokeExpr invokeExpr, ArrayList<Value> checkedVariables, ArrayList<Integer> paraList) {
    for (int i = 0; i < invokeExpr.getArgCount(); i++) {
      Value v = invokeExpr.getArg(i);
      for (Value vDefined : checkedVariables) {
        if (v.equals(vDefined) && !paraList.contains(i)) {
          paraList.add(i);
        }
      }
    }
  }

  public void recursiveCheck(SootMethod sMethod, ArrayList<Integer> paraList, ArrayList<Value> checkedVariables, ArrayList<SootMethod> checkedMethods, int direction, ArrayList<SootMethod> history) {
    //UnitGraph graph = new BriefUnitGraph(sMethod.getActiveBody());
    Iterator stMethods = null;
    ArrayList<Value> backupCheckedVariables = null;
    ArrayList<SootMethod> backupCheckedMethods = null;

    if (checkedVariables != null) {
      backupCheckedVariables = (ArrayList<Value>)checkedVariables.clone();
    }
    if (checkedMethods != null) {
      backupCheckedMethods = (ArrayList<SootMethod>)checkedMethods.clone();
    }

    if (direction == 0) {
      stMethods = new Sources(cg.edgesInto(sMethod));
    }
    else {
      ArrayList<SootMethod> tmpList = new ArrayList<SootMethod>();
      tmpList.add(sMethod);
      stMethods = tmpList.iterator();
    }

    if (history == null) {
      history = new ArrayList<SootMethod>();
    }

    while (stMethods.hasNext()) {
      SootMethod sootMethod = (SootMethod)stMethods.next();

      Body sootBody = null;
      if (sootMethod.hasActiveBody()) {
        sootBody = sootMethod.getActiveBody();
      }
      else {
        System.out.println(sootMethod + "does not have active body.");
        continue;
      }

      // Do not duplicate the procedure
      if (history.contains(sootMethod)) {
        continue;
      }
      else {
        history.add(sootMethod);
      }

      // Initialization for some structures 
      if (backupCheckedVariables == null) {
        checkedVariables = new ArrayList<Value>();
      }
      else {
        checkedVariables = new ArrayList<Value>(backupCheckedVariables);
      }

      if (backupCheckedMethods == null) {
        checkedMethods = new ArrayList<SootMethod>();
      }
      else {
        checkedMethods = new ArrayList<SootMethod>(backupCheckedMethods);
      }
      checkedMethods.add(sootMethod);

      // Add one checked variable because one parameter is used
      if (paraList != null && paraList.size() > 0) {
        for (int paraIndex : paraList) {
          checkedVariables.add(sootBody.getParameterLocal(paraIndex));
        }
      }

      HashMap<SootMethod, ArrayList<Integer>> invokeFuncs = new HashMap<SootMethod, ArrayList<Integer>>();

      //System.out.println("Start traverse @" + sootBody + ", checkedVariables = " + checkedVariables);
      for (Unit unit: sootBody.getUnits()) {

        for (ValueBox valueBox : unit.getUseBoxes()) {
          Value value = valueBox.getValue();
          if (checkIfUsed(value, checkedVariables, checkedMethods)) {
            String historyString = null;
            if (unit.getTags().size() > 0) {
              System.out.println(value + "|" + unit + "|" + sootMethod);
              historyString = sootMethod + "@Line" + unit.getTags().get(0);
            }
            else {
              historyString = sootMethod.toString();
            }

            if (finalResults.contains(historyString)) {
            }
            else {
              System.out.println(historyString);
              finalResults.add(historyString);
            }

            try {
              if (unit instanceof AssignStmt) {
                for (ValueBox vb : unit.getDefBoxes()) {
                  Value definedValue = vb.getValue();
                  //System.out.println(value + " define to " + definedValue);
                  if (!checkedVariables.contains(definedValue)) {
                    checkedVariables.add(definedValue);
                  }
                  if (definedValue instanceof FieldRef) {
                    //Static ???
                    if (!classFields.contains(definedValue)) {
                      classFields.add(definedValue);
                    }
                    //System.out.println("special assignment " + definedValue);
                  }
                }
              }
              else if (unit instanceof InvokeStmt) {
                InvokeStmt is = (InvokeStmt)unit;
                SootMethod invokeMethod = is.getInvokeExpr().getMethod();
                if (!checkedMethods.contains(invokeMethod)) {
                  if (!invokeFuncs.containsKey(invokeMethod)) {
                    invokeFuncs.put(invokeMethod, new ArrayList<Integer>());
                  }
                  ArrayList<Integer> paraNextList = invokeFuncs.get(invokeMethod);
                  checkRefPos(is.getInvokeExpr(), checkedVariables, paraNextList);
                  invokeFuncs.put(invokeMethod, paraNextList);
                }
              }
              else {
                //System.out.println("Fang|" + unit);
                //System.out.println("Fang|" + unit.getClass().getName());
              }
            }
            catch (Exception e) {
              continue;
            }
          }
        }
      }

      for (SootMethod invokeMethod : invokeFuncs.keySet()) {
        //System.out.println(invokeMethod);
        //System.out.println(invokeFuncs.get(invokeMethod));
        recursiveCheck(invokeMethod, invokeFuncs.get(invokeMethod), null, null, 1, history);
      }
    }
  }

  public void findByGetConf() {
    sm = Scene.v().getSootClass(confClass).getMethodByName(confFunc);
    //firstRoundCheck in the Body sourceBody, String confClass, String confFunc, String paraName);
    //System.out.println(sourceBody);
    recursiveCheck(sm, null, null, null, 0, null);
  }

  public void findByClassField() {
    while(classFields.size() > 0) {
      Chain<SootClass> app = Scene.v().getApplicationClasses();
      ArrayList<Value> classFieldsCopy = (ArrayList<Value>)classFields.clone();
      classFields.clear();
      for (Value classField : classFields) { 
        for (SootClass ac : app) {
          List<SootMethod> sootMethods = ac.getMethods();
          for (SootMethod sm : sootMethods) {
            if (sm.hasActiveBody()) {
              List<ValueBox> useBoxes = sm.getActiveBody().getUseBoxes();
              for (ValueBox v : useBoxes) {
                if (v.toString().contains(((FieldRef)classField).getField().toString())) {
                  //Speical case
                  if (sm.toString().contains("access$200")) {
                    Iterator sources = new Sources(cg.edgesInto(sm));
                    while (sources.hasNext()) {
                      SootMethod smTmp = (SootMethod)sources.next();
                      System.out.println(classField + " is used by " + smTmp);
                      ArrayList<SootMethod> tmpMethodList = new ArrayList<SootMethod>();
                      tmpMethodList.add(sm);
                      recursiveCheck(smTmp, null, null, tmpMethodList, 1, null);
                    }
                  }
                  else {
                    ArrayList<Value> tmpValueList = new ArrayList<Value>();
                    tmpValueList.add(classField);
                    recursiveCheck(sm, null, tmpValueList, null, 1, null);
                  }
                  System.out.println(((FieldRef)classField).getField().toString() + " is used by " + sm);
                }
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

    t.findByGetConf();
    //for (Value classField : t.classFields) { 
    //  System.out.println(((FieldRef)classField).getField());
    //}

    t.findByClassField();
    //t.findFunctionCall();
    //t.findClassField("org.apache.hadoop.hdfs.server.blockmanagement.ProvidedStorageMap", "defaultReplication");
    //t.findClassField("org.apache.hadoop.hdfs.server.blockmanagement.BlockManager", "defaultReplication");

    // Generate final outputs
    System.out.println("----------------------");
    for (String s : t.finalResults) {
      System.out.println(s);
    }
    System.out.println("----------------------");

  }

  public void configureSoot(String classpath) { Options.v().set_whole_program(true);  // process whole program
    Options.v().set_allow_phantom_refs(true); // load phantom references
    Options.v().set_prepend_classpath(true); // prepend class path
    Options.v().set_src_prec(Options.src_prec_class); // process only .class files
    ArrayList<String> list = new ArrayList<>();
    list.add(classpath);
    list.add("/nfs/mysoot/git/hadoop-3.1.2-src/hadoop-common-project/hadoop-common/target/classes");
    Options.v().set_process_dir(list); // process all .class files in directory
    Options.v().setPhaseOption("cg.spark", "on"); // use spark for call graph
    Options.v().set_output_dir("/tmp/sootOutput"); // use spark for call graph
    Options.v().set_keep_line_number(true);
  }
}
