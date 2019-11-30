import java.util.*;

public class HbaseAnalysis extends JunitTestAnalysis {
    private static List<String> processDirs = new ArrayList<String>();
    private static List<String> clusterKeys = new ArrayList<String>();
    private static List<StartAfterStop> sasList = new ArrayList<StartAfterStop>();
    private static String base = "/root/hbase-2.2.1/";
    
    static {
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
         * MiniHBaseCluster extends abstract class HBaseCluster
         * HDFS/ZK related start()/stop() is no supported
         */
	clusterKeys.add("org.apache.hadoop.hbase.HBaseTestingUtility");
	clusterKeys.add("org.apache.hadoop.hbase.LocalHBaseCluster");
	clusterKeys.add("org.apache.hadoop.hbase.util.JVMClusterUtil");
	clusterKeys.add("org.apache.hadoop.hbase.MiniHBaseCluster");
	clusterKeys.add("org.apache.hadoop.hbase.zookeeper.MiniZooKeeperCluster");

        /*
         * TestRetainAssignmentOnRestart
         */
        //sasList.add(new StartAfterStop("", "startMaster"));
        /*sasList.add(new StartAfterStop("", "org.apache.hadoop.hbase.util.JVMClusterUtil$MasterThread: void start"));
        sasList.add(new StartAfterStop("", "org.apache.hadoop.hbase.master.HMaster: void stopMaster"));
        sasList.add(new StartAfterStop("org.apache.hadoop.hbase.master.HMaster: void stopMaster", "org.apache.hadoop.hbase.util.JVMClusterUtil$MasterThread: void start"));
        sasList.add(new StartAfterStop("MasterThread stopMaster", "MasterThread startMaster"));
        sasList.add(new StartAfterStop("stopMaster", "startMaster"));
        sasList.add(new StartAfterStop("stopRegionServer", "startRegionServer"));
        sasList.add(new StartAfterStop("JVMClusterUtil.MasterThread abortMaster", "JVMClusterUtil.MasterThread startMaster"));
        sasList.add(new StartAfterStop("", "startMaster"));
        sasList.add(new StartAfterStop("", "stopMaster"));
        sasList.add(new StartAfterStop("", "restartHBaseCluster"));
        sasList.add(new StartAfterStop("stopMaster", "startMaster"));*/
        /*sasList.add(new StartAfterStop("", "startMiniCluster("));
        sasList.add(new StartAfterStop("", "MasterThread: void start"));
        sasList.add(new StartAfterStop("", "MiniHBaseCluster: void init"));
        sasList.add(new StartAfterStop("", "HMaster: void stopMaster("));
        sasList.add(new StartAfterStop("", "HMaster: void stop("));
        sasList.add(new StartAfterStop("", "HMaster: void shutdown("));*/
        sasList.add(new StartAfterStop("", "HMaster: void stopMaster"));
        sasList.add(new StartAfterStop("", "HBaseCluster: void stopMaster"));
        sasList.add(new StartAfterStop("", "HBaseCluster: void shutdown"));
        sasList.add(new StartAfterStop("", "shutdown"));
//        sasList.add(new StartAfterStop("HMaster: void stopMaster", "MasterThread: void start"));
//        sasList.add(new StartAfterStop("HMaster: void stop", "MasterThread: void start"));
        //sasList.add(new StartAfterStop("", "HMaster stop"));
        //sasList.add(new StartAfterStop("org.apache.hadoop.hdfs.server.namenode.NameNode: void stop", 
            //"org.apache.hadoop.hdfs.server.namenode.NameNode: org.apache.hadoop.hdfs.server.namenode.NameNode createNameNode"));
    }

    public HbaseAnalysis() {
	super(processDirs, clusterKeys, sasList);
    }
    
    public static void main(String[] args) {
	HbaseAnalysis analysis = new HbaseAnalysis();
	analysis.start();
	analysis.analysis();
    }
} 
