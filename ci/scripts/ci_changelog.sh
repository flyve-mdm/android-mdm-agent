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

# get gh-pages branch
git fetch origin gh-pages

# move to gh-pages
git checkout -f gh-pages

# clean unstage file on gh-pages to remove all others files gotten on checkout
sudo git clean -fdx

# remove old CHANGELOG.md on gh-pages
rm _includes/CHANGELOG.md

# get changelog from branch
git checkout $CIRCLE_BRANCH CHANGELOG.md

# move changelog to _includes folder
sudo mv CHANGELOG.md _includes/CHANGELOG.md

# add
git add _includes/ && git add CHANGELOG.md

# create commit
git commit -m "docs(changelog): update changelog$1 with version ${GIT_TAG}"

# push to branch
git push origin gh-pages

# got back to original branch
git checkout $CIRCLE_BRANCH