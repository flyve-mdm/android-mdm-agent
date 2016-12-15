package org.fusioninventory;
import android.app.AlarmManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.Calendar;

public class Agent
        extends Service {

    public InventoryTask inventory = null;

    static final int MSG_CLIENT_REGISTER = 0;
    static final int MSG_AGENT_STATUS = 1;
    static final int MSG_INVENTORY_START = 2;
    static final int MSG_INVENTORY_FINISHED = 4;
    static final int MSG_INVENTORY_RESULT = 6;
    static final int MSG_INVENTORY_SEND = 7;


    private static String TAG = "Agent";

    AlarmManager am;
    private	Calendar cal = Calendar.getInstance();

    public class AgentBinder
            extends Binder {
        Agent getService() {
            return Agent.this;
        }
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate: ");
        SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoInventory = customSharedPreference.getBoolean("autoStartInventory", false);
        String timeInventory = customSharedPreference.getString("timeInventory", "Week");

        if (autoInventory)
        {
            if (timeInventory.equals("Day"))
            {
                cal.set(Calendar.HOUR_OF_DAY, 18);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
            else if(timeInventory.equals("Week"))
            {
                cal.set(Calendar.DAY_OF_WEEK, 1);
                cal.set(Calendar.HOUR_OF_DAY, 18);
                cal.set(Calendar.MINUTE, 33);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }
            else if(timeInventory.equals("Month"))
            {
                cal.set(Calendar.WEEK_OF_MONTH, 1);
                cal.set(Calendar.DAY_OF_WEEK, 1);
                cal.set(Calendar.HOUR_OF_DAY, 18);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            }

            am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        }

        inventory = new InventoryTask(this);

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.

        // mNM.cancel(NOTIFICATION);
        //     start_inventory();
        return START_STICKY;
    }

    public void start_inventory() {

        Log.i(TAG, "start_inventory: ");
        //  inventory.execute();

    }

    @Override
    public void onDestroy() {
        // Cancel the persistent notification.
        SharedPreferences customSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
