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

# # create code coverage report
# ./gradlew createDebugCoverageReport
#
# # move code coverage
# mv -v app/build/reports/coverage development
#
# # move Android test
# mv -v app/build/reports/androidTests development
#
# # rename folders to match respective section on project site
# mv development/debug development/coverage
# mv development/androidTests/connected development/test-reports
#
# # replace .resources with resource because github doesn't support folders with "_" or "." at the beginning
# mv development/coverage/.resources development/coverage/resources
#
# # find and replace links to the old name of file
# grep -rl .resources development/coverage/ | xargs sed -i 's|.resources|resources|g'
#
# # replace .sessions
# mv development/coverage/.sessions.html development/coverage/sessions.html
#
# # find and replace links to the old name of file
# grep -rl .sessions.html development/coverage/ | xargs sed -i 's|.sessions.html|sessions.html|g'

# Generate javadoc this folder must be on .gitignore
javadoc -d ./development/code-documentation -sourcepath ./app/src/main/java -subpackages . -bootclasspath $ANDROID_HOME/platforms/android-28/android.jar

# delete the index.html file
sudo rm ./development/code-documentation/index.html

# rename the overview-summary.html file to index.html
mv ./development/code-documentation/overview-summary.html ./development/code-documentation/index.html

# find and replace links to the old name of file
grep -rl overview-summary.html development/code-documentation/ | xargs sed -i 's|overview-summary.html|index.html|g'

# send development folder to project site with the documentation updated, also removes the folder with old docs
yarn gh-pages --dist ./development/ --dest ./development/ -m "docs(development): update documentation

update coverage and test reports
update code documentation"