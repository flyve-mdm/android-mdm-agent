---
layout: howtos
published: true
title: Join our Community!
permalink: howtos/contribute
description: Be part of our awesome team
category: contributing
---

Welcome to our ever growing community!

 We are more than happy to accept external contributions to the project in the form of feedback, [translations](http://flyve.org/android-mdm-agent/howtos/contribute-translating), bug reports and even better, pull requests!

Here you'll learn everything you must know to start contributing in any of the Flyve MDM projects.

Starting from the basics we include Awesome tips: <img src="{{ '/images/picto-information.png' | absolute_url }}" alt="Awesome tip:" height="16px"> and Watch outs: <img src="{{ '/images/picto-warning.png' | absolute_url }}" alt="Watch out:" height="16px"> to get the most of it.

## Downloads & Accounts

You'll require to install and open new accounts in the following:

* [Telegram account](https://telegram.org/)
* [GitHub account](https://github.com/)
* [Keybase Account](https://keybase.io/)
* [Git](https://git-scm.com/downloads)
* [Visual Studio Code](https://code.visualstudio.com/#alt-downloads)
* [GPG command line tools](https://www.gnupg.org/download/)

## Set things up

### Git

After installing git, run:

```terminal

git config --global user.name "First.Name Last.Name"
git config --global user.email "my.email@email.com"

```

<img src="{{ '/images/picto-information.png' | absolute_url }}" alt="Awesome tip:" height="16px"> Git provides the [Pro Git book](https://git-scm.com/book/en/v2), available in several languages, and [Try Git](https://try.github.io/levels/1/challenges/1), a 15 min tutorial, both very helpful if you're starting with this SCM.

### GitHub account

For security reasons, we demand our members to have enabled the following Authentication measures. The instructions to configure each one are available from the GitHub Help Documentation, this assures us that any change added to any of our repositories comes from an authorized member of our team.

* #### Two Factor Authentication

This is the easiest step, you only need to follow the GitHub Documentation, [Securing your account with 2FA](https://help.github.com/articles/securing-your-account-with-two-factor-authentication-2fa/), there are several methods to do this, for example by [SMS Text](https://help.github.com/articles/configuring-two-factor-authentication-via-text-message/) or a [TOTP app](https://help.github.com/articles/configuring-two-factor-authentication-via-a-totp-mobile-app/).

##### Test it!

Sign out then sign in again, GitHub will ask you the Authentication code.

* #### SSH

Follow the GitHub guide, [Connecting to GitHub with SSH](https://help.github.com/articles/connecting-to-github-with-ssh/), there is everything you need to know to successfully add your SSH key.

##### Test it!

1. [Create a Test Repository on GitHub](https://help.github.com/articles/create-a-repo/)

2. Create a ```test``` directory on your work environment

3. From the terminal go to your ```test``` directory and run:

   ```git clone git@github.com:MY-USER-NAME/MY-REPO-NAME.git```

4. Run ```ls -a``` to list all directories in test/

If there is a new folder with your repo name, then the setup of SSH was successful!

* #### GPG signing

This configuration requires some patience.

1. Go to your Keybase account and generate a new GPG key.
2. Sign in from a browser to your Keybase Account.
3. Click on the ID of your PGP key.
4. Copy and paste the command to import your public GPG key on the terminal: ```curl https://keybase.io/MY_USER_NAME/pgp_keys.asc | gpg --import```
5. Copy everything between:

    ```key

    -----BEGIN PGP PUBLIC KEY BLOCK-----
    -----END PGP PUBLIC KEY BLOCK-----

    ```

6. Paste it on your GitHub settings
   Go to settings > SSH and GPG keys > New GPG key
7. Import your private key to your PC:

      7.1. Go to your keybase account on your browser

      7.2. Next to your key ID, click on edit and select export private

      <img src="https://github.com/Naylin15/Screenshots/blob/master/docs/Export-private-key.png?raw=true" alt="Export private key on Keybase">

      7.3. Copy and paste your private key in a txt editor, and save it with the name ```private.key```

      <img src="{{ '/images/picto-information.png' | absolute_url }}" alt="Awesome tips:" height="16px"> On Windows make sure it is on your user folder.

      7.4. Go to command line and run:

      ```gpg --import private.key```

      7.5 Check the key was imported by running:

      ```gpg --list-secret-keys --keyid-format LONG```

   Here should be listed your key, check the ID from keybase is the same on the sec line.

     <div>
       <img src="https://github.com/Naylin15/Screenshots/blob/master/docs/check-key-id-terminal.png?raw=true" alt="Key ID on Terminal">
       <img src="https://github.com/Naylin15/Screenshots/blob/master/docs/check-key-id.png?raw=true" alt="Key ID on Keybase">
     </div>

8. Telling git of your GPG key, run:

```git config --global user.signingkey B344E73DA95715F4```

Also run the following command to sign all commits by default:

```git config --global commit.gpgsign true```

* On Windows also run:

  ```git config --global gpg.program "C:\Program Files (x86)\GnuPG\bin\gpg.exe"```

### Test it!

1. With your Visual Studio Code open the folder of your cloned repo
2. Open the Readme and add a new line to it, for example: ```Hello World```
3. Save changes and open the terminal on VS Code:
    * On Windows use ```ctrl + ñ```
    * On OSX use ```ctrl + ` ```
4. Run:```git add . && git commit -s -m "my first commit" && git push```
5. Now go to your repo and click on commits, you should see the Verified label.

![Verified commit](https://github.com/Naylin15/Screenshots/blob/master/docs/verified.png?raw=true)

* Git explanation:
  * ```git add .``` -> Adds the changes to be committed
  * ```git commit -s -m "message"``` -> commits the changes, -s is for signing the commit and -m "message", the message describing the changes
  * ```git push``` -> pushes the local changes to your remote repo (the repo on GitHub)

<img src="{{ '/images/picto-information.png' | absolute_url }}" alt="Awesome tips:" height="16px"> Don't forget you can learn more about these git commands with the [Pro Git book](https://git-scm.com/book/en/v2), available in several languages, and [Try Git](https://try.github.io/levels/1/challenges/1) tutorial.

<img src="{{ '/images/picto-warning.png' | absolute_url }}" alt="Watch out:" height="16px"> Make sure that the email address in git, the Primary email in your GitHub account and the one in your GPG key are all the same.

In case you want to add an email account to your GPG, follow this guide [Associating an email account with your GPG key](https://help.github.com/articles/associating-an-email-with-your-gpg-key/)

After adding your email account, remember to update your GPG on keybase, to do that, follow these steps:

1. Sign in from a browser to your Keybase Account.
2. Next to your key ID, click on edit and select _Update my key (I edited it elsewhere)_.
3. Run again ```gpg –-armor –-export B344E73DA95715F4```
4. Copy the output and paste it where indicated in keybase.

## Now that everything is set up

Read our [Contributing guidelines](https://github.com/flyve-mdm/android-mdm-agent/blob/develop/CONTRIBUTING.md), you'll learn every step to contribute, from making an issue to closing them, using the [Git Flow](http://git-flow.readthedocs.io/en/latest/), [Conventional Commits](http://conventionalcommits.org/) and the tools we implement.

### If you need Assistance

You can find us in any of our channels, we'll help you as soon as possible:

* For Questions & Doubts:
  * [Flyve MDM group](https://t.me/flyvemdm) on Telegram.
  * [#flyve-mdm on freenode](http://webchat.freenode.net/?channels=flyve-mdm) via IRC chat.
* Technical questions:
  * [StackOverflow](http://stackoverflow.com/)
* General Discussion:
  * [Flyve MDM mailing list](http://mail.ow2.org/wws/info/flyve-mdm-dev)
* Customers Assistance:
  * [Support channel](https://support.teclib.com/)

Happy Coding!