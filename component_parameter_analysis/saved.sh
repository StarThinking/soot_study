for i in mapreduce/*.txt; do ./filter.sh $i > mapreduce/filter/$(echo $i | awk -F '/' '{print $2}'); done
