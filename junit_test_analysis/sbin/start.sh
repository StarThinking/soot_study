#!/bin/bash

component=$1

function check_restart() {
    #methodfile="org.apache.hadoop.tracing.TestTracingShortCircuitLocalRead#testShortCircuitTraceHooks"
    methodfile=$1
    started=0
    while IFS= read -r line
    do
        if [ "$line" == "[msx-restart] $component start" ]; then
                started=1
        fi
    done < "$methodfile"
    
    return $started
}

for i in *
do
    check_restart $i
    ret=$?
    if [ $ret -eq 1 ]; then
        echo "$i $component start"
    fi
done
