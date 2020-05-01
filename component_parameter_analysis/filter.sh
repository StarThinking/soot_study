#!/bin/bash

if [ $# -ne 2 ]; then echo "./filter.sh [proj] [file]"; exit -1; fi

proj=$1
file=$2

for i in $(cat $file)
do 
    if [ "$(grep -Fw $i /root/reconf_test_gen/"$proj"/all_parameters.txt)" != "" ]; then 
	echo $i
    fi
 done | sort -u
