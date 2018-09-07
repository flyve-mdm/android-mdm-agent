#!/usr/bin/env bash

COMMIT_MESSAGE=$(git log --pretty=oneline -n 1 $CIRCLE_SHA1)

# only update list when documentation is updated
if [[ $COMMIT_MESSAGE == *"docs(development): update documentation"* && -z "$CIRCLE_PULL_REQUEST" ]]; then

# check if support folder exists
if [ -d "development/code-documentation/support" ]; then

# remove list to create a new one and not duplicate folders
rm ./_data/whitelist_version.yml

# create fresh list
touch ./_data/whitelist_version.yml

# set path to directory where the versions folders are
FOLDER_PATH="development/code-documentation/support"

# get folders in release directory
DIRS=`ls $FOLDER_PATH`

# add version folders to list
for DIR in $DIRS
do
echo  - ${DIR} >> ./_data/whitelist_version.yml
done
# if the list has changed commit and push changes
  if [ -n "$(git status --porcelain _data/whitelist_version.yml)" ]; then

    echo "Updating version list"

    # configure git
    git config --global user.email "apps@teclib.com"
    git config --global user.name "Teclib' bot"

    # add new remote to push changes
    git remote remove origin
    git remote add origin https://$GITHUB_USER:$GITHUB_TOKEN@github.com/$CIRCLE_PROJECT_USERNAME/$CIRCLE_PROJECT_REPONAME.git

    git add _data/whitelist_version.yml && git commit -m "ci(list): update version list"
    git push origin gh-pages
  fi
fi

fi