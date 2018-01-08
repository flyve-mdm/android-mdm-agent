package org.flyve.mdm.agent.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.supervisor.SupervisorData;
import org.flyve.mdm.agent.utils.FlyveLog;

public class LockActivity extends AppCompatActivity {

    private TextView txtNameSupervisor;
    private TextView txtDescriptionSupervisor;
    private ViewGroup mTopView;
    private WindowManager wm;
    private LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver broadcastLock = new BroadcastReceiver() {
        @Override
        public void onReceive(Context arg0, Intent intent) {
            String action = intent.getAction();
            if (action.equals("org.flyvemdm.finishlock")) {
                if (mTopView != null) wm.removeView(mTopView);
                Intent miIntent = new Intent(LockActivity.this, MainActivity.class);
                LockActivity.this.startActivity(miIntent);
                LockActivity.this.finish();
            }
        }
    };

    @Override
    protected void onPause() {
        //unregister our receiver
        mLocalBroadcastManager.unregisterReceiver(broadcastLock);
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mLocalBroadcastManager.registerReceiver(broadcastLock, new IntentFilter("org.flyvemdm.finishlock"));

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN,
                PixelFormat.TRANSLUCENT);

        wm = (WindowManager) getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);

        mTopView = (ViewGroup) getLayoutInflater().inflate(R.layout.activity_lock, null);
        getWindow().setAttributes(params);
        wm.addView(mTopView, params);

        txtNameSupervisor = (TextView) mTopView.findViewById(R.id.txtNameSupervisor);
        txtDescriptionSupervisor = (TextView) mTopView.findViewById(R.id.txtDescriptionSupervisor);

        loadSupervisor();
    }

    /**
     * Load Supervisor information
     */
    private void loadSupervisor() {

        try {
            SupervisorData supervisor = new SupervisorData(LockActivity.this);

            if (supervisor.getName() != null && !supervisor.getName().equals("")) {
                txtNameSupervisor.setText(supervisor.getName());
            }
            if (supervisor.getEmail() != null && !supervisor.getEmail().equals("")) {
                txtDescriptionSupervisor.setText(supervisor.getEmail());
            }
        } catch(Exception ex) {
            FlyveLog.e(ex.getMessage());
        }
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    public void unlockScreen(View view) {
        if (mTopView != null) wm.removeView(mTopView);
        Intent miIntent = new Intent(LockActivity.this, MainActivity.class);
        LockActivity.this.startActivity(miIntent);
        LockActivity.this.finish();

//        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager
//                .getInstance(LockActivity.this);
//        localBroadcastManager.sendBroadcast(new Intent(
//                "org.flyvemdm.finishlock"));
    }

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)) {
            return true;
        }
        if ((keyCode == KeyEvent.KEYCODE_HOME)) {

            return true;
        }

        return false;

    }

    // handle the key press events here itself
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP
                || (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (event.getKeyCode() == KeyEvent.KEYCODE_POWER)) {
            return false;
        }
        if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {

            return true;
        }
        return false;
    }
}