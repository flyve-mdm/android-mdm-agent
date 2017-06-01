# flyve-mdm-android
Flyve MDM Agent for Android

This version work with Deep Link the format of the link is: flyve://register?data={{base64encode JSON}}

JSON Format:
{"url":"https://demo.flyve.org/api","user_token":"value","invitation_token":"value"}

Features:
- If not register just show and Splash
- Enroll with deep link
- Working on background services
- Simple register workflow with user and email
- Get and reply ping from API
- Show message on Screen if it open

Know Issues:
- Don't validate data from API