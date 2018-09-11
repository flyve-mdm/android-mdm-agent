#!/usr/bin/env bash
#
#  LICENSE
#
#  This file is part of Flyve MDM.
#
#  Admin Dashboard for Android is a subproject of Flyve MDM. Flyve MDM is a
#  mobile device management software.
#
#  Flyve MDM Admin Dashboard for Android is free software: you can redistribute 
#  it and/or modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3
#  of the License, or (at your option) any later version.
#
#  Flyve MDM Admin Dashboard for Android is distributed in the hope that it will 
#  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  ------------------------------------------------------------------------------
#  @author    Rafael Hernandez - <rhernandez@teclib.com>
#  @author    Naylin Medina    - <nmedina@teclib.com>
#  @copyright Copyright (c) Teclib'
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/android-mdm-dashboard/
#  @link      http://flyve.org/android-mdm-dashboard/
#  @link      https://flyve-mdm.com/
#  ------------------------------------------------------------------------------
#

GH_COMMIT_MESSAGE=$(git log --pretty=oneline -n 1 $CIRCLE_SHA1)

# validate commit message to avoid repeated builds and loops
if [[ $GH_COMMIT_MESSAGE != *"ci(release): generate CHANGELOG.md for version"* && $GH_COMMIT_MESSAGE != *"build(properties): add new properties values"* && $GH_COMMIT_MESSAGE != *"ci(release): update version on android manifest"* ]]; then

    # Get the version number from package.json
    export GIT_TAG=$(jq -r ".version" package.json)

    # get if is a release or a pre release with "-"
    IS_PRERELEASE="$( cut -d '-' -f 2 <<< "$GIT_TAG" )";

    if [[ $GIT_TAG != "$IS_PRERELEASE" ]]; then
        fastlane android "playstore"
    else
        fastlane android "beta"
    fi

fi