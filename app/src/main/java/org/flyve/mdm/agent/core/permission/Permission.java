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

package org.flyve.mdm.agent.core.permission;

import android.content.Context;

public interface Permission {

    interface View {
        void showSnackError(int type, String message);
        void inventorySuccess(String inventory);
    }

    interface Presenter {
        // Views
        void showSnackError(int type, String message);
        void inventorySuccess(String inventory);

        // Models
        void showDialogShare(final Context context);
        void generateInventory(final Context context);
    }

    interface Model {
        void showDialogShare(final Context context);
        void generateInventory(final Context context);
    }
}
