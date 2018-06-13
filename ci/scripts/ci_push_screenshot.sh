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
#  @author    Rafael Hernandez - <rafaelje@teclib.com>
#  @copyright Copyright (c) 2017 - 2018 Teclib'
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/android-mdm-agent/
#  @link      http://flyve.org/android-mdm-agent/
#  @link      https://flyve-mdm.com/
#  --------------------------------------------------------------------------------
#

# Push commits and tags to origin branch
sudo mv ./fastlane/metadata/android ./screenshots
sudo mv ./screenshots/screenshots.html ./screenshots/index.html

# add
git add .

# temporal commit
git commit -m "ci(tmp): temporal commit"

# fetch
git fetch origin gh-pages

# move to branch
git checkout gh-pages

# clean workspace
sudo git clean -fdx

# git get screenshots
git checkout $CIRCLE_BRANCH ./screenshots

# add header
ruby ./ci/add_header_screenshot.rb

# add
git add ./screenshots

# commit
git commit -m "ci(screenshot): update screenshot"

# push to branch
git push origin gh-pages

# got back to original branch
git checkout $CIRCLE_BRANCH