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

# create enviroment vars to work with fastlane
echo TELEGRAM_WEBHOOKS=$TELEGRAM_WEBHOOKS > .env
echo GIT_REPO=$TRAVIS_REPO_SLUG >> .env
echo GIT_BRANCH=$TRAVIS_BRANCH >> .env

#-----------------------------------------------------------------
# DEVELOP DEPLOY
# - send to google play like beta
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" && "$TRAVIS_RUN" == "true" ]]; then

    # decrypt deploy on google play file
    openssl aes-256-cbc -K $encrypted_27dcfd0dda78_key -iv $encrypted_27dcfd0dda78_iv -in gplay.tar.gz.enc -out ci/gplay.tar.gz -d

    # uncompress cert file
    tar -zxvf ci/gplay.tar.gz -C ci/

    # sign and deploy to store with fastlane
    fastlane android beta storepass:'$KEYSTORE' keypass:'$ALIAS'

    # config git
    git config --global user.email $GH_EMAIL
    git config --global user.name "Flyve MDM"
    git remote remove origin
    git remote add origin https://$GH_USER:$GH_TOKEN@github.com/flyve-mdm/flyve-mdm-android-agent.git

    git add -u
    git commit -m "ci(build): release **beta** for version $GIT_TAG-beta"

    git push origin $TRAVIS_BRANCH
fi

#-----------------------------------------------------------------
# MASTER DEPLOY
# - send to google play like release
# - create a changelog
# - create a javadoc
# - send javadoc to gh-pages branch
# - send CHANGELOG.md to gh-pages branch
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "master" && "$TRAVIS_PULL_REQUEST" == "false" && "$TRAVIS_RUN" == "true" ]]; then

    # decrypt deploy on google play file
    openssl aes-256-cbc -K $encrypted_27dcfd0dda78_key -iv $encrypted_27dcfd0dda78_iv -in gplay.tar.gz.enc -out ci/gplay.tar.gz -d

    # uncompress cert file
    tar -zxvf ci/gplay.tar.gz -C ci/

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
    git config --list

    # add modified and delete files
    git add -u

    # create commit
    git commit -m "ci(build): release **version** ${GIT_TAG}"

    # push to branch
    git push origin $TRAVIS_BRANCH

    #------------------------ GH-PAGES --------------------------

    # Generate javadoc this folder must be on .gitignore
    javadoc -d ./javadoc -sourcepath ./app/src/main/java -subpackages .

    # get
    git fetch origin gh-pages

    # move to gh-pages
    git checkout gh-pages

    # add javadoc folder
    git add javadoc

    # create commit
    git commit -m "docs(javadoc): update javadoc with version ${GIT_TAG}"

    # clean unstage file on gh-pages
    git clean -fdx

    # get changelog from branch
    git checkout $TRAVIS_BRANCH CHANGELOG.md

    # Create header content
    HEADER="---"$'\r'"layout: modal"$'\r'"title: changelog"$'\r'"---"$'\r\r'

    # Duplicate CHANGELOG.md
    cp CHANGELOG.md CHANGELOG_COPY.md

    # Add header to CHANGELOG.md
    (echo $HEADER ; cat CHANGELOG_COPY.md) > CHANGELOG.md

    # Remove CHANGELOG_COPY.md
    rm CHANGELOG_COPY.md

    # add
    git add CHANGELOG.md

    # create commit
    git commit -m "docs(changelog): update changelog with version ${GIT_TAG}"

    # push to branch
    git push origin gh-pages --force
fi