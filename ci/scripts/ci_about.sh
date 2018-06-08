#!/usr/bin/env bash

GIT_TAG=$(jq -r ".version" package.json)

echo "about.version=${GIT_TAG}" > app/src/main/assets/about.properties
echo "about.build=$CIRCLE_BUILD_NUM" >> app/src/main/assets/about.properties
echo "about.date=$(date "+%a %b %d %H:%M:%S %Y")" >> app/src/main/assets/about.properties
echo "about.commit=${CIRCLE_SHA1:0:7}" >> app/src/main/assets/about.properties
echo "about.commitFull=$CIRCLE_SHA1" >> app/src/main/assets/about.properties
echo "about.github=https://github.com/flyve-mdm/flyve-mdm-android-agent" >> app/src/main/assets/about.properties

if [[ $CIRCLE_BRANCH == *"master"* || $CIRCLE_BRANCH == *"develop"* ]]; then
    git add app/src/main/assets/about.properties
    git commit -m "build(properties): add new properties values"
fi