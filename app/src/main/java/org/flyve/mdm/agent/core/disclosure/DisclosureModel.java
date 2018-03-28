/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
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
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.core.disclosure;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Intent;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.receivers.FlyveAdminReceiver;
import org.flyve.mdm.agent.ui.MainActivity;
import org.flyve.mdm.agent.utils.Helpers;

public class DisclosureModel implements Disclosure.Model {
    private Disclosure.Presenter presenter;
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1;

    public DisclosureModel(Disclosure.Presenter presenter) {
        this.presenter = presenter;
    }

    public void requestDeviceAdmin(Activity activity) {
        // Device Admin
        ComponentName mDeviceAdmin = new ComponentName(activity, FlyveAdminReceiver.class);

        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "EXPLANATION");
        activity.startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
    }

    public void checkDeviceAdminResult(Activity activity, int requestCode, int resultCode) {
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN && resultCode==activity.RESULT_OK) {
            Helpers.openActivity(activity, MainActivity.class, true);
        } else {
            presenter.showError(activity.getResources().getString(R.string.disclosure_decline));
        }
    }
}
