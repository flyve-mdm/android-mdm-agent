# Change Log

All notable changes to this project will be documented in this file. See [standard-version](https://github.com/conventional-changelog/standard-version) for commit guidelines.

<a name="0.99.46"></a>
## [0.99.46](https://github.com/flyve-mdm/flyve-mdm-android/compare/0.99.45...0.99.46) (2018-01-02)

### Bug Fixes

* add device admin configuration ([223d09c](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/223d09c))
* **drawer:** remove unused vars ([3543bf9](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/3543bf9))
* Linting, refactoring and CI ([#59](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/59)) ([4959d82](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/4959d82))
* **agent:** remove log screen ([e8a080d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/e8a080d))
* **data:** remove test data class ([37a80ee](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/37a80ee))
* **design:** remove ic_warning file from drawable ([3e74031](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/3e74031))
* **drawer:** remove dummy text ([283b755](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/283b755))
* **enroll:** permission on enrollment process ([#91](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/91)) ([90f0724](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/90f0724))
* **enroll:** remove get picture from cache on enrollment ([ffdd708](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ffdd708))
* **enroll:** store information with correct field ([a71ab8e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/a71ab8e))
* **history:** close history back ([d2b3c1d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d2b3c1d))
* **mqtt:** check if intent is null to prevent error on close ([6f53c3f](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6f53c3f))
* **mqtt:** check if password is null before unrollment ([0b0537a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/0b0537a))
* **mqtt:** clear array topic on reconnection ([dc5140b](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/dc5140b))
* **mqtt:** fail connection ([87c78c5](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/87c78c5))
* **mqtt:** fix mqtt service running on background ([f9df540](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/f9df540))
* **mqtt:** prevent action null value ([c4928a2](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/c4928a2))
* **MQTT:** change time to reconnect to every 10 minutes if is down ([aa00e70](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/aa00e70))
* **navigation:** close splash screen on enrollment with deep link ([b6fdc42](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/b6fdc42))
* **photo:** get path image from Uri ([8d8429a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/8d8429a))
* **photo:** update capture image on edit user ([d0b1cf4](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d0b1cf4))
* **service:** change name of agent service ([a357387](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/a357387))
* **splash:** add single task ([4a4f658](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/4a4f658))
* **user:** add default image if is empty ([7113d85](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7113d85))
* **user:** add id on title user screen ([d407617](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d407617))
* **user:** create just fields for phone available on cache ([93625ed](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/93625ed))
* **user:** remove close activity ([b37693f](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/b37693f))
* **user:** save multiples email without repeat ([ca3e39c](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ca3e39c))
* **user:** save phone2 field with correct data ([ba996b3](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ba996b3))
* **user:** set edittext email disable ([e76e438](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/e76e438))


### Chores

* **package:** working on new version ([a88bb8c](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/a88bb8c))


### Features

* **admin:** add device admin integration ([#63](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/63)) ([93ca0c8](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/93ca0c8))
* **agent:** add easter egg jobs ([c182683](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/c182683))
* **agent:** Add status to the agent screen ([abe473d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/abe473d))
* **agent:** add toast to easter egg ([85aac9a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/85aac9a))
* **agent:** multiples improves  ([#85](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/85)) ([784eb68](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/784eb68))
* **deeplink:** read new fields name, phone, website and email on CSV ([625bec7](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/625bec7))
* **disclosure:** add disclosure permission screen ([683a65b](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/683a65b))
* **drawer:** add drawer navigation ([c09cfae](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/c09cfae))
* **drawer:** add feedback item on menu without action ([9896156](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/9896156))
* **drawer:** add header menu ([6e9c907](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6e9c907))
* **drawer:** add log screen as easter egg ([0e8066b](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/0e8066b))
* **drawer:** add separator line ([5f91162](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/5f91162))
* **easteregg:** reload list drawer on activate easteregg ([4d28181](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/4d28181))
* **enroll:** add deeplink and background service ([#62](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/62)) ([d0d42f6](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d0d42f6))
* **enroll:** add snackbar error on enroll ([#113](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/113)) ([7e6736d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7e6736d))
* **enroll:** add wizard for the enrollment ([#80](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/80)) ([b959c9a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/b959c9a))
* **enrollment:** add deeplink enrolmment workflow ([d83038d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d83038d))
* **fastlane:** Add slack to fastlane to get information about the deploy script ([6bf1faa](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6bf1faa))
* **geolocation:** add geolocations ([#67](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/67)) ([7dbdddd](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7dbdddd))
* **help:** add help item to menu ([0293f4d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/0293f4d))
* **help:** add help screen with flyve-mdm web ([db28ae9](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/db28ae9))
* **help:** add viewpager to create a slide help on splash ([fc1297c](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/fc1297c))
* **helpdesk:** add main page ([#90](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/90)) ([f08db95](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/f08db95))
* **inventory:** add receiver and broadcast ([c4ae0b8](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/c4ae0b8))
* **log:** add reverse read file for log ([6e44836](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6e44836))
* **manifest:** get data from MQTT service ([ae9edbb](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ae9edbb))
* **manifest:** store manifest version on local storage ([7e7f87d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7e7f87d))
* **mqtt:** if unenroll open splash screen ([c9c12d2](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/c9c12d2))
* **mqtt:** send inventory when add, remove or update some app ([e1010a0](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/e1010a0))
* **mqtt:** send start enrollment ([92367aa](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/92367aa))
* **MQTT:** reconnect the service when go offline ([1de18f2](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/1de18f2))
* **orientation:** screen orientation management ([3bcb210](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/3bcb210))
* **peripheral:** device access and connectivity ([#68](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/68)) ([bd2bfd7](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/bd2bfd7))
* **permission:** request permission ([e12243c](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/e12243c))
* **photo:** add function to convert bitmap to string and vice-versa ([336a839](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/336a839))
* **photo:** add function to select and store photo ([d3e788f](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d3e788f))
* **photo:** add photo selection on enrollment ([db242e3](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/db242e3))
* **photo:** add rounded image class ([8517011](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/8517011))
* **photo:** adjust image size on user form ([ac862a6](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ac862a6))
* **photo:** get photo from api level 16 to up on Edit user form ([a70f47e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/a70f47e))
* **photo:** get photo from api level 16 to up on enrollment ([ff31ab5](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ff31ab5))
* **photo:** hide keyboard when open the camera selection ([7dc83b1](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7dc83b1))
* **photo:** improve photo orientation ([517a7b2](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/517a7b2))
* **photo:** load rounded image on information ([488b979](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/488b979))
* **photo:** reload the user data on information when this is updated ([38e738e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/38e738e))
* **photo:** store picture on local storage ([18f52cf](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/18f52cf))
* **policies:** add files and apps ([#79](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/79)) ([7dccd8e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7dccd8e))
* **policies:** add password policies ([#70](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/70)) ([04c270a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/04c270a))
* **rc:** add sonar qube and automation scripts ([865e2ab](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/865e2ab))
* **status:** add will to MQTT sending status false ([cccf288](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/cccf288))
* **status:** send status true to MQTT on Success ([348df1d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/348df1d))
* **store:** add easter egg var on local store ([6369790](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6369790))
* **store:** add supervisor information on data storage ([6076048](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6076048))
* **supervisor:** add storage cache on supervisor form ([ec594b5](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ec594b5))
* **supervisor:** add supervisor edit screen ([ec9ddb6](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ec9ddb6))
* **supervisor:** storage supervisor information ([a3de853](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/a3de853))
* **user:** add administrative number ([3665a36](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/3665a36))
* **user:** add dynamics email fields ([9d19038](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/9d19038))
* **user:** add edit user class ([ca25886](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ca25886))
* **user:** add language spinner ([08ab4a0](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/08ab4a0))
* **user:** add multiples type ([fc0c3f1](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/fc0c3f1))
* **user:** add new email on key press ([c2cca11](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/c2cca11))
* **user:** add preview user screen ([43f0cec](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/43f0cec))
* **user:** add spinner to multiples list ([f59acc7](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/f59acc7))
* **user:** add supervisor controller, model and storage ([73563de](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/73563de))
* **user:** add user data model class ([7d364f8](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/7d364f8))
* **user:** add values on multiples edit text ([93feca1](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/93feca1))
* **user:** create user local storage ([ef99df6](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ef99df6))
* **user:** load data storage on user form ([d9e392a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d9e392a))
* **user:** load emails storage on local cache ([094639e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/094639e))
* **user:** load phones storage on local cache ([ecf41fa](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ecf41fa))
* **user:** load user emails on preview screen ([5a1f22b](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/5a1f22b))
* **user:** load user information on preview screen ([4a1b51e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/4a1b51e))
* **user:** remove items ([8fc55c9](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/8fc55c9))
* **user:** select language on spinner from local storage ([ea08e93](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/ea08e93))
* **user:** storage data on cache ([67d3d38](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/67d3d38))
* **user:** store emails, phone , language and administrative number ([6a1d6cd](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/6a1d6cd))
* **walkthrough:** add 3 step to walkthrough ([d82c91d](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/d82c91d))
* **walkthrough:** add dot steps on walkthrough screen ([011413c](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/011413c))
* **walkthrough:** add link format to message ([67eb1c6](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/67eb1c6))
* **walkthrough:** add links to walkthrough screen ([bce59ea](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/bce59ea))
* **walkthrought:** add design skelethon of slides ([46f5d6e](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/46f5d6e))


### Performance Improvements

* **IntentService:** replace IntentService instead Service to improved performance ([#44](https://github.com/flyve-mdm/flyve-mdm-android-agent/issues/44)) ([9d1a69a](https://github.com/flyve-mdm/flyve-mdm-android-agent/commit/9d1a69a))


### BREAKING CHANGES

* **package:** new enrollment workflow

<a name="0.99.45"></a>
## 0.99.45 (2017-06-01)


### Features

* **pre-release:** relase for early adopter stage ([e705e82](https://github.com/flyve-mdm/flyve-mdm-android/commit/e705e82))
