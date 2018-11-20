/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @author    Dorian LARGET
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.utils;

import android.os.Environment;

import com.orhanobut.logger.Logger;

import org.flyve.mdm.agent.data.database.MDMLogData;
import org.flyve.mdm.agent.ui.MDMAgent;

/**
 * This is a Log wrapper
 */
public class FlyveLog {

    private static final String FILE_NAME_FEEDBACK = "FlyveMDMFeedback.txt";
    public static final String FILE_NAME_LOG = "FlyveMDMLog.txt";
    public static final String FLYVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/FlyveMDM/";

    /**
     * private constructor to prevent instances of this class
     */
    private FlyveLog() {
    }

    /**
     * Send a DEBUG log message
     * @param object Object to write
     */
    public static void d(Object object) {
        if(MDMAgent.getIsDebuggable()){
            Logger.d(object);
        }
    }

    /**
     * Send a DEBUG log message
     * @param message String message to log
     * @param args Objects
     */
    public static void d(String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null){
            // do something for a debug build
            Logger.d(message,args);
        }
    }

    /**
     * Send a VERBOSE log message
     * @param message String message
     * @param args Objects
     */
    public static void v(String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null){
            Logger.v(message, args);
        }
    }

    /**
     * Send INFORMATION log message
     * @param message String message
     * @param args Objects
     */
    public static void i(String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null) {
            Logger.i(message, args);
        }
    }

    /**
     * Send ERROR log message
     * @param throwable Throwable error
     * @param message String message
     * @param args Objects
     */
    public static void e(String where, Throwable throwable, String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null) {
            Logger.e(throwable, message, args);
            f("ERROR", where, message);
        }
    }

    /**
     * Send Error log message
     * @param message String message
     * @param args Objects
     */
    public static void e(String where, String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null) {
            Logger.e(message, args);
            f("Error", where, message);
        }
    }

    /**
     * send What a Terrible Failure log message
     * @param message String message
     * @param args Objects
     */
    public static void wtf(String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null) {
            Logger.wtf(message, args);
        }
    }

    /**
     * Send a JSON log message
     * @param json String the json to show
     */
    public static void json(String json) {
        if(MDMAgent.getIsDebuggable() && json != null) {
            Logger.json(json);
        }
    }

    /**
     * Send a XML log message
     * @param xml String the xml to show
     */
    public static void xml(String xml) {
        if(MDMAgent.getIsDebuggable() && xml != null) {
            Logger.xml(xml);
        }
    }

    /**
     * Logs the message in a directory
     * @param message
     */
    public static void f(String type, String title, String message) {
        type = (type == null) ? "" : type;
        title = (title == null) ? "" : title;
        message = (message == null) ? "" : message;

        String msg = Helpers.broadCastMessage(type, title,  message);
        MDMLogData log = new MDMLogData(MDMAgent.getInstance());
        log.addLog(msg);
    }


}
