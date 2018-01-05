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
public class EnrollmentPresenter implements Enrollment.Presenter {

    private Enrollment.View view;
    private Enrollment.Model model;

    public EnrollmentPresenter(Enrollment.View view){
        this.view = view;
        model = new EnrollmentModel(this);
    }

    @Override
    public void showError(String message) {
        if(view!=null) {
            view.showError(message);
        }
    }

    @Override
    public void enrollSuccess() {
        if(view!=null) {
            view.enrollSuccess();
        }
    }

    @Override
    public void X509certificationSuccess() {
        if(view!=null) {
            view.X509certificationSuccess();
        }
    }

    @Override
    public void inventorySucess() {
        if(view!=null) {
            view.inventorySucess();
        }
    }

    @Override
    public void createInventory(Context context) {
        model.createInventory(context);
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
    public void enroll(Context context, List<UserModel.EmailsData> arrEmails, String firstName, String lastName, String phone, String phone2, String mobilePhone, String inventory, String photo, String language, String administrativeNumber) {
        model.enroll(context, arrEmails, firstName, lastName, phone, phone2, mobilePhone, inventory, photo, language, administrativeNumber);
    }
}
