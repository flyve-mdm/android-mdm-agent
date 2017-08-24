#!/bin/bash

#Deletes the build directory.
gradle clean

if [[ ("$TRAVIS_BRANCH" == "develop" ||  "$TRAVIS_BRANCH" == "master") && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # Runs all device checks on currently connected devices.
    gradle build connectedCheck
else
    # Assembles all variants of all applications and secondary packages.
    gradle assemble
fi