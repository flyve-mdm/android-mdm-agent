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

package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.enrollment.Enrollment;
import org.flyve.mdm.agent.core.enrollment.EnrollmentPresenter;
import org.flyve.mdm.agent.data.localstorage.UserData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.MultipleEditText;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.flyve.mdm.agent.R.string.email;

/**
 * Register the agent to the platform
 */
public class EnrollmentActivity extends AppCompatActivity implements Enrollment.View {

    private static final int REQUEST_CAMERA = 0;
    private static final int SELECT_FILE = 1;

    private ProgressBar pbx509;
    private TextView txtMessage;
    private EditText editName;
    private EditText editLastName;
    private EditText editAdministrative;
    private MultipleEditText editEmail;
    private MultipleEditText editPhone;
    private Spinner spinnerLanguage;
    private boolean sendEnrollment = false;
    private String strPicture;
    private ImageView imgPhoto;
    private ProgressDialog pd;
    private File filePhoto;
    public static String inventory = "";
    private Enrollment.Presenter presenter;

    /**
     * Called when the activity is starting
     * It displays the UI to enroll the user information
     * @param savedInstanceState if the activity is re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            try {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", onCreate", ex.getMessage());
            }

            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        }

        TextView txtVersion = findViewById(R.id.txtVersion);
        txtVersion.setText(MDMAgent.getCompleteVersion());

        presenter = new EnrollmentPresenter(EnrollmentActivity.this);

        pbx509 = findViewById(R.id.progressBarX509);

        imgPhoto = findViewById(R.id.imgPhoto);

        ImageView btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.selectPhoto(EnrollmentActivity.this, REQUEST_CAMERA, SELECT_FILE);
            }
        });

        txtMessage = findViewById(R.id.txtMessage);

        editName = findViewById(R.id.editName);
        editLastName = findViewById(R.id.editLastName);

        // Multiples Emails
        LinearLayout lnEmails = findViewById(R.id.lnEmails);
        editEmail = new MultipleEditText(this, lnEmails, getResources().getString(email));
        editEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editEmail.setSpinnerArray(R.array.email_array);
        lnEmails.addView( editEmail.createEditText() );

        // 3 Phones
        LinearLayout lnPhones = findViewById(R.id.lnPhones);
        editPhone = new MultipleEditText(this, lnPhones, getResources().getString(R.string.phone));
        editPhone.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        editPhone.setLimit(3);
        editPhone.setSpinnerArray(R.array.phone_array);
        lnPhones.addView( editPhone.createEditText() );

        // Language
        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLanguage.setAdapter(adapter);

        editAdministrative = findViewById(R.id.editAdministrative);
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

        ImageView btnRegister = findViewById(R.id.btnSave);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateForm();
            }
        });

        // start creating a certificated
        pbx509.setVisibility(View.VISIBLE);
        presenter.createX509certification(EnrollmentActivity.this);
    }

    /**
     * Called when a launched activity exits
     * It proccesses the information from galleryIntent and cameraIntent
     * @param requestCode the request code originally supplied, allowing to identify who this result came from
     * @param resultCode the integer result code returned
     * @param data an Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            filePhoto = new File(presenter.getPhoto().getPath());
        } catch (Exception ex) {
            FlyveLog.e(this.getClass().getName() + ", onActivityResult", ex.getMessage());
            return;
        }

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
                    FlyveLog.e(this.getClass().getName() + ", onActivityResult", ex.getMessage());
                }

                strPicture = Helpers.bitmapToString(bitmap);
                imgPhoto.setImageBitmap(bitmap);
            }
        }
    }

    /**
     * Retrieves the selected image from the gallery
     * @param data of the image
     * @throws IOException error message
     */
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm=null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                FlyveLog.e(this.getClass().getName() + ", onSelectFromGalleryResult", e.getMessage());
            }
        }

        strPicture = Helpers.bitmapToString(bm);
        imgPhoto.setImageBitmap(bm);
    }

    /**
     * Send information to validateForm
     */
    private void validateForm() {

        // waiting for cert x509
        if(pbx509.getVisibility() == View.VISIBLE) {
            sendEnrollment = true;
            pd = ProgressDialog.show(EnrollmentActivity.this, "", getResources().getString(R.string.creating_certified_x509));
            return;
        }

        pd = ProgressDialog.show(EnrollmentActivity.this, "", getResources().getString(R.string.enrollment_in_process));

        // Get all emails
        ArrayList<UserData.EmailsData> arrEmails = new ArrayList<>();

        List<EditText> emailEdit = editEmail.getEditList();
        List<Spinner> emailTypeEdit = editEmail.getSpinnList();

        for (int i=0; i<emailEdit.size(); i++) {
            EditText editText = emailEdit.get(i);
            Spinner spinner = emailTypeEdit.get(i);

            if(!editText.getText().toString().equals("")) {
                UserData.EmailsData emails = new UserData(EnrollmentActivity.this).new EmailsData();
                emails.setEmail(editText.getText().toString());
                emails.setType(spinner.getSelectedItem().toString());
                arrEmails.add(emails);
            }
        }

        String mobilePhone = "";
        String phone = "";
        String phone2 = "";

        // Mobile Phone
        if(!editPhone.getEditList().isEmpty()) {
            String mMobilePhone = editPhone.getEditList().get(0).getText().toString();
            if (!mMobilePhone.equals("")) {
                mobilePhone = mMobilePhone;
            }
        }

        // Phone
        if(editPhone.getEditList().size() > 1) {
            String mPhone = editPhone.getEditList().get(1).getText().toString();
            if (!phone.equals("")) {
                phone = mPhone;
            }
        }

        // Phone 2
        if(editPhone.getEditList().size() > 2) {
            String mPhone2 = editPhone.getEditList().get(2).getText().toString();
            if (!mPhone2.equals("")) {
                phone2 = mPhone2;
            }
        }

        // Enroll the user
        presenter.enroll(EnrollmentActivity.this,
                arrEmails,
                editName.getText().toString(),
                editLastName.getText().toString(),
                phone,
                phone2,
                mobilePhone,
                inventory,
                strPicture,
                spinnerLanguage.getSelectedItem().toString(),
                editAdministrative.getText().toString()
        );
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

    @Override
    public void showDetailError(int type, String message) {
        pd.dismiss();
        pbx509.setVisibility(View.GONE);

        txtMessage.setText(getResources().getString(R.string.error_message_with_number, String.valueOf(type), message));
    }

    @Override
    public void showSnackError(int type, String message) {
        if(pd!=null) {
            pd.dismiss();
        }

        pbx509.setVisibility(View.GONE);

        Helpers.snack(this, getResources().getString(R.string.error_message_with_number, String.valueOf(type), message), this.getResources().getString(R.string.snackbar_close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void enrollSuccess() {
        if(pd!=null) {
            pd.dismiss();
        }

        nextStep();
    }

    @Override
    public void certificationX509Success() {
        pbx509.setVisibility(View.GONE);
        if(sendEnrollment) {
            pd.dismiss();
            validateForm();
        }
    }
}
