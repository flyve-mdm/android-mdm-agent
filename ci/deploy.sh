#!/bin/bash

GP_TRAVIS="true"

if [[ "$TRAVIS_BRANCH" == "feature-gplay" ]];
then
    echo travis=$GP_TRAVIS >> ../local.properties
    echo storePassword=$GP_STOREPASSWORD >> ../local.properties
    echo keyAlias=$GP_KEYALIAS >> ../local.properties
    echo keyPassword=$GP_KEYPASSWORD >> ../local.properties
    echo serviceAccountEmail=$GP_SERVICEACCOUNTEMAIL >> ../local.properties

    cd ..
    gradle publishApkRelease
fi