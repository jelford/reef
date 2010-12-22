#!/bin/bash

# Excluded directories and files
function writeExcluded {
  echo "./google"
  echo "./startup.py"
}

# Take a file path and echo a module name
function getModule {
  mod=${1%\.py}
  mod=${mod#\./}
  mod=$(echo "$mod" | sed 's/\//\./g')
  
  echo "$mod"
}

# Take a module and give the file path for the rst file
function getRst {
  echo "$docsrc/$(echo "$1" | sed 's/\./\//g').rst"
}

# Take a pattern and echo the arguments to find needed for finding those files
# If the pattern has a directory, this searches the whole name
function getFindArgs {
  echo "."
  echo "("
  
  for excl in $(writeExcluded); do
    echo "-wholename \"$excl\""
    echo "-o"
  done
  echo "-false"
  echo ")"
  echo "-prune"
  echo "-o"
  echo "("
  # If we have a directory in the pattern
  if [[ "$1" =~ .*/.* ]]; then
    echo "-wholename"
  else
    echo "-name"
  fi
  echo "$1"
  echo ")"
  echo "-print"
}

# Find all python files that are subdirectories of the current directory
# __init__.pys are left until the end and then the directory name is given
function findPy {
  inits=( )
  for file in $(getFindArgs "*.py" | xargs find); do
    if [ "`basename $file`" == "__init__.py" ]; then
      # This is a directory module
      inits=( "${inits[@]}" "`dirname $file`" )
    else
      echo $file
    fi
  done
  for dir in "$inits"; do
    echo $dir
  done
}

# Pass a module and this will create the basic rst file for it
function createRst {
  module=$1
  rstfile=$(getRst $module)

  echo "  Creating doc file for $module..."

  title="\`\`$module\`\` Module"
  underline=$(echo "$title" | sed 's/./=/g')

  mkdir -p `dirname $rstfile`
  echo $title > $rstfile
  echo $underline >> $rstfile
  echo >> $rstfile
  echo ".. automodule:: $module" >> $rstfile
}

pydocs=$(cd `dirname $0`; pwd)
docsrc="$pydocs/source"

echo "Creating corresponding files for all new python source."
echo "This should ensure we at least get a warning if we forget to document"

cd `dirname $0`
cd ../../reef

# Go through each found module
for file in $(findPy); do
  module=$(getModule $file)
  rst=$(getRst $module)

  if [ ! -e "$rst" ]; then
    echo "No doc file exists for the module: $module"
    createRst $module

    if [[ "$file" =~ .*\.py ]]; then
      echo "  This module is just a basic module."
    else
      echo "  This module is a directory, adding ToC..."

      echo >> $rst
      echo ".. toctree::" >> $rst
      echo "   :maxdepth: 1" >> $rst
      echo >> $rst

      thismodule=`basename $file`

      for sub in $(cd $file; ls *.py); do
        if [ "$sub" != "__init__.py" ]; then
          mod=${sub%\.py}
          echo "  Adding $sub"
          echo "   $thismodule/$mod.rst" >> $rst
        fi
      done
    fi
  fi

done

echo "Documentation skeleton generation complete."
