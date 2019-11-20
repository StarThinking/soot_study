import java.util.*;

public class HdfsAnalysis extends JunitTestAnalysis {
    private static List<String> processDirs = new ArrayList<String>();
    private static List<String> keys = new ArrayList<String>();
    private static List<StartAfterStop> sasList = new ArrayList<StartAfterStop>();

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
//	keys.add("restartNameNode");
//	keys.add("restartDataNode");
//	keys.add("restartJournalNode");
	
	//sasList.add(new StartAfterStop("restartDataNode", "createNameNode"));
	sasList.add(new StartAfterStop("", "restartDataNode"));
	sasList.add(new StartAfterStop("void shutdown", "runDatanodeDaemon"));
	//sasList.add(new StartAfterStop("org.apache.hadoop.hdfs.server.datanode.DataNode: void shutdown", "org.apache.hadoop.hdfs.server.datanode.DataNode: org.apache.hadoop.hdfs.server.datanode.DataNode createDataNode"));
	//sasList.add(new StartAfterStop("stopNameNode", "startNameNode"));
    }

    public HdfsAnalysis() {
	super(processDirs, keys, sasList);
    }

    public static void main(String[] args) {
        HdfsAnalysis analysis = new HdfsAnalysis();
        analysis.start();
	analysis.analysisByKey(1);	
    }
} 
