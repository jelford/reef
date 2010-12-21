#!/bin/bash

# To use later
repo="page-repo"
rundir=`dirname $0`

# Go to run directory
cd $rundir

# Grab current branch name
echo "Getting current branch..."
branchname="$(git symbolic-ref HEAD 2>/dev/null)" ||
branchname="(unnamed branch)"     # detached HEAD
branchname=${branchname##refs/heads/}
echo "You are on branch $branchname"

# Get the current commit
echo "Getting latest commit..."
commitsha="$(git rev-parse master)"
echo "Your commit is $commitsha"

# Clear page-repo directory
echo "Clearing repo space..."
rm -rf $repo
mkdir $repo
echo "All gone."

# Move our repo in
echo "Copying our current repo into itself..."
cp -r ../.git $repo
echo "Done."

# Move to our new repo and checkout gh-pages
cd $repo

# Get up to date pages repo
echo "Grabbing most up to date version of the current documentation..."
# Go to master so we can remove the current doc repo
git checkout master &> /dev/null
# Then remove the pages branch
git branch -D gh-pages &> /dev/null
# And fetch the new version
git fetch origin gh-pages
# Finally check it out
git checkout gh-pages 
echo "Got it, sorry if that took a while."

# Get rid of the current javadoc dir and replace the contents
echo "Clearing the javadoc directory..."
git rm -r javadoc
rm -rf javadoc
echo "..and filling it with the new docs..."
javadoc -quiet -d javadoc -windowtitle "Reef JavaDocs" -private -sourcepath "../../gwt/ReefFront/src" -classpath "../jdoclibs/*:../../gwt/*" -subpackages uk.ac.imperial.vazels.reef
echo "Done."

# Commit and push changes
git add javadoc
git commit -m "Added JavaDocs for the branch \"$branchname\" at commit: $commitsha"
git push origin gh-pages

# Remove remnants
cd ..
rm -rf $repo
