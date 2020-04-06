#!/bin/bash

for component in org.apache.hadoop.hdfs.server.namenode.NameNode org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode org.apache.hadoop.hdfs.server.datanode.DataNode org.apache.hadoop.hdfs.qjournal.server.JournalNode org.apache.hadoop.hdfs.server.balancer.Balancer org.apache.hadoop.hdfs.server.mover.Mover
do
    java -cp build/libs/component_parameter_analysis-1.0.jar ComponentAnalysis $component > "$component".txt
done
