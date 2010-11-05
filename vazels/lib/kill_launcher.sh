#!/bin/bash

kill_all() {
    export children_of_$1="`ps -ef| awk '$3 == '${1}' { print $2 }'`"
    kill -15 $1
    evalname='eval "echo \$children_of_$1"'
    for child in `eval $evalname`; do
        kill_all $child
    done
}

kill_all $1
