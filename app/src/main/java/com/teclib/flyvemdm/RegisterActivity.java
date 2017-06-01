package com.teclib.flyvemdm;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.teclib.data.DataStorage;
import com.teclib.utils.ConnectionHTTP;
import com.teclib.utils.Helpers;
import com.teclib.utils.Routes;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by rafaelhernandez on 6/5/17.
 */

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar pb;
    private Routes routes;
    private DataStorage cache;

    private TextView txtdata;
    private LinearLayout user_data;
    private Button btn_register;

    private EditText txtName;
    private EditText txtEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        pb = (ProgressBar) findViewById(R.id.progressBar);

        Intent intent = getIntent();
        Uri data = intent.getData();

        String deeplink_data = Helpers.base64decode(data.getQueryParameter("data"));

        cache = new DataStorage( RegisterActivity.this );

        String broker = cache.getVariablePermanente("broker");
        if(broker != null) {
            abrirMain();
        }

        try {
            JSONObject jsonLink = new JSONObject(deeplink_data);

            String url = jsonLink.getString("url");
            String user_token = jsonLink.getString("user_token");
            String invitation_token = jsonLink.getString("invitation_token");

            cache.setVariablePermanente("url", url);
            cache.setVariablePermanente("user_token", user_token);
            cache.setVariablePermanente("invitation_token", invitation_token);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        routes = new Routes( RegisterActivity.this );

        txtdata = (TextView) findViewById(R.id.data);
        user_data = (LinearLayout) findViewById(R.id.user_data);

        txtName = (EditText) findViewById(R.id.txtName);
        txtEmail = (EditText) findViewById(R.id.txtEmail);
        txtEmail.setImeActionLabel("Done", KeyEvent.KEYCODE_ENTER);

        btn_register = (Button) findViewById(R.id.btn_register);
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pluginFlyvemdmAgent();
            }
        });


        initSession();

    }

    // STEP 1
    private void initSession() {
        try {
            pb.setVisibility(View.VISIBLE);
            txtdata.setText("Init Session");
            ConnectionHTTP.getWebData(
                    routes.initSession( cache.getVariablePermanente("user_token") ),
                    "GET" ,
                    new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    try {
                        JSONObject jsonSession = new JSONObject(data);
                        cache.setVariablePermanente("session_token", jsonSession.getString("session_token"));

                        txtdata.setText("get Full Session");

                        getFullSession();

                    } catch (Exception ex) {
                        txtdata.setText("ERROR JSON: initSession");
                        pb.setVisibility(View.GONE);
                        ex.printStackTrace();
                    }
                }
            });
        }
        catch (Exception ex) {
            pb.setVisibility(View.GONE);
            ex.printStackTrace();
            txtdata.setText("ERROR: initSession");
        }
    }

    // STEP 2
    private void getFullSession() {
        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getVariablePermanente("session_token"));

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.getFullSession(), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {

                    txtdata.setText("changeActiveProfile");

                    try {
                        JSONObject jsonFullSession = new JSONObject(data);

                        JSONObject jsonSession = jsonFullSession.getJSONObject("session");

                        JSONObject jsonActiveProfile = jsonSession.getJSONObject("glpiactiveprofile");

                        String profile_id = jsonActiveProfile.getString("id");
                        cache.setVariablePermanente("profile_id", profile_id);

                        changeActiveProfile();

                    } catch (Exception ex) {
                        pb.setVisibility(View.GONE);
                        txtdata.setText("ERROR JSON: getFullSession");
                        ex.printStackTrace();
                    }

                    changeActiveProfile();

                }
            });
        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            ex.printStackTrace();
            txtdata.setText("ERROR: getFullSession");
        }
    }

    // STEP 3
    private void changeActiveProfile() {

        try {

            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getVariablePermanente("session_token"));

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.getFullSession());

            ConnectionHTTP.getWebData(routes.changeActiveProfile(cache.getVariablePermanente("profile_id")), "GET", header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    pb.setVisibility(View.GONE);
                    txtdata.setText("changeActiveProfile Ok!");
                    user_data.setVisibility(View.VISIBLE);
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            ex.printStackTrace();
            txtdata.setText("ERROR: changeActiveProfile");
        }

    }

    // STEP 4
    private void pluginFlyvemdmAgent() {

        pb.setVisibility( View.VISIBLE );
        txtdata.setText("Register Agent");

        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getVariablePermanente("session_token"));

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            //header.put("User-Agent","Flyve MDM");
            //header.put("Referer",routes.PluginFlyvemdmAgent());

            JSONObject payload = new JSONObject();
            JSONObject input = new JSONObject();

            //AndroidCryptoProvider csr = new AndroidCryptoProvider(RegisterActivity.this.getBaseContext());
            //String requestCSR = URLEncoder.encode(csr.getlCsr(), "UTF-8");

            try {
                payload.put("_email", txtEmail.getText());
                payload.put("_invitation_token", cache.getVariablePermanente("invitation_token"));
                payload.put("_serial", Build.SERIAL); //Build.SERIAL
                payload.put("csr", "");
                payload.put("firstname", txtName.getText());
                payload.put("lastname", "Without");
                payload.put("version", "0.99.0");
                input.put("input", payload);
            } catch (JSONException ex) {
                pb.setVisibility(View.GONE);
                txtdata.setText( "ERROR pluginFlyvemdmAgent JSON" );
                ex.printStackTrace();
            }

            ConnectionHTTP.getWebData(routes.PluginFlyvemdmAgent(), input, header, new ConnectionHTTP.DataCallback() {
                @Override
                public void callback(String data) {
                    txtdata.setText("Register Agent");

                    try {
                        JSONObject jsonAgent = new JSONObject(data);
                        cache.setVariablePermanente("agent_id", jsonAgent.getString("id"));

                        getDataPluginFlyvemdmAgent();
                    } catch(Exception ex) {
                        pb.setVisibility(View.GONE);
                        txtdata.setText( "ERROR pluginFlyvemdmAgent HTTP" + ex.getMessage() );
                        ex.printStackTrace();
                    }
                }
            });

        } catch (Exception ex) {
            pb.setVisibility(View.GONE);
            txtdata.setText( "ERROR pluginFlyvemdmAgent" );
            ex.printStackTrace();
        }
    }

    // STEP 5
    private void getDataPluginFlyvemdmAgent() {

        try {
            HashMap<String, String> header = new HashMap();
            header.put("Session-Token",cache.getVariablePermanente("session_token"));

            header.put("Accept","application/json");
            header.put("Content-Type","application/json; charset=UTF-8");
            header.put("User-Agent","Flyve MDM");
            header.put("Referer",routes.PluginFlyvemdmAgent());

            ConnectionHTTP.getWebData(routes.PluginFlyvemdmAgent(cache.getVariablePermanente("agent_id")), "GET", header, new ConnectionHTTP.DataCallback() {
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
                    int mComputers_id = jsonObject.getInt("computers_id");
                    int mId = jsonObject.getInt("id");
                    int mEntities_id = jsonObject.getInt("entities_id");
                    int mFleet_id = jsonObject.getInt("plugin_flyvemdm_fleets_id");

                    cache.setVariablePermanente("broker", mbroker);
                    cache.setVariablePermanente("port", mport);
                    cache.setVariablePermanente("tls", mssl);
                    cache.setVariablePermanente("topic", mtopic);
                    cache.setVariablePermanente("mqttpasswd", mpassword);
                    cache.setVariablePermanente("certificate", mcert);
                    cache.setVariablePermanente("name", mNameEmail);
                    cache.setVariablePermanente("agent_id", String.valueOf(mId));
                    cache.setVariablePermanente("computers_id", String.valueOf(mComputers_id));
                    cache.setVariablePermanente("entities_id", String.valueOf(mEntities_id));
                    cache.setVariablePermanente("plugin_flyvemdm_fleets_id", String.valueOf(mFleet_id));

                    abrirMain();

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                }
            });

        } catch (Exception ex) {
            ex.printStackTrace();
            pb.setVisibility(View.GONE);
            txtdata.setText( "ERROR getDataPluginFlyvemdmAgent" );
        }
    }

    private void abrirMain() {
        Intent miIntent = new Intent(RegisterActivity.this, MainActivity.class);
        RegisterActivity.this.startActivity(miIntent);
        RegisterActivity.this.finish();
    }
}
