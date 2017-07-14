#!/bin/bash

if [[ "$TRAVIS_BRANCH" == "feature/travis" ]];
then
    fastlane android alpha storepass:'#KEYSTORE' keypass:'#ALIAS'
fi