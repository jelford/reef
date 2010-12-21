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
ensureInstalled sphinx-build

# Clean the current python build dir and replace the contents
echo "Adding skeleton pages for new modules."
pydocs/ensureAllDoced.sh
echo
echo "Clearing the sphinx build directory..."
cd pydocs
make clean &> /dev/null
echo "and filling it with the new docs..."
make html 2> ../pydocs.out
echo "Now checking for documentation coverage."
make coverage &> /dev/null
echo >> ../pydocs.out
cat build/coverage/python.txt >> ../pydocs.out
echo >> ../pydocs.out
echo "There may be more undocumented data, coverage doesn't seem to work properly" >> ../pydocs.out
echo "Done. Look in pydocs.out for info on how this went."
cd ..
