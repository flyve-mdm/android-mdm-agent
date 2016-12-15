package org.fusioninventory;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TimeAlarm extends BroadcastReceiver {

    @Override
        public void onReceive(Context context, Intent intent) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction("org.fusioninventory.AutoInventory");
            context.startService(serviceIntent);
            context.stopService(serviceIntent);
        }
}
