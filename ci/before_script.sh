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

echo $TRAVIS_BRANCH
#-----------------------------------------------------------------
# DEVELOP DEPLOY
# - increse version code
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "develop" && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # increment version code, need to be unique to send to store
    gradle increaseVersionCode
fi

#-----------------------------------------------------------------
# MASTER DEPLOY
# - increse version code
# - run release to increment version name, create a tag and commit this this tag
# - increment version name on manifest
#-----------------------------------------------------------------
if [[ "$TRAVIS_BRANCH" == "master" && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # increment version code, need to be unique to send to store
    gradle increaseVersionCode

    # increment version on package.json, create tag and commit with changelog
    npm run release -- -m "ci(release): generate **CHANGELOG.md** for version %s"
    # increment version name generate on package json
    gradle incrementVersionName
fi