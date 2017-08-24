#!/bin/bash

#   Copyright © 2017 Teclib. All rights reserved.
#
#   This file is part of flyve-mdm-android-agent
#
# flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
# device management software.
#
# Flyve MDM is free software: you can redistribute it and/or
# modify it under the terms of the GNU General Public License
# as published by the Free Software Foundation; either version 3
# of the License, or (at your option) any later version.
#
# Flyve MDM is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
# ------------------------------------------------------------------------------
# @author    Rafael Hernandez - rafaelje
# @date      24/9/17
# @copyright Copyright © 2017 Teclib. All rights reserved.
# @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
# @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
# @link      https://flyve-mdm.com
# ------------------------------------------------------------------------------


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

        git checkout $TRAVIS_BRANCH -f

        # config git
        git config --global user.email $GH_EMAIL
        git config --global user.name "Flyve MDM"
        git remote remove origin
        git remote add origin https://$GH_USER:$GH_TOKEN@github.com/flyve-mdm/flyve-mdm-android-agent.git

        git add -A
        git commit -m "ci(build): increment **version code**"

        git push origin $TRAVIS_BRANCH
fi

#-----------------------------------------------------------------
# MASTER DEPLOY
# - send to google play like release
# - create a changelog
# - add changes to repository
# - commit and push
# - send CHANGELOG.md to gh-pages branch
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "master" && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # this conditional is to prevent loop
    if [[ $TRAVIS_COMMIT_MESSAGE != *"**version**"* && $TRAVIS_COMMIT_MESSAGE != *"**CHANGELOG.md**"* ]]; then
        # uncompress cert file
        cd ci
        tar -zxvf gplay.tar.gz
        cd ..

        # sign and deploy to store with fastlane
        fastlane android playstore storepass:'$KEYSTORE' keypass:'$ALIAS'

        # push tag to github
        conventional-github-releaser -t $GH_TOKEN -r 0

        # config git
        git config --global user.email $GH_EMAIL
        git config --global user.name "Flyve MDM"
        git remote remove origin
        git remote add origin https://$GH_USER:$GH_TOKEN@github.com/flyve-mdm/flyve-mdm-android-agent.git

        #------------------------ UPDATE CHANGES --------------------------

        # move to branch
        git checkout $TRAVIS_BRANCH -f

        # add all new files
        git add -A

        # create commit
        git commit -m "ci(build): increment **version** ${GIT_TAG}"

        # push to branch
        git push origin $TRAVIS_BRANCH

        #------------------------ GH-PAGES --------------------------

        # move to gh-pages
        git checkout gh-pages

        # get changelog from branch
        git checkout $TRAVIS_BRANCH CHANGELOG.md

        # remove all other files
        git clean -fdx

        # create commit
        git commit -m "docs(changelog): update changelog with version ${GIT_TAG}"

        # push to branch
        git push origin gh-pages
    fi
fi
