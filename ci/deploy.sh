#!/bin/bash

echo $TRAVIS_BRANCH
if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]];
then
    gradle increaseVersionCode
    gradle incrementVersionName

    cd ci
    tar -zxvf google.tar.gz
    cd ..
    fastlane android beta storepass:'$KEYSTORE' keypass:'$ALIAS'
fi