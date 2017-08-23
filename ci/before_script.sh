#!/bin/bash

echo TELEGRAM_WEBHOOKS=$TELEGRAM_WEBHOOKS > .env
echo GIT_REPO=$TRAVIS_REPO_SLUG >> .env
echo GIT_BRANCH=$TRAVIS_BRANCH >> .env

echo $TRAVIS_BRANCH
#if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]];
#then
    # increment version code, need to be unique to send to store
    gradle increaseVersionCode

    # increment version on package.json, create tag and commit with changelog
    npm run release -- -m "ci(release): generate **CHANGELOG.md** for version %s"
    # increment version name generate on package json
    gradle incrementVersionName



#fi