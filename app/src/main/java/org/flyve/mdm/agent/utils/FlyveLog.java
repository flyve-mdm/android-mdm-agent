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
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android-agent
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */
package org.flyve.mdm.agent.utils;

import com.orhanobut.logger.Logger;
import org.flyve.mdm.agent.FlyveMDMApp;

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
        if(FlyveMDMApp.getIsDebuggable()){
            Logger.d(object);
        }
    }

    /**
     * Send a DEBUG log message
     * @param message String message to log
     * @param args Objects
     */
    public static void d(String message, Object... args) {
        if(FlyveMDMApp.getIsDebuggable()){
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
        if(FlyveMDMApp.getIsDebuggable()){
            Logger.v(message, args);
        }
    }

    /**
     * Send INFORMATION log message
     * @param message String message
     * @param args Objects
     */
    public static void i(String message, Object... args) {
        if(FlyveMDMApp.getIsDebuggable()) {
            Logger.i(message, args);
        }
    }

    /**
     * Send ERROR log message
     * @param throwable Throwable error
     * @param message String message
     * @param args Objects
     */
    public static void e(Throwable throwable, String message, Object... args) {
        if(FlyveMDMApp.getIsDebuggable()) {
            Logger.e(throwable, message, args);
        }
    }

    /**
     * Send Error log message
     * @param message String message
     * @param args Objects
     */
    public static void e(String message, Object... args) {
        if(FlyveMDMApp.getIsDebuggable()) {
            Logger.e(message, args);
        }
    }

    /**
     * send What a Terrible Failure log message
     * @param message String message
     * @param args Objects
     */
    public static void wtf(String message, Object... args) {
        if(FlyveMDMApp.getIsDebuggable()) {
            Logger.wtf(message, args);
        }
    }

    /**
     * Send a JSON log message
     * @param json String the json to show
     */
    public static void json(String json) {
        if(FlyveMDMApp.getIsDebuggable()) {
            Logger.json(json);
        }
    }

    /**
     * Send a XML log message
     * @param xml String the xml to show
     */
    public static void xml(String xml) {
        if(FlyveMDMApp.getIsDebuggable()) {
            Logger.xml(xml);
        }
    }

}
