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
import android.widget.EditText;
import android.widget.Spinner;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.data.UserData;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.MultipleEditText;

import java.util.ArrayList;
import java.util.List;

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
    public void save(Activity activity,
                     EditText editName,
                     EditText editLastName,
                     EditText editAdministrative,
                     Spinner spinnerLanguage,
                     MultipleEditText editEmail,
                     MultipleEditText editPhone,
                     String strPicture) {

        UserSchema userSchema = new UserSchema();

        // -------------
        // Emails
        // -------------
        ArrayList<UserData.EmailsData> arrEmails = new ArrayList<>();

        List<EditText> emailEdit = editEmail.getEditList();
        List<Spinner> emailTypeEdit = editEmail.getSpinnList();

        for (int i=0; i<emailEdit.size(); i++) {
            UserData.EmailsData emails = new UserData(activity).new EmailsData();
            EditText editText = emailEdit.get(i);
            Spinner spinner = emailTypeEdit.get(i);

            if(!editText.getText().toString().equals("")) {
                emails.setEmail(editText.getText().toString());
                emails.setType(spinner.getSelectedItem().toString());
                arrEmails.add(emails);
            }
        }

        userSchema.setEmails(arrEmails);
        userSchema.setFirstName(editName.getText().toString());
        userSchema.setLastName(editLastName.getText().toString());
        userSchema.setPicture(strPicture);
        userSchema.setLanguage(spinnerLanguage.getSelectedItem().toString());
        userSchema.setAdministrativeNumber(editAdministrative.getText().toString());

        // Mobile Phone
        if(!editPhone.getEditList().isEmpty()) {
            String mobilePhone = editPhone.getEditList().get(0).getText().toString();
            if (!mobilePhone.equals("")) {
                userSchema.setMobilePhone(mobilePhone);
            }
        }

        // Phone
        if(editPhone.getEditList().size() > 1) {
            String phone = editPhone.getEditList().get(1).getText().toString();
            if (!phone.equals("")) {
                userSchema.setPhone(phone);
            }
        }

        // Phone 2
        if(editPhone.getEditList().size() > 2) {
            String phone2 = editPhone.getEditList().get(2).getText().toString();
            if (!phone2.equals("")) {
                userSchema.setPhone2(phone2);
            }
        }

        StringBuilder errMsg = new StringBuilder(activity.getResources().getString(R.string.validate_error) );
        boolean allow = true;

        Helpers.hideKeyboard(activity);

        if(userSchema.getEmails().isEmpty() || userSchema.getEmails().get(0).getEmail().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_email_at_least_one) );
            allow = false;
        }

        if(userSchema.getFirstName().trim().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_first_name) );
            allow = false;
        }

        if(userSchema.getLastName().trim().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_last_name) );
            allow = false;
        }

        if(!allow) {
            presenter.showError(errMsg.toString());
            return;
        }

        // -------------
        // USER
        // -------------
        UserData user = new UserData(activity);

        user.setFirstName(userSchema.getFirstName());
        user.setLastName(userSchema.getLastName());
        user.setEmails(userSchema.getEmails());
        user.setPhone(userSchema.getPhone());
        user.setPhone2(userSchema.getPhone2());
        user.setMobilePhone(userSchema.getMobilePhone());
        user.setPicture(userSchema.getPicture());
        user.setLanguage(userSchema.getLanguage());
        user.setAdministrativeNumber(userSchema.getAdministrativeNumber());

        presenter.saveSuccess();
    }
}
