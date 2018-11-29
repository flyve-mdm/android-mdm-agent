---
layout: howtos
published: true
title: Getting Started
permalink: howtos/getting-started
description: Welcome to Flyve MDM
category: user
date: 2017-12-20
---

Flyve MDM is a Mobile device management software that enables you to secure and manage all the mobile devices of your business or family.

The Android MDM Agent works in conjunction with the [Web MDM Dashboard](http://flyve.org/web-mdm-dashboard/) and [Flyve MDM plugin](http://flyve.org/glpi-plugin/) for GLPI. The Agent will take control of the Android devices applying the commands given through the Dashboard or plugin for GLPI.

Here you'll learn everything you need to start using your brand new MDM Agent.

* [Enrollment](#1)
* [Learn what is happening in Activity](#2)
* [How to know if the policies are working with Feedback](#3)
* [Configuration](#4)
* [For the Administrators only, Easter Egg](#5)

## <a name="1"></a> 1. Enrollment Process

### 1.1. Invitation

In order to enroll the devices, it is required to invite the user of the device. This can be done either from the Web MDM Dashboard or Flyve MDM plugin for GLPI.

Once the user receives the email with the invitation, he will be able to start the enrollment by opening the deeplink or scanning the QR code.

* If the MDM Agent is [installed in the device](http://flyve.org/android-mdm-agent/howtos/installation), it will ask to Open with the App.

<img src="{{ 'images/screenshots/open-with.png' | absolute_url }}" alt="Open with MDM Agent" width="300">

<br>

* If the MDM Agent isn't installed, it will take the user to the [PlayStore](https://play.google.com/store/apps/details?id=org.flyve.mdm.agent) to download it.

### 1.2. Permissions

Once the enrollment starts, the Agent will ask you to Allow the permissions it requires to work properly.

<br>

<div>
<img src="{{ 'images/screenshots/start-enrollment.png' | absolute_url }}" alt="Start Enrollment" width="300">

<img src="{{ 'images/screenshots/permission.gif' | absolute_url }}" alt="Permission request" width="300">
</div>

### 1.3. Inventory

The Agent must send an inventory of the device, you will be able to view and share it before continuing with the enrollment.

<img src="{{ 'images/screenshots/inventory.gif' | absolute_url }}" alt="Inventory" width="300">

### 1.4. User information

Add your information to finish the enrollment.

<img src="{{ '/images/picto-information.png' | absolute_url }}" alt="Good to know:" height="16px"> The email must be the same to which the invitation was sent.

<img src="{{ 'images/screenshots/enrollment.png' | absolute_url }}" alt="Fill the form" width="300">

## Success!

Your device is enrolled! You can now see its online status and navigate through the menu.

<div>
<img src="{{ 'images/screenshots/information.png' | absolute_url }}" alt="Information" width="300">

<img src="{{ 'images/screenshots/menu.png' | absolute_url }}" alt="Menu" width="300">
</div>

## <a name="2"></a> 2. Activity

In the Activity section you'll be able to see the different actions taken place in the MDM Agent:

### 2.1 Log

Here you'll be able to see the MQTT Messages.

<img src="{{ 'images/screenshots/activity-log.png' | absolute_url }}" alt="Log" width="300">

#### MQTT (MQ Telemetry Transport)

The MQTT is a Machine to Machine protocol we implemented, is useful for connections with remote locations due to its design as an extremely lightweight message transport. Ideal for mobile applications due to its small size, low power usage and efficient distribution of information to one or many receivers.

Thanks to it, the Agent is capable of maintaining a connection with the backend.

### 2.2 Connectivity & Policies

Here you will be able to see which policies are assigned and the value it has.

<div>
<img src="{{ 'images/screenshots/activity-connectivity.png' | absolute_url }}" alt="Connectivity" width="300">

<img src="{{ 'images/screenshots/activity-policies.png' | absolute_url }}" alt="Policies" width="300">
</div>

For more information, check our [Policies & API Level](http://flyve.org/android-mdm-agent/howtos/policies).

### 2.3 Applications

All the applications deployed will be listed here.

<img src="{{ 'images/screenshots/activity-applications.png' | absolute_url }}" alt="Applications" width="300">

## <a name="3"></a> 3. Feedback

You can send a feedback about which policies are working perfectly on the device, additionally with a message.

<img src="{{ 'images/screenshots/feedback.gif' | absolute_url }}" alt="Applications" width="300">

## <a name="4"></a> 4. Configuration

Disable the notifications and the log data.

<img src="{{ 'images/screenshots/configuration.png' | absolute_url }}" alt="Applications" width="300">

## <a name="5"></a> 5. Easter Egg

To access special features touch ten times the Flyve MDM in the Information section.

In MQTT Configuration you will be able to edit the parameters, we really hope you know what you are doing here since any change may cause the lost of connection with the backend.

<img src="{{ 'images/screenshots/easteregg.gif' | absolute_url }}" alt="Easter Egg" width="300">

You can join the community in GitHub and participate to contribute, test and correct bugs: [Flyve MDM on GitHub](https://github.com/flyve-mdm)!