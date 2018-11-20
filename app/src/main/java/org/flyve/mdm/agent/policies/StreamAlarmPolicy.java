package org.flyve.mdm.agent.policies;

import android.content.Context;
import android.media.AudioManager;
import android.os.Build;

import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.FlyveLog;

import static android.content.Context.AUDIO_SERVICE;

/*
 *   Copyright  2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @author    rafael hernandez
 * @date      15/5/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class StreamAlarmPolicy extends BasePolicies {

    public static final String POLICY_NAME = "disableStreamAlarm";

    public StreamAlarmPolicy(Context context) {
        super(context, POLICY_NAME);
    }

    @Override
    protected boolean process() {
        try {
            boolean disable = Boolean.parseBoolean(this.policyValue.toString());

            AudioManager aManager = (AudioManager) MDMAgent.getInstance().getApplicationContext().getSystemService(AUDIO_SERVICE);
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    if(disable) {
                        aManager.setStreamVolume(AudioManager.STREAM_ALARM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
                    } else {
                        aManager.setStreamVolume(AudioManager.STREAM_ALARM, 100, 0);
                    }
                } else {
                    //media
                    aManager.setStreamMute(AudioManager.STREAM_ALARM, disable);
                }
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            }

            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            return false;
        }
    }
}