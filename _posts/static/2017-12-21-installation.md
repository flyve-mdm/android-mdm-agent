---
layout: howtos
published: true
title: Installation
permalink: howtos/installation
description: Install it as System App!
category: user
---

Flyve MDM Agent for Android can be installed from Google Play or as System App.

* [Compatibility Matrix](#matrix)
* [Download APK](#download)
* [Beta version](#beta-version)
* [Install as System App](#system-app)

## <a name="matrix"></a> Compatibility Matrix

### Flyve MDM Projects

Check the following compatibility matrix to make sure, you're getting the right version.

<table class="zebra-style">
    <tr>
        <td>MDM Agent</td>
        <td>0.99.x</td>
        <td>1.0.0</td>
        <td>2.0.0</td>
    </tr>
    <tr>
        <td>GLPI</td>
        <td>9.1</td>
        <td>9.2</td>
        <td>9.2+</td>
    </tr>
    <tr>
        <td>Flyve MDM plugin</td>
        <td align="center">-</td>
        <td>2.0.0</td>
        <td>2.0.0</td>
    </tr>
    <tr>
        <td>Web MDM Dashboard</td>
        <td align="center">-</td>
        <td>2.0.0</td>
        <td>2.0.0</td>
    </tr>
</table>

### MDM Agent & Android versions

<table class="zebra-style">
    <tr>
        <td>API</td>
        <td>Android</td>
        <td>Support</td>
        <td>SSL</td>
    </tr>
    <tr>
        <td align="center">16</td>
        <td align="center">Android 4.1</td>
        <td align="center">X</td>
        <td align="center">-</td>
    </tr>
    <tr>
        <td align="center">17</td>
        <td align="center">Android 4.2</td>
        <td align="center">X</td>
        <td align="center">-</td>
    </tr>
    <tr>
        <td align="center">18</td>
        <td align="center">Android 4.3</td>
        <td align="center">X</td>
        <td align="center">-</td>
    </tr>
    <tr>
        <td align="center">19</td>
        <td align="center">Android 4.4</td>
        <td align="center">X</td>
        <td align="center">-</td>
    </tr>
    <tr>
        <td align="center">20</td>
        <td align="center">Android 4.4</td>
        <td align="center">X</td>
        <td align="center">-</td>
    </tr>
    <tr>
        <td align="center">21</td>
        <td align="center">Android 5.0</td>
        <td align="center">X</td>
        <td align="center">X</td>
    </tr>
    <tr>
        <td align="center">22</td>
        <td align="center">Android 5.1</td>
        <td align="center">X</td>
        <td align="center">X</td>
    </tr>
    <tr>
        <td align="center">23</td>
        <td align="center">Android 6.0</td>
        <td align="center">X</td>
        <td align="center">X</td>
    </tr>
    <tr>
        <td align="center">24</td>
        <td align="center">Android 7.0</td>
        <td align="center">X</td>
        <td align="center">X</td>
    </tr>
    <tr>
        <td align="center">25</td>
        <td align="center">Android 7.1.1</td>
        <td align="center">X</td>
        <td align="center">X</td>
    </tr>
    <tr>
        <td align="center">26</td>
        <td align="center">Android 8.0</td>
        <td align="center">-</td>
        <td align="center">-</td>
    </tr>
</table>

## Download

You can install the application by downloading it from the **Play Store**

<a href="https://play.google.com/store/apps/details?id=org.flyve.mdm.agent" target="_blank"><img src="https://user-images.githubusercontent.com/663460/26973322-4ddf78a4-4d16-11e7-8b58-4c03b4bc2490.png" width="300" alt="Download from the Play Store"></a>

<!--Or from **F-Droid**

<a href="" target="_blank"><img src="https://camo.githubusercontent.com/f9574a79e3fe61202392c44e55f0bdab261a9561/68747470733a2f2f662d64726f69642e6f72672f62616467652f6765742d69742d6f6e2e706e67" width="300" alt="Download from the F-Droid"></a>-->

Or get the APK from the **Release** page on Github

<a href="https://github.com/flyve-mdm/android-mdm-agent/releases" target="_blank"><img src="https://user-images.githubusercontent.com/663460/26973090-f8fdc986-4d14-11e7-995a-e7c5e79ed925.png" width="300" alt="Download from the Release page on Github"></a>

## Beta version

Download the [Beta testing app from Google Play](https://play.google.com/apps/testing/org.flyve.mdm.agent)

## <a name="system-app"></a>As System App with Android Debug Bridge

This simple guide asumes that you have some basic knowledge about command line, Android and ADB tool if not please feel free to review the ADB official information: [Android Debug Bridge](https://developer.android.com/studio/command-line/adb.html?hl=es-419).

<img src="{{ '/images/picto-information.png' | absolute_url }}" alt="Good to know: " height="16px">  If the App is installed as System App it will be able to apply policies without requiring the user consent, like deploying apps and files directly to the device.

#### Requirement:

You will require root access to do this.

### Step 1:

Connect your device to the computer.

### Step 2:

Run these commands:

```shell
$adb shell
$su
$mount -o rw,remount /system
```

### Step 3:

You have two ways to move your apk to the system folder:

#### Option 1

Copy the apk directly to the System folder.

##### For Android 4.3 or newest

```shell
$adb push yourAPKFile.apk /system/priv-app
```

##### Older Android version

```shell
$adb push yourAPKFile.apk /system/app
```

#### Option 2

If you can't copy directly to the folder or has the apk in external sdcard, move the apk to the system folder.

##### For Android 4.3 or newest

```shell
$mv /storage/sdcard1/yourAPKFile.apk /system/priv-app
```

##### Older Android version

```shell
$mv /storage/sdcard1/yourAPKFile.apk /system/app
```

### Step 4:

Add permission to the APK.

```shell
$chmod 644 yourAPKFile.apk
```

### Step 5:

Reboot the device.

```shell
$adb reboot
```