---
layout: howtos
published: true
title: How it works
permalink: howtos/how-it-works
description: A brief introduction
category: user
---

The Android Agent works in conjuction with the [Web MDM Dashboard](http://flyve.org/web-mdm-dashboard/) and [Flyve MDM plugin](http://flyve.org/glpi-plugin/) for GLPI.

The Agent will take control of the Android devices in your IT Infrastructure while from the Web Dashboard or GLPI plugin the Administrator will be able to manage all the terminals, once these are enrolled.

### Invitation

To enroll the devices, the owner of the device must be invited by the Administrator, the invitation contains a deeplink or QR code.

The deeplink or QR code contains some base64 encoded semicolon separated fields like a CSV format:

* the URL of the backend for REST API requests
* a user API token belonging to a human user account
* an invitation token
* the name of the company's helpdesk
* the phone number of the company's helpdesk
* the website of the company's helpdesk
* the email of the company's helpdesk

All fields related to the helpdesk may not be populated by the administrators. The fields are ordered.

Once the user receives the email with the invitation and opens the link with the MDM Agent, the user will be able to provide the information requested.

#### Deeplink display

* Email account

When the user opens the deeplink from his email account, in the respective device to enroll, the email will display the deeplink with the information of the invitation encoded. Also the QR code image is attached.

* QR code

The deeplink can also be opened in a computer's browser, by clicking on the deeplink, in this case part of the information will be decoded and display the Helpdesk information and the QR code, so the user can scan it from the MDM Agent.

* Mobile device browser

In case the invitation is opened with the browser of the device, it will display a button that contains the deeplink so the MDM Agent can recognize it and start the enrollment.

### Communication

#### MQ Telemetry Transport (MQTT)

For Android 4.1 Jelly Bean to Android 7 Nougat, we implemented [MQTT](http://mqtt.org/), a Machine to Machine protocol, useful for connections with remote locations due to its design as an extremely lightweight message transport. Ideal for mobile applications due to its small size, low power usage and efficient distribution of information to one or many receivers.

Thanks to it, the Agent is capable of maintaining a connection with the backend.

#### Firebase Cloud Messaging

Following the upgrades of Android 8 regarding the background services, we implemented the [Firebase Cloud Messaging](https://firebase.google.com/products/cloud-messaging/) as substitute of the MQTT protocol whilst keeping a secure communication between the backend and the devices.

Therefore, currently the Agents with Android 8 or later will work with FCM.

### Inventory

The Agent at the moment of the enrollment, creates an inventory of the device to send it to the GLPI instance, in order to keep a record of the device's hardware and software.

To learn what is sent in the inventory, check the [Android Inventory Library project](http://flyve.org/android-inventory-library/).

### Organization information

It will also provide you information of the user and supervisor of the infrastructure.

<br>

<div>
<img src="{{ 'images/screenshots/user-information.png' | absolute_url }}" alt="User Information" width="300">

<img src="{{ 'images/screenshots/supervisor-information.png' | absolute_url }}" alt="Supervisor Information" width="300">
</div>

The Flyve MDM Agent requires Android 4.1 or higher
