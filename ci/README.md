# Continuous Integration script and files

Here is placed files and bash script required to build, test and deploy the app.

## Files description

- release.keystore is the key to certify the app to deploy, required by fastlane
- gplay.json.enc is the file with Google Play configuration, required by fastlane

## Workflow description

### On feature branch

- run Build

### On develop

- Setup environment (ci_setup.sh)
- Update version and code (ci_updateversion.sh)
- Transifex for translations (ci_transifex.sh)
- Deploy to Google Play Beta version (ci_fastlane.sh)
- Create Coverage and Test reports (ci_coverage)
- Create Code Documentation (ci_javadoc)
- Validate Workflow (ci_validate_workflow.sh)
- Update screenshots (ci_screenshots.sh)

### On master

- Update version and code (ci_updateversion.sh)
- Deploy to Google Play (ci_fastlane.sh)
- Create a GitHub release (ci_github_release.sh)
- Create changelog (ci_changelog.sh)

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