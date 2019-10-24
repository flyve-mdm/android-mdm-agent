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
 * @author    Rafael Hernandez
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.ui;


import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.utils.UtilsCrash;

/**
 * All the application configuration
 */
public class MDMAgent extends Application {

    private static MDMAgent instance;
    private static Boolean isDebuggable;
    private static MqttAndroidClient mqttAndroidClient;
    private LockActivity lockActivity = null;

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public void setLockActivity (LockActivity activity) {
        this.lockActivity = activity ;
    }

    public LockActivity getLockActivity () {
        return this.lockActivity;
    }

    /**
     * Called when the application is starting before any activity, service or receiver objects have been created
     */
    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;

        UtilsCrash.configCrash(this, true);

        try {
            FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                    .tag(getPackageName())   // (Optional) Global tag for every log.
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        } catch (Exception ex) {
            ex.getStackTrace();
        }

        isDebuggable = true; // ( 0 != ( getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE ) );
    }

    /**
     * Get application instance
     * @return MDMAgent object
     */
    public static MDMAgent getInstance(){
        return instance;
    }

    public static MqttAndroidClient getMqttClient() {
        return mqttAndroidClient;
    }

    public static void setMqttClient(MqttAndroidClient client) {
        mqttAndroidClient = client;
    }

    /**
     * Get if the app is debuggable or not
     * @return Boolean
     */
    public static Boolean getIsDebuggable() {
        return isDebuggable;
    }

    public static Boolean isSecureVersion() { return false; }

    public static String getCompleteVersion() {
        return BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE;
    }
}
