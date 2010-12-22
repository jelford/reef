#!/bin/bash

cd `dirname $0`
rm -rf javadoc
rm javadoc.out
cd pydocs
make clean &> /dev/null
