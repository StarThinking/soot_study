import java.util.*;

public class HdfsAnalysis extends JunitTestAnalysis {
    private static List<String> processDirs = new ArrayList<String>();
    private static List<String> clusterKeys = new ArrayList<String>();
    private static List<StartAfterStop> sasList = new ArrayList<StartAfterStop>();

    static {
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-native-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-nfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-rbf/target/test-classes");

	clusterKeys.add("org.apache.hadoop.hdfs.MiniDFSClusterWithNodeGroup");
        clusterKeys.add("org.apache.hadoop.hdfs.MiniDFS");
	clusterKeys.add("org.apache.hadoop.hdfs.qjournal.MiniQJMHACluster");
	clusterKeys.add("org.apache.hadoop.hdfs.qjournal.MiniJournalCluster");

        /* 
         * JournalNode
         * MiniJournalCluster object is field in MiniQJMHACluster
         * but they have different tests, no subset relation
         */
        //sasList.add(new StartAfterStop("", "restartJournalNode"));
        sasList.add(new StartAfterStop("org.apache.hadoop.hdfs.qjournal.server.JournalNode: void stop", 
                    "org.apache.hadoop.hdfs.qjournal.server.JournalNode: void start"));

        /* 
         * DataNode
         */
	//sasList.add(new StartAfterStop("", "restartDataNode"));
	sasList.add(new StartAfterStop("org.apache.hadoop.hdfs.server.datanode.DataNode: void shutdown", 
                    "org.apache.hadoop.hdfs.server.datanode.DataNode: void runDatanodeDaemon"));
	
        /* 
         * NameNode
         * sas has 6 more methods than solely searching by restartNameNode
         * <org.apache.hadoop.hdfs.TestRollingUpgradeRollback: void testRollbackCommand()>
         * <org.apache.hadoop.hdfs.server.namenode.TestFSEditLogLoader: void testDisplayRecentEditLogOpCodes()>
         * <org.apache.hadoop.hdfs.server.datanode.TestDataNodeMultipleRegistrations: void testMiniDFSClusterWithMultipleNN()>
         * <org.apache.hadoop.hdfs.server.namenode.TestEditLog: void testEditChecksum()>
         * <org.apache.hadoop.hdfs.server.namenode.TestMetadataVersionOutput: void testMetadataVersionOutput()>
         * <org.apache.hadoop.hdfs.qjournal.TestNNWithQJM: void testMismatchedNNIsRejected()>
         */
        //sasList.add(new StartAfterStop("", "restartNameNode"));
	sasList.add(new StartAfterStop("org.apache.hadoop.hdfs.server.namenode.NameNode: void stop", 
                    "org.apache.hadoop.hdfs.server.namenode.NameNode: org.apache.hadoop.hdfs.server.namenode.NameNode createNameNode"));
    }

    public HdfsAnalysis() {
	super(processDirs, clusterKeys, sasList);
    }

    public static void main(String[] args) {
        HdfsAnalysis analysis = new HdfsAnalysis();
        analysis.start();
	analysis.analysis();	
    }
} 
