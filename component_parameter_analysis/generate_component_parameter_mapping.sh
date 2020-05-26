#!/bin/bash

method_array+=("get")
method_array+=("getTrimmed")
method_array+=("getRaw")
method_array+=("get")
method_array+=("getInt")
method_array+=("getInts")
method_array+=("getLong")
method_array+=("getLongBytes")
method_array+=("getFloat")
method_array+=("getDouble")
method_array+=("getBoolean")
method_array+=("getEnum")
method_array+=("getTimeDuration")
method_array+=("getTimeDurationHelper")
method_array+=("getTimeDurations")
method_array+=("getStorageSize")
method_array+=("getPattern")
method_array+=("getPropertySources")
method_array+=("getRange")
method_array+=("getStringCollection")
method_array+=("getStrings")
method_array+=("getTrimmedStringCollection")
method_array+=("getTrimmedStrings")
method_array+=("getPassword")
method_array+=("getPasswordFromCredentialProviders")
method_array+=("getClassByName")
method_array+=("getClassByNameOrNull")
method_array+=("getClasses")
method_array+=("getClass")
method_array+=("getClass")
method_array+=("getInstances")
method_array+=("getResource")
method_array+=("getConfResourceAsInputStream")

hdfs_components+=('org.apache.hadoop.hdfs.server.namenode.NameNode')
hdfs_components+=('org.apache.hadoop.hdfs.server.datanode.DataNode')
hdfs_components+=('org.apache.hadoop.hdfs.qjournal.server.JournalNode')
hdfs_components+=('org.apache.hadoop.hdfs.server.namenode.SecondaryNameNode')
yarn_components+=('org.apache.hadoop.yarn.server.resourcemanager.ResourceManager')
yarn_components+=('org.apache.hadoop.yarn.server.nodemanager.NodeManager')
yarn_components+=('org.apache.hadoop.yarn.server.applicationhistoryservice.ApplicationHistoryServer')
mapreduce_components+=('org.apache.hadoop.mapreduce.v2.hs.JobHistoryServer')
hbase_components+=('org.apache.hadoop.hbase.master.HMaster')
hbase_components+=('org.apache.hadoop.hbase.regionserver.HRegionServer')

if [ $# -ne 2 ]; then echo 'wrong argument: [component_project] [dst_dir], exit.'; exit -1; fi
component_project=$1
dst_dir=$2
if [ ! -d $dst_dir ]; then echo 'WARN: dst_dir not exists. let create it'; mkdir $dst_dir; fi

component_array="$component_project"_components[@]
# dynamically create a new array
components=( $(for c in ${!component_array}; do echo $c; done) )

classpath_path="/root/reconf_test_gen/$component_project/soot_path/classpath.txt"
procdir_path="/root/reconf_test_gen/$component_project/soot_path/proc_dir.txt"

echo "component_project: $component_project"
echo "classpath_path: $classpath_path"
echo "procdir_path: $procdir_path"
echo "components: ${components[@]}"
echo "method_array: ${method_array[@]}"
echo ""

for method in ${method_array[@]}
do
    for component in ${components[@]}
    do 
	# obtain the last field, e.g., NameNode
	component_short=$(echo $component | awk -F '.' '{print $NF}')
	java -cp build/libs/component_parameter_analysis-1.0.jar ComponentAnalysis $procdir_path $classpath_path $method $component | awk -F ',' '{print $1}' > $dst_dir/"$method"_"$component_short"_2c.txt
	cat $dst_dir/"$method"_"$component_short"_2c.txt | awk -F ' ' '{print $1}' > $dst_dir/"$method"_"$component_short".txt
    done
done
