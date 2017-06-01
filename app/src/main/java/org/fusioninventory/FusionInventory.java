package org.fusioninventory;

import android.app.Activity;

import android.os.Bundle;
import android.util.Log;

public class FusionInventory
        extends Activity {


    public static void log(Object obj, String msg, int level) {
        String final_msg = String.format("[%s] %s", obj.getClass().getName(), msg);
        Log.println(level, "FusionInventory", final_msg);
    }

    /** Called when the activity is first created. */
    /*
     * (non-Javadoc)
     *
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FusionInventory.log(this, "OnDestroy()", Log.INFO);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        FusionInventory.log(this, "OnPause()", Log.INFO);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        FusionInventory.log(this, "OnResume()", Log.INFO);

    }


}