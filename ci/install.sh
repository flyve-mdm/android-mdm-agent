#!/bin/bash

#-----------------------------------------------------------------
# DEVELOP MASTER and NOT PULLREQUEST
#-----------------------------------------------------------------
if [[ ("$TRAVIS_BRANCH" == "develop" ||  "$TRAVIS_BRANCH" == "master") && "$TRAVIS_PULL_REQUEST" == "false" ]]; then

    # install ruby 2.3.4 to execute gems
    rvm install 2.3.4

    # install fastlane
    gem install fastlane

    # install node_js
    rm -rf ~/.nvm && git clone https://github.com/creationix/nvm.git ~/.nvm && (cd ~/.nvm && git checkout `git describe --abbrev=0 --tags`) && source ~/.nvm/nvm.sh && nvm install $TRAVIS_NODE_VERSION

    # install node package available on package.json
    npm install

    # install globally
    npm install -g conventional-github-releaser
fi