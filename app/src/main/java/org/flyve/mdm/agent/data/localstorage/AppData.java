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

package org.flyve.mdm.agent.data.localstorage;

import android.content.Context;

import org.flyve.mdm.agent.data.localstorage.LocalStorage;

public class AppData extends LocalStorage {
    /**
     * Constructor
     *
     * @param context
     */
    public AppData(Context context) {
        super(context);
    }

    /**
     * Set the state of the Easter Egg
     * @param enable the state, true if enabled, false otherwise
     */
    public void setEasterEgg(boolean enable) {
        setData("easterEgg", String.valueOf(enable));
    }

    /**
     * Get the state of the Easter Egg
     * @return boolean the state, true if enabled, false otherwise
     */
    public boolean getEasterEgg() {
        return Boolean.valueOf(getData("easterEgg"));
    }

    /**
     * Get the online status
     * @return boolean the value represented by the string
     */
    public boolean getOnlineStatus() {
        return Boolean.valueOf(getData("onlineStatus"));
    }

    /**
     * Set the online status
     * @param status enable / disable
     */
    public void setOnlineStatus(boolean status) {
        setData("onlineStatus", String.valueOf(status));
    }

    /**
     * Get if the notifications are disable
     * @return boolean the value represented by the string
     */
    public boolean getDisableNotification() {
        return Boolean.valueOf(getData("disableNotification"));
    }

    /**
     * Set if the notifications are disable
     * @param status enable / disable
     */
    public void setDisableNotification(boolean status) {
        setData("disableNotification", String.valueOf(status));
    }

    /**
     * Get if the notifications are disable
     * @return boolean the value represented by the string
     */
    public boolean getEnableNotificationConnection() {
        return Boolean.valueOf(getData("enableNotificationConnection"));
    }

    /**
     * Set if the notifications are disable
     * @param status enable / disable
     */
    public void setEnableNotificationConnection(boolean status) {
        setData("enableNotificationConnection", String.valueOf(status));
    }

    /**
     * Get if dark theme enable
     * @return boolean the value represented by the string
     */
    public boolean getDarkTheme() {
        return Boolean.valueOf(getData("darkTheme"));
    }

    /**
     * Set if dark theme enable
     * @param status enable / disable
     */
    public void setDarkTheme(boolean status) {
        setData("darkTheme", String.valueOf(status));
    }

}
