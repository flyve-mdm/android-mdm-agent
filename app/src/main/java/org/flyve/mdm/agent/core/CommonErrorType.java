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

    // PERMISSION
    public static final int PERMISSION_XML_INVENTORY = 301;
    public static final int PERMISSION_ACTIVE_SESSION = 302;
    public static final int PERMISSION_ONREQUESTPERMISSIONSRESULT = 303;

}
