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

package org.flyve.mdm.agent.core.user;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.data.localstorage.UserData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;

public class UserModel implements User.Model {

    private User.Presenter presenter;

    public UserModel(User.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void load(Context context) {
        UserSchema userSchema = new UserSchema();

        UserData user = new UserData(context);
        userSchema.setEmails(user.getEmails());
        userSchema.setFirstName(user.getFirstName());
        userSchema.setLastName(user.getLastName());
        userSchema.setPhone(user.getPhone());
        userSchema.setPhone2(user.getPhone2());
        userSchema.setMobilePhone(user.getMobilePhone());
        userSchema.setLanguage(user.getLanguage());
        userSchema.setPicture(user.getPicture());
        userSchema.setAdministrativeNumber(user.getAdministrativeNumber());

        presenter.loadSuccess(userSchema);
    }

    @Override
    public void selectPhoto(final Activity activity, final int requestCamera, final int requestFile) {
        final CharSequence[] items = {
                activity.getResources().getString(R.string.take_photo),
                activity.getResources().getString(R.string.choose_from_library),
                activity.getResources().getString(R.string.cancel)
        };

        Helpers.hideKeyboard(activity);

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(activity.getResources().getString(R.string.add_photo) );
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(activity.getResources().getString(R.string.take_photo))) {
                    Helpers.cameraIntent(activity, requestCamera);

                } else if (items[item].equals(activity.getResources().getString(R.string.choose_from_library))) {
                    Helpers.galleryIntent(activity, requestFile);

                } else if (items[item].equals(activity.getResources().getString(R.string.cancel) )) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void save(Activity activity, UserSchema schema) {

        StringBuilder errMsg = new StringBuilder(activity.getResources().getString(R.string.validate_error) );
        boolean allow = true;

        Helpers.hideKeyboard(activity);

        if(schema.getEmails().isEmpty() || schema.getEmails().get(0).getEmail().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_email_at_least_one) );
            allow = false;
        }

        if(schema.getFirstName().trim().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_first_name) );
            allow = false;
        }

        if(schema.getLastName().trim().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_last_name) );
            allow = false;
        }

        if(!allow) {
            presenter.showDetailError(CommonErrorType.USER_SAVE_VALIDATION, errMsg.toString());
            return;
        }

        // -------------
        // USER
        // -------------
        try {
            UserData user = new UserData(activity);

            user.setFirstName(schema.getFirstName());
            user.setLastName(schema.getLastName());
            user.setEmails(schema.getEmails());
            user.setPhone(schema.getPhone());
            user.setPhone2(schema.getPhone2());
            user.setMobilePhone(schema.getMobilePhone());
            user.setPicture(schema.getPicture());
            user.setLanguage(schema.getLanguage());
            user.setAdministrativeNumber(schema.getAdministrativeNumber());

            presenter.saveSuccess();
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", save", ex.getMessage());
            presenter.showDetailError(CommonErrorType.USER_SAVE_EXCEPTION, ex.getMessage());
        }
    }
}
