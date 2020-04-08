#!/bin/bash

method_array+=('getInt')
method_array+=('getInts')
method_array+=('getLong')
method_array+=('getLongBytes')
method_array+=('getFloat')
method_array+=('getDouble')
method_array+=('getBoolean')
method_array+=('getStorageSize')
method_array+=('getStrings')
method_array+=('getTimeDuration')
method_array+=('getRaw')
method_array+=('getEnum')
method_array+=('get')

component_array+=('org.apache.hadoop.hdfs.server.namenode.NameNode')
component_array+=('org.apache.hadoop.hdfs.server.datanode.DataNode')
component_array+=('org.apache.hadoop.hdfs.qjournal.server.JournalNode')
component_array+=('org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode')

for method in ${method_array[@]}
do
    for component in ${component_array[@]}
    do 
	# obtain the last field, e.g., NameNode
	component_short=$(echo $component | awk -F '.' '{print $NF}')
	java -cp build/libs/component_parameter_analysis-1.0.jar ComponentAnalysis $method $component | awk -F ',' '{print $1}' > "$method"_"$component_short"_2c.txt
	cat "$method"_"$component_short"_2c.txt | awk -F ' ' '{print $1}' > "$method"_"$component_short".txt
    done
done
