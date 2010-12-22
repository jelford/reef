#!/bin/bash

cd `dirname $0`

# Check for necessary tools
function ensureInstalled {
  if [ ! $(which $1) ]; then
    echo "Cannot find $1!"
    echo "Make sure this is installed and on the system path and try again."
    exit
  fi
}

ensureInstalled which
ensureInstalled javadoc

# Get rid of the current javadoc dir and replace the contents
echo -n "Clearing the javadoc directory..."
rm -rf javadoc &> /dev/null
echo "and filling it with the new docs..."
javadoc -quiet -d javadoc -windowtitle "Reef JavaDocs" -private -sourcepath "../gwt/ReefFront/src" -classpath "jdoclibs/*:../gwt/*" -subpackages uk.ac.imperial.vazels.reef 2> javadoc.out
echo "Done. Look in javadoc.out for info on how this went."
