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

package org.flyve.policies.utils;

import com.orhanobut.logger.Logger;

/**
 * This is a Log wrapper
 */
public class FlyveLog {

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
        Logger.d(object);
    }

    /**
     * Send a DEBUG log message
     * @param message String message to log
     * @param args Objects
     */
    public static void d(String message, Object... args) {
        // do something for a debug build
        Logger.d(message,args);
    }

    /**
     * Send a VERBOSE log message
     * @param message String message
     * @param args Objects
     */
    public static void v(String message, Object... args) {
        Logger.v(message, args);
    }

    /**
     * Send INFORMATION log message
     * @param message String message
     * @param args Objects
     */
    public static void i(String message, Object... args) {
        Logger.i(message, args);
    }

    /**
     * Send ERROR log message
     * @param throwable Throwable error
     * @param message String message
     * @param args Objects
     */
    public static void e(String where, Throwable throwable, String message, Object... args) {
        Logger.e(throwable, message, args);
    }

    /**
     * Send Error log message
     * @param message String message
     * @param args Objects
     */
    public static void e(String where, String message, Object... args) {
        Logger.e(message, args);
    }

    /**
     * send What a Terrible Failure log message
     * @param message String message
     * @param args Objects
     */
    public static void wtf(String message, Object... args) {
        Logger.wtf(message, args);
    }

    /**
     * Send a JSON log message
     * @param json String the json to show
     */
    public static void json(String json) {
        Logger.json(json);
    }

    /**
     * Send a XML log message
     * @param xml String the xml to show
     */
    public static void xml(String xml) {
        Logger.xml(xml);
    }

}
