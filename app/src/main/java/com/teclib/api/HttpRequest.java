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

package com.teclib.api;


import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;

import android.os.StrictMode;

import com.teclib.flyvemdm.BuildConfig;
import com.teclib.security.AndroidCryptoProvider;
import com.teclib.flyvemdm.DownloadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.HttpsURLConnection;



public class HttpRequest {

    private final Context mContext;

    private int NOTIFICATION_ID = 2;
    private Notification mNotification;
    private NotificationManager mNotificationManager;

    /**
     * Constructor
     *
     * @param context context
     */
    public HttpRequest(Context context) {
        this.mContext = context;
        mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
    }

    // convert inputstream to String
    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    /**
     * make a Get request in HTTP or HTTPS
     *
     * @param download if true, get answer is a file
     * @param urls     0 : url -- 1->X : http headers
     * @return http answer
     */
    public String GetRequest(Boolean download, String... urls) throws NoSuchAlgorithmException, IOException {
        DownloadTask downloadTask = new DownloadTask(mContext);

        // check protocol
        if (urls[0].split(":")[0].equals("http")) {
            try {
                URL obj = new URL(urls[0]);

                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                if (connection != null) {
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Flyve MDM");
                    //Add headers
                    if (urls.length != 1) {
                        for (int i = 1; i < urls.length; i++) {
                            String[] headers = urls[i].split("::");
                            FlyveLog.d("GetRequest: " + headers[0] + "::" + headers[1]);
                            connection.setRequestProperty(headers[0], headers[1]);
                        }
                    }
                }

                if (download) {
                    String filename = DownloadTask.getmJsonDownload();
                    try {
                        int fileLength = connection.getContentLength();
                        FlyveLog.d("GetRequest: filename" + filename);
                        new File(DownloadTask.directory).mkdirs();

                        //copying
                        InputStream input = connection.getInputStream();
                        OutputStream output = new FileOutputStream(DownloadTask.directory + filename);
                        FlyveLog.d("GetRequest input stream = " + input.toString());


                        byte data[] = new byte[4096];
                        long total = 0;
                        int count;

                        while ((count = input.read(data)) != -1) {
                            total += count;
                            if (fileLength > 0) //publish progress only if total length is known
                                onProgressUpdate((int) (total * 100 / fileLength));//(int)(total / 1024), fileLength/1024 );
                            output.write(data, 0, count);
                        }

                        if (output != null)
                            output.close();
                        if (input != null)
                            input.close();

                        if (connection != null)
                            connection.disconnect();
                        return "OK";

                    } catch (Exception e) {
                        FlyveLog.e(e.toString());
                        return e.toString();
                    }
                } else {

                    int responseCode = connection.getResponseCode();
                    FlyveLog.d("Sending 'GET' request to URL : " + urls[0]);
                    FlyveLog.d("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                    FlyveLog.json("Response : " + response.toString());

                    return response.toString();
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }

        } else if (urls[0].split(":")[0].equals("https")) {

            try {

                URL obj = new URL(urls[0]);

                HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();
                if (connection != null) {
                    connection.setRequestMethod("GET");
                    connection.setRequestProperty("User-Agent", "Flyve MDM");
                    if (urls.length != 1) {
                        for (int i = 1; i < urls.length; i++) {
                            String[] headers = urls[i].split("::");
                            FlyveLog.d("GetRequest: " + headers[0] + "::" + headers[1]);
                            connection.setRequestProperty(headers[0], headers[1]);
                        }
                    }
                }

                //read http answer and save file
                if (download) {
                    String filename = DownloadTask.getmJsonDownload();
                    int fileLength = connection.getContentLength();

                    FlyveLog.d("GetRequest: filename" + filename);
                    new File(DownloadTask.directory).mkdirs();
                    InputStream input = connection.getInputStream();
                    OutputStream output = new FileOutputStream(DownloadTask.directory + filename);

                    FlyveLog.d("GetRequest input stream = " + input.toString());

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;

                    while ((count = input.read(data)) != -1) {
                        total += count;
                        if (fileLength > 0) //publish progress only if total length is known
                            onProgressUpdate((int) (total * 100 / fileLength));//(int)(total / 1024), fileLength/1024 );
                        output.write(data, 0, count);

                    }
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();

                    if (connection != null)
                        connection.disconnect();
                    return "OK";
                } else {

                    int responseCode = connection.getResponseCode();
                    FlyveLog.d("Sending 'GET' request to URL : " + urls[0]);
                    FlyveLog.d("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    //print result
                    FlyveLog.d("Response : " + response.toString());
                    return response.toString();
                }

            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }

        }

        return "ERROR_PROTOCOL";
    }

    public String PostRequest(String url, String email, String invitation_token, String serial, String name) throws NoSuchAlgorithmException {
        JSONObject payload = new JSONObject();
        JSONObject input = new JSONObject();
        if (url.split(":")[0].equals("http")) {
            try {
                AndroidCryptoProvider csr = new AndroidCryptoProvider(mContext);
                String Requestcsr = URLEncoder.encode(csr.getlCsr(), "UTF-8");

                try {
                    payload.put("_email", email);
                    payload.put("_invitation_token", invitation_token);
                    payload.put("_serial", serial);
                    payload.put("csr", Requestcsr);
                    payload.put("firstname", name);
                    payload.put("lastname", "");
                    payload.put("version", BuildConfig.VERSION_NAME);
                    input.put("input", payload);
                } catch (JSONException e) {
                    FlyveLog.e(e.getMessage());
                }

                URL obj = new URL(url);

                HttpURLConnection connection = (HttpURLConnection) obj.openConnection();
                if (connection != null) {
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(15000);
                    connection.setConnectTimeout(15000);

                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("User-Agent", "Flyve MDM");
                    connection.setRequestProperty("Referer", url);

                    // Send post request
                    connection.setDoOutput(true);

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(input.toString());
                    os.flush();
                    os.close();

                    FlyveLog.d("PostRequest: input = " + input.toString());
                }
                FlyveLog.i("sendPost: URL = " + url);

                String response = "";
                int responseCode;
                InputStream _is;

                responseCode = connection.getResponseCode();
                FlyveLog.i("responseCode = " + responseCode);

                if (connection.getResponseCode() / 100 == 2) { // 2xx code means success
                    _is = connection.getInputStream();
                    response = convertInputStreamToString(_is);

                    JSONObject answer = new JSONObject(response);
                    if (answer.has("id")) {
                        return answer.getString("id");
                    }

                } else {
                    _is = connection.getErrorStream();
                    response = convertInputStreamToString(_is);
                    FlyveLog.i("Error != 2xx", response);

                    try {
                        JSONArray answer = new JSONArray(response);
                        FlyveLog.d("PostRequest: " + answer.getString(0));
                        FlyveLog.d("PostRequest: " + answer.getString(1));
                        if (answer.getString(0).equals("ERROR_GLPI_ADD")) {
                            return answer.getString(1);
                        }
                    } catch (JSONException e) {
                        FlyveLog.e(e.getMessage());
                    }
                }

                try {
                    JSONObject answer = new JSONObject(response);
                    if (answer.has("id")) {
                        return answer.getString("id");
                    } else if (answer.has("ERROR_GLPI_ADD")) {
                        return answer.getString("ERROR_GLPI_ADD");
                    }
                } catch (JSONException e) {
                    FlyveLog.e(e.getMessage());
                } catch (Exception ex) {
                    FlyveLog.e(ex.getMessage());
                }
            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());

            }

        } else if (url.split(":")[0].equals("https")) {

            try {


                AndroidCryptoProvider csr = new AndroidCryptoProvider(mContext);
                String Requestcsr = URLEncoder.encode(csr.getlCsr(), "UTF-8");

                //escape the double quotes in json string
                try {
                    payload.put("_email", email);
                    payload.put("_invitation_token", invitation_token);
                    payload.put("_serial", serial);
                    payload.put("csr", Requestcsr);
                    payload.put("firstname", name);
                    payload.put("lastname", "");
                    payload.put("version", BuildConfig.VERSION_NAME);
                    input.put("input", payload);
                } catch (JSONException e) {
                    FlyveLog.e(e.getMessage());
                }

                URL obj = new URL(url);

                FlyveLog.i("sendPost: URL = " + url);

                HttpsURLConnection connection = (HttpsURLConnection) obj.openConnection();

                if (connection != null) {
                    connection.setRequestMethod("POST");
                    connection.setReadTimeout(15000);
                    connection.setConnectTimeout(15000);

                    connection.setRequestProperty("Accept", "application/json");
                    connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                    connection.setRequestProperty("User-Agent", "Flyve MDM");
                    connection.setRequestProperty("Referer", url);

                    // Send post request
                    connection.setDoOutput(true);

                    DataOutputStream os = new DataOutputStream(connection.getOutputStream());
                    os.writeBytes(input.toString());
                    os.flush();
                    os.close();
                    FlyveLog.d("PostRequest: input = " + input.toString());
                }

                String response = "";
                int responseCode;
                InputStream _is;

                responseCode = connection.getResponseCode();

                FlyveLog.i("responseCode = " + responseCode);

                if (connection.getResponseCode() / 100 == 2) { // 2xx code means success
                    _is = connection.getInputStream();
                    response = convertInputStreamToString(_is);

                    try {
                        JSONObject answer = new JSONObject(response);
                        if (answer.has("id")) {
                            return answer.getString("id");
                        }
                    } catch (JSONException e) {
                        FlyveLog.e(e.getMessage());
                    }

                } else {
                    _is = connection.getErrorStream();
                    response = convertInputStreamToString(_is);
                    FlyveLog.i("Error != 2xx", response);

                    try {
                        JSONArray answer = new JSONArray(response);
                        FlyveLog.d("PostRequest: " + answer.getString(0));
                        FlyveLog.d("PostRequest: " + answer.getString(1));
                        if (answer.getString(0).equals("ERROR_GLPI_ADD")) {
                            return answer.getString(1);
                        }
                    } catch (JSONException e) {
                        FlyveLog.e(e.getMessage());
                    }

                }

                try {
                    JSONObject answer = new JSONObject(response);
                    if (answer.has("id")) {
                        return answer.getString("id");
                    } else if (answer.has("ERROR_GLPI_ADD")) {
                        return answer.getString("ERROR_GLPI_ADD");
                    }
                } catch (JSONException e) {
                    FlyveLog.e(e.getMessage());
                    FlyveLog.e(e.getMessage());
                }

            } catch (Exception ex) {
                FlyveLog.e(ex.getMessage());
            }

        }
        return "ERROR_PROTOCOL";
    }

    protected void onProgressUpdate(Integer... progress) {

        //Build the notification using Notification.Builder
        Notification.Builder builder = new Notification.Builder(mContext)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setAutoCancel(true)
                .setContentTitle("Data download is in progress")
                .setContentText("");

        //FlyveLog.d("onProgressUpdate: " + progress[0]);

        builder.setProgress(100, progress[0], false);

        //Get current notification
        mNotification = builder.getNotification();

        //Show the notification
        mNotificationManager.notify(NOTIFICATION_ID, mNotification);

        if (progress[0].equals(100)) {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

}
