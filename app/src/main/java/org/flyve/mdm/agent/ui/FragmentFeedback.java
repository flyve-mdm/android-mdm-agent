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
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.policies.AirplaneModePolicy;
import org.flyve.mdm.agent.policies.BluetoothPolicy;
import org.flyve.mdm.agent.policies.CameraPolicy;
import org.flyve.mdm.agent.policies.GPSPolicy;
import org.flyve.mdm.agent.policies.HostpotTetheringPolicy;
import org.flyve.mdm.agent.policies.MaximumFailedPasswordForWipePolicy;
import org.flyve.mdm.agent.policies.MaximumTimeToLockPolicy;
import org.flyve.mdm.agent.policies.MobileLinePolicy;
import org.flyve.mdm.agent.policies.NFCPolicy;
import org.flyve.mdm.agent.policies.PasswordEnablePolicy;
import org.flyve.mdm.agent.policies.PasswordMinLengthPolicy;
import org.flyve.mdm.agent.policies.PasswordMinLetterPolicy;
import org.flyve.mdm.agent.policies.PasswordMinLowerCasePolicy;
import org.flyve.mdm.agent.policies.PasswordMinNonLetterPolicy;
import org.flyve.mdm.agent.policies.PasswordMinNumericPolicy;
import org.flyve.mdm.agent.policies.PasswordMinSymbolsPolicy;
import org.flyve.mdm.agent.policies.PasswordMinUpperCasePolicy;
import org.flyve.mdm.agent.policies.PasswordQualityPolicy;
import org.flyve.mdm.agent.policies.RoamingPolicy;
import org.flyve.mdm.agent.policies.ScreenCapturePolicy;
import org.flyve.mdm.agent.policies.SpeakerphonePolicy;
import org.flyve.mdm.agent.policies.StatusBarPolicy;
import org.flyve.mdm.agent.policies.StorageEncryptionPolicy;
import org.flyve.mdm.agent.policies.UsbAdbPolicy;
import org.flyve.mdm.agent.policies.UsbMtpPolicy;
import org.flyve.mdm.agent.policies.UsbPtpPolicy;
import org.flyve.mdm.agent.policies.VPNPolicy;
import org.flyve.mdm.agent.policies.WifiPolicy;
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
        map.put("Password enabled", Boolean.parseBoolean(cache.getValue(PasswordEnablePolicy.POLICY_NAME).value));
        map.put("Password quality", !cache.getValue(PasswordQualityPolicy.POLICY_NAME).value.isEmpty());
        map.put("Password minimum length", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinLengthPolicy.POLICY_NAME).value)));
        map.put("Password minimum lower case", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinLowerCasePolicy.POLICY_NAME).value)));
        map.put("Password minimum upper case", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinUpperCasePolicy.POLICY_NAME).value)));
        map.put("Password minimum non letter", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinNonLetterPolicy.POLICY_NAME).value)));
        map.put("Password minimum letters", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinLetterPolicy.POLICY_NAME).value)));
        map.put("Password minimum numeric", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinNumericPolicy.POLICY_NAME).value)));
        map.put("Password minimum symbols", moreThanCero(Integer.valueOf(cache.getValue(PasswordMinSymbolsPolicy.POLICY_NAME).value)));
        map.put("Maximum failed passwords for wipe", moreThanCero(Integer.valueOf(cache.getValue(MaximumFailedPasswordForWipePolicy.POLICY_NAME).value)));
        map.put("Maximum time to lock", moreThanCero(Integer.valueOf(cache.getValue(MaximumTimeToLockPolicy.POLICY_NAME).value)));
        map.put("Storage encryption", Boolean.parseBoolean(cache.getValue(StorageEncryptionPolicy.POLICY_NAME).value));
        map.put("Disable camera", Boolean.parseBoolean(cache.getValue(CameraPolicy.POLICY_NAME).value));
        map.put("Disable bluetooth", Boolean.parseBoolean(cache.getValue(BluetoothPolicy.POLICY_NAME).value));
        map.put("Deploy app", true);
        map.put("Remove app",true);
        map.put("Deploy file",true);
        map.put("Remove file",true);
        map.put("Disable screen capture", Boolean.parseBoolean(cache.getValue(ScreenCapturePolicy.POLICY_NAME).value));
        map.put("Disable airplane mode", Boolean.parseBoolean(cache.getValue(AirplaneModePolicy.POLICY_NAME).value));
        map.put("Disable GPS", Boolean.parseBoolean(cache.getValue(GPSPolicy.POLICY_NAME).value));
        map.put("Disable Hostpot/Tethering", Boolean.parseBoolean(cache.getValue(HostpotTetheringPolicy.POLICY_NAME).value));
        map.put("Disable roaming", Boolean.parseBoolean(cache.getValue(RoamingPolicy.POLICY_NAME).value));
        map.put("Disable wifi", Boolean.parseBoolean(cache.getValue(WifiPolicy.POLICY_NAME).value));
//        map.put("Use TLS", cache.getUseTLS());
        map.put("Disable mobile line", Boolean.parseBoolean(cache.getValue(MobileLinePolicy.POLICY_NAME).value));
        map.put("Disable NFC", Boolean.parseBoolean(cache.getValue(NFCPolicy.POLICY_NAME).value));
        map.put("Disable statusbar", Boolean.parseBoolean(cache.getValue(StatusBarPolicy.POLICY_NAME).value));
//        map.put("ResetPassword",false);
        map.put("Disable Usb Mtp", Boolean.parseBoolean(cache.getValue(UsbMtpPolicy.POLICY_NAME).value));
        map.put("Disable Usb Ptp", Boolean.parseBoolean(cache.getValue(UsbPtpPolicy.POLICY_NAME).value));
        map.put("Disable Usb Adb", Boolean.parseBoolean(cache.getValue(UsbAdbPolicy.POLICY_NAME).value));
        map.put("Disable speakerphone", Boolean.parseBoolean(cache.getValue(SpeakerphonePolicy.POLICY_NAME).value));
//        map.put("DisableSmsMms",false);
        map.put("Disable create VPN Profiles", Boolean.parseBoolean(cache.getValue(VPNPolicy.POLICY_NAME).value));

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

    private Boolean moreThanCero(int value) {
        return (value>0);
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
