#!/bin/bash

component=$1

function check_restart() {
    methodfile=$1
    stopped=0
    restarted=0
    while IFS= read -r line
    do
#        echo "$line"
        if [ "$line" == "[msx-restart] $component stop" ]; then
            stopped=1
        fi
        if [ "$line" == "[msx-restart] $component start" ]; then
            if [ $stopped -eq 1 ]; then
                restarted=1
            fi
        fi
    done < "$methodfile"
    
    return $restarted
}

for i in *
do
    check_restart $i
    ret=$?
    if [ $ret -eq 1 ]; then
        echo "$i $component restart"
    fi
done
