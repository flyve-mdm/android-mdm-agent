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

# create code coverage report
./gradlew createDebugCoverageReport

# move code coverage
sudo mv -v app/build/reports/coverage development

# move Android test
sudo mv -v app/build/reports/androidTests development

# rename folders to match respective section
sudo mv development/debug development/coverage
sudo mv development/androidTests development/test-reports

# replace .resources with resource because github don't support folders with "_" or "." at the beginning
mv development/coverage/.resources development/coverage/resources

index=$(<development/coverage/index.html)
newindex="${index//.resources/resources}"
echo $newindex > development/coverage/index.html

# replace .sessions
mv development/coverage/.sessions.html development/coverage/sessions.html

# add code coverage and test result
git add development -f

# temporal commit
git commit -m "tmp development"

# get gh-pages branch
git fetch origin gh-pages

# clean
rm app/src/main/assets/setup.properties

# move to gh-pages
git checkout gh-pages

# clean
sudo git clean -fdx

## remove old development folder
sudo rm -R development

# get development documentation folder
git checkout $CIRCLE_BRANCH development

# remove css
sudo rm ./development/coverage/resources/report.css
sudo rm ./development/test-reports/connected/css/base-style.css
sudo rm ./development/test-reports/connected/css/style.css

# add new css
cp ./css/coverage.css ./development/coverage/resources/report.css
cp ./css/androidTests.css ./development/test-reports/connected/css/style.css
touch ./development/test-reports/connected/css/base-style.css

# add
git add ./development/coverage/resources/report.css
git add ./development/test-reports/connected/css/style.css
git add ./development/test-reports/connected/css/base-style.css

# create commit
git commit -m "docs(coverage): update code coverage and test result"

# push to branch
git push origin gh-pages

# got back to original branch
git checkout $CIRCLE_BRANCH