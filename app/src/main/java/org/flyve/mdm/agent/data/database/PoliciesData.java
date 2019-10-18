package org.flyve.mdm.agent.data.database;

/*
 *   Copyright  2018 Teclib. All rights reserved.
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
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

import android.content.Context;

import org.flyve.mdm.agent.data.database.setup.AppDataBase;
import org.flyve.mdm.agent.data.database.entity.Policies;

import java.util.List;

public class PoliciesData {

    public AppDataBase dataBase;

    public PoliciesData(Context context) {
        dataBase = AppDataBase.getAppDatabase(context);
    }

    public List<Policies> getAllPolicies() {
        return dataBase.PoliciesDao().loadAll();
    }

    public Policies getValue(String policyName) {
        List<Policies> arrPolicies = dataBase.PoliciesDao().getPolicyByName(policyName);
        if(!arrPolicies.isEmpty()) {
            return arrPolicies.get(0);
        } else {
            return new Policies();
        }
    }

    public Policies getByTaskId(String taskId) {
        List<Policies> arrPolicies = dataBase.PoliciesDao().getPolicyByTaskId(taskId);
        if(!arrPolicies.isEmpty()) {
            return arrPolicies.get(0);
        } else {
            return new Policies();
        }
    }

    public Object setValue(String policyName, String taskId, String value, int priority) {
        if(dataBase.PoliciesDao().getPolicyByTaskId(taskId).isEmpty()) {
            Policies policies = new Policies();
            policies.policyName = policyName;
            policies.taskId = taskId;
            policies.value = value;
            policies.priority = priority;
            dataBase.PoliciesDao().insert(policies);
        } else {
            Policies policies = dataBase.PoliciesDao().getPolicyByTaskId(taskId).get(0);
            policies.value = value;
            dataBase.PoliciesDao().update(policies);
        }

        // Return the priority value
        Policies policies = dataBase.PoliciesDao().getPolicyByTaskId(taskId).get(0);
        return policies.value;
    }

    public void removeValue(String taskId) {
        Policies policies = dataBase.PoliciesDao().getPolicyByTaskId(taskId).get(0);
        dataBase.PoliciesDao().delete(policies);
    }

    public void deleteAll() {
        dataBase.PoliciesDao().deleteAll();
    }
}
