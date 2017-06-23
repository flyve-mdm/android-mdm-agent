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
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */
package com.teclib.utils;

import com.orhanobut.logger.Logger;
import com.teclib.flyvemdm.MainApplication;

public class FlyveLog {

    private FlyveLog() {

    }

    public static void d(Object object) {
        if(MainApplication.getInstance().getIsDebuggable()) {
            Logger.d(object);
        }
    }

    public static void d(String message, Object... args) {
        if(MainApplication.getInstance().getIsDebuggable()){
            // do something for a debug build
            Logger.d(message,args);
        }
    }


    public static void v(String message, Object... args) {
        if(MainApplication.getInstance().getIsDebuggable()){

            Logger.v(message, args);
        }
    }

    public static void i(String message, Object... args) {
        Logger.i(message, args);
    }

    public static void e(Throwable throwable, String message, Object... args) {
        Logger.e(throwable,message,args);
    }

    public static void e(String message, Object... args) {
        Logger.e(message, args);
    }

    public static void wtf(String message, Object... args) {
        Logger.wtf(message,args);
    }

    public static void json(String json) {
        if(MainApplication.getInstance().getIsDebuggable()) {
            Logger.json(json);
        }
    }

    public static void xml(String xml) {
        Logger.xml(xml);
    }

}
