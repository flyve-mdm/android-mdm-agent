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

package org.flyve.mdm.agent.utils;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConnectionHTTP {

	private static Handler uiHandler;
	private ConnectionModel connectionModel;

	public ConnectionHTTP(ConnectionModel connectionModel) {
		this.connectionModel = connectionModel;
	}

	static {
		uiHandler = new Handler(Looper.getMainLooper());
	}

	private static final String EXCEPTION_HTTP = "EXCEPTION_HTTP_";

	private static void runOnUI(Runnable runnable) {
		uiHandler.post(runnable);
	}

	public void getCall(CallResponse callResponse) {
	}

	public interface CallResponse {
		String onResponse();
		String onFailure();
	}

	/**
	 * Get the data in a synchronous way
	 * @param url
	 * @param method
	 * @param header
	 */
	public static String getSyncWebData(String url, String method, Map<String, String> header) {
		try {
			HttpURLConnection conn = getHttpURLConnection(url, method);
			StringBuilder logHeader = getStringBuilder(header, conn);
			return responseHTTP(url, conn, logHeader);
		}
		catch (final Exception ex) {
			FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncWebData", ex.getClass() +" : " + ex.getMessage());
			return EXCEPTION_HTTP + ex.getMessage();
		}
	}

	/**
	 * Get the data in a synchronous way
	 * @param url the url
	 * @param data
	 * @param header
	 */
	public static String getSyncWebData(final String url, final JSONObject data, final Map<String, String> header) {
		try {
			/* Without setInstanceFollowRedirects */
			HttpURLConnection conn = getHttpURLConnection(url, "POST");
			StringBuilder logHeader = getStringBuilder(header, conn);
			// Send post request - Special
			senPost(data, conn);
			return responseHTTP(url, conn, logHeader);
		}
		catch (final Exception ex) {
			String error = EXCEPTION_HTTP + ex.getMessage();
			FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncWebData", error);
			return error;
		}
	}

	/**
	 * Download and save files on device
	 *
	 * @param url      String the url to download the file
	 * @param pathFile String place to save
	 * @return Boolean if file is write
	 */
	public static Boolean getSyncFile(final String url, final String pathFile, String sessionToken, final ProgressCallback callback) {
		OutputStream output = null;
		try {
			/* Without method */
			HttpURLConnection conn = getHttpURLConnection(url, "");

			HashMap<String, String> header = new HashMap<>();
			header.put("Accept", "application/octet-stream");
			header.put("Content-Type", "application/json");
			header.put("Session-Token", sessionToken);

			StringBuilder logHeader = getStringBuilder(header, conn);

			int fileLength = conn.getContentLength();
			InputStream input = conn.getInputStream();
			output = new FileOutputStream(pathFile);

			byte[] data = new byte[4096];
			long totalDataRead = 0;
			int count;

			while ((count = input.read(data)) != -1) {
				totalDataRead += count;
				//publish progress only if total length is known
				if (fileLength > 0) {
					float percent = (totalDataRead * 100) / fileLength;
					callback.progress(Math.round(percent));
				}
				output.write(data, 0, count);
			}

			FlyveLog.d("Download complete size: " + totalDataRead);
			FlyveLog.i(responseHTTP(url, conn, logHeader));
			return true;
		} catch (final Exception ex) {
			FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncFile", ex.getClass() + " : " + ex.getMessage());
			return false;
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception ex) {
					FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncFile", ex.getMessage());
				}
			}
		}
	}

	/**
	 * Send information by post with a JSONObject and header
	 * @param url String url
	 * @param data JSONObject data to send
	 * @param header Map with al the header information
	 * @param callback DataCallback
	 */
	public static void getWebData(final String url, final JSONObject data, final Map<String, String> header, final DataCallback callback) {
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					/* Without setInstanceFollowRedirects */
					HttpURLConnection conn = getHttpURLConnection(url, "POST");
					StringBuilder logHeader = getStringBuilder(header, conn);

					// Send post request
					senPost(data, conn);

					/* Callback */
					if (conn.getResponseCode() >= 400) {
						InputStream is = conn.getErrorStream();
						final String result = inputStreamToString(is);
						ConnectionHTTP.runOnUI(new Runnable() {
							public void run() {
								callback.callback(result);
							}
						});
						return;
					}

					final String requestResponse = inputStreamToString(conn.getInputStream());
					Log(responseHTTP(url, conn, logHeader));

					ConnectionHTTP.runOnUI(new Runnable() {
						public void run() {
							callback.callback(requestResponse);
						}
					});

				} catch (final Exception ex) {
					ConnectionHTTP.runOnUI(new Runnable() {
						public void run() {
							callback.callback(EXCEPTION_HTTP + ex.getMessage());
							FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getWebData", ex.getClass() + " : " + ex.getMessage());
						}
					});
				}
			}
		});
		t.start();
	}

	private static HttpURLConnection getHttpURLConnection(String url, String method) throws IOException {
		URL dataURL = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();
		conn.setConnectTimeout(1800000);
		conn.setReadTimeout(600000);
		conn.setInstanceFollowRedirects(true);
		conn.setRequestMethod(method);
		return conn;
	}

	@NonNull
	private static StringBuilder getStringBuilder(Map<String, String> header, HttpURLConnection conn) {
		StringBuilder logHeader = new StringBuilder();
		if (header != null) {
			for (Map.Entry<String, String> entry : header.entrySet()) {
				logHeader.append("- ").append(entry.getKey()).append(" : ").append(entry.getValue()).append("\n");
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}
		} else {
			logHeader.append("Empty");
		}
		return logHeader;
	}

	@NonNull
	private static String responseHTTP(String url, HttpURLConnection conn, StringBuilder logHeader) throws IOException {
		if (conn.getResponseCode() >= 400) {
			InputStream is = conn.getErrorStream();
			return inputStreamToString(is);
		}

		InputStream is = conn.getInputStream();
		String requestResponse = inputStreamToString(is);
		String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod()
				+ "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage()
				+ "\n\n Header:\n" + logHeader + "\n\n Response:\n" + requestResponse + "\n\n";
		Log(response);

		return requestResponse;
	}

	private static void senPost(JSONObject data, HttpURLConnection conn) throws IOException {
		conn.setDoOutput(true);
		DataOutputStream os = new DataOutputStream(conn.getOutputStream());
		os.writeBytes(data.toString());
		os.flush();
		os.close();
	}

	/**
	 * Convert inputStream to String
	 * @param stream InputStream to convert
	 * @return String converted
	 * @throws IOException error
	 */
	private static String inputStreamToString(final InputStream stream) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = br.readLine()) != null) {
			sb.append(line).append("\n");
		}
		br.close();
		return sb.toString();
	}

	private static void Log(String message){
		FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", Log", message);
	}

	/**
	 * This is the return data interface
	 */
	public interface DataCallback {
		void callback(String data);
	}

	public interface ProgressCallback {
		void progress(int value);
	}

}
