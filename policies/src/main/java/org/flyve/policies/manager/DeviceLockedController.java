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

package org.flyve.policies.manager;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import org.flyve.policies.utils.FlyveLog;

public class DeviceLockedController {

    private Context context;

    public DeviceLockedController(Context context) {
        this.context = context;
    }

    public boolean isDeviceScreenLocked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return isDeviceLocked();
        } else {
            return isPatternSet() || isPassOrPinSet();
        }
    }

    /**
     * @return true if pattern set, false if not (or if an issue when checking)
     */
    private boolean isPatternSet() {
        ContentResolver cr = context.getContentResolver();
        try {
            int lockPatternEnable = Settings.Secure.getInt(cr, Settings.Secure.LOCK_PATTERN_ENABLED);
            return lockPatternEnable == 1;
        } catch (Settings.SettingNotFoundException e) {
            return false;
        }
    }

    /**
     * @return true if pass or pin set
     */
    private boolean isPassOrPinSet() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //api 16+
            return keyguardManager.isKeyguardSecure();
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", isPassOrPinSet", ex.getMessage());
            return false;
        }
    }

    /**
     * @return true if pass or pin or pattern loacks screen
     */
    @TargetApi(23)
    private boolean isDeviceLocked() {
        try {
            KeyguardManager keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE); //api 23+
            return keyguardManager.isDeviceSecure();
        } catch (Exception ex) {
         FlyveLog.e(this.getClass().getName() + ", isDeviceLocked", ex.getMessage());
            return false;
        }
    }
}
