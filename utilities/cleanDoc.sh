#!/bin/bash

cd `dirname $0`
rm -rf javadoc &> /dev/null
rm javadoc.out &> /dev/null
rm pydocs.out &> /dev/null
cd pydocs
make clean &> /dev/null
