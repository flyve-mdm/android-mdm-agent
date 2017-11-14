package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.user.UserController;
import org.flyve.mdm.agent.core.user.UserModel;

import java.util.ArrayList;
import java.util.List;

public class LockActivity extends Activity {

    private WindowManager wm;
    private PackageManager packageManager;
    private int flags; // Window flags
    private View disableStatusBar;

    // Components for home launcher activity
    private ComponentName cnHome;
    private int componentEnabled;
    private EditText editEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IntentFilter filter = new IntentFilter("org.flyve.mdm.agent.unlock");
        this.registerReceiver(new Receiver(), filter);

        // Activity window flags
        flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD |
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH;

        disableStatusBar = new View(this);

        // Get screen density and calculate disableStatusBar view height
        float screenDensity = getResources().getDisplayMetrics().density;

        float disableStatusBar_height = screenDensity * 70;

        // DisableStatusBar view parameters
        WindowManager.LayoutParams handleParamsTop = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                (int) disableStatusBar_height,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSPARENT);

        handleParamsTop.gravity = Gravity.TOP | Gravity.CENTER;

        packageManager = getPackageManager();
        componentEnabled = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;

        cnHome = new ComponentName(this, "org.flyve.mdm.agent.LockHome");

        // Enable home launcher activity component
        packageManager.setComponentEnabledSetting(cnHome, componentEnabled, PackageManager.DONT_KILL_APP);

        // Get window manager service, add the window flags and the disableStatusBar view
        wm = (WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE);
        this.getWindow().addFlags(flags);
        wm.addView(disableStatusBar, handleParamsTop);

        // If Android version 4.4 or bigger, add translucent status bar flag
        if (Build.VERSION.SDK_INT >= 19) {
            this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        // When disableStatusBar view touched, consume touch
        disableStatusBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        setContentView(R.layout.activity_lock);

        editEmail = (EditText) findViewById(R.id.editEmail);

        // Check default launcher, if current package isn't the default one
        // Open the 'Select home app' dialog for user to pick default home launcher
        if (!isMyLauncherDefault()) {
            packageManager.clearPackagePreferredActivities(getPackageName());

            Intent launcherPicker = new Intent();
            launcherPicker.setAction(Intent.ACTION_MAIN);
            launcherPicker.addCategory(Intent.CATEGORY_HOME);
            startActivity(launcherPicker);
        }
    }

    // Method to check whether the package is the default home launcher or not
    public boolean isMyLauncherDefault() {
        final IntentFilter launcherFilter = new IntentFilter(Intent.ACTION_MAIN);
        launcherFilter.addCategory(Intent.CATEGORY_HOME);

        List<IntentFilter> filters = new ArrayList<IntentFilter>();
        filters.add(launcherFilter);

        final String myPackageName = getPackageName();
        List<ComponentName> activities = new ArrayList<ComponentName>();

        packageManager.getPreferredActivities(filters, activities, "org.flyve.mdm.agent");

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    public void unlockScreen(View view) {
        //Instead of using finish(), this totally destroys the process
        UserModel user = new UserController(LockActivity.this).getCache();

        if(editEmail.getText().toString().equals(user.getEmails().get(0).getEmail())) {
            closeLock();
        }
    }

    private void closeLock() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    protected void onPause() {
        super.onPause();

        ActivityManager activityManager = (ActivityManager) getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);

        activityManager.moveTaskToFront(getTaskId(), 0);
    }

    // Handle button clicks
    @Override
    public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                || (keyCode == KeyEvent.KEYCODE_POWER)
                || (keyCode == KeyEvent.KEYCODE_VOLUME_UP)
                || (keyCode == KeyEvent.KEYCODE_CAMERA)
                || (keyCode == KeyEvent.KEYCODE_HOME)) {
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
        } else if ((event.getKeyCode() == KeyEvent.KEYCODE_HOME)) {
            return true;
        }
        return false;
    }

    private class Receiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if("unlock".equalsIgnoreCase(action)) {
                closeLock();
            }
        }
    }
}
