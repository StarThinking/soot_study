import java.util.*;

public class HdfsAnalysis extends JunitTestAnalysis {

    private static List<String> processDirs = new ArrayList<String>();
    private static List<String> keys = new ArrayList<String>();

    static {
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-native-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-nfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-rbf/target/test-classes");

	keys.add("org.apache.hadoop.hdfs.MiniDFSClusterWithNodeGroup");
	keys.add("org.apache.hadoop.hdfs.MiniDFS");
	keys.add("org.apache.hadoop.hdfs.qjournal.MiniQJMHACluster");
	keys.add("org.apache.hadoop.hdfs.qjournal.MiniJournalCluster");
    }

    public HdfsAnalysis() {
	super(processDirs, keys);
    }

    public static void main(String[] args) {
        HdfsAnalysis analysis = new HdfsAnalysis();
        analysis.start();
	Map<String, Map<String, Integer>> keyStatByMethod = analysis.getKeyStatByMethod();
	System.out.println("num of keyStatByMethod: " + keyStatByMethod.size());

	Map<String, Set<String>> methodSetByKey = new HashMap<String, Set<String>>();
	for (String key : keys) {
	    methodSetByKey.put(key, new HashSet<String>());
	}

	for (String method : keyStatByMethod.keySet()) {
	    Map<String, Integer> keyStat =  keyStatByMethod.get(method);
	    for (String key : keyStat.keySet()) {
		if (keyStat.get(key) > 0) {
		    methodSetByKey.get(key).add(method);
		}
	    }
	}
	
	for (String key : keys) {
	    System.out.println("num of methods involving" + key + " : " + methodSetByKey.get(key).size());
	}
	
    }
} 
