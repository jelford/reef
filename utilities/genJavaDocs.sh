#!/bin/bash

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

# Get rid of the current javadoc dir and replace the contents
echo -n "Clearing the javadoc directory..."
git rm -r javadoc &> /dev/null
rm -rf javadoc &> /dev/null
echo "and filling it with the new docs..."
javadoc -quiet -d javadoc -windowtitle "Reef JavaDocs" -private -sourcepath "../../gwt/ReefFront/src" -classpath "../jdoclibs/*:../../gwt/*" -subpackages uk.ac.imperial.vazels.reef 2> ../javadoc.out
echo "Done."

# Commit and push changes
echo -n "Committing new pages..."
git add javadoc &> /dev/null
git commit -m "Added JavaDocs for the branch \"$branchname\" at commit: $commitsha" &> /dev/null
echo "and pushing them"
git push origin gh-pages
echo "Finished!"

# Remove remnants
cd .. &> /dev/null
rm -rf $repo &> /dev/null
