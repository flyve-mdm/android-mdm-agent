package org.flyve.mdm.agent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.json.JSONObject;

public class FragmentInformation extends Fragment {

    private IntentFilter mIntent;
    private TextView txtOnline;
    private ImageView imgOnline;
    private int countEasterEgg;

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

    @Override
    public void onResume() {
        // register the broadcast
        super.onResume();
        LocalBroadcastManager.getInstance(FragmentInformation.this.getActivity()).registerReceiver(broadcastServiceStatus, new IntentFilter("flyve.mqtt.status"));
        LocalBroadcastManager.getInstance(FragmentInformation.this.getActivity()).registerReceiver(broadcastMessage, new IntentFilter("flyve.mqtt.msg"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_information, container, false);

        final DataStorage cache = new DataStorage(FragmentInformation.this.getActivity());

        ImageView imgLogo = (ImageView) v.findViewById(R.id.imgLogo);
        imgLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!cache.getEasterEgg()) {
                    countEasterEgg++;
                    if (countEasterEgg > 6 && countEasterEgg <= 10) {
                        Toast.makeText(FragmentInformation.this.getActivity(), "You have " + countEasterEgg + " Attempts", Toast.LENGTH_SHORT).show();
                    }
                    if (countEasterEgg >= 10) {
                        Toast.makeText(FragmentInformation.this.getActivity(), "Now you have log version agent", Toast.LENGTH_SHORT).show();
                        cache.setEasterEgg(true);
                    }
                }
            }
        });

        TextView txtName = (TextView) v.findViewById(R.id.txtNameUser);
        txtName.setText(cache.getUserFirstName() + " " + cache.getUserLastName());

        TextView txtEmail = (TextView) v.findViewById(R.id.txtDescriptionUser);
        txtEmail.setText(cache.getUserEmail());

        txtOnline = (TextView) v.findViewById(R.id.txtOnline);
        imgOnline = (ImageView) v.findViewById(R.id.imgOnline);

        return v;
    }

    private void showError() {
        Snackbar.make(FragmentInformation.this.getView(), "Message", Snackbar.LENGTH_LONG)
                .setActionTextColor(getResources().getColor(R.color.snackbar_action))
                .setAction("Retry", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FlyveLog.d("Retry", "reconnect");
                    }
                })
                .show();
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
                    if (Boolean.parseBoolean(msg)) {
                        txtOnline.setText(getResources().getString(R.string.online));
                        imgOnline.setImageResource(R.drawable.ic_online);
                    } else {
                        txtOnline.setText(getResources().getString(R.string.offline));
                        imgOnline.setImageResource(R.drawable.ic_offline);
                    }
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }
            }
        }
    };

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

                } catch (Exception ex) {
                    FlyveLog.d("ERROR" + ex.getMessage());
                }
            }
        }
    };

    private void openSplash() {
        Intent intent = new Intent(FragmentInformation.this.getActivity(), SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        FragmentInformation.this.getActivity().startActivity(intent);
        FragmentInformation.this.getActivity().finish();
    }
}
