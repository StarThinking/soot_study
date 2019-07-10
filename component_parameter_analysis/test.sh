#!/bin/bash

for component in org.apache.hadoop.hdfs.server.namenode.NameNode org.apache.hadoop.hdfs.server.datanode.DataNode org.apache.hadoop.hdfs.qjournal.server.JournalNode; do echo $component; java -cp build/libs/component_parameter_analysis-1.0.jar ComponentAnalysis /root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/classes $component; echo ""; done > result.txt
