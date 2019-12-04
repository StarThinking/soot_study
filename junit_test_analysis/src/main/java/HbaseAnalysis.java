import java.util.*;


/*
 * DistributedHBaseCluster
 * MiniHBaseCluster: This class creates a single process HBase cluster.
 * implement abstract class HBaseCluster
 */
public class HbaseAnalysis extends JunitTestAnalysis {
    private static String classPath = "";
    private static List<String> processDirs = new ArrayList<String>();
    private static List<String> clusterKeys = new ArrayList<String>();
    private static List<StartAfterStop> sasList = new ArrayList<StartAfterStop>();
    private static String base = "/root/hbase-2.2.1/";
    
    static {
        /* we have to add the following jar to load class LocalHBaseCluster */
        classPath += "/root/hbase-2.2.1/hbase-server/target/hbase-server-2.2.1.jar";

        processDirs.add(base + "hbase-rsgroup/target/test-classes");
        processDirs.add(base + "hbase-examples/target/test-classes");
        processDirs.add(base + "hbase-annotations/target/test-classes");
        processDirs.add(base + "hbase-common/target/test-classes");
        processDirs.add(base + "hbase-hadoop-compat/target/test-classes");
        processDirs.add(base + "hbase-protocol-shaded/target/test-classes");
        processDirs.add(base + "hbase-it/target/test-classes");
        processDirs.add(base + "hbase-protocol/target/test-classes");
        processDirs.add(base + "hbase-client/target/test-classes");
        processDirs.add(base + "hbase-thrift/target/test-classes");
        processDirs.add(base + "hbase-checkstyle/target/test-classes");
        processDirs.add(base + "hbase-metrics/target/test-classes");
        processDirs.add(base + "hbase-hadoop2-compat/target/test-classes");
        processDirs.add(base + "hbase-external-blockcache/target/test-classes");
        processDirs.add(base + "hbase-procedure/target/test-classes");
        processDirs.add(base + "hbase-replication/target/test-classes");
        processDirs.add(base + "hbase-rest/target/test-classes");
        processDirs.add(base + "hbase-archetypes/hbase-client-project/target/test-classes");
        processDirs.add(base + "hbase-archetypes/hbase-shaded-client-project/target/test-classes");
        processDirs.add(base + "hbase-http/target/test-classes");
        processDirs.add(base + "hbase-testing-util/target/test-classes");
        processDirs.add(base + "hbase-metrics-api/target/test-classes");
        processDirs.add(base + "hbase-endpoint/target/test-classes");
        processDirs.add(base + "hbase-zookeeper/target/test-classes");
        processDirs.add(base + "hbase-mapreduce/target/test-classes");
        processDirs.add(base + "hbase-shell/target/test-classes");
        processDirs.add(base + "hbase-shaded/hbase-shaded-testing-util-tester/target/test-classes");

        processDirs.add(base + "hbase-server/target/test-classes");
        //processDirs.add(base + "hbase-hbtop/target/test-classes"); // for 2.2.2

        /* 
         * MiniHBaseCluster extends abstract class HBaseCluster.
         * HDFS/ZK related start/stop methods are no supported in MiniHBaseCluster.
         */
	clusterKeys.add("org.apache.hadoop.hbase.HBaseTestingUtility");
	clusterKeys.add("org.apache.hadoop.hbase.LocalHBaseCluster");
	clusterKeys.add("org.apache.hadoop.hbase.util.JVMClusterUtil");
	clusterKeys.add("org.apache.hadoop.hbase.MiniHBaseCluster");
	clusterKeys.add("org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster");

        /*
         * One example to study cluser start/stop: TestRetainAssignmentOnRestart
         */
        sasList.add(new StartAfterStop("", "JVMClusterUtil$MasterThread: void start()"));
        sasList.add(new StartAfterStop("", "org.apache.hadoop.hbase.master.HMaster: void stopMaster()"));
        sasList.add(new StartAfterStop("org.apache.hadoop.hbase.master.HMaster: void stopMaster()", "JVMClusterUtil$MasterThread: void start()"));
        
        sasList.add(new StartAfterStop("", "JVMClusterUtil$RegionServerThread: void start()"));
        sasList.add(new StartAfterStop("", "org.apache.hadoop.hbase.regionserver.HRegionServer: void stop(java.lang.String)"));
        sasList.add(new StartAfterStop("org.apache.hadoop.hbase.regionserver.HRegionServer: void stop(java.lang.String)", "JVMClusterUtil$RegionServerThread: void start()"));
    }

    public HbaseAnalysis() {
	super(classPath, processDirs, clusterKeys, sasList);
    }
    
    public static void main(String[] args) {
	HbaseAnalysis analysis = new HbaseAnalysis();
	analysis.start();
	analysis.analysis();
    }
} 
