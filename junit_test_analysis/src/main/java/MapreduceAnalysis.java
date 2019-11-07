public class MapreduceAnalysis extends JUnitTestAnalysis {

    public MapreduceAnalysis(String clusterInvolvedKey) {
	super(clusterInvolvedKey);
    }

    @Override
    protected void initProcessDir() {
	String base = "/root/hadoop-3.1.2-src/hadoop-mapreduce-project/hadoop-mapreduce-client/";
        processDirs.add(base + "hadoop-mapreduce-client-app/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-common/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-core/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-hs/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-hs-plugins/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-jobclient/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-nativetask/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-shuffle/target/test-classes");
        processDirs.add(base + "hadoop-mapreduce-client-uploader/target/test-classes");
    }
} 
