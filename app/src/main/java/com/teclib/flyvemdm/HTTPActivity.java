/*
 * Copyright (C) 2016 Teclib'
 *
 * This file is part of Flyve MDM Android.
 *
 * Flyve MDM Android is a subproject of Flyve MDM. Flyve MDM is a mobile
 * device management software.
 *
 * Flyve MDM Android is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Dorian LARGET
 * @copyright Copyright (c) 2016 Flyve MDM
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyvemdm/flyvemdm-android
 * @link      http://www.glpi-project.org/
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.teclib.api.FlyveLog;
import com.teclib.api.HttpRequest;
import com.teclib.api.Network;
import com.teclib.database.SharedPreferenceMQTT;
import com.teclib.database.SharedPreferenceSettings;
import com.teclib.security.AndroidCryptoProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HTTPActivity extends Activity {

    private String link;
    private SharedPreferenceMQTT sharedPreferenceMQTT;
    private SharedPreferenceSettings sharedPreferenceSettings;
    Activity context = this;

    private String mServeur;
    private String mUserToken;
    private String mInvitationToken;
    private String mSerial;
    private String mName;
    private String mEmail;
    private String mSessionToken;

    private String mbroker;
    private String mport;
    private String mssl;
    private String mtopic;
    private String mpassword;
    private String mcert;

    private int mComputers_id;
    private int mId;
    private int mEntities_id;
    private int mFleet_id;
    private String mNameEmail;
    private AndroidCryptoProvider mCrypto;
    private String mPluginId;

    private RelativeLayout btn_nxt;
    private RelativeLayout connection_status;
    private RelativeLayout serial_status;
    private RelativeLayout finishBtn;
    private String Serial;
    private String previous;
    private String EnrolmentStatus = "";
    private Button btn_enrolment;
    private TextView errorMessage;
    private RelativeLayout mCertificate;
    private ProgressBar bar;
    private Button certificate_btn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);

        sharedPreferenceMQTT = new SharedPreferenceMQTT();
        sharedPreferenceSettings = new SharedPreferenceSettings();
        mCrypto = new AndroidCryptoProvider(context);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            link = extras.getString("link");
            previous = extras.getString("previous");
        }
        FlyveLog.d(link);

        try {
            JSONObject jsonObj = new JSONObject(link);
            if (jsonObj.has("url")) {
                Uri uri = Uri.parse(jsonObj.getString("url"));
                mServeur = jsonObj.getString("url");
                String protocol = uri.getScheme();
                String path = uri.getPath();

                FlyveLog.d("protocol = " + protocol);
                FlyveLog.d("server = " + mServeur);
                FlyveLog.d("path = " + path);

            }

            if (jsonObj.has("user_token")) {
                mUserToken = jsonObj.getString("user_token");
                mInvitationToken = jsonObj.getString("invitation_token");
                mSerial = jsonObj.getString("serial");
                mName = jsonObj.getString("name");
                mEmail = jsonObj.getString("email");
                FlyveLog.d("userToken: " + mUserToken);
                FlyveLog.d("invitationToken: " + mInvitationToken);
            }

        } catch (JSONException e) {
            FlyveLog.e("",e);
        }

        btn_nxt = (RelativeLayout) findViewById(R.id.imageButton2);
        btn_nxt.setVisibility(View.INVISIBLE);

        connection_status = (RelativeLayout) findViewById(R.id.connection_status);
        serial_status = (RelativeLayout) findViewById(R.id.serial_status);
        mCertificate = (RelativeLayout) findViewById(R.id.certificate_status);

        connection_status.setVisibility(View.INVISIBLE);
        serial_status.setVisibility(View.INVISIBLE);
        mCertificate.setVisibility(View.INVISIBLE);

        finishBtn = (RelativeLayout) findViewById(R.id.finishBtn);
        finishBtn.setVisibility(View.INVISIBLE);

        btn_enrolment = (Button) findViewById(R.id.enrolment);
        btn_enrolment.setVisibility(View.INVISIBLE);

        errorMessage = (TextView) findViewById(R.id.errorMessage);
        errorMessage.setVisibility(View.INVISIBLE);


        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        checkConnection();

        bar = (ProgressBar) this.findViewById(R.id.progressBar);
    }

    public void checkConnection() {

        TextView connection_status_text = (TextView) findViewById(R.id.connection_status_text);
        ImageView connection_status_image = (ImageView) findViewById(R.id.connection_status_image);

        TextView serial_status_text = (TextView) findViewById(R.id.serial_status_text);
        ImageView serial_status_image = (ImageView) findViewById(R.id.serial_status_image);

        TextView certificate_status_text = (TextView) findViewById(R.id.certificate_status_text);
        ImageView certificate_status_image = (ImageView) findViewById(R.id.certificate_status_image);

        Button retry_btn = (Button) findViewById(R.id.retry_btn);
        retry_btn.setVisibility(View.INVISIBLE);

        certificate_btn = (Button) findViewById(R.id.certificate_btn);
        certificate_btn.setVisibility(View.INVISIBLE);

        Network ac = new Network();

        int flag = 0;
        boolean isCertificate = true;
        File checkFile = new File(getFilesDir().getAbsolutePath() + File.separator + "client.key");

        if(!checkFile.exists()){
            isCertificate = false;
            mCertificate.setBackground(getResources().getDrawable(R.drawable.layout_bg_fail));
            certificate_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_48dp));
            certificate_status_text.setText(R.string.no_certificate);
            mCertificate.setVisibility(View.VISIBLE);
            certificate_btn.setVisibility(View.VISIBLE);
        }
        else {
            btn_enrolment.setVisibility(View.VISIBLE);
            mCertificate.setBackground(getResources().getDrawable(R.drawable.layout_bg_ok));
            certificate_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_48dp));
            certificate_status_text.setText(R.string.ok_certificate);
            certificate_btn.setVisibility(View.INVISIBLE);
        }
        if (ac.getInstance(this).isOnline()) {
            retry_btn.setVisibility(View.INVISIBLE);
            connection_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_ok));
            connection_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_48dp));
            connection_status_text.setText(R.string.connected);
            connection_status.setVisibility(View.VISIBLE);
        } else {
            flag += 1;
            connection_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_fail));
            connection_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_48dp));
            connection_status_text.setText(R.string.no_connection);
            connection_status.setVisibility(View.VISIBLE);
            retry_btn.setVisibility(View.VISIBLE);
        }
        Serial = ac.getSerial();

        if (!Serial.isEmpty()) {
            serial_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_ok));
            serial_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_done_white_48dp));
            serial_status_text.setText("SN: " + Serial);
        } else {
            flag += 1;
            serial_status.setBackground(getResources().getDrawable(R.drawable.layout_bg_fail));
            serial_status_image.setImageDrawable(getResources().getDrawable(R.drawable.ic_clear_white_48dp));
            serial_status_text.setText("NO SN");
            retry_btn.setVisibility(View.VISIBLE);
        }
        serial_status.setVisibility(View.VISIBLE);

        if ("ok".equals(EnrolmentStatus)) {
            btn_nxt.setVisibility(View.VISIBLE);
            btn_enrolment.setVisibility(View.INVISIBLE);
        }
        if (isCertificate) {
            certificate_btn.setVisibility(View.INVISIBLE);
        }
        if (flag != 0) {
            retry_btn.setVisibility(View.VISIBLE);
        }
    }

    public void retry(View v) {
        this.checkConnection();
    }

    public void btn_click(View v) {

        new HttpAsyncTask().execute(link);
        this.checkConnection();
    }

    public void generate(View v) {
        certificate_btn.setEnabled(false);
        certificate_btn.setText(R.string.wait_string);
        new CertificateAsyncTask().execute(link);
        this.checkConnection();
    }

    public void prev_Page(View v) {
        if ("DNSActivity".equals(previous)) {
            Intent previous = new Intent(HTTPActivity.this, DNSActivity.class);
            HTTPActivity.this.startActivity(previous);
            this.finish();
        } else if ("LoginActivity".equals(previous)) {
            Intent previous = new Intent(HTTPActivity.this, LoginActivity.class);
            previous.putExtra("link", link.split("&serial")[0]);
            HTTPActivity.this.startActivity(previous);
            this.finish();
        }

    }

    public void next_Page(View v) {
        Intent next = new Intent(HTTPActivity.this, MQTTNotifierActivity.class);
        HTTPActivity.this.startActivity(next);
        this.finish();
    }

    @Override
    public void onDestroy() {

        super.onDestroy();

        finish();
    }

    private class CertificateAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            AndroidCryptoProvider createCertif = new AndroidCryptoProvider(getBaseContext());
            createCertif.generateRequest();
            createCertif.loadCsr();
            return null;
        }

        protected void onPostExecute(String result) {
            bar.setVisibility(View.GONE);
            checkConnection();
        }
    }

    private class HttpAsyncTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            bar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpRequest request = new HttpRequest(context);
            try {

                //get
                String jsonGet = request.GetRequest(false,mServeur + "/" + "initSession?" + "user_token=" + mUserToken);
                FlyveLog.json(jsonGet);
                //post
                JSONObject jsonObj = new JSONObject(jsonGet);
                if (jsonObj.has("session_token")) {
                    mSessionToken = jsonObj.getString("session_token");
                }

                String jsonFullSession = request.GetRequest(false,mServeur + "/" + "getFullSession?" + "session_token=" + mSessionToken);
                FlyveLog.json(jsonFullSession);

                String pattern = "\"plugin_flyvemdm_guest_profiles_id\":\\s*([0-9]+)";

                // Create a Pattern object
                Pattern search = Pattern.compile(pattern);

                // Now create matcher object.
                Matcher m = search.matcher(jsonFullSession);
                if (m.find( )) {
                    mPluginId = m.group(1);
                }else {
                    FlyveLog.wtf("plugin_flyvemdm_guest_profiles_id");
                }

                request.GetRequest(false,mServeur + "/" + "changeActiveProfile?" + "session_token=" + mSessionToken + "&profiles_id=" + mPluginId);

                String Post = request.PostRequest(mServeur + "/" + "PluginFlyvemdmAgent?" + "session_token=" + mSessionToken, mEmail, mInvitationToken, mSerial, mName);
                FlyveLog.d(Post);
                if (isInteger(Post, 10)) {
                    String Get = request.GetRequest(false,mServeur + "/" + "PluginFlyvemdmAgent/" + Post + "?" + "session_token=" + mSessionToken);
                    FlyveLog.json(Get);
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(Get);
                        mbroker = jsonObject.getString("broker");
                        mport = jsonObject.getString("port");
                        mssl = jsonObject.getString("tls");
                        mtopic = jsonObject.getString("topic");
                        mpassword = jsonObject.getString("mqttpasswd");
                        mcert = jsonObject.getString("certificate");
                        mComputers_id = jsonObject.getInt("computers_id");
                        mId = jsonObject.getInt("id");
                        mEntities_id = jsonObject.getInt("entities_id");
                        mNameEmail = jsonObject.getString("name");
                        mFleet_id = jsonObject.getInt("plugin_flyvemdm_fleets_id");
                    } catch (JSONException e) {
                        //TODO ne pas continuer l'enrollement
                        FlyveLog.e(e.getMessage());
                        e.printStackTrace();
                        return "fail";
                    }

                    if(mServeur.contains("flyve.org")){
                        FlyveLog.d("find demo url");
                        String entity = mServeur.split("//")[1].split("\\.")[0];
                        FlyveLog.d(entity);

                        //String entity = apiUrl.split("//")[1].split(".")[0];
                        //FlyveLog.d(entity);

                        SharedPreferences acra = PreferenceManager.getDefaultSharedPreferences(context);
                        SharedPreferences.Editor editor = acra.edit();
                        editor.putString("server", entity);
                        editor.putString("login", entity);
                        editor.putString("server", entity);
                        editor.commit();
                    }

                    MainApplication.getInstance().acraInit();

                    // sauvegarde des infos
                    sharedPreferenceMQTT.savePort(context, mport);
                    sharedPreferenceMQTT.saveAdress(context, mbroker);
                    sharedPreferenceMQTT.saveTLS(context, mssl);
                    sharedPreferenceMQTT.savePassword(context, mpassword);
                    sharedPreferenceMQTT.saveSerialTopic(context, mtopic);

                    sharedPreferenceSettings.saveComputersId(context, mComputers_id);
                    sharedPreferenceSettings.saveId(context, mId);
                    sharedPreferenceSettings.saveEntitiesId(context,mEntities_id);
                    sharedPreferenceSettings.saveFleetId(context,mFleet_id);
                    sharedPreferenceSettings.saveNameID(context,mNameEmail);
                    sharedPreferenceSettings.saveUserToken(context, mUserToken);
                    sharedPreferenceSettings.saveApiServer(context, mServeur);
                    if (!mcert.equals("")) {
                        mCrypto.saveCertKey(mcert);
                    }
                    return "ok";
                } else {
                    return Post;
                }

            } catch (Exception e) {
                FlyveLog.e(e.getMessage());
                e.printStackTrace();
                return "fail";
            }

        }

        // onPostExecute displays the results of the AsyncTask.
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        protected void onPostExecute(String result) {

            if (result.equals("ok")) {
                EnrolmentStatus = "ok";
                Toast.makeText(getBaseContext(), "Successful", Toast.LENGTH_LONG).show();
            } else {
                EnrolmentStatus = result;
                errorMessage.setText(EnrolmentStatus);
                errorMessage.setVisibility(View.VISIBLE);
                btn_enrolment.setVisibility(View.INVISIBLE);
                finishBtn.setVisibility(View.VISIBLE);
                Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_LONG).show();
            }
            bar.setVisibility(View.GONE);
            checkConnection();
        }
    }

    public static boolean isInteger(String s, int radix) {
        if (s.isEmpty()) return false;
        for (int i = 0; i < s.length(); i++) {
            if (i == 0 && s.charAt(i) == '-') {
                if (s.length() == 1) return false;
                else continue;
            }
            if (Character.digit(s.charAt(i), radix) < 0) return false;
        }
        return true;
    }


    public void finish(View v) {
        this.finish();
    }
}
