#!/usr/bin/env bash

GH_COMMIT_MESSAGE=$(git log --pretty=oneline -n 1 $CIRCLE_SHA1)

if [[ $GH_COMMIT_MESSAGE == *"ci(release): generate CHANGELOG.md for version"* || $GH_COMMIT_MESSAGE == *"build(properties): add new properties values"* || $GH_COMMIT_MESSAGE == *"ci(release): update version on android manifest"* ]]; then
    echo "Running duplicated"
    exit 1
fi