package com.teclib.utils;

import android.content.Context;

import com.teclib.data.DataStorage;

/**
 * Created by rafaelhernandez on 6/5/17.
 */

public class Routes {

    String url;

    public Routes(Context context) {
        DataStorage cache = new DataStorage(context);
        url = cache.getVariablePermanente("url");
    }

    public String initSession(String user_token) {
        return url + "/initSession?user_token=" + user_token;
    }

    public String getFullSession() {
        return url + "/getFullSession";
    }

    public String changeActiveProfile(String profile_id) {
        return url + "/changeActiveProfile?profile_id=" + profile_id;
    }

    public String PluginFlyvemdmAgent() {
        return url + "/PluginFlyvemdmAgent";
    }

    public String PluginFlyvemdmAgent(String agent_id) {
        return url + "/PluginFlyvemdmAgent/" + agent_id;
    }


}
