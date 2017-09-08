/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * this file is part of flyve-mdm-android-agent
 *
 * flyve-mdm-android-agent is a subproject of Flyve MDM. Flyve MDM is a mobile
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
 * @date      02/06/2017
 * @copyright Copyright © 2017 Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.ui;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.flyvemdm.inventory.categories.Hardware;

import org.flyve.mdm.agent.BuildConfig;
import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.user.UserController;
import org.flyve.mdm.agent.core.user.UserModel;
import org.flyve.mdm.agent.data.DataStorage;
import org.flyve.mdm.agent.security.AndroidCryptoProvider;
import org.flyve.mdm.agent.utils.EnrollmentHelper;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.InputValidatorHelper;
import org.flyve.mdm.agent.utils.MultipleEditText;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static org.flyve.mdm.agent.R.string.email;


/**
 * Register the agent to the platform
 */
public class EnrollmentActivity extends AppCompatActivity {

    private ProgressBar pbx509;
    private DataStorage cache;
    private EnrollmentHelper enroll;
    private TextView txtMessage;
    private EditText editName;
    private EditText editLastName;
    private EditText editAdministrative;
    private MultipleEditText editEmail;
    private MultipleEditText editPhone;
    private Spinner spinnerLanguage;
    private ImageView btnRegister;
    private boolean sendEnrollment = false;
    private int REQUEST_CAMERA = 0, SELECT_FILE = 1;
    private String strPicture;
    private ImageView imgPhoto;
    private ProgressDialog pd;
    private File filePhoto;

    /**
     * Called when the activity is starting
     * It displays the UI to enroll the user information
     * @param Bundle if the activity is re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        android.support.v7.widget.Toolbar toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        // Request all the permissions that the library need
        int permissionAll = 1;
        String[] permissions = { Manifest.permission.READ_PHONE_STATE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA };

        if(!hasPermissions(this, permissions)){
            ActivityCompat.requestPermissions(this, permissions, permissionAll);
        }

        pbx509 = (ProgressBar) findViewById(R.id.progressBarX509);

        enroll = new EnrollmentHelper(EnrollmentActivity.this);
        cache = new DataStorage(EnrollmentActivity.this);

        imgPhoto = (ImageView) findViewById(R.id.imgPhoto);

        ImageView btnCamera = (ImageView) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        txtMessage = (TextView) findViewById(R.id.txtMessage);

        editName = (EditText) findViewById(R.id.editName);
        editLastName = (EditText) findViewById(R.id.editLastName);

        // Multiples Emails
        LinearLayout lnEmails = (LinearLayout) findViewById(R.id.lnEmails);
        editEmail = new MultipleEditText(this, lnEmails, getResources().getString(email));
        editEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editEmail.setSpinnerArray(R.array.email_array);
        lnEmails.addView( editEmail.createEditText() );

        // 3 Phones
        LinearLayout lnPhones = (LinearLayout) findViewById(R.id.lnPhones);
        editPhone = new MultipleEditText(this, lnPhones, getResources().getString(R.string.phone));
        editPhone.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        editPhone.setLimit(3);
        editPhone.setSpinnerArray(R.array.phone_array);
        lnPhones.addView( editPhone.createEditText() );

        // Language
        spinnerLanguage = (Spinner) findViewById(R.id.spinnerLanguage);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLanguage.setAdapter(adapter);

        editAdministrative = (EditText) findViewById(R.id.editAdministrative);
        editAdministrative.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editAdministrative.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    validateForm();
                    return true;
                }
                return false;
            }
        });

        btnRegister = (ImageView) findViewById(R.id.btnSave);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        // start creating a certificated
        pbx509.setVisibility(View.VISIBLE);
        enroll.createX509cert(new EnrollmentHelper.enrollCallBack() {
            @Override
            public void onSuccess(String data) {
                pbx509.setVisibility(View.GONE);
                if(sendEnrollment) {
                    pd.dismiss();
                    validateForm();
                }
            }

            @Override
            public void onError(String error) {
                pbx509.setVisibility(View.GONE);
                showError(getResources().getString(R.string.error_certified_x509));
            }
        });
    }

    /**
     * This function request the permission needed on Android 6.0 and above
     * @param context The context of the app
     * @param permissions The list of permissions needed
     * @return true or false
     */
    public static boolean hasPermissions(Context context, String... permissions) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * It displays the options to select the image of the user
     */
    private void selectImage() {
        final CharSequence[] items = {
                getResources().getString(R.string.take_photo),
                getResources().getString(R.string.choose_from_library),
                getResources().getString(R.string.cancel)
        };

        hideKeyboard();

        AlertDialog.Builder builder = new AlertDialog.Builder(EnrollmentActivity.this);
        builder.setTitle(getResources().getString(R.string.add_photo) );
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {

                if (items[item].equals(getResources().getString(R.string.take_photo))) {
                    cameraIntent();

                } else if (items[item].equals(getResources().getString(R.string.choose_from_library))) {
                    galleryIntent();

                } else if (items[item].equals(getResources().getString(R.string.cancel) )) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file) ),SELECT_FILE);
    }

    private void cameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
            startActivityForResult(takePictureIntent, REQUEST_CAMERA);
        }
    }

    public Uri getImageUri() {
        // Store image in dcim
        filePhoto = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "flyveUser.jpg");
        return Uri.fromFile(filePhoto);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA && filePhoto.exists()) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(filePhoto.getAbsolutePath(), options);
                try {
                    bitmap = Helpers.modifyOrientation(bitmap, filePhoto.getAbsolutePath());
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }

                strPicture = Helpers.BitmapToString(bitmap);
                imgPhoto.setImageBitmap(bitmap);
            }
        }
    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                FlyveLog.e(e.getMessage());
            }
        }

        strPicture = Helpers.BitmapToString(bm);
        imgPhoto.setImageBitmap(bm);
    }


    public void hideKeyboard() {
        // Hide keyboard
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * Send information to validateForm
     */
    private void validateForm() {
        StringBuilder errMsg = new StringBuilder(getResources().getString(R.string.validate_error) );
        txtMessage.setText("");

        // Hide keyboard
        hideKeyboard();

        // waiting for cert x509
        if(pbx509.getVisibility() == View.VISIBLE) {
            sendEnrollment = true;
            pd = ProgressDialog.show(EnrollmentActivity.this, "", getResources().getString(R.string.creating_certified_x509));
            return;
        }

        //Validate and Save
        boolean allowSave = true;

        String name = editName.getText().toString().trim();
        String lastName = editLastName.getText().toString().trim();

        // First name
        if (InputValidatorHelper.isNullOrEmpty(name)) {
            errMsg.append(getResources().getString(R.string.validate_first_name) );
            allowSave = false;
        }

        // Last name
        if (InputValidatorHelper.isNullOrEmpty(lastName)) {
            errMsg.append(getResources().getString(R.string.validate_last_name) );
            allowSave = false;
        }

        // Email
        if (editEmail.getEditList().isEmpty()) {
            errMsg.append(getResources().getString(R.string.validate_email_at_least_one) );
            allowSave = false;
        }

        if(allowSave){
            sendEnroll();
        } else {
            txtMessage.setText(errMsg);
        }
    }

    /**
     * Send information to validateForm the device
     */
    private void sendEnroll() {
        try {
            pd = ProgressDialog.show(EnrollmentActivity.this, "", getResources().getString(R.string.enrollment_in_process));

            AndroidCryptoProvider csr = new AndroidCryptoProvider(EnrollmentActivity.this.getBaseContext());
            String requestCSR = "";
            if( csr.getlCsr() != null ) {
                requestCSR = URLEncoder.encode(csr.getlCsr(), "UTF-8");
            }

            JSONObject payload = new JSONObject();

            payload.put("_email", editEmail.getEditList().get(0).getText().toString());
            payload.put("_invitation_token", cache.getInvitationToken());
            payload.put("_serial", Helpers.getDeviceSerial());
            payload.put("_uuid", new Hardware(EnrollmentActivity.this).getUUID());
            payload.put("csr", requestCSR);
            payload.put("firstname", editName.getText().toString());
            payload.put("lastname", editLastName.getText().toString());
            if(!editPhone.getEditList().isEmpty()) {
                payload.put("phone", editPhone.getEditList().get(0).getText().toString());
            }
            payload.put("version", BuildConfig.VERSION_NAME);

            enroll.enrollment(payload, new EnrollmentHelper.enrollCallBack() {
                @Override
                public void onSuccess(String data) {
                    pd.dismiss();

                    ArrayList<UserModel.EmailsData> arrEmails = new ArrayList<>();

                    List<EditText> emailEdit = editEmail.getEditList();
                    List<Spinner> emailTypeEdit = editEmail.getSpinnList();

                    for (int i=0; i<emailEdit.size(); i++) {
                        EditText editText = emailEdit.get(i);
                        Spinner spinner = emailTypeEdit.get(i);

                        if(!editText.getText().toString().equals("")) {
                            UserModel.EmailsData emails = new UserModel().new EmailsData();
                            emails.setEmail(editText.getText().toString());
                            emails.setType(spinner.getSelectedItem().toString());
                            arrEmails.add(emails);
                        }
                    }

                    // -------------------------------
                    // Store user information
                    // -------------------------------
                    UserModel userModel = new UserModel();
                    userModel.setFirstName(editName.getText().toString());
                    userModel.setLastName(editLastName.getText().toString());
                    userModel.setEmails(arrEmails);

                    // Mobile Phone
                    if(!editPhone.getEditList().isEmpty()) {
                        String mobilePhone = editPhone.getEditList().get(0).getText().toString();
                        if (!mobilePhone.equals("")) {
                            userModel.setMobilePhone(mobilePhone);
                        }
                    }

                    // Phone
                    if(editPhone.getEditList().size() > 1) {
                        String phone = editPhone.getEditList().get(1).getText().toString();
                        if (!phone.equals("")) {
                            userModel.setPhone(phone);
                        }
                    }

                    // Phone 2
                    if(editPhone.getEditList().size() > 2) {
                        String phone2 = editPhone.getEditList().get(2).getText().toString();
                        if (!phone2.equals("")) {
                            userModel.setPhone2(phone2);
                        }
                    }

                    userModel.setPicture(strPicture);
                    userModel.setLanguage( spinnerLanguage.getSelectedItem().toString() );
                    userModel.setAdministrativeNumber( editAdministrative.getText().toString() );

                    new UserController(EnrollmentActivity.this).save(userModel);

                    nextStep();
                }

                @Override
                public void onError(String error) {
                    pd.dismiss();
                    showError(error);
                }
            });
        } catch (Exception ex) {
            pd.dismiss();
            showError( ex.getMessage() );
            FlyveLog.e( ex.getMessage() );
        }
    }

    private void showError(String message) {
        Helpers.snack(this, message, this.getResources().getString(R.string.snackbar_close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    /**
     * Open the next activity
     */
    private void nextStep() {
        Intent intent = new Intent(EnrollmentActivity.this, DisclosureActivity.class);
        EnrollmentActivity.this.startActivity(intent);
        setResult(RESULT_OK, null);
        EnrollmentActivity.this.finish();
    }
}
