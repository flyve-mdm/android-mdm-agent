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

public class EnrollmentPresenter implements Enrollment.Presenter {

    private Enrollment.View view;
    private Enrollment.Model model;

    public EnrollmentPresenter(Enrollment.View view){
        this.view = view;
        model = new EnrollmentModel(this);
    }

    @Override
    public void showDetailError(int type, String message) {
        if(view!=null) {
            view.showDetailError(type, message);
        }
    }

    @Override
    public void showSnackError(int type, String message) {
        if(view!=null) {
            view.showSnackError(type, message);
        }
    }

    @Override
    public void enrollSuccess() {
        if(view!=null) {
            view.enrollSuccess();
        }
    }

    @Override
    public void certificationX509Success() {
        if(view!=null) {
            view.certificationX509Success();
        }
    }

    @Override
    public Uri getPhoto() {
        return model.getPhoto();
    }

    @Override
    public void createX509certification(Context context) {
        model.createX509certification(context);
    }

    @Override
    public void selectPhoto(Activity activity, int requestCamera, int requestFile) {
        model.selectPhoto(activity, requestCamera, requestFile);
    }

    @Override
    public void enroll(Activity activity, List<UserData.EmailsData> arrEmails, String firstName, String lastName, String phone, String phone2, String mobilePhone, String inventory, String photo, String language, String administrativeNumber, String notificationToken, Context context) {
        model.enroll(activity, arrEmails, firstName, lastName, phone, phone2, mobilePhone, inventory, photo, language, administrativeNumber, notificationToken, context);
    }
}
