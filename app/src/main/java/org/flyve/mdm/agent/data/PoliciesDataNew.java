package org.flyve.mdm.agent.data;

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
 * @author    Rafael Hernandez
 * @date      15/5/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.flyve.mdm.agent.room.database.AppDataBase;
import org.flyve.mdm.agent.room.entity.Policies;

import java.util.List;

public class PoliciesDataNew {

    public AppDataBase dataBase;

    public PoliciesDataNew(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public String getStringValue(String policyName) {
        List<Policies> arrPolicies = dataBase.PoliciesDao().getPolicyByName(policyName);
        if(!arrPolicies.isEmpty()) {
            return arrPolicies.get(0).value;
        } else {
            return "";
        }
    }

    public Object setStringValue(String policyName, String value, int priority) {
        if(dataBase.PoliciesDao().getPolicyBy(policyName, priority).isEmpty()) {
            Policies policies = new Policies();
            policies.policyName = policyName;
            policies.value = value;
            policies.priority = priority;
            dataBase.PoliciesDao().insert(policies);
        } else {
            Policies policies = dataBase.PoliciesDao().getPolicyBy(policyName, priority).get(0);
            policies.value = value;
            dataBase.PoliciesDao().update(policies);
        }

        // Return the priority value
        Policies policies = dataBase.PoliciesDao().getPolicyByName(policyName).get(0);
        return policies.value;
    }

    public boolean getBooleanValue(String policyName) {
        String value = getStringValue(policyName);
        if(value.equals("")) {
            value = "false";
        }
        return Boolean.valueOf(value);
    }

    public Object setBooleanValue(String policyName, Boolean enable, int priority) {
        Object data = setStringValue(policyName, String.valueOf(enable), priority);
        if(data.equals("")) {
            return null;
        }
        return data;
    }

    public int getIntValue(String policyName) {
        String value = getStringValue(policyName);
        if(value.equals("")) {
            value = "0";
        }
        return Integer.parseInt(value);
    }

    public Object setIntValue(String policyName, int value, int priority) {
        Object data = setStringValue(policyName, String.valueOf(value), priority);
        if(data.equals("")) {
            return null;
        }
        return data;
    }
}
