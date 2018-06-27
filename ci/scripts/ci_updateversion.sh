#!/usr/bin/env bash
#
#  LICENSE
#
#  This file is part of Flyve MDM Agent for Android.
#
#  Flyve MDM Agent for Android is a subproject of Flyve MDM. Flyve MDM is a mobile
#  device management software.
#
#  Flyve MDM is free software: you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3
#  of the License, or (at your option) any later version.
#
#  Flyve MDM Agent for Android is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  --------------------------------------------------------------------------------
#  @author    Rafael Hernandez - <rhernandez@teclib.com>
#  @copyright Copyright (c) 2017 - 2018 Teclib'
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/android-mdm-agent/
#  @link      http://flyve.org/android-mdm-agent/
#  @link      https://flyve-mdm.com/
#  --------------------------------------------------------------------------------
#

# increment version code, need to be unique to send to store
./gradlew updateVersionCode -P vCode=$(($CIRCLE_BUILD_NUM))

# increment version name on package.json, create tag and commit with changelog
yarn run release -m "ci(release): generate CHANGELOG.md for version %s"

if [[ $CIRCLE_BRANCH == *"master"* ]]; then
    # send changelog to gh-pages
    yarn gh-pages --dist ./ --src CHANGELOG.md --dest ./_includes/ --add -m "docs(changelog): update changelog$1 with version ${GIT_TAG}"
    
    # Get version number from package.json
    export GIT_TAG=$(jq -r ".version" package.json)

    # update version name generate on package json
    ./gradlew updateVersionName -P vName=$GIT_TAG
fi

git add app/src/main/AndroidManifest.xml
git commit -m "ci(release): update version on android manifest"

# push commits only in master branch
if [[ $CIRCLE_BRANCH == *"master"* ]]; then
git push origin $CIRCLE_BRANCH
fi