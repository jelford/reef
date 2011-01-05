#!/bin/bash

# This will be run inside the git repo and given the vars
# - $1 = branchname
# - $2 = commit sha

# Get rid of the current javadoc dir and replace the contents
echo -n "Clearing the javadoc directory..."
git rm -r javadoc &> /dev/null
rm -rf javadoc &> /dev/null
echo "and filling it with the new docs..."
cp -r ../javadoc javadoc &> /dev/null
echo "Done."

# Commit changes
echo "Committing new pages..."
git add javadoc &> /dev/null
git commit -m "Added JavaDocs for the branch \"$1\" at commit: $2" &> /dev/null
