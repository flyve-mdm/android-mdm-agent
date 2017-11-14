package org.example.lockscreen;

import android.app.Activity;
import android.app.ActivityManager;
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
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class LockActivity extends Activity {

    // Max number of times the launcher pick dialog toast should show
    private int launcherPickToast = 2;

    // Member variables
    private LockscreenUtils mLockscreenUtils;

    private WindowManager wm;
    private PackageManager packageManager;
    private int flags; // Window flags
    private View disableStatusBar;

    // Components for home launcher activity
    private ComponentName cnHome;
    private int componentEnabled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mLockscreenUtils = new LockscreenUtils();

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

        cnHome = new ComponentName(this, "org.example.lockscreen.LockHome");

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

        startService(new Intent(this,LockScreenService.class));

        setContentView(R.layout.activity_main);

        // Check default launcher, if current package isn't the default one
        // Open the 'Select home app' dialog for user to pick default home launcher
        if (!isMyLauncherDefault()) {
            packageManager.clearPackagePreferredActivities(getPackageName());

            Intent launcherPicker = new Intent();
            launcherPicker.setAction(Intent.ACTION_MAIN);
            launcherPicker.addCategory(Intent.CATEGORY_HOME);
            startActivity(launcherPicker);

            if (launcherPickToast > 0) {
                launcherPickToast -= 1;

                Toast toast = Toast.makeText(getApplicationContext(),
                        "toast message",
                        Toast.LENGTH_LONG);
                toast.show();
            }
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

        packageManager.getPreferredActivities(filters, activities, "org.example.lockscreen");

        for (ComponentName activity : activities) {
            if (myPackageName.equals(activity.getPackageName())) {
                return true;
            }
        }

        return false;
    }


    // Lock home button
    public void lockHomeButton() {
        mLockscreenUtils.lock(LockActivity.this);
    }

    /**
     * A simple method that sets the screen to fullscreen.  It removes the Notifications bar,
     *   the Actionbar and the virtual keys (if they are on the phone)
     */
    public void makeFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED|
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    public void onBackPressed() {
        return; //Do nothing!
    }

    public void unlockScreen(View view) {
        //Instead of using finish(), this totally destroys the process
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
