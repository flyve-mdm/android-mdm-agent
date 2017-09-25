#!/usr/bin/env bash
set -e # halt script on error

bundle exec jekyll build
rm -rf _site/reports
bundle exec htmlproofer ./_site --allow-hash-href true --assume-extension true --file-ignore ./_site/CHANGELOG.html