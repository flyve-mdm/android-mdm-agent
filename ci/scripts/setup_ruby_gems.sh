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
#  @author    Alexander Salas Bastidas - <asalas@teclib.com>
#  @copyright Copyright (c) Teclib' 2019
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/android-mdm-agent/
#  @link      http://flyve.org/android-mdm-agent/
#  @link      https://flyve-mdm.com/
#  --------------------------------------------------------------------------------
#

# Maintenance commands
sudo apt-get update

# install gems
sudo apt-get install ruby-full build-essential

# update Rubygems
sudo gem update --system --no-document

# install fastlane
sudo gem install fastlane --no-document

# update bundler
sudo gem install bundler --no-document

# update Gemfile.lock
sudo bundler update --bundler

# install gem bundler local
gem install bundler

# check bundle
bundle check || bundle install --path vendor/bundle
