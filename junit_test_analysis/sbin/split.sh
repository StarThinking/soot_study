#!/bin/bash
set -u
numofnotequal=0
function split_class_into_methods {
    file=$1
    #file='org.apache.hadoop.hdfs.qjournal.server.TestJournalNodeSync-output.txt'
    IFS_backup=$IFS
    IFS=$'\n'
    startlinearray=( $(grep -rn testStarted $file) )
    finishlinearray=( $(grep -rn testFinished $file) )
    methodnamearray=()
    startarray=()
    finisharray=()
    
    class=$(echo $file | awk -F '-output.txt' '{print $1}')
   
    if [ ${#startlinearray[@]} -eq 0 ] || [ ${#finishlinearray[@]} -eq 0 ]; then
        echo 'startlinearray or finishlinearray has no method test, skip'
        return
    fi

    for line in ${startlinearray[@]}
    do 
        name=$(echo $line | awk -F ':| '  '{print $4}')
        methodnamearray+=("$name")
    done
    
    
    for line in ${startlinearray[@]}
    do 
        linenum=$(echo $line | awk -F ':'  '{print $1}')
        startarray+=("$linenum")
    done
    
    for line in ${finishlinearray[@]}
    do 
        linenum=$(echo $line | awk -F ':'  '{print $1}')
        finisharray+=("$linenum")
    done
    
    IFS=$IFS_backup
    startarray_size=${#startarray[@]}
    finisharray_size=${#finisharray[@]}
    if [ $startarray_size -ne $finisharray_size ]; then
        echo "arrays size are not equal, skip"
        numofnotequal=$(( numofnotequal + 1 ))
        return
    fi
    
    for i in ${!startarray[@]}
    do
        classandmethod="$class""#""${methodnamearray[$i]}"
        echo "classandmethod is $classandmethod"
        echo "start at ${startarray[$i]}"
        echo "finish at ${finisharray[$i]}"
        sed -n "${startarray[$i]},${finisharray[$i]}p" $file > ../method/"$classandmethod"
        echo ""
    done
}

logdir='/root/hadoop-3.1.2-src/hadoop-hdfs-project/hadoop-hdfs/target/surefire-reports/'
for file in $(ls . | grep Test | grep "output.txt")
do
    filepath="$file"
    echo $filepath
    split_class_into_methods $filepath
done
echo "numofnotequal is $numofnotequal"
