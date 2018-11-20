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
import android.widget.Spinner;
import android.widget.TextView;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.core.user.User;
import org.flyve.mdm.agent.core.user.UserPresenter;
import org.flyve.mdm.agent.core.user.UserSchema;
import org.flyve.mdm.agent.data.localstorage.UserData;
import org.flyve.mdm.agent.utils.FlyveLog;
import org.flyve.mdm.agent.utils.Helpers;
import org.flyve.mdm.agent.utils.MultipleEditText;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditUserActivity extends AppCompatActivity implements User.View {

    private static final int REQUEST_CAMERA = 0;
    private static final int SELECT_FILE = 1;

    private User.Presenter presenter;
    private TextView txtMessage;
    private EditText editName;
    private EditText editLastName;
    private EditText editAdministrative;
    private ImageView imgPhoto;
    private MultipleEditText editEmail;
    private MultipleEditText editPhone;
    private Spinner spinnerLanguage;
    private String strPicture;
    private String photoPath = "";

    /**
     * Called when the activity is starting
     * It display the UI to edit the user information
     * @param savedInstanceState if the activity is re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_form);

        presenter = new UserPresenter(this);

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

        TextView txtTitle = findViewById(R.id.txtTitle);
        txtTitle.setVisibility(View.GONE);

        imgPhoto = findViewById(R.id.imgPhoto);

        editName = findViewById(R.id.editName);

        editLastName = findViewById(R.id.editLastName);

        spinnerLanguage = findViewById(R.id.spinnerLanguage);

        editAdministrative = findViewById(R.id.editAdministrative);
        editAdministrative.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editAdministrative.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save();
                    return true;
                }
                return false;
            }
        });

        txtMessage = findViewById(R.id.txtMessage);

        // Button Camera
        ImageView btnCamera = findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.selectPhoto(EditUserActivity.this,REQUEST_CAMERA, SELECT_FILE);
            }
        });

        // Button Register
        ImageView btnRegister = findViewById(R.id.btnSave);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                save();
            }
        });

        presenter.load(EditUserActivity.this);
    }

    /**
     * Storage information
     */
    private void save() {
        UserSchema userSchema = new UserSchema();

        // -------------
        // Emails
        // -------------
        ArrayList<UserData.EmailsData> arrEmails = new ArrayList<>();

        List<EditText> emailEdit = editEmail.getEditList();
        List<Spinner> emailTypeEdit = editEmail.getSpinnList();

        if(!emailEdit.isEmpty()) {
            for (int i = 0; i < emailEdit.size(); i++) {
                UserData.EmailsData emails = new UserData(EditUserActivity.this).new EmailsData();
                EditText editText = emailEdit.get(i);
                Spinner spinner = emailTypeEdit.get(i);

                if (!editText.getText().toString().equals("")) {
                    emails.setEmail(editText.getText().toString());
                    emails.setType(spinner.getSelectedItem().toString());
                    arrEmails.add(emails);
                }
            }
        }

        if(!arrEmails.isEmpty()) {
            userSchema.setEmails(arrEmails);
        }

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

        presenter.save(EditUserActivity.this, userSchema);
    }

    /**
     * Called when a launched activity exits
     * It processes the information from galleryIntent and cameraIntent
     * @param requestCode the request code originally supplied, allowing to identify who this result came from
     * @param resultCode the integer result code returned
     * @param data an Intent, which can return result data to the caller
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(photoPath, options);
                try {
                    bitmap = Helpers.modifyOrientation(bitmap, photoPath);
                    strPicture = Helpers.bitmapToString(bitmap);
                    imgPhoto.setImageBitmap(bitmap);
                } catch (Exception ex) {
                    FlyveLog.e(this.getClass().getName() + ", onActivityResult", ex.getMessage());
                }
            }
        }
    }

    /**
     * Retrieves the selected image from the gallery
     * @param data of the image
     */
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());
                strPicture = Helpers.bitmapToString(bm);
                imgPhoto.setImageBitmap(bm);
            } catch (IOException e) {
                FlyveLog.e(this.getClass().getName() + ", onSelectFromGalleryResult", e.getMessage());
            }
        }
    }

    @Override
    public void loadSuccess(UserSchema userSchema) {

        // photo on the header
        if(!userSchema.getPicture().equals("")) {
            try {
                imgPhoto.setImageBitmap(Helpers.stringToBitmap(userSchema.getPicture()));
            } catch (Exception ex) {
                FlyveLog.e(this.getClass().getName() + ", loadSuccess", ex.getMessage());
            }
        }

        // first name
        editName.setText(userSchema.getFirstName());

        // last name
        editLastName.setText(userSchema.getLastName());

        // Multiples Emails
        LinearLayout lnEmails = findViewById(R.id.lnEmails);
        editEmail = new MultipleEditText(this, lnEmails, getResources().getString(R.string.email));
        editEmail.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        editEmail.setSpinnerArray(R.array.email_array);

        // load store values
        List<String> arrEmails = new ArrayList<>();
        List<String> arrEmailTypes = new ArrayList<>();

        for(int i = 0; i < userSchema.getEmails().size(); i++) {
            arrEmails.add( userSchema.getEmails().get(i).getEmail() );
            arrEmailTypes.add( userSchema.getEmails().get(i).getType() );
        }

        editEmail.setValue( arrEmails, arrEmailTypes );
        lnEmails.addView( editEmail.createEditText() );

        // load information phones
        LinearLayout lnPhones = findViewById(R.id.lnPhones);
        editPhone = new MultipleEditText(this, lnPhones, getResources().getString(R.string.phone));
        editPhone.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_CLASS_PHONE);
        editPhone.setLimit(3);
        editPhone.setSpinnerArray(R.array.phone_array);

        // load store values
        List<String> arrPhones = new ArrayList<>();
        List<String> arrPhoneTypes = new ArrayList<>();

        if(!userSchema.getMobilePhone().equals("")) {
            arrPhones.add(userSchema.getMobilePhone());
            arrPhoneTypes.add("");
        }

        if(!userSchema.getPhone().equals("")) {
            arrPhones.add(userSchema.getPhone());
            arrPhoneTypes.add("");
        }

        if(!userSchema.getPhone2().equals("")) {
            arrPhones.add(userSchema.getPhone2());
            arrPhoneTypes.add("");
        }

        editPhone.setValue( arrPhones, arrPhoneTypes );
        lnPhones.addView( editPhone.createEditText() );

        // Language
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.language_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinnerLanguage.setAdapter(adapter);

        // select language stored on cache
        int spinnerPosition = adapter.getPosition(userSchema.getLanguage());
        spinnerLanguage.setSelection(spinnerPosition);

        // Administrative Number
        editAdministrative.setText(userSchema.getAdministrativeNumber());
    }

    @Override
    public void saveSuccess() {
        Helpers.snack(this, getResources().getString(R.string.saved), this.getResources().getString(R.string.snackbar_close), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public void showDetailError(int type, String message) {
        txtMessage.setText(getResources().getString(R.string.error_message_with_number, String.valueOf(type), message));
    }
}
