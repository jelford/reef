#!/bin/bash

# This will be run inside the git repo and given the vars
# - $1 = branchname
# - $2 = commit sha

# Get rid of the current pydoc dir and replace the contents
echo -n "Clearing the pydoc directory..."
git rm -r pydoc &> /dev/null
rm -rf pydoc &> /dev/null
echo "and filling it with the new docs..."
cp -r ../pydocs/build/html pydoc &> /dev/null
echo "Done."

# Commit changes
echo "Committing new pages..."
git add pydoc &> /dev/null
git commit -m "Added PyDocs for the branch \"$1\" at commit: $2" &> /dev/null
