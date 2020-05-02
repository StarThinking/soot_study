#!/bin/bash

if [ $# -ne 2 ]; then echo "./filter.sh [file] [proj]"; exit -1; fi

file=$1
proj=$2

parameter_store=/root/reconf_test_gen/"$proj"/all_parameters.txt

for i in $(cat $file)
do 
    if [ "$(grep ^"$i"$ $parameter_store)" != "" ]; then 
	echo $i
    fi
 done | sort -u
