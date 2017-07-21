package org.flyve.mdm.agent;

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
import android.widget.TextView;

import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.utils.FlyveLog;

public class FragmentInformation extends Fragment {

    private IntentFilter mIntent;
    private TextView txtOnline;
    private ImageView imgOnline;

    @Override
    public void onPause() {
        // unregister the broadcast
        if(mIntent != null) {
            getActivity().unregisterReceiver(broadcastServiceStatus);
            mIntent = null;
        }
        super.onPause();
    }

    @Override
    public void onResume() {
        // register the broadcast
        super.onResume();
        LocalBroadcastManager.getInstance(FragmentInformation.this.getActivity()).registerReceiver(broadcastServiceStatus, new IntentFilter("flyve.mqtt.status"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_information, container, false);

        DataStorage cache = new DataStorage(FragmentInformation.this.getActivity());

        TextView txtName = (TextView) v.findViewById(R.id.txtNameUser);
        txtName.setText(cache.getUserFirstName() + " " + cache.getUserLastName());

        TextView txtEmail = (TextView) v.findViewById(R.id.txtDescriptionUser);
        txtEmail.setText(cache.getUserEmail());

        txtOnline = (TextView) v.findViewById(R.id.txtOnline);
        imgOnline = (ImageView) v.findViewById(R.id.imgOnline);

        return v;
    }

    /**
     * broadcastServiceStatus instance that receive service status from MQTTService
     */
    private BroadcastReceiver broadcastServiceStatus = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("message");

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
    };
}
