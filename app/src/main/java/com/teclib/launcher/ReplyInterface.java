/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Mathieu BREBAN
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.launcher;


public interface ReplyInterface {
        /** Response flags */
        int sFLAG_SUCCESS = 0x0000;
        int sFLAG_ERR_JSON = 0x0001;
        int sFLAG_ERR_APP = 0x0002;
        int sFLAG_ERR_LAUNCH = 0x0004;
        int sFLAG_ERR_CONFIG = 0x0008;
        int sFLAG_ERR_JSON_CODE = 0x0010;
        int sFLAG_ERR_JSON_DATA = 0x0020;

        /** Response actions */
        String sACTION_SENDJSON = "com.teclib.launcher.SEND_JSON";
        String sACTION_STARTJSON = "com.teclib.launcher.START_JSON";
        String sACTION_START = "com.teclib.launcher.START";
        String sACTION_REPLY = "com.teclib.launcher.REPLY";

        /**
         *
         * @param code
         * @param flag
         */
        void broadcastResponse(String code, int flag);
}
