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

Thanks to the MQTT, a Machine to Machine protocol, the Agent is capable of maintaining a connection with the backend, which gives you remote control over your fleet.

The Agent counts with an intuitive and simple User Interface that helps you through the enrollment process.

<br>

<div>
<img src="{{ 'images/screenshots/start-enrollment.png' | absolute_url }}" alt="Start Enrollment" width="300">

<img src="{{ 'images/screenshots/enrollment.png' | absolute_url }}" alt="Enrollment" width="300">
</div>

It will also provide you information of the user and supervisor of the infrastructure.

<br>

<div>
<img src="{{ 'images/screenshots/information.png' | absolute_url }}" alt="Information" width="300">

<img src="{{ 'images/screenshots/user-information.png' | absolute_url }}" alt="User Information" width="300">

<img src="{{ 'images/screenshots/supervisor-information.png' | absolute_url }}" alt="Supervisor Information" width="300">
</div>

The Flyve MDM Agent requires Android 4.1 or higher
