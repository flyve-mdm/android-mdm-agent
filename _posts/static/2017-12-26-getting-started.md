---
layout: post
howtos: true
published: true
title: Getting Started
permalink: howtos/getting-started
description: Welcome to Flyve MDM
---
Flyve MDM is a Mobile device management software that enables you to secure and manage all the mobile devices of your business or family.

The Android Agent works in conjuction with the [Web MDM Dashboard](http://flyve.org/web-mdm-dashboard/) and [Flyve MDM plugin](http://flyve.org/glpi-plugin/) for GLPI.

## 1. User invitation

In order to enroll the devices, they must be invited firts, either from the Web Dashboard or Flyve MDM plugin for GLPI, once the user receives the invitation link in his email account and opens it:

* If the MDM Agent is installed in the device, it will Open the link with the App.

<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/open-with.png" alt="Open with MDM Agent" width="300">

<br>

* If the MDM Agent isn't installed, it will take the user to the [PlayStore](https://play.google.com/store/apps/details?id=org.flyve.mdm.agent) to download it.

## 2. Enrollment

The Agent counts with an intuitive and simple User Interface through all the enrollment process, just fill the blanks and everything will be set.

<br>

<div>
<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/start-enrollment.png" alt="Start Enrollment" width="300">

<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/enrollment.png" alt="Enrollment" width="300">
</div>

## 3. Manage your fleet

From there on the Agent will implement in the device the commands given from the Dashboard to:

* Configure and deploy your fleet
* Control Connectivity Access
* Implement Security Features
* Get Mobile Fleet Inventory
* Applications management

### MQ Telemetry Transport

We implemented the MQTT protocol, which is useful for connections with remote locations since it was designed as an extremely lightweight message transport. It is also ideal for mobile applications because of its small size, low power usage, minimised data packets and efficient distribution of information to one or many receivers. Thanks to it, the Agent is capable of maintaining a connection with the backend.

<br>

<div>
<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/mqtt-info.png" alt="MQTT Log" width="300">

<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/mqtt-config.png" alt="MQTT Configuration" width="300">
</div>

### Information

The Agent will display the relevant information for the user, it will show the Supervisor information and edit his own.

<br>

<div>
<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/information.png" alt="Information" width="300">
    
<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/supervisor-information.png" alt="Supervisor Information" width="300">

<img src="https://raw.githubusercontent.com/Naylin15/Screenshots/8a9c071d160f7a2cec5e9604dea8289662e6f176/Android-Agent/user-information.png" alt="User Information" width="300">
</div>

You can join the community in GitHub and participate to contribute, test and correct bugs: [Flyve MDM on GitHub](https://github.com/flyve-mdm)!