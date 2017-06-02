package com.teclib.flyvemdm;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.teclib.services.MQTTService;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver statusReceiver;
    private IntentFilter mIntent;

    Intent mServiceIntent;
    private MQTTService mMQTTService;

    private TextView tvMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ------------------
        // MQTT SERVICE
        // ------------------
        mMQTTService = new MQTTService(this);
        mServiceIntent = new Intent(MainActivity.this, mMQTTService.getClass());
        if (!isMyServiceRunning(mMQTTService.getClass())) {
            startService(mServiceIntent);
        }

        tvMsg = (TextView) findViewById(R.id.tvMsg);

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i ("isMyServiceRunning?", true+"");
                return true;
            }
        }
        Log.i ("isMyServiceRunning?", false+"");
        return false;
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String type = intent.getStringExtra("message");  //get the type of message from MyGcmListenerService 1 - lock or 0 -Unlock
            tvMsg.setText( type );
        }
    };

    @Override
    protected void onPause() {
        if(mIntent != null) {
            unregisterReceiver(statusReceiver);
            mIntent = null;
        }
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        //registerReceiver(statusReceiver,mIntent);
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("NOW"));
    }

    @Override
    protected void onDestroy() {
        stopService(mServiceIntent);
        Log.i("MAINACT", "onDestroy!");
        super.onDestroy();

    }
}
