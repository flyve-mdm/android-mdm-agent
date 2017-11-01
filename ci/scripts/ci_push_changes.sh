#!/usr/bin/env bash

# Push commits and tags to origin branch
git push --follow-tags origin $CIRCLE_BRANCH

# Merge back the develop branch step

# delete branch
git branch -D develop

# get fresh branch
git fetch origin develop

# go to develop
git checkout develop

# review some change
git pull origin develop

# merge with master
git merge master

# push develop
git push origin develop --force

# return to master
git checkout master