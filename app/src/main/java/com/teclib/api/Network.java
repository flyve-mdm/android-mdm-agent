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

package com.teclib.api;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;


public class Network {
    private static Network mInstance = new Network();
    private static Context mContext;
    ConnectivityManager connectivityManager;
    boolean connected = false;

    public static Network getInstance(Context ctx) {
        mContext = ctx.getApplicationContext();
        return mInstance;
    }

    /**
     * check if data network is enable
     *
     * @return true if network enable
     */
    public boolean isOnline() {
        try {
            connectivityManager = (ConnectivityManager) mContext
                    .getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
            connected = networkInfo != null && networkInfo.isAvailable() &&
                    networkInfo.isConnected();
            return connected;


        } catch (Exception e) {
            FlyveLog.e(e.toString());
        }
        return connected;
    }

    /**
     * get Android version
     *
     * @return android version
     */
    public String getVersion() {
        String version = null;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            version = (String) get.invoke(c, "ro.build.date.utc");
        } catch (Exception ignored) {
        }
        return version;
    }

    /**
     * get serial number
     *
     * @return serial number
     */
    public String getSerial() {
        String serial = null;

        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialno");
        } catch (Exception ignored) {
        }
        return serial;
    }

    /*
    public static ArrayList<String> getDNSAddresses() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException  {
        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        ArrayList<String> servers = new ArrayList<String>();
        for (String name : new String[] { "net.dns1", "net.dns2", "net.dns3", "net.dns4", }) {
            String value = (String) method.invoke(null, name);
            if (value != null && !"".equals(value) && !servers.contains(value)) {
                servers.add(value);
            }
        }


        return servers;
    }

    */

    /**
     * check if an application is installed
     *
     * @return true if app is installed
     */
    public static String getDNSAddress() throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, ClassNotFoundException {
        Class<?> SystemProperties = Class.forName("android.os.SystemProperties");
        Method method = SystemProperties.getMethod("get", new Class[]{String.class});
        String servers = "";
        servers = (String) method.invoke(null, "net.dns1");
        return servers;
    }

    /**
     * get dns address
     *
     * @return return dns adress
     */
    public static String getAddress(String hostName) {
        InetAddress inetAddress = null;
        String IP = "";
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        displayStuff("local host", inetAddress);
        try {
            inetAddress = InetAddress.getByName(hostName);
            IP = inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        displayStuff(hostName, inetAddress);
        return IP;
    }

    /**
     * Print informations
     *
     */
    public static void displayStuff(String whichHost, InetAddress inetAddress) {
        FlyveLog.d("Which Host:" + whichHost);
        FlyveLog.d("Canonical Host Name:" + inetAddress.getCanonicalHostName());
        FlyveLog.d("Host Name:" + inetAddress.getHostName());
        FlyveLog.d("Host Address:" + inetAddress.getHostAddress());
    }

}

