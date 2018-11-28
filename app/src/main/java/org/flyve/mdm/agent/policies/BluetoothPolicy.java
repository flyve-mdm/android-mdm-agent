package org.flyve.mdm.agent.policies;

import android.content.Context;

import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.policies.manager.CustomPolicies;

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
 * @author    rafael hernandez
 * @date      15/5/18
 * @copyright Copyright  2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

public class BluetoothPolicy extends BasePolicies {

    public static final String POLICY_NAME = "disableBluetooth";

    public BluetoothPolicy(Context context) {
        super(context, POLICY_NAME);
    }

    @Override
    protected boolean process() {
        try {
            boolean disable = Boolean.parseBoolean(this.policyValue.toString());

            CustomPolicies customPolicies = new CustomPolicies(context);
            customPolicies.disableBluetooth(disable);

            return true;
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", process", ex.getMessage());
            return false;
        }
    }
}