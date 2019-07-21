package org.flyve.mdm.agent.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.flyve.mdm.agent.ui.SplashActivity;
import org.flyve.mdm.agent.utils.FlyveLog;

public class RunOnStartup extends BroadcastReceiver {

    public static final String START_AFTER_BOOTING = "START_AFTER_BOOTING";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            FlyveLog.d("Run SplashScreen after booting");
            Intent i = new Intent(context, SplashActivity.class);
            i.putExtra(RunOnStartup.START_AFTER_BOOTING, true);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }

}