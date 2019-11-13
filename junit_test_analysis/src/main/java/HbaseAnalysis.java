public class HbaseAnalysis extends JunitTestAnalysis {

//    private static final String clusterInvolvedKey = "org.apache.hadoop.hbase.MiniHBaseCluster";
    
    public HbaseAnalysis() {
	super(new String[] {"org.apache.hadoop.hbase.MiniHBaseCluster"});
    }

    @Override
    protected void initProcessDir() {
        String base = "/root/hbase-2.2.2/";
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
	processDirs.add(base + "hbase-hbtop/target/test-classes");
    }
    
    public static void main(String[] args) {
	HbaseAnalysis analysis = new HbaseAnalysis();
	analysis.start();
    }
} 
