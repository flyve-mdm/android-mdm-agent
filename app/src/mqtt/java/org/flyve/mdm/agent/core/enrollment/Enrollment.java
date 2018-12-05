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

package org.flyve.mdm.agent.core.enrollment;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;

import org.flyve.mdm.agent.data.localstorage.UserData;

import java.util.List;

public interface Enrollment {

    interface View {
        void showDetailError(int type, String message);
        void showSnackError(int type, String message);
        void enrollSuccess();
        void certificationX509Success();
    }

    interface Presenter {
        // Views
        void showDetailError(int type, String message);
        void showSnackError(int type, String message);
        void enrollSuccess();
        void certificationX509Success();

        // Models
        Uri getPhoto();
        void createX509certification(Context context);
        void selectPhoto(final Activity activity, final int requestCamera, final int requestFile);
        void enroll(final Activity activity, final List<UserData.EmailsData> arrEmails, final String firstName, final String lastName, final String phone, final String phone2, final String mobilePhone, final String inventory, final String photo, final String language, final String administrativeNumber);
    }

    interface Model {
        Uri getPhoto();
        void createX509certification(Context context);
        void selectPhoto(final Activity activity, final int requestCamera, final int requestFile);
        void enroll(final Activity activity, final List<UserData.EmailsData> arrEmails, final String firstName, final String lastName, final String phone, final String phone2, final String mobilePhone, final String inventory, final String photo, final String language, final String administrativeNumber);
    }
}
