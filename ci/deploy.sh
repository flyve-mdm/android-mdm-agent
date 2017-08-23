#!/bin/bash

echo $TRAVIS_BRANCH

#-----------------------------------------------------------------
# DEVELOP DEPLOY
# - send to google play like beta
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
        # uncompress cert file
        cd ci
        tar -zxvf gplay.tar.gz
        cd ..

        # sign and deploy to store with fastlane
        fastlane android beta storepass:'$KEYSTORE' keypass:'$ALIAS'
fi

#-----------------------------------------------------------------
# MASTER DEPLOY
# - send to google play like release
# - create a changelog
# - add changes to repository
# - commit and push
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "master" && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # this conditional is to prevent loop
    if [[ $TRAVIS_COMMIT_MESSAGE != *"**version**"* && $TRAVIS_COMMIT_MESSAGE != *"**CHANGELOG.md**"* ]]; then
        # uncompress cert file
        cd ci
        tar -zxvf gplay.tar.gz
        cd ..

        # sign and deploy to store with fastlane
        fastlane android beta storepass:'$KEYSTORE' keypass:'$ALIAS'

        # push tag to github
        conventional-github-releaser -t $GH_TOKEN -r 0

        git checkout $TRAVIS_BRANCH -f

        # config git
        git config --global user.email $GH_EMAIL
        git config --global user.name "Flyve MDM"
        git remote remove origin
        git remote add origin https://$GH_USER:$GH_TOKEN@github.com/flyve-mdm/flyve-mdm-android-agent.git

        git add -A
        git commit -m "ci(build): increment **version** ${GIT_TAG}"

        git push origin $TRAVIS_BRANCH
    fi
fi
