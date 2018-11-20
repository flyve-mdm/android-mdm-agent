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

package org.flyve.mdm.agent.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class TimeAlarm extends BroadcastReceiver {

    /**
     * Schedules the alarm
     * @param context
     */
    public void setAlarm(Context context) {

        FlyveLog.d("Set Alarm");

        AlarmManager am =(AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, TimeAlarm.class);
        i.setAction("org.flyve.inventory.agent.ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(context);
        String timeInventory = customSharedPreference.getString("timeInventory", "Week");

        int time = 60 * 1000;

        if (timeInventory.equals("Day")) {
            time = 24 * 60 * 60 * 1000;
            FlyveLog.d("Alarm Daily");
        } else if(timeInventory.equals("Week")) {
            time = 7 * 24 * 60 * 60 * 1000;
            FlyveLog.d("Alarm Weekly");
        } else if(timeInventory.equals("Month")) {
            time = 30 * 24 * 60 * 60 * 1000;
            FlyveLog.d("Alarm Monthly");
        }

        try {
            am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), time, pi);
        } catch (NullPointerException ex) {
            FlyveLog.e(this.getClass().getName() + ", setAlarm", ex.getMessage());
        }
    }

    /**
     * If the success XML is created, it sends the inventory
     * @param context in which the receiver is running
     * @param intent being received
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        FlyveLog.d("Launch inventory from alarm");



    }

    /**
     * Removes the alarm with a matching argument
     * @param context
     */
    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, TimeAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}
