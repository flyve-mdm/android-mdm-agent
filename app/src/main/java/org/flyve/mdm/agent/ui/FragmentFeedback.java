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

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.PoliciesData;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class FragmentFeedback extends Fragment {

    private Switch[] swPolicy;
    private EditText editMessage;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_feedback, container, false);

        HashMap<String, Boolean> map = new HashMap<>();

        PoliciesData cache = new PoliciesData(FragmentFeedback.this.getContext());

        map.put("Ping",false);
        map.put("Geolocate",false);
        map.put("Inventory",false);
        map.put("Lock",false);
        map.put("Wipe",false);
        map.put("Unenroll",false);
        map.put("Password enabled", false);
        map.put("Password quality", !cache.getPasswordQuality().isEmpty() );
        map.put("Password minimum length", !cache.getPasswordLength().isEmpty() );
        map.put("Password minimum lower case", !cache.getPasswordMinimumLowerCase().isEmpty() );
        map.put("Password minimum upper case", !cache.getPasswordMinimumUpperCase().isEmpty() );
        map.put("Password minimum non letter",!cache.getPasswordMinimumNonLetter().isEmpty() );
        map.put("Password minimum letters", !cache.getPasswordMinimumLetters().isEmpty() );
        map.put("Password minimum numeric", !cache.getPasswordMinimumNumeric().isEmpty() );
        map.put("Password minimum symbols", !cache.getPasswordMinimumSymbols().isEmpty() );
        map.put("Maximum failed passwords for wipe", !cache.getMaximumFailedPasswordsForWipe().isEmpty() );
        map.put("Maximum time to lock", !cache.getMaximumTimeToLock().isEmpty());
        map.put("Storage encryption", !cache.getStorageEncryptionDevice());
        map.put("Disable camera", !cache.getDisableCamera());
        map.put("Disable bluetooth", !cache.getConnectivityBluetoothDisable());
        map.put("Deploy app", false);
        map.put("Remove app",false);
        map.put("Deploy file",true);
        map.put("Remove file",false);
        map.put("Disable screen capture", false);
        map.put("Disable airplane mode",!cache.getConnectivityAirplaneModeDisable());
        map.put("Disable GPS", !cache.getConnectivityGPSDisable());
        map.put("Disable Hostpot/Tethering", !cache.getConnectivityHostpotTetheringDisable());
        map.put("Disable roaming", !cache.getConnectivityRoamingDisable());
        map.put("Disable wifi", !cache.getConnectivityWifiDisable());
        map.put("Use TLS", false);
        map.put("Disable mobile line", !cache.getConnectivityMobileLineDisable());
        map.put("Disable NFC", !cache.getConnectivityNFCDisable());
        map.put("Disable statusbar", false);
//        map.put("ResetPassword",false);
        map.put("Disable Usb Mtp", !cache.getConnectivityMTPUsbFileTransferProtocolsDisable() );
        map.put("Disable Usb Ptp", !cache.getConnectivityPTPUsbFileTransferProtocolsDisable() );
        map.put("Disable Usb Adb", !cache.getConnectivityADBUsbFileTransferProtocolsDisable() );
        map.put("Disable speakerphone", !cache.getdisableSpeakerphone());
//        map.put("DisableSmsMms",false);
        map.put("Disable create VPN Profiles", false);

        LinearLayout ln = v.findViewById(R.id.lnFields);
        editMessage = v.findViewById(R.id.editMessage);

        Map<String, Boolean> treeMap = new TreeMap<>(map);
        createField(FragmentFeedback.this.getContext(), treeMap, ln);

        Button btnSend = v.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendFeedbackChollima(createFeedbackJSON(swPolicy));
            }
        });

        return v;
    }

    private JSONObject createFeedbackJSON(Switch[] sw){
        JSONObject jsonFeedback = new JSONObject();
        JSONObject jsonPolicy = new JSONObject();

        try {
            for (int i=0; i < sw.length; i++) {
                jsonPolicy.put(sw[i].getText().toString(), String.valueOf(sw[i].isChecked()));
            }
            jsonPolicy.put("message", editMessage.getText().toString());
            jsonFeedback.put("feedback", jsonPolicy);
        } catch (Exception ex) {
            FlyveLog.e(ex.getMessage());
        }

        return jsonFeedback;
    }

    private void sendFeedbackChollima(JSONObject json) {
        ConnectionHTTP.getWebData("", json, null, new ConnectionHTTP.DataCallback() {
            @Override
            public void callback(String data) {
                Helpers.snack(FragmentFeedback.this.getActivity(), getString(R.string.feedback_send_success));
            }
        });
    }

    private void createField(Context context, Map<String, Boolean> map, LinearLayout ln) {
        swPolicy = new Switch[map.size()];
        int index = 0;

        for(Map.Entry<String, Boolean> entry : map.entrySet()) {
            String policy = entry.getKey();
            Boolean highlight = entry.getValue();

            swPolicy[index] = new Switch(context);
            swPolicy[index].setText(policy);
            if(highlight) {
                swPolicy[index].setTextColor(Color.DKGRAY);
            } else {
                swPolicy[index].setTextColor(Color.GRAY);
            }

            ln.addView(swPolicy[index]);
            index++;
        }
    }
}
