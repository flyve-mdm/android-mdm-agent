#!/bin/bash

#-----------------------------------------------------------------
# DEVELOP MASTER and NOT PULLREQUEST
#-----------------------------------------------------------------
if [[ ("$TRAVIS_BRANCH" == "develop" ||  "$TRAVIS_BRANCH" == "master") && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # decrypt deploy on google play file
    openssl aes-256-cbc -K $encrypted_27dcfd0dda78_key -iv $encrypted_27dcfd0dda78_iv -in gplay.tar.gz.enc -out ci/gplay.tar.gz -d
fi
