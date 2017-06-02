package com.teclib.flyvemdm;


import android.app.Application;

import com.teclib.data.DataStorage;


public class FlyveMDMApp extends Application {

    public static DataStorage cache;

    @Override
    public void onCreate() {
        super.onCreate();

        cache = new DataStorage(this);

    }


}
