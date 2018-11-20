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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.localstorage.SupervisorData;
import org.flyve.mdm.agent.data.localstorage.UserData;
import org.flyve.mdm.agent.data.localstorage.AppData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

public class FragmentInformation extends Fragment {

    private static final int EDIT_USER = 100;

    private IntentFilter mIntent;
    private TextView txtOnline;
    private ImageView imgOnline;
    private AppData cache;
    private TextView txtNameUser;
    private TextView txtEmailUser;
    private ImageView imgUser;
    private TextView txtNameSupervisor;
    private TextView txtDescriptionSupervisor;
    private int countEasterEgg;

    /**
     * Called when the Fragment is no longer resumed
     */
    @Override
    public void onPause() {
        // unregister the broadcast
        if(mIntent != null) {
            getActivity().unregisterReceiver(broadcastServiceStatus);
            getActivity().unregisterReceiver(broadcastMessage);
            mIntent = null;
        }
        super.onPause();
    }

    /**
     * Called when the fragment is visible to the user and actively running
     * Load the client and supervisor information
     */
    @Override
    public void onResume() {
        // register the broadcast
        super.onResume();
        LocalBroadcastManager.getInstance(FragmentInformation.this.getActivity()).registerReceiver(broadcastServiceStatus, new IntentFilter("flyve.mqtt.status"));
        LocalBroadcastManager.getInstance(FragmentInformation.this.getActivity()).registerReceiver(broadcastMessage, new IntentFilter("flyve.mqtt.msg"));

        loadClientInfo();
        loadSupervisor();
    }

    /**
     * Instantiate the user interface View
     * @param inflater the object that can be used to inflate any views in the fragment
     * @param container the parent View the fragment's UI should be attached to
     * @param savedInstanceState this fragment is being re-constructed from a previous saved state
     * @return View the View for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_information, container, false);

        cache = new AppData(FragmentInformation.this.getActivity());

        ImageView imgLogo = v.findViewById(R.id.imgLogo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cache.getEasterEgg()) {
                    countEasterEgg++;
                    if (countEasterEgg > 6 && countEasterEgg <= 10) {
                        Toast.makeText(FragmentInformation.this.getActivity(), getResources().getQuantityString(R.plurals.easter_egg_attempts, countEasterEgg, countEasterEgg), Toast.LENGTH_SHORT).show();
                    }
                    if (countEasterEgg >= 10) {
                        Toast.makeText(FragmentInformation.this.getActivity(), getResources().getString(R.string.easter_egg_success), Toast.LENGTH_SHORT).show();
                        cache.setEasterEgg(true);
                        ((MainActivity) FragmentInformation.this.getActivity()).loadListDrawer(0, "");
                    }
                }
            }
        });

        txtNameUser = v.findViewById(R.id.txtNameUser);
        txtEmailUser = v.findViewById(R.id.txtDescriptionUser);
        imgUser = v.findViewById(R.id.imgLogoUser);

        txtNameSupervisor = v.findViewById(R.id.txtNameSupervisor);
        txtDescriptionSupervisor = v.findViewById(R.id.txtDescriptionSupervisor);

        RelativeLayout layoutSupervisor = v.findViewById(R.id.rlSupervisor);
        layoutSupervisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSupervisorUser();
            }
        });

        RelativeLayout layoutUser = v.findViewById(R.id.rlUser);
        layoutUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewUser();
            }
        });

        txtOnline = v.findViewById(R.id.txtOnline);
        imgOnline = v.findViewById(R.id.imgOnline);

        statusMQTT(cache.getOnlineStatus());
        loadSupervisor();
        loadClientInfo();

        return v;
    }

    /**
     * Receive the result from a previous call to startActivityForResult(Intent, int)
     * @param requestCode the integer request code, it allows to identify who this result came from
     * @param resultCode the result code returned by the child activity
     * @param data the intent data, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == EDIT_USER) {
            loadClientInfo();
        }
    }

    /**
     * Load Supervisor information
     */
    private void loadSupervisor() {

        try {
            SupervisorData supervisor = new SupervisorData(FragmentInformation.this.getActivity());

            if (supervisor.getName() != null && !supervisor.getName().equals("")) {
                txtNameSupervisor.setText(supervisor.getName());
            }
            if (supervisor.getEmail() != null && !supervisor.getEmail().equals("")) {
                txtDescriptionSupervisor.setText(supervisor.getEmail());
            }
        } catch(Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", loadSupervisor", ex.getMessage());
        }
    }

    /**
     * Load Client information
     */
    private void loadClientInfo() {

        try {
            UserData user = new UserData(FragmentInformation.this.getActivity());

            if (user.getFirstName() != null && !user.getFirstName().equals("")) {
                txtNameUser.setText(user.getFirstName() + " " + user.getLastName());
            }

            if (user.getEmails().get(0).getEmail() != null && !user.getEmails().get(0).getEmail().equals("")) {
                txtEmailUser.setText(user.getEmails().get(0).getEmail());
            }

            if (user.getPicture() == null || user.getPicture().equals("")) {
                imgUser.setImageResource(R.drawable.ic_user_round);
            } else {
                try {
                    imgUser.setImageBitmap(Helpers.stringToBitmap(user.getPicture()));
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", loadClientInfo", ex.getMessage());
                    imgUser.setImageResource(R.drawable.ic_user_round);
                }
            }
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", loadClientInfo", ex.getMessage());
        }
    }

    /**
     * Set the text and image source of the button according the status of the Message Queue Telemetry Transport (MQTT)
     * @param bval the value, online if true, otherwise offline
     */
    private void statusMQTT(Boolean bval) {
        if (bval) {
            txtOnline.setText(getResources().getString(R.string.online));
            imgOnline.setImageResource(R.drawable.ic_online);
        } else {
            txtOnline.setText(getResources().getString(R.string.offline));
            imgOnline.setImageResource(R.drawable.ic_offline);
        }
    }

    /**
     * broadcastServiceStatus instance that receive service status from MQTTService
     */
    private BroadcastReceiver broadcastServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String msg = intent.getStringExtra("message");

            // status ONLINE / OFFLINE
            if("flyve.mqtt.status".equalsIgnoreCase(action)) {
                try {
                    if(isAdded()) {
                        statusMQTT(Boolean.parseBoolean(msg));
                    }
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", broadcastServiceStatus", ex.getMessage());
                }
            }
        }
    };

    /**
     * Instance that receive message from mqtt service
     */
    private BroadcastReceiver broadcastMessage = new BroadcastReceiver() {

        /**
         * Open Splash Activity
         */
        private void openSplash() {
            Intent intent = new Intent(FragmentInformation.this.getActivity(), SplashActivity.class);
            FragmentInformation.this.getActivity().startActivity(intent);
            FragmentInformation.this.getActivity().finish();
        }


        @Override
        public void onReceive(Context context, Intent intent) {

            // exit if not attached to an activity
            if(!isAdded()) {
                return;
            }

            String action = intent.getAction();
            String msg = intent.getStringExtra("message");

            // Message from service
            if("flyve.mqtt.msg".equalsIgnoreCase(action)) {

                try {
                    JSONObject json = new JSONObject(msg);

                    String type = json.getString("type");
                    String title = json.getString("title");
                    String body = json.getString("body");

                    if("action".equalsIgnoreCase(type) && "open".equalsIgnoreCase(title) && "splash".equalsIgnoreCase(body)) {
                        openSplash();
                    }

                    if("ERROR".equalsIgnoreCase(type)) {

                        Helpers.snack(FragmentInformation.this.getActivity(), body, getResources().getString(R.string.close), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        });
                    }

                } catch (Exception ex) {
                    FlyveLog.d("ERROR" + ex.getMessage());
                }
            }
        }
    };


    /**
     * Open Edit User Activity
     */
    private void openViewUser() {
        Intent intent = new Intent(FragmentInformation.this.getActivity(), PreviewUserActivity.class);
        FragmentInformation.this.startActivityForResult(intent, EDIT_USER);
    }

    /**
     * Open Supervisor Activity
     */
    private void openSupervisorUser() {
        Intent intent = new Intent(FragmentInformation.this.getActivity(), PreviewSupervisorActivity.class);
        FragmentInformation.this.getActivity().startActivity(intent);
    }
}
