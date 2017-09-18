#!/usr/bin/env bash
#
#  Copyright (C) 2017 Teclib'
#
#  This file is part of Flyve MDM Inventory Agent Android.
#
#  Flyve MDM Inventory Agent Android is a subproject of Flyve MDM. Flyve MDM is a mobile
#  device management software.
#
#  Flyve MDM Android is free software: you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3
#  of the License, or (at your option) any later version.
#
#  Flyve MDM Inventory Agent Android is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  ------------------------------------------------------------------------------
#  @author    Rafael Hernandez - rafaelje
#  @copyright Copyright (c) 2017 Flyve MDM
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/flyve-mdm-android-inventory-agent/
#  @link      http://www.glpi-project.org/
#  @link      https://flyve-mdm.com/
#  ------------------------------------------------------------------------------
#
# increment version code, need to be unique to send to store
# this factor is used if you need increase you version code to deploy on Google Play by default is 0
export BUILD_INCREMENT_FACTOR=600
./gradlew updateVersionCode -P vCode=$(($CIRCLE_BUILD_NUM + $BUILD_INCREMENT_FACTOR))

# increment version on package.json, create tag and commit with changelog
npm run release -- -m "ci(release): generate CHANGELOG.md for version %s"

# Get version number from package.json
export GIT_TAG=$(jq -r ".version" package.json)

# update version name generate on package json
./gradlew updateVersionName -P vName=$GIT_TAG

# git add app/src/main/AndroidManifest.xml
#
# git commit -m "ci(release): update version ($GIT_TAG) and code number ($CIRCLE_BUILD_NUM)"
#
# git push origin $CIRCLE_BRANCH