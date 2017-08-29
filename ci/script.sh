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

# Deletes the build directory.
gradle clean

if [[ ("$TRAVIS_BRANCH" == "develop" ||  "$TRAVIS_BRANCH" == "master") && "$TRAVIS_PULL_REQUEST" == "false" && "$TRAVIS_RUN" == "true" ]]; then
    # Runs all device checks on currently connected devices.
    gradle build connectedCheck
else
    if [[ "$TRAVIS_RUN" == "true" ]]; then
        # Assembles all variants of all applications and secondary packages.
        gradle assemble
    fi
fi