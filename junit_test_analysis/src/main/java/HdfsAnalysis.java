public class HdfsAnalysis extends JunitTestAnalysis {

//    private static final String clusterInvolvedKey = "org.apache.hadoop.hdfs.MiniDFS";

    public HdfsAnalysis() {
	super(new String[] {"org.apache.hadoop.hdfs.MiniDFS"});
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

    public static void main(String[] args) {
        HdfsAnalysis analysis = new HdfsAnalysis();
        analysis.start();
    }
} 
