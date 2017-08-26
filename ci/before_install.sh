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


#-----------------------------------------------------------------
# DEVELOP MASTER and NOT PULLREQUEST
#-----------------------------------------------------------------
if [[ ("$TRAVIS_BRANCH" == "develop" ||  "$TRAVIS_BRANCH" == "master") && "$TRAVIS_PULL_REQUEST" == "false" ]]; then
    # move to branch
    git checkout $TRAVIS_BRANCH -f

    # decrypt deploy on google play file
    openssl aes-256-cbc -K $encrypted_27dcfd0dda78_key -iv $encrypted_27dcfd0dda78_iv -in gplay.tar.gz.enc -out ci/gplay.tar.gz -d
fi
