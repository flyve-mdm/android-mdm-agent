#
#  Copyright (C) 2017 Teclib'
#
#  This file is part of Flyve MDM Android.
#
#  Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
#  device management software.
#
#  Flyve MDM Android is free software: you can redistribute it and/or
#  modify it under the terms of the GNU General Public License
#  as published by the Free Software Foundation; either version 3
#  of the License, or (at your option) any later version.
#
#  Flyve MDM Android is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#  ------------------------------------------------------------------------------
#  @author    Rafael Hernandez - rafaelje
#  @copyright Copyright (c) 2017 Flyve MDM
#  @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
#  @link      https://github.com/flyve-mdm/flyve-mdm-android
#  @link      http://www.glpi-project.org/
#  @link      https://flyve-mdm.com/
#  ------------------------------------------------------------------------------
#

# Add header to all files on the folder reports/javadoc
Dir.glob("reports/debug/**/*.html") do |search_file| # note one extra "*"
    file = File.open("#{search_file}", "r+")
    buffer = file.read
    file.rewind
    file.puts "---"
    file.puts "layout: coverage"
    file.puts "---"
    file.print buffer
    file.close

    # rename folder resources
    data = File.read("#{search_file}")
    filtered_data = data.gsub(".resources", "resources")
    File.open("#{search_file}", "w") {|file| file.puts filtered_data }

    # rename .session.html
    data = File.read("#{search_file}")
    filtered_data = data.gsub(".session", "session")
    File.open("#{search_file}", "w") {|file| file.puts filtered_data }

end

# Add header to all files on the folder androidTests
Dir.glob("reports/androidTests/**/*.html") do |search_file| # note one extra "*"
    file = File.open("#{search_file}", "r+")
    buffer = file.read
    file.rewind
    file.puts "---"
    file.puts "layout: coverage"
    file.puts "---"
    file.print buffer
    file.close
end

# Add header to all files on the folder reports/javadoc
Dir.glob("reports/javadoc/**/*.html") do |search_file| # note one extra "*"
    file = File.open("#{search_file}", "r+")
    buffer = file.read
    file.rewind
    file.puts "---"
    file.puts "layout: codeDocumentation"
    file.puts "---"
    file.print buffer
    file.close
end

# add CHANGELOG header
    file = File.open("CHANGELOG.md", "r+")
    buffer = file.read
    file.rewind
    file.puts "---"
    file.puts "layout: modal"
    file.puts "title: changelog"
    file.puts "---"
    file.print buffer
    file.close