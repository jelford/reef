#!/bin/bash

# Excluded directories and files
function writeExcluded {
  echo "./google"
  echo "./startup.py"
}

pydocs=$(cd `dirname $0`; pwd)
echo $pydocs
docsrc="$pydocs/source"

echo "Creating corresponding files for all the python source."
echo "This should ensure we at least get a warning if we forget to document"

cd `dirname $0`
cd ../../reef

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
  echo "-name"
  echo "*.py"
  echo ")"
  echo "-print"
}

for file in $(getFindArgs | xargs find); do
  base=${file%\.py}
  base=${base#\./}

  rstfile="$docsrc/$base.rst"

  if [ ! -e "$rstfile" ]; then
    echo "No .rst file for: $file"
    echo "Creating one now..."
    
    mkdir -p `dirname $rstfile`

    module=$(echo "$base" | sed 's/\//./g')
    title="\`\`$module\`\` Module"
    
    underline=$(echo "$title" | sed 's/./=/g')
    echo $title > $rstfile
    echo $underline >> $rstfile
    echo >> $rstfile
    echo "  .. automodule:: $module" >> $rstfile
  fi
done
