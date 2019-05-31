#!/bin/bash

for i in dfs.replication dfs.namenode.list.openfiles.num.responses dfs.namenode.edit.log.autoroll.check.interval.ms dfs.namenode.file.close.num-committed-allowed dfs.bytes-per-checksum; do java -cp build/libs/taint_analysis_bak-1.0.jar Test /root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/classes org.apache.hadoop.hdfs.server.namenode.NameNode $i; done
