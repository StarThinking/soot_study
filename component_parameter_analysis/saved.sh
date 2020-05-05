for i in mapreduce/*.txt; do ./filter.sh $i > mapreduce/filter/$(echo $i | awk -F '/' '{print $2}'); done

for proj in hdfs hbase yarn mapreduce; do get_funcs=$(ls "$proj"/*txt | awk -F '_' '{print $1}' | sort -u); for func in ${get_funcs[@]}; do echo "$func"; cat "$func"*; done; done
