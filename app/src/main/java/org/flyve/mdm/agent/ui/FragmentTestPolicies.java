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

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.database.PoliciesData;
import org.flyve.mdm.agent.policies.AirplaneModePolicy;
import org.flyve.mdm.agent.policies.BluetoothPolicy;
import org.flyve.mdm.agent.policies.CameraPolicy;
import org.flyve.mdm.agent.policies.GPSPolicy;
import org.flyve.mdm.agent.policies.HostpotTetheringPolicy;
import org.flyve.mdm.agent.policies.MobileLinePolicy;
import org.flyve.mdm.agent.policies.NFCPolicy;
import org.flyve.mdm.agent.policies.StorageEncryptionPolicy;
import org.flyve.mdm.agent.policies.WifiPolicy;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.utils.ConnectionHTTP;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.StorageFolder;
import org.flyve.policies.manager.AndroidPolicies;
import org.flyve.policies.manager.CustomPolicies;

public class FragmentTestPolicies extends Fragment {

    private PoliciesData cache;
    private AndroidPolicies mdm;
    private CustomPolicies customPolicies;
    private static final int PRIORITY = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_test_policies, container, false);

        cache = new PoliciesData(FragmentTestPolicies.this.getContext());
        mdm = new AndroidPolicies(FragmentTestPolicies.this.getContext(),FlyveAdminReceiver.class);
        customPolicies = new CustomPolicies(FragmentTestPolicies.this.getContext());
        Switch swGPS = v.findViewById(R.id.swGPS);

        swGPS.setChecked(Boolean.parseBoolean(cache.getValue(GPSPolicy.POLICY_NAME).value));
        swGPS.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    customPolicies.disableGps(isChecked);
                }
            }
        });

        Switch swAirplane = v.findViewById(R.id.swAirplane);
        swAirplane.setChecked(Boolean.parseBoolean(cache.getValue(AirplaneModePolicy.POLICY_NAME).value));
        swAirplane.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                customPolicies.disableAirplaneMode(isChecked);
            }
        });

        Switch swBluetooth = v.findViewById(R.id.swBluetooth);
        swBluetooth.setChecked(Boolean.parseBoolean(cache.getValue(BluetoothPolicy.POLICY_NAME).value));
        swBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    customPolicies.disableBluetooth(isChecked);
                }
            }
        });

        Switch swWifi = v.findViewById(R.id.swWifi);
        swWifi.setChecked(Boolean.parseBoolean(cache.getValue(WifiPolicy.POLICY_NAME).value));
        swWifi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    customPolicies.disableWifi(isChecked);
                }
            }
        });

        Switch swNFC = v.findViewById(R.id.swNFC);
        swNFC.setChecked(Boolean.parseBoolean(cache.getValue(NFCPolicy.POLICY_NAME).value));
        swNFC.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    customPolicies.disableNFC(isChecked);
                }
            }
        });

        Switch swHostpot = v.findViewById(R.id.swHostpot);
        swHostpot.setChecked(Boolean.parseBoolean(cache.getValue(HostpotTetheringPolicy.POLICY_NAME).value));
        swHostpot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    customPolicies.disableHostpotTethering(isChecked);
                }
            }
        });

        Switch swMobileLine = v.findViewById(R.id.swMobileLine);
        swMobileLine.setChecked(Boolean.parseBoolean(cache.getValue(MobileLinePolicy.POLICY_NAME).value));
        swMobileLine.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    customPolicies.disableMobileLine(isChecked);
                }
            }
        });

        Button btnLock = v.findViewById(R.id.btnLock);
        btnLock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdm.lockScreen(LockActivity.class, getContext());
            }
        });

        Switch swDisableCamera = v.findViewById(R.id.swDisableCamera);
        swDisableCamera.setChecked(Boolean.parseBoolean(cache.getValue(CameraPolicy.POLICY_NAME).value));
        swDisableCamera.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mdm.disableCamera(isChecked);
            }
        });

        Switch swStorageEncryptionDevice = v.findViewById(R.id.swStorageEncryptionDevice);
        swStorageEncryptionDevice.setChecked(Boolean.parseBoolean(cache.getValue(StorageEncryptionPolicy.POLICY_NAME).value));
        swStorageEncryptionDevice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mdm.storageEncryptionDevice(isChecked);
            }
        });

        Button btnReboot = v.findViewById(R.id.btnReboot);
        btnReboot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdm.reboot();
            }
        });

        Button btnClearMQTT = v.findViewById(R.id.btnCleatMQTT);
        btnClearMQTT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Helpers.deleteMQTTCache(FragmentTestPolicies.this.getContext());
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", onCreateView", ex.getMessage());
                }
            }
        });

        final EditText edtPasswordLength = v.findViewById(R.id.edtPasswordLength);
        final EditText edtPasswordMinimumLetters = v.findViewById(R.id.edtPasswordMinimumLetters);
        final EditText edtPasswordMinimumLowerCase =  v.findViewById(R.id.edtPasswordMinimumLowerCase);
        final EditText edtPasswordMinimumUpperCase =  v.findViewById(R.id.edtPasswordMinimumUpperCase);
        final EditText edtPasswordMinimumNonLetter = v.findViewById(R.id.edtPasswordMinimumNonLetter);
        final EditText edtPasswordMinimumNumeric = v.findViewById(R.id.edtPasswordMinimumNumeric);
        final EditText edtPasswordMinimumSymbols = v.findViewById(R.id.edtPasswordMinimumSymbols);
        final EditText edtMaximumFailedPasswordsForWipe = v.findViewById(R.id.edtMaximumFailedPasswordsForWipe);
        final EditText edtMaximumTimeToLock = v.findViewById(R.id.edtMaximumTimeToLock);

        Button btnPassword = v.findViewById(R.id.btnPassword);
        btnPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int minimumLength = Integer.parseInt(edtPasswordLength.getText().toString());
                int minimumLetters = Integer.parseInt(edtPasswordMinimumLetters.getText().toString());
                int minimumUpperCase = Integer.parseInt(edtPasswordMinimumUpperCase.getText().toString());
                int minimumLowerCase = Integer.parseInt(edtPasswordMinimumLowerCase.getText().toString());
                int minimumNonLetter = Integer.parseInt(edtPasswordMinimumNonLetter.getText().toString());
                int minimumNumeric = Integer.parseInt(edtPasswordMinimumNumeric.getText().toString());
                int minimumSymbols = Integer.parseInt(edtPasswordMinimumSymbols.getText().toString());
                int maximumFailedPasswordsForWipe = Integer.parseInt(edtMaximumFailedPasswordsForWipe.getText().toString());
                int maximumTimeToLock = Integer.parseInt(edtMaximumTimeToLock.getText().toString());

                mdm.setPasswordLength(minimumLength);
                mdm.setPasswordMinimumLetters(minimumLetters);
                mdm.setPasswordMinimumUpperCase(minimumUpperCase);
                mdm.setPasswordMinimumLowerCase(minimumLowerCase);
                mdm.setPasswordMinimumNonLetter(minimumNonLetter);
                mdm.setPasswordMinimumNumeric(minimumNumeric);
                mdm.setPasswordMinimumSymbols(minimumSymbols);
                mdm.setMaximumFailedPasswordsForWipe(maximumFailedPasswordsForWipe);
                mdm.setMaximumTimeToLock(maximumTimeToLock);
            }
        });

        Button btnPasswordEnable = v.findViewById(R.id.btnPasswordEnable);
        btnPasswordEnable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mdm.enablePassword(true, "", MainActivity.class);
            }
        });

        Button btnDownloadFile = v.findViewById(R.id.btnDownloadFile);
        btnDownloadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    public void run() {
                        //
                        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/CHANGELOG.md";
                        FlyveLog.d(path);
                        ConnectionHTTP.getSyncFile("https://raw.githubusercontent.com/flyve-mdm/android-mdm-agent/develop/CHANGELOG.md", path, "", new ConnectionHTTP.ProgressCallback() {
                            @Override
                            public void progress(int value) {

                            }
                        });
                    }
                }).start();


            }
        });

        Button btnDownloadAPK = v.findViewById(R.id.btnDownloadAPK);
        btnDownloadAPK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = new StorageFolder(getContext()).getDocumentsDir() + "/flyve-apk.apk";
                FlyveLog.d(path);
                ConnectionHTTP.getSyncFile("https://f-droid.org/repo/org.flyve.inventory.agent_37960.apk", path, "", new ConnectionHTTP.ProgressCallback() {
                    @Override
                    public void progress(int value) {

                    }
                });
            }
        });

        Button btnEnablePassword = v.findViewById(R.id.btnEnablePassword);
        btnEnablePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Helpers.sendToNotificationBar(getContext(), 1009, "MDM Agent", "Please create a new password", true, MainActivity.class, "TestPolicies");
            }
        });

        Button btnInstallSilently = v.findViewById(R.id.btnInstallSilently);
        btnInstallSilently.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String path = new StorageFolder(FragmentTestPolicies.this.getContext()).getDownloadDir() + "/test.apk";
                Helpers.installApkSilently(path);
            }
        });

        return v;
    }
}
