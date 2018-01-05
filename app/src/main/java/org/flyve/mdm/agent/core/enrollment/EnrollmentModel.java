package org.flyve.mdm.agent.core.enrollment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;

import org.flyve.inventory.InventoryTask;
import org.flyve.inventory.categories.Hardware;
import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.user.UserController;
import org.flyve.mdm.agent.core.user.UserModel;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.AndroidCryptoProvider;
import org.flyve.mdm.agent.utils.Helpers;
import org.json.JSONObject;

import java.io.File;
import java.net.URLEncoder;
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
public class EnrollmentModel implements Enrollment.Model {

    private Enrollment.Presenter presenter;

    public EnrollmentModel(Enrollment.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void createInventory(Context context) {
        InventoryTask inventoryTask = new InventoryTask(context, "FlyveMDM-Agent");
        inventoryTask.getJSON(new InventoryTask.OnTaskCompleted() {
            @Override
            public void onTaskSuccess(String s) {
                presenter.inventorySuccess(s);
            }

            @Override
            public void onTaskError(Throwable throwable) {
                presenter.showSnackError("Inventory fail");
            }
        });

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
            public void onError(String error) {
                presenter.showSnackError(error);
            }
        });
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
                    cameraIntent(activity, requestCamera);

                } else if (items[item].equals(activity.getResources().getString(R.string.choose_from_library))) {
                    galleryIntent(activity, requestFile);

                } else if (items[item].equals(activity.getResources().getString(R.string.cancel) )) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * If the user selects the image with the option from the gallery
     */
    private void galleryIntent(Activity activity, int requestFile) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        activity.startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(R.string.select_file) ),requestFile);
    }

    /**
     * If the user selects the image with the option take photo
     */
    private void cameraIntent(Activity activity, int requestCamera) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
            activity.startActivityForResult(takePictureIntent, requestCamera);
        }
    }

    /**
     * Get the URI of the image
     * @return the URI
     */
    private Uri getImageUri() {
        // Store image in dcim
        File filePhoto = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "flyveUser.jpg");
        return Uri.fromFile(filePhoto);
    }

    @Override
    public void enroll(final Context context, final List<UserModel.EmailsData> arrEmails, final String firstName, final String lastName, final String phone, final String phone2, final String mobilePhone, final String inventory, final String photo, final String language, final String administrativeNumber) {

        StringBuilder errMsg = new StringBuilder(context.getResources().getString(R.string.validate_error) );
        boolean allow = true;

        if(arrEmails.isEmpty() || arrEmails.get(0).getEmail().equals("")) {
            errMsg.append(context.getResources().getString(R.string.validate_email_at_least_one) );
            allow = false;
        }

        if(firstName.trim().equals("")) {
            errMsg.append(context.getResources().getString(R.string.validate_first_name) );
            allow = false;
        }

        if(lastName.trim().equals("")) {
            errMsg.append(context.getResources().getString(R.string.validate_last_name) );
            allow = false;
        }

        if(inventory.contains("fail")) {
            errMsg.append(context.getResources().getString(R.string.validate_inventory) );
            allow = false;
        }

        // inventory running
        if(inventory.equals("")) {
            errMsg.append(context.getResources().getString(R.string.validate_inventory_wait) );
            allow = false;
        }

        if(!allow) {
            presenter.showSnackError(context.getResources().getString(R.string.validate_check_details));
            presenter.showDetailError(errMsg.toString());
            return;
        }

        try {
            AndroidCryptoProvider csr = new AndroidCryptoProvider(context);
            String requestCSR = "";
            if( csr.getlCsr() != null ) {
                requestCSR = URLEncoder.encode(csr.getlCsr(), "UTF-8");
            }

            DataStorage cache = new DataStorage(context);
            String invitationToken = cache.getInvitationToken();

            JSONObject payload = new JSONObject();

            payload.put("_email", arrEmails.get(0).getEmail()); // get first email
            payload.put("_invitation_token", invitationToken);
            payload.put("_serial", Helpers.getDeviceSerial());
            payload.put("_uuid", new Hardware(context).getUUID());
            payload.put("csr", requestCSR);
            payload.put("firstname", firstName);
            payload.put("lastname", lastName);
            payload.put("phone", phone);
            payload.put("version", BuildConfig.VERSION_NAME);
            payload.put("type", "android");
            payload.put("has_system_permission", Helpers.isSystemApp(context));
            payload.put("inventory", inventory);

            EnrollmentHelper enroll = new EnrollmentHelper(context);
            enroll.enrollment(payload, new EnrollmentHelper.EnrollCallBack() {
                @Override
                public void onSuccess(String data) {

                    // -------------------------------
                    // Store user information
                    // -------------------------------
                    UserModel userModel = new UserModel();
                    userModel.setFirstName(firstName);
                    userModel.setLastName(lastName);
                    userModel.setEmails(arrEmails);
                    userModel.setPicture(photo);
                    userModel.setLanguage(language);
                    userModel.setAdministrativeNumber(administrativeNumber);

                    new UserController(context).save(userModel);

                    presenter.enrollSuccess();
                }

                @Override
                public void onError(String error) {
                    presenter.showSnackError(error);
                }
            });
        } catch (Exception ex) {
            presenter.showSnackError(ex.getMessage());
        }
    }
}
