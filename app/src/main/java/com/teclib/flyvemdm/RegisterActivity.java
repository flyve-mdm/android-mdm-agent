/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 *   com.teclib.data is part of flyve-mdm-android
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
 * @date      02/06/2017
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package com.teclib.flyvemdm;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teclib.data.DataStorage;
import com.teclib.utils.ConnectionHTTP;
import com.teclib.utils.FlyveLog;
import com.teclib.utils.Helpers;
import com.teclib.utils.Routes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class RegisterActivity extends Activity {

    private ProgressBar pb;
    private Routes routes;
    private DataStorage cache;
    private TextView tvData;
    private LinearLayout lyUserData;
    private EditText txtName;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pb = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        Uri data = intent.getData();

        String deepLinkData = Helpers.base64decode(data.getQueryParameter("data"));

        cache = new DataStorage( RegisterActivity.this );

        String broker = cache.getBroker();
        if(broker != null) {
            abrirMain();
        }

        try {
            JSONObject jsonLink = new JSONObject(deepLinkData);

            String url = jsonLink.getString("url");
            String userToken = jsonLink.getString("user_token");
            String invitationToken = jsonLink.getString("invitation_token");

            cache.setUrl(url);
            cache.setUserToken( userToken );
            cache.setInvitationToken( invitationToken );

        } catch (Exception ex) {
            FlyveLog.e( ex.getMessage() );
        }

        routes = new Routes( RegisterActivity.this );

        tvData = (TextView) findViewById(R.id.data);
        lyUserData = (LinearLayout) findViewById(R.id.user_data);

        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);

        Button btnRegister = (Button) findViewById(R.id.btn_register);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createX509cert();
            }
        });

        initSession();
    }

    // STEP 1
    private void initSession() {
        try {
            pb.setVisibility(View.VISIBLE);
            tvData.setText("Init Session");
            ConnectionHTTP.getWebData(
                    routes.initSession( cache.getUserToken() ),
                    "GET" ,
                    new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    try {
                        JSONObject jsonSession = new JSONObject(data);
                        cache.setSessionToken( jsonSession.getString("session_token") );

                        tvData.setText("get Full Session");

                        getFullSession();

                    } catch (Exception ex) {
                        tvData.setText("ERROR JSON: initSession");
                        pb.setVisibility(View.GONE);
                        FlyveLog.e( ex.getMessage() );
                    }
                }
            });
        }
        catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvData.setText("ERROR: initSession");
            FlyveLog.e( ex.getMessage() );
        }
    }

    // STEP 2
    private void getFullSession() {
        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.getFullSession(), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    tvData.setText("changeActiveProfile");

                    try {
                        JSONObject jsonFullSession = new JSONObject(data);

                        JSONObject jsonSession = jsonFullSession.getJSONObject("session");

                        JSONObject jsonActiveProfile = jsonSession.getJSONObject("glpiactiveprofile");

                        String profileId = jsonActiveProfile.getString("id");
                        cache.setProfileId( profileId );

                        changeActiveProfile();

                    } catch (Exception ex) {
                        pb.setVisibility(View.GONE);
                        tvData.setText("ERROR JSON: getFullSession");
                        FlyveLog.e( ex.getMessage() );
                    }

                    changeActiveProfile();

                }
            });
        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvData.setText("ERROR: getFullSession");
            FlyveLog.e( ex.getMessage() );
        }
    }

    // STEP 3
    private void changeActiveProfile() {

        try {

            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.changeActiveProfile(cache.getProfileId()), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    pb.setVisibility(View.GONE);
                    tvData.setText("changeActiveProfile Ok!");
                    lyUserData.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvData.setText("ERROR: changeActiveProfile");
            FlyveLog.e( ex.getMessage() );
        }

    }

    // STEP 4
    private void createX509cert() {
        tvData.setText("Creating Certificate");
        pb.setVisibility( View.VISIBLE );

        new Thread(new Runnable() {
            public void run() {

                try {
//                    AndroidCryptoProvider createCertif = new AndroidCryptoProvider(getBaseContext());
//                    createCertif.generateRequest();
//                    createCertif.loadCsr();

                    runOnUiThread(new Runnable() {
                        public void run() {
                            pluginFlyvemdmAgent();
                        }
                    });
                } catch (Exception ex) {
                    pb.setVisibility(View.GONE);
                    tvData.setText("ERROR: Creating Certificate X509");
                    FlyveLog.e(ex.getMessage());
                }
            }
        }).start();

    }

    // STEP 5
    private void pluginFlyvemdmAgent() {

        tvData.setText("Register Agent");

        try {

            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");

            JSONObject payload = new JSONObject();
            JSONObject input = new JSONObject();

            //AndroidCryptoProvider csr = new AndroidCryptoProvider(RegisterActivity.this.getBaseContext());
//            String requestCSR = "";
//            if( csr.getlCsr() != null ) {
//                requestCSR = URLEncoder.encode(csr.getlCsr(), "UTF-8");
//            }

            try {
                payload.put("_email", txtEmail.getText());
                payload.put("_invitation_token", cache.getInvitationToken());
                payload.put("_serial", Build.SERIAL);
                //payload.put("csr", requestCSR);
                payload.put("csr", "");
                payload.put("firstname", txtName.getText());
                payload.put("lastname", "Without");
                payload.put("version", "0.99.0");
                input.put("input", payload);
            } catch (JSONException ex) {
                pb.setVisibility(View.GONE);
                tvData.setText( "ERROR pluginFlyvemdmAgent JSON" );
                FlyveLog.e( ex.getMessage() );
            }

            ConnectionHTTP.getWebData(routes.pluginFlyvemdmAgent(), input, header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    tvData.setText("Register Agent");

                    try {
                        JSONObject jsonAgent = new JSONObject(data);
                        cache.setAgentId(jsonAgent.getString("id"));

                        getDataPluginFlyvemdmAgent();
                    } catch(Exception ex) {
                        pb.setVisibility(View.GONE);
                        tvData.setText( "ERROR pluginFlyvemdmAgent HTTP " + data );
                        FlyveLog.e( ex.getMessage() );
                    }
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvData.setText( "ERROR pluginFlyvemdmAgent" );
            FlyveLog.e(ex.getMessage());
        }
    }

    // STEP 6
    private void getDataPluginFlyvemdmAgent() {

        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getSessionToken());

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.pluginFlyvemdmAgent());

            ConnectionHTTP.getWebData(routes.pluginFlyvemdmAgent(cache.getAgentId()), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                pb.setVisibility(View.GONE);

                try {
                    JSONObject jsonObject = new JSONObject(data);

                    String mbroker = jsonObject.getString("broker");
                    String mport = jsonObject.getString("port");
                    String mssl = jsonObject.getString("tls");
                    String mtopic = jsonObject.getString("topic");
                    String mpassword = jsonObject.getString("mqttpasswd");
                    String mcert = jsonObject.getString("certificate");
                    String mNameEmail = jsonObject.getString("name");
                    int mComputersId = jsonObject.getInt("computers_id");
                    int mId = jsonObject.getInt("id");
                    int mEntitiesId = jsonObject.getInt("entities_id");
                    int mFleetId = jsonObject.getInt("plugin_flyvemdm_fleets_id");

                    cache.setBroker( mbroker );
                    cache.setPort( mport );
                    cache.setTls( mssl );
                    cache.setTopic( mtopic );
                    cache.setMqttuser( Build.SERIAL );
                    cache.setMqttpasswd( mpassword );
                    cache.setCertificate( mcert );
                    cache.setName( mNameEmail );
                    cache.setComputersId( String.valueOf(mComputersId) );
                    cache.setEntitiesId( String.valueOf(mEntitiesId) );
                    cache.setPluginFlyvemdmFleetsId( String.valueOf(mFleetId) );

                    abrirMain();

                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            tvData.setText( "ERROR getDataPluginFlyvemdmAgent" );
            FlyveLog.e(ex.getMessage());
        }
    }

    private void abrirMain() {
        Intent miIntent = new Intent(RegisterActivity.this, MainActivity.class);
        RegisterActivity.this.startActivity(miIntent);
        RegisterActivity.this.finish();
    }
}
