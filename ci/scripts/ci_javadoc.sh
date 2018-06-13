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

# Generate code-documentation directory, this folder must be on .gitignore
javadoc -d ./development/code-documentation -sourcepath ./app/src/main/java -subpackages . -bootclasspath $ANDROID_HOME/platforms/android-26/android.jar

# delete the index.html file
sudo rm ./development/code-documentation/index.html

# rename the overview-summary.html file to index.html
mv ./development/code-documentation/overview-summary.html ./development/code-documentation/index.html

# add development folder
git add development -f

# create commit with temporary development folder
git commit -m "tmp development commit"

# get gh-pages branch
git fetch origin gh-pages

# move to gh-pages
git checkout gh-pages

# delete old code-documentation folder
sudo rm -R development/code-documentation

# get code-documentation folder
git checkout $CIRCLE_BRANCH development/code-documentation

# remove default stylesheet.css
sudo rm ./development/code-documentation/stylesheet.css

# add new css
cp ./css/codeDocumentation.css ./development/code-documentation/stylesheet.css

# git add code-documentation folder
git add development/code-documentation

# git add
git add ./development/code-documentation/stylesheet.css

# create commit for documentation
git commit -m "docs(development): update code documentation"

# change headers
ruby ci/add_header.rb

# git add
git add .

# git commit
git commit -m "docs(headers): update headers"

# push to branch
git push origin gh-pages

# got back to original branch
git checkout $CIRCLE_BRANCH