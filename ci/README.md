# Continuous Integration script and files

Here is placed files and bash script required to build, test and deploy the app.

## Files description

- release.keystore is the key to certify the app to deploy, required by fastlane
- gplay.json.enc is the file with Google Play configuration, required by fastlane

## Workflow description

### On feature branch

- run Build
- run Test Api v16

### On develop

There are two main scripts which include the commit validation to avoid repeated workflows, and contain the scripts related to their process.

- Setup environment (ci_setup.sh)
- Deploy Beta (ci_deploy_beta.sh)
  - Update version and code (ci_updateversion.sh)
  - Create about information (ci_about.sh)
  - Transifex for translations (ci_transifex.sh)
  - Create APK
  - Deploy to Google Play Beta version (ci_fastlane.sh)
- Documentation (ci_documentation.sh)
  - Create Coverage, Test reports and Code Documentation (ci_generate_documentation.sh)
  - Update headers and styles for proper display on project site
- Update screenshots (ci_screenshots.sh)

### On master

There is one main script that includes the commit validation to avoid repeated workflows, and contains the scripts related to the process.

- Setup environment (ci_setup.sh)
- Deploy Production (ci_deploy_production.sh)
  - Update version and code, generate Changelog (ci_updateversion.sh)
  - Create about information (ci_about.sh)
  - Transifex for translations (ci_transifex.sh)
  - Create APK
  - Push new tags and update develop branch (ci_push_changes.sh)
  - Deploy to Google Play (ci_fastlane.sh)
  - Create a GitHub release (ci_github_release.sh)

## Environment variables

On this project we use the following variables:

- BUILD_TOOL -> Used to build the application, set to 26.0.0

- ci_fastlane
  - $KEYSTORE -> Key store for apk signing
  - $ALIAS    -> The alias of the certificate to sign the apk
- ci_github_release
  - $GITHUB_TOKEN -> GitHub Token
- ci_setup
  - $TELEGRAM_WEBHOOKS -> Used to send notifications to Telegram
  - $THESTRALBOT_URL   -> URL to Thestralbot
  - $ENCRYPTED_KEY     -> Used to desencrypt the key to sign the APK
  - $GITHUB_EMAIL      -> GitHub Email
  - $GITHUB_USER       -> GitHub User
  - $GITHUB_TOKEN      -> GitHub Token
- ci_transifex
  - $TRANSIFEX_USER      -> User of Transifex
  - $TRANSIFEX_API_TOKEN -> API Token of Transifex

## Libraries

We use the following:

- [conventional-github-releaser](https://github.com/conventional-changelog/releaser-tools)
- [gh-pages](https://github.com/tschaub/gh-pages)
- [node-github-releaser](https://github.com/miyajan/node-github-release)
- [standard-version](https://github.com/conventional-changelog/standard-version)