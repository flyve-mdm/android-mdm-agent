#!/usr/bin/env bash
set -e # halt script on error

# install libraries for nokogiri
sudo apt-get install build-essential ruby-dev

# install bundle
sudo gem install bundle

# install html link checker
go get -v -u github.com/raviqqe/muffet

# install dependencies
npm install
bundle install