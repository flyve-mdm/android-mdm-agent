package org.flyve.mdm.agent.policies;

/*
 *   Copyright © 2018 Teclib. All rights reserved.
 *
 *   This file is part of flyve-mdm-android
 *
 * flyve-mdm-android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    rafael hernandez
 * @date      15/5/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.flyve.mdm.agent.data.PoliciesDataNew;

public abstract class Policies implements BasePolicies {

    protected Context context;
    private String policyName;
    private PoliciesDataNew data;
    private boolean enableLog;
    private MqttAndroidClient MQTTclient;
    protected String policyValue;
    protected int policyPriority;

    public Policies(Context context, String policyName) {
        this.context = context;
        this.data = new PoliciesDataNew(context);
        this.policyName = policyName;
        this.policyPriority = 0;
        this.enableLog = false;

        onStart();
    }

    public void setValue(String value) {
        this.policyValue = value;
    }

    public void setPriority(int priority) {
        this.policyPriority = priority;
    }

    public void setLog(Boolean enable) {
        this.enableLog = enable;
    }

    public void setMQTTclient(MqttAndroidClient client) {
        this.MQTTclient = client;
    }

    @Override
    public void onStart() {
        // store the policy on database
        data.setStringValue(policyName, policyValue, policyPriority);
    }

    public abstract void execute(PolicyCallback policyCallback);

    @Override
    public void onFinish() {

    }

    public interface PolicyCallback {
        void onSuccess();
        void onFail(String error);
    }
}
