#!/bin/bash

if [ $# -ne 1 ]; then echo "./filter.sh [file]"; exit -1; fi

file=$1

parameter_store=/root/reconf_test_gen//all_all_parameters.txt

for i in $(cat $file)
do 
    if [ "$(grep -Fw $i $parameter_store)" != "" ]; then 
	echo $i
    fi
 done | sort -u
