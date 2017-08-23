#!/bin/bash

echo $TRAVIS_BRANCH

#if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]];
#then
    # uncompress cert file
    cd ci
    tar -zxvf gplay.tar.gz
    cd ..

    # sign and deploy to store with fastlane
    fastlane android beta storepass:'$KEYSTORE' keypass:'$ALIAS'

    # create a new release on git
    #git config --global user.email $GH_EMAIL
    #git config --global user.name "Flyve MDM"

    #create a git tag
    #export GIT_TAG=$(jq -r ".version" package.json)
    #git tag $GIT_TAG -a -m "build(tag): Generated tag from TravisCI for build $TRAVIS_BUILD_NUMBER"

    #send tag to git hub
    git push -q https://$GH_USER:${GH_TOKEN}@github.com/flyve-mdm/flyve-mdm-android-agent.git --tags
    #conventional-github-releaser -t $GH_TOKEN -r 0
#fi
