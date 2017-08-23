#!/bin/bash

echo TELEGRAM_WEBHOOKS=$TELEGRAM_WEBHOOKS > .env
echo GIT_REPO=$TRAVIS_REPO_SLUG >> .env
echo GIT_BRANCH=$TRAVIS_BRANCH >> .env

echo $TRAVIS_BRANCH
#if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]];
#then
    npm run release
    gradle increaseVersionCode
    gradle incrementVersionName
#fi