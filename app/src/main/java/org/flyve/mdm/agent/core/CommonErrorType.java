package org.flyve.mdm.agent.core;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @date      21/8/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class CommonErrorType {

    // DEEPLINK
    public static final int DEEPLINK_GENERAL_EXCEPTION = 100;
    public static final int DEEPLINK_BASE64DECODE = 101;
    public static final int DEEPLINK_URL_EMPTY = 102;
    public static final int DEEPLINK_USER_TOKEN = 103;
    public static final int DEEPLINK_INVITATION_TOKEN = 104;
    public static final int DEEPLINK_CSV_WRONG_FORMAT = 105;
    public static final int DEEPLINK_ENROLLMENT_FAIL = 107;
    public static final int DEEPLINK_GETQUERYPARAMETER = 108;

    // ENROLLMENT
    public static final int ENROLLMENT_X509CERTIFICATION = 201;
    public static final int ENROLLMENT_FIELD_VALIDATION = 202;
    public static final int ENROLLMENT_REQUEST = 203;
    public static final int ENROLLMENT_REQUEST_EXCEPTION = 204;
    public static final int ENROLLMENT_HELPER_INITSESSION = 205;
    public static final int ENROLLMENT_HELPER_FULLSESSION = 206;
    public static final int ENROLLMENT_HELPER_CHANGEACTIVEPROFILE = 207;
    public static final int ENROLLMENT_HELPER_GETACTIVESESSIONTOKEN = 208;
    public static final int ENROLLMENT_HELPER_INPUT_PAYLOAD = 209;
    public static final int ENROLLMENT_HELPER_REQUEST_PAYLOAD = 210;
    public static final int ENROLLMENT_HELPER_AGENT_ID = 211;
    public static final int ENROLLMENT_HELPER_DATA_AGENT = 212;
    public static final int ENROLLMENT_HELPER_X509CERTIFICATION = 213;

    // PERMISSION
    public static final int PERMISSION_XML_INVENTORY = 301;
    public static final int PERMISSION_ACTIVE_SESSION = 302;
    public static final int PERMISSION_ONREQUESTPERMISSIONSRESULT = 303;

    // USER
    public static final int USER_SAVE_VALIDATION = 401;
    public static final int USER_SAVE_EXCEPTION = 401;

    // MQTT
    public static final int MQTT_INVENTORY_FAIL = 501;
    public static final int MQTT_IN_INITIALIZER_ERROR = 502;
    public static final int MQTT_CONNECTION_LOST = 503;
    public static final int MQTT_OPTIONS = 504;
    public static final int MQTT_CONNECTION = 505;
    public static final int MQTT_ACTION_CALLBACK = 506;
    public static final int MQTT_PING = 507;
    public static final int MQTT_GEOLOCATE = 508;
    public static final int MQTT_INVENTORY = 509;
    public static final int MQTT_LOCK = 510;
    public static final int MQTT_WIPE = 511;
    public static final int MQTT_UNENROLL = 512;
    public static final int MQTT_SUBSCRIBE = 513;
    public static final int MQTT_RESETPASSWORD = 514;
    public static final int MQTT_USETLS = 515;
    public static final int MQTT_DEPLOYAPP = 516;
    public static final int MQTT_REMOVEAPP = 517;
    public static final int MQTT_DEPLOYFILE = 518;
    public static final int MQTT_REMOVEFILE = 519;
    public static final int MQTT_CALLPOLICY_NEWINSTANCE = 520;
    public static final int MQTT_CALLPOLICY_JSON_PARSE = 521;
    public static final int MQTT_DESTROY_START_SERVICE = 522;
    public static final int MQTT_DELIVERY_COMPLETE = 523;
}
