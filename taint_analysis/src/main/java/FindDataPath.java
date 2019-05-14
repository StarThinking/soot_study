import soot.*;
import soot.jimple.*;
import soot.options.Options;
import soot.util.*;
import java.io.*;
import java.util.*;

public class FindDataPath {
	public static void main(String[] args) {
		PackManager.v().getPack("wjtp").add(
				new Transform("wjtp.myTransform", new SceneTransformer() {
					protected void internalTransform(String phaseName,
							Map options) {
					System.err.println(Scene.v().getApplicationClasses());
					}
					}));
		soot.Main.main(args);
	} 
}
