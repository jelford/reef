#!/bin/sh

CURDIR=`pwd`

cd `dirname $0`

get_proname()
{
  FNAME=$1
  BASE=`basename $FNAME`
  echo ${BASE#pro_}
}

#bob

PRONAME="pro_default"

if [ $# -eq 0 ]; then
  echo "Current projects are:"
  echo ""
  for file in $( find . -name pro_* -print )
  do
    get_proname $file
  done
else
  PRONAME="pro_$1"

  mkdir $PRONAME
  cd $PRONAME
  python ../reef/startup.py
fi

cd $CURDIR
