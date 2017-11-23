# How to contribute

Welcome to our ever growing community! We are more than happy to accept external contributions to the project in the form of feedback, bug reports and even better, pull requests!

As a contributor, we present you the guidelines to start contributing in any of the Flyve MDM projects.

* [Questions or Doubts?](#questions-or-doubts)
* [Bugs](#found-a-bug)
* [Proposed Features](#new-feature)
* [Submitting issues and Pull Requests](#submitting)
* [Coding Rules](#coding-rules)

### Questions or Doubts

If you require general support assistance, you can find us in [Telegram](https://t.me/flyvemdm), and we'll help you as soon as possible.

For notices about major changes and general discussion of Flyve MDM development, subscribe to the [/r/FlyveMDM](https://www.reddit.com/r/FlyveMDM/) subreddit. You can also chat with us via IRC in [#flyve-mdm on freenode](http://webchat.freenode.net/?channels=flyve-mdm).

This is in order to keep GitHub issues for bug reports and new features only.

### Found a bug? 

You can let us know in our [issue Dashboard](#submit-an-issue).

Know how to fix it? Great! Then submit a [pull request](#submit-a-pull-request).

### New feature?

You can _**request**_ a new feature by submitting an issue, and if you would like to _**implement**_ a new feature, please submit an issue with a proposal for your work first, to be sure that we can use it, this will allow us to better coordinate our efforts, prevent duplication of work, and help you to craft the change so that it is successfully accepted into the project. Please consider what kind of change it is:

- For a Major Feature, first open an issue and outline your proposal so it can be discussed.
- Small Features can be crafted and directly submitted as a Pull Request.

## Submitting

### Submit an Issue

Before submitting the issue please check the [issue tracker](https://github.com/flyve-mdm/android-mdm-agent/issues), there exists the possibility that the bug was already reported by other contributor.

This way you help us to maximize the effort we can spend fixing issues and adding new features, by not reporting duplicate issues.

We'll work very hard to fix all the issues without delay, but before fixing it we need to confirm it, for that we require you to provide us of the following information:

- Overview of the Issue - if an error is being thrown a description of the problem is extremely helpful
- Motivation for or Use Case - explain why this is a bug for you
- Project Version(s)
- Device Operating System & Model - is this a problem with all devices or only specific ones?
- Reproduce the Error - provide a live example or an unambiguous set of steps.
- Related Issues - has a similar issue been reported before?
- Suggest a Fix - if you can't fix the bug yourself, perhaps you can point to what might be causing the problem (line of code or commit)

You can file new issues by filling out our [new issue form](https://github.com/flyve-mdm/android-mdm-agent/issues/new).

### Submit a Pull Request

Before submitting your Pull Request check  for an open or closed PR that relates to your submission. We don't want to duplicate efforts.

- Make your changes in a new branch, the project is organized according to the branch model [Git Flow](http://git-flow.readthedocs.io/en/latest/), though this is not mandatory it's really useful:

```console
    git checkout -b my-fix-branch develop
```

- Follow our [Coding Rules](#coding-rules).

- Commit your changes using a descriptive commit message that follows the [Conventional Commit](http://conventionalcommits.org/). This is **indispensable** since the release notes and changelogs are automatically generated from these messages.

- Push your branch to GitHub:

```console
    git push origin my-fix-branch
```

- In GitHub, send a pull request to our [Repository](https://github.com/flyve-mdm/android-mdm-agent).

- If we suggest changes then:

    - Make the required updates.

    - Rebase your branch and force push to your GitHub repository (this will update your Pull Request):

    **That's it! :tada:  Thank you for your contribution!**

#### After your pull request is merged

You can safely delete your branch and pull the changes from the main (upstream) repository:

- Delete the remote branch on GitHub either through the GitHub web UI or your local shell as you prefer.

## Coding Rules

To ensure consistency throughout the source code, keep these rules in mind as you are working:

- All features or bug fixes must be [tested](#test-and-build) by one or more specs (unit-tests).
- All methods must be documented.

## Test and Build

- Fork and clone the source code from our repo

- Build the app with Android Studio

- Select the AndroidTest folder right-click on the directory and select Run tests
