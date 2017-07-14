#!/bin/bash

echo $TRAVIS_BRANCH
if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]];
then
    fastlane android alpha storepass:'$KEYSTORE' keypass:'$ALIAS'
fi