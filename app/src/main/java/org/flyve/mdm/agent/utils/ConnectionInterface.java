package org.flyve.mdm.agent.utils;

import android.content.Context;

import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.MqttData;

import java.util.HashMap;

public class ConnectionInterface {

    private Context context;

    public ConnectionInterface(Context context) {
        this.context = context;
    }

    public ConnectionModel activateSessionToken(HashMap<String, String> header) {
        ConnectionModel connectionModel = new ConnectionModel();
        String url = new Routes(context).initSession(new MqttData(context).getUserToken());
        connectionModel.setEndpoint(url);
        connectionModel.setMethod("GET");
        connectionModel.setHeader(header);
        return connectionModel;
    }
}
