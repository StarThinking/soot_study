public class YarnAnalysis extends JUnitTestAnalysis {

    public YarnAnalysis(String clusterInvolvedKey) {
	super(clusterInvolvedKey);
    }

    @Override
    protected void initProcessDir() {
        String base = "/root/hadoop-3.1.2-src/hadoop-yarn-project/hadoop-yarn";
        processDirs.add(base + "/hadoop-yarn-api/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-applications/hadoop-yarn-applications-distributedshell/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-applications/hadoop-yarn-services/hadoop-yarn-services-api/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-applications/hadoop-yarn-services/hadoop-yarn-services-core/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-applications/hadoop-yarn-applications-unmanaged-am-launcher/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-client/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-common/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-registry/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-applicationhistoryservice/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-common/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-nodemanager/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-resourcemanager/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-router/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-sharedcachemanager/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-tests/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-timeline-pluginstorage/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-timelineservice/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/hadoop-yarn-server-timelineservice-hbase-client/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/hadoop-yarn-server-timelineservice-hbase-common/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase/hadoop-yarn-server-timelineservice-hbase-server/hadoop-yarn-server-timelineservice-hbase-server-1/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-timelineservice-hbase-tests/target/test-classes");
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-web-proxy/target/test-classes");   
    }
} 
