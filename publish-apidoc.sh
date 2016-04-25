#!/bin/bash

# fail and abort on any error
set -e

mvn javadoc:javadoc

# go to the apidocs
cd target/site/apidocs

# initialise a (temporary!) repository
git init
git checkout -b gh-pages
git remote add javadoc "https://github.com/spinfo/Strings.git"
git fetch --depth=1 javadoc gh-pages

# add, commit and merge overwriting the previous gh-pages completely
git add --all
git commit -m "New javadoc."
git merge --no-edit -s ours remotes/javadoc/gh-pages

# push to the remote
git push javadoc gh-pages

# delete the temporary repo
rm -rf ".git"
