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
import org.flyve.mdm.agent.core.supervisor.SupervisorController;
import org.flyve.mdm.agent.core.supervisor.SupervisorModel;
import org.flyve.mdm.agent.core.user.UserController;
import org.flyve.mdm.agent.core.user.UserModel;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

public class FragmentInformation extends Fragment {

    private static final int EDIT_USER = 100;

    private IntentFilter mIntent;
    private TextView txtOnline;
    private ImageView imgOnline;
    private DataStorage cache;
    private TextView txtNameUser;
    private TextView txtEmailUser;
    private ImageView imgUser;
    private TextView txtNameSupervisor;
    private TextView txtDescriptionSupervisor;
    private ImageView imgSupervisor;

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
     * Instantiate the user interface view
     * @param LayoutInflater inflater the object that can be used to inflate any views in the fragment
     * @param ViewGroup the parent view the fragment's UI should be attached to
     * @param Bundle this fragment is being re-constructed from a previous saved state
     * @return View the view for the fragment's UI
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_information, container, false);

        cache = new DataStorage(FragmentInformation.this.getActivity());

        ImageView imgLogo = (ImageView) v.findViewById(R.id.imgLogo);
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
                        ((MainActivity) FragmentInformation.this.getActivity()).loadListDrawer();
                    }
                }
            }
        });

        txtNameUser = (TextView) v.findViewById(R.id.txtNameUser);
        txtEmailUser = (TextView) v.findViewById(R.id.txtDescriptionUser);
        imgUser = (ImageView) v.findViewById(R.id.imgLogoUser);

        txtNameSupervisor = (TextView) v.findViewById(R.id.txtNameSupervisor);
        txtDescriptionSupervisor = (TextView) v.findViewById(R.id.txtDescriptionSupervisor);
        imgSupervisor = (ImageView) v.findViewById(R.id.imgLogoSupervisor);

        RelativeLayout layoutSupervisor = (RelativeLayout) v.findViewById(R.id.rlSupervisor);
        layoutSupervisor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSupervisorUser();
            }
        });

        RelativeLayout layoutUser = (RelativeLayout) v.findViewById(R.id.rlUser);
        layoutUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViewUser();
            }
        });

        txtOnline = (TextView) v.findViewById(R.id.txtOnline);
        imgOnline = (ImageView) v.findViewById(R.id.imgOnline);

        statusMQTT(cache.getOnlineStatus());
        loadSupervisor();
        loadClientInfo();

        return v;
    }

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
        SupervisorModel supervisor = new SupervisorController(FragmentInformation.this.getActivity()).getCache();

        if(supervisor.getName() != null && !supervisor.getName().equals("")) {
            txtNameSupervisor.setText(supervisor.getName());
        }
        if(supervisor.getEmail() != null && !supervisor.getEmail().equals("")) {
            txtDescriptionSupervisor.setText(supervisor.getEmail());
        }
    }

    /**
     * Load Client information
     */
    private void loadClientInfo() {
        UserModel user = new UserController(FragmentInformation.this.getActivity()).getCache();

        if(user.getFirstName() != null && !user.getFirstName().equals("")) {
            txtNameUser.setText(user.getFirstName() + " " + user.getLastName());
        }

        if(user.getEmails().get(0).getEmail() != null && !user.getEmails().get(0).getEmail().equals("")) {
            txtEmailUser.setText(user.getEmails().get(0).getEmail());
        }

        if(user.getPicture().equals("")) {
            imgUser.setImageResource(R.drawable.ic_user_round);
        } else {
            try {
                imgUser.setImageBitmap(Helpers.StringToBitmap(user.getPicture()));
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
                imgUser.setImageResource(R.drawable.ic_user_round);
            }
        }
    }

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
                    statusMQTT(Boolean.parseBoolean(msg));
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }
            }
        }
    };

    /**
     * Instance that receive message from mqtt service
     */
    private BroadcastReceiver broadcastMessage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
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
     * Open Splash Activity
     */
    private void openSplash() {
        Intent intent = new Intent(FragmentInformation.this.getActivity(), SplashActivity.class);
        FragmentInformation.this.getActivity().startActivity(intent);
        FragmentInformation.this.getActivity().finish();
    }

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
