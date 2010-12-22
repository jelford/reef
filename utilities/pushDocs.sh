#!/bin/bash

if [ $# -eq 0 ]; then
  echo "Usage: $0 doctype1 doctype2 ..."
  echo "Example: $0 java python"
  exit
fi

# Test for doc types
for type in "$@"; do
  scr=./getDocs_$type.sh
  if [ ! -e "$scr" ]; then
    echo "$type is not a valid type of documentation. Terminating."
    exit
  fi
done


# To use later
repo="page-repo"
rundir=`dirname $0`

# Go to run directory
cd $rundir

# Grab current branch name
echo -n "Getting current branch..."
branchname="$(git symbolic-ref HEAD 2>/dev/null)" ||
branchname="(unnamed branch)"     # detached HEAD
branchname=${branchname##refs/heads/}
echo "$branchname"

# Get the current commit
echo -n "Getting latest commit..."
commitsha="$(git rev-parse $branchname)"
echo "$commitsha"

# Clear page-repo directory
echo -n "Clearing repo space..."
rm -rf $repo &> /dev/null
mkdir $repo &> /dev/null
echo "All gone."

# Move our repo in
echo -n "Copying our current repo into itself..."
cp -r ../.git $repo &> /dev/null
echo "Done."

# Move to our new repo and checkout gh-pages
cd $repo &> /dev/null

# Get up to date pages repo
echo -n "Grabbing most up to date version of the current documentation..."
# Go to master so we can remove the current doc repo
git checkout master &> /dev/null
# Then remove the pages branch
git branch -D gh-pages &> /dev/null
# Finally check it out
git checkout gh-pages &> /dev/null
# And fetch the new version
git pull origin gh-pages &> /dev/null
echo "Got it, sorry if that took a while."

# Perform the documentation grabbing
for type in "$@"; do
  ../getDocs_$type.sh
done

echo "Pushing all changes..."
git push origin gh-pages
echo "Finished!"

# Remove remnants
cd .. &> /dev/null
rm -rf $repo &> /dev/null
