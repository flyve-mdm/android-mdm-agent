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
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;

import org.flyve.inventory.categories.Hardware;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.CommonErrorType;
import org.flyve.mdm.agent.data.database.MqttData;
import org.flyve.mdm.agent.data.localstorage.UserData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

import java.util.List;

public class EnrollmentModel implements Enrollment.Model {

    private Enrollment.Presenter presenter;
    private Uri photo;

    public EnrollmentModel(Enrollment.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void createX509certification(Context context) {
        EnrollmentHelper enroll = new EnrollmentHelper(context);
        enroll.createX509cert(new EnrollmentHelper.EnrollCallBack() {
            @Override
            public void onSuccess(String data) {
                presenter.certificationX509Success();
            }

            @Override
            public void onError(int type, String error) {
                presenter.showSnackError(type, error);
            }
        });
    }

    @Override
    public Uri getPhoto() {
        return photo;
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
                    photo = Helpers.cameraIntent(activity, requestCamera);
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
    public void enroll(final Activity activity, final List<UserData.EmailsData> arrEmails, final String firstName, final String lastName, final String phone, final String phone2, final String mobilePhone, final String inventory, final String photo, final String language, final String administrativeNumber, final String notificationToken, Context context) {

        StringBuilder errMsg = new StringBuilder(activity.getResources().getString(R.string.validate_error) );
        boolean allow = true;

        Helpers.hideKeyboard(activity);

        if(arrEmails.isEmpty() || arrEmails.get(0).getEmail().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_email_at_least_one) );
            allow = false;
        }

        if(firstName.trim().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_first_name) );
            allow = false;
        }

        if(lastName.trim().equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_last_name) );
            allow = false;
        }

        if(notificationToken.equals("")) {
            errMsg.append(activity.getResources().getString(R.string.validate_fcm_token) );
            allow = false;
        }

        if(inventory.contains("fail")) {
            errMsg.append(activity.getResources().getString(R.string.validate_inventory) );
            allow = false;
        }

        // inventory running
        if(inventory.equals("")) {
            errMsg.append(activity.getResources().getString(R.string.inventory_not_exists) );
            allow = false;
        }

        if(!allow) {
            presenter.showSnackError(CommonErrorType.ENROLLMENT_FIELD_VALIDATION, activity.getResources().getString(R.string.validate_check_details));
            presenter.showDetailError(CommonErrorType.ENROLLMENT_FIELD_VALIDATION, errMsg.toString());
            return;
        }

        try {
            MqttData cache = new MqttData(activity);
            String invitationToken = cache.getInvitationToken();

            JSONObject payload = new JSONObject();

            String mInventory = Helpers.base64encode( inventory.trim() );

            payload.put("_email", arrEmails.get(0).getEmail()); // get first email
            payload.put("_invitation_token", invitationToken);
            payload.put("_serial", Helpers.getDeviceSerial());
            payload.put("_uuid", new Hardware(activity).getUUID());
            payload.put("csr", "");
            payload.put("firstname", firstName);
            payload.put("lastname", lastName);
            payload.put("phone", phone);
            payload.put("version", BuildConfig.VERSION_NAME);
            payload.put("type", "android");
            payload.put("has_system_permission", Helpers.isSystemApp(activity));
            payload.put("inventory", mInventory);
            // could be mqtt or fcm
            payload.put("notification_type", "fcm");
            // this is the token get from fcm register
            payload.put("notification_token", notificationToken);

            FlyveLog.d(mInventory);

            EnrollmentHelper enroll = new EnrollmentHelper(activity);
            enroll.enrollment(payload, new EnrollmentHelper.EnrollCallBack() {
                @Override
                public void onSuccess(String data) {

                    // -------------------------------
                    // Store user information
                    // -------------------------------
                    UserData userData = new UserData(activity);
                    userData.setFirstName(firstName);
                    userData.setLastName(lastName);
                    userData.setEmails(arrEmails);
                    userData.setPicture(photo);
                    userData.setLanguage(language);
                    userData.setAdministrativeNumber(administrativeNumber);

                    // -------------------------------
                    // Remove Deeplink information
                    // -------------------------------
                    MqttData cache = new MqttData(activity);
                    cache.setInvitationToken("");

                    presenter.enrollSuccess();
                }

                @Override
                public void onError(int type, String error) {
                    presenter.showSnackError(type, error);
                }
            });
        } catch (Exception ex) {
            presenter.showSnackError(CommonErrorType.ENROLLMENT_REQUEST_EXCEPTION, ex.getMessage());
        }
    }
}

