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

package org.flyve.mdm.agent.receivers;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.telephony.PhoneStateListener;

import org.flyve.mdm.agent.data.PoliciesData;
import org.flyve.mdm.agent.ui.MDMAgent;
import org.flyve.mdm.agent.utils.FlyveLog;

public class CustomPhoneStateListener extends PhoneStateListener {

    public void onCallStateChanged(int state, String incomingNumber) {

        // 0 = CALL_STATE_IDLE (close or no activity)
        // 1 = CALL_STATE_RINGING
        // 2 = CALL_STATE_OFFHOOK (At least one call exists that is dialing, active, or on hold, and no calls are ringing or waiting.)
        FlyveLog.d("Status: " + state);
        final Context context = MDMAgent.getInstance();
        Boolean disable = new PoliciesData(context).getdisableSpeakerphone();

        if (state == 2) {

            // Disable Speaker Phone
            if(disable) {
                final Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
                        audioManager.setMode(AudioManager.MODE_IN_CALL);
                        audioManager.setMode(AudioManager.MODE_NORMAL);
                        audioManager.setSpeakerphoneOn(false);
                        FlyveLog.d("incoming_call: speaker_off");
                    }
                }, 500);
            }

        }
    }
}