package org.flyve.mdm.agent.utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @date      4/8/17
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public class LogFileReader {

    private static ArrayList<HashMap<String, String>> arrData;
    private static Handler uiHandler;
    static {
        uiHandler = new Handler(Looper.getMainLooper());
    }
    private static void runOnUI(Runnable runnable) {
        uiHandler.post(runnable);
    }

    /**
     * Constructor
     */
    private LogFileReader() {
    }

    /**
     * Add a new line
     * @param string the line to add
     * @throws Exception an error message
     */
    private static void addLine(String line) {
        try {
            HashMap<String, String> map = new HashMap<>();

            JSONObject json = new JSONObject(line);

            map.put("type", json.getString("type"));
            map.put("title", json.getString("title"));
            map.put("body", json.getString("body"));
            map.put("date", json.getString("date"));

            arrData.add(map);
        } catch (Exception ex) {
            FlyveLog.d("ERROR" + ex.getMessage());
        }
    }

    public static void loadLog(final String fileName, final LogFileCallback callback) {
        arrData = new ArrayList<>();

        Thread t = new Thread(new Runnable() {
            public void run() {
                File file = new File("/sdcard/FlyveMDM/" + fileName);
                FileReader fr = null;

                try {
                    fr = new FileReader(file);
                    BufferedReader br = new BufferedReader(fr);
                    String line;
                    while ((line = br.readLine()) != null) {
                        addLine(line);
                    }
                    br.close();

                    LogFileReader.runOnUI(new Runnable() {
                        public void run() {
                            callback.onSuccess( arrData );
                        }
                    });

                } catch (final Exception ex) {
                    FlyveLog.e(ex.getMessage());
                    LogFileReader.runOnUI(new Runnable() {
                        public void run() {
                            callback.onError(ex.getMessage());
                        }
                    });
                } finally {
                    if (fr != null) {
                        try {
                            fr.close();
                        } catch (final Exception ex) {
                            FlyveLog.e(ex.getMessage());
                            LogFileReader.runOnUI(new Runnable() {
                                public void run() {
                                    callback.onError(ex.getMessage());
                                }
                            });
                        }
                    }
                }
            }
        });
        t.start();
    }

    /**
     * This is the return data interface
     */
    public interface LogFileCallback {
        void onSuccess(ArrayList<HashMap<String, String>> data);
        void onError(String error);
    }
}
