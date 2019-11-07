public class HbaseAnalysis extends JUnitTestAnalysis {

    public HbaseAnalysis(String clusterInvolvedKey) {
	super(clusterInvolvedKey);
    }

    @Override
    protected void initProcessDir() {
        String base = "/root/hadoop-3.1.2-src/hadoop-yarn-project/hadoop-yarn";
        processDirs.add(base + "/hadoop-yarn-server/hadoop-yarn-server-web-proxy/target/test-classes");   
    }
} 
