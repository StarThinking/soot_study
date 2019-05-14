# Learn how to use Soot
A guide to learn how to use Soot to do static analysis

## Prerequisite

```.shell
sudo apt-get update
sudo apt-get -y upgrade
sudo apt-get -y install vim maven
```

## Build jar
```.shell
cd soot_study
git clone https://github.com/Sable/soot
cd soot
mvn clean compile assembly:single
```

## Build and run examples
```.shell
. ~/soot_study/export_cp.sh
cd tutorial/guide/examples/analysis_framework/src
javac dk/brics/soot/GenHelloWorld.java
javac dk/brics/soot/RunLiveAnalysis.java
javac testers/LiveVarsClass.java

cd ~/soot_study/soot
java dk/brics/soot/GenHelloWorld
java dk/brics/soot/RunLiveAnalysis
```

```
java soot.Main -cp ~/soot_study/soot/tutorial/guide/examples/analysis_framework/src:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/root/soot_study/soot/target/sootclasses-trunk-jar-with-dependencies.jar -output-format J dk.brics.soot.RunLiveAnalysis
```
