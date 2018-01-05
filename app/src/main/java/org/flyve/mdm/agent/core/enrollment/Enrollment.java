package org.flyve.mdm.agent.core.enrollment;

import android.app.Activity;
import android.content.Context;

import org.flyve.mdm.agent.core.user.UserModel;

import java.util.List;

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
 * @date      4/1/18
 * @copyright Copyright © 2018 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */
public interface Enrollment {

    interface View {
        void showError(String message);
        void enrollSuccess();
        void certificationX509Success();
        void inventorySuccess(String inventory);
    }

    interface Presenter {
        // Views
        void showError(String message);
        void enrollSuccess();
        void certificationX509Success();
        void inventorySuccess(String inventory);

        // Models
        void createInventory(Context context);
        void createX509certification(Context context);
        void selectPhoto(final Activity activity, final int requestCamera, final int requestFile);
        void enroll(final Context context, final List<UserModel.EmailsData> arrEmails, final String firstName, final String lastName, final String phone, final String phone2, final String mobilePhone, final String inventory, final String photo, final String language, final String administrativeNumber);
    }

    interface Model {
        void createInventory(Context context);
        void createX509certification(Context context);
        void selectPhoto(final Activity activity, final int requestCamera, final int requestFile);
        void enroll(final Context context, final List<UserModel.EmailsData> arrEmails, final String firstName, final String lastName, final String phone, final String phone2, final String mobilePhone, final String inventory, final String photo, final String language, final String administrativeNumber);
    }
}
