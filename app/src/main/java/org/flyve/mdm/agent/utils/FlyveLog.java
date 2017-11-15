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

import android.os.Environment;
import com.orhanobut.logger.Logger;
import org.flyve.mdm.agent.ui.MDMAgent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This is a Log wrapper
 */
public class FlyveLog {

    private static final String FILE_NAME_FEEDBACK = "FlyveMDMFeedback.txt";
    public static final String FILE_NAME_LOG = "FlyveMDMLog.txt";
    public static final String FLYVE_PATH = Environment.getExternalStorageDirectory().getPath() + "/FlyveMDM";

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
    public static void e(Throwable throwable, String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null) {
            Logger.e(throwable, message, args);
            f(message, FILE_NAME_FEEDBACK);
        }
    }

    /**
     * Send Error log message
     * @param message String message
     * @param args Objects
     */
    public static void e(String message, Object... args) {
        if(MDMAgent.getIsDebuggable() && message != null) {
            Logger.e(message, args);
            f(message, FILE_NAME_FEEDBACK);
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
     * @param filename
     */
    public static void f(String message, String filename) {
        String state = Environment.getExternalStorageState();

        File dir = new File(FLYVE_PATH);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if(!dir.exists()) {
                FlyveLog.d("Create directory on f");
                dir.mkdirs();
            }

            File logFile = new File(FLYVE_PATH + filename);

            if (!logFile.exists())  {
                try  {
                    Boolean log = logFile.createNewFile();
                    FlyveLog.d("Create File on f %s", log);
                } catch (IOException ex) {
                    FlyveLog.e(ex.getMessage());
                }
            }

            FileWriter fw = null;
            try {
                //BufferedWriter for performance, true to set append to file flag
                fw = new FileWriter(logFile, true);
                BufferedWriter buf = new BufferedWriter(fw);

                buf.write(message);
                buf.newLine();
                buf.flush();
                buf.close();
                fw.close();
            }
            catch (IOException ex) {
                e(ex.getMessage());
            }
            finally {
                if(fw!=null) {
                    try {
                        fw.close();
                    } catch(Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Clear the log
     * @param filename
     */
    public static void clearLog(String filename) {
        String state = Environment.getExternalStorageState();

        File dir = new File(FLYVE_PATH);
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            if (!dir.exists()) {
                FlyveLog.d("Created Directory on clearLog ");
                dir.mkdirs();
            }

            File logFile = new File(FLYVE_PATH + filename);

            FileWriter fw = null;
            try {
                //BufferedWriter for performance, true to set append to file flag
                fw = new FileWriter(logFile, false);
                PrintWriter pwOb = new PrintWriter(fw, false);
                pwOb.flush();
                pwOb.close();
            }
            catch (IOException ex) {
                e(ex.getMessage());
            }
            finally {
                if(fw!=null) {
                    try {
                        fw.close();
                    } catch(Exception ex) {
                        FlyveLog.e(ex.getMessage());
                    }
                }
            }

        }
    }

}
