#!/usr/bin/env bash

# Push commits and tags to origin branch
sudo mv ./fastlane/metadata/android ./screenshots
sudo mv ./screenshots/screenshots.html ./screenshots/index.html

# add
git add .

# temporal commit
git commit -m "ci(tmp): temporal commit"

# fetch
git fetch origin gh-pages

# move to branch
git checkout gh-pages

# clean workspace
sudo git clean -fdx

# git get screenshots
git checkout $CIRCLE_BRANCH ./screenshots

# add header
ruby ./ci/add_header_screenshot.rb

# add
git add ./screenshots

# commit
git commit -m "ci(screenshot): update screenshot"

# push to branch
git push origin gh-pages

# got back to original branch
git checkout $CIRCLE_BRANCH