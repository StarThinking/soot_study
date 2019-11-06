public class HdfsAnalysis extends JUnitTestAnalysis {

    public HdfsAnalysis(String clusterInvolvedKey) {
	super(clusterInvolvedKey);
    }

    @Override
    protected void initProcessDir() {
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-httpfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-native-client/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-nfs/target/test-classes");
        processDirs.add("/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs-rbf/target/test-classes");
    }
} 
