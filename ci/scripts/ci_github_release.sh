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
#  @author    Naylin Medina    - <nmedina@teclib.com>
#  @copyright Copyright (c) Teclib'
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/android-mdm-agent/
#  @link      http://flyve.org/android-mdm-agent/
#  @link      https://flyve-mdm.com/
#  --------------------------------------------------------------------------------
#

# push tag to github
yarn conventional-github-releaser -t $GITHUB_TOKEN 2> /dev/null || true

# get tag number
GIT_TAG=$(jq -r ".version" package.json)

# get apk path
FILE=$(find ./app/build/outputs/apk/release -name '*.apk' | head -1)

# Update release name
yarn github-release edit \
--user $CIRCLE_PROJECT_USERNAME \
--repo $CIRCLE_PROJECT_REPONAME \
--tag ${GIT_TAG} \
--name "MDM Agent v${GIT_TAG}" \

# Upload example code release
yarn github-release upload \
--user $CIRCLE_PROJECT_USERNAME \
--repo $CIRCLE_PROJECT_REPONAME \
--tag ${GIT_TAG} \
--name "MDMAgent-${GIT_TAG}.apk" \
--file ${FILE}