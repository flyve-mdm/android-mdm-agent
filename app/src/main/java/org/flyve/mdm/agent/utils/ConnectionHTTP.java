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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.flyve.mdm.agent.core.Routes;
import org.flyve.mdm.agent.data.database.MqttData;
import org.json.JSONArray;
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

	static {
		uiHandler = new Handler(Looper.getMainLooper());
	}

	private static int timeout = 1800000;
	private static int readtimeout = 600000;
	private static final String EXCEPTION_HTTP = "EXCEPTION_HTTP_";

	private static void runOnUI(Runnable runnable) {
		uiHandler.post(runnable);
	}

	/**
	 * Get the data in a synchronous way
	 * @param url
	 * @param method
	 * @param header
	 */
	public static String getSyncWebData(String url, String method, Map<String, String> header) {
		try {
			URL dataURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(readtimeout);
			conn.setInstanceFollowRedirects(true);
			conn.setRequestMethod(method);

			StringBuilder logHeader = new StringBuilder();
			if(header != null) {
				for (Map.Entry<String, String> entry : header.entrySet()) {
					logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			} else {
				logHeader.append("Empty");
			}

			if(conn.getResponseCode() >= 400) {
				InputStream is = conn.getErrorStream();
				return inputStreamToString(is);
			}

			InputStream is = conn.getInputStream();
			String requestResponse = inputStreamToString(is);
			String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Response:\n" + requestResponse + "\n\n";
			LogDebug(response);

			return requestResponse;
		}
		catch (final Exception ex) {
			FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncWebData", ex.getClass() +" : " + ex.getMessage());
			return EXCEPTION_HTTP + ex.getMessage();
		}
	}

	public static void killSession(final Context context, final String sessionToken) {
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Routes routes = new Routes(context);
					MqttData cache = new MqttData(context);
					String url = routes.pluginFlyvemdmAgent(cache.getAgentId());
					URL dataURL = new URL(url);
					HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

					conn.setRequestMethod("GET");
					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(readtimeout);

					HashMap<String, String> header = new HashMap();
					header.put("Accept","application/octet-stream");
					header.put("Content-Type","application/json");
					header.put("Session-Token", sessionToken);

					StringBuilder logHeader = new StringBuilder();
					if(header != null) {
						for (Map.Entry<String, String> entry : header.entrySet()) {
							logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
							conn.setRequestProperty(entry.getKey(), entry.getValue());
						}
					} else {
						logHeader.append("Empty");
					}

					if(conn.getResponseCode() >= 400) {
						InputStream is = conn.getErrorStream();
						String result = inputStreamToString(is);
						Log(result);
						return;
					}

					InputStream is = conn.getInputStream();
					final String requestResponse = inputStreamToString(is);

					String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Response:\n" + requestResponse + "\n\n";
					LogDebug(response);

				}
				catch (final Exception ex)
				{
					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
							FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getWebData",ex.getClass() + " : " + ex.getMessage());
						}
					});
				}
			}
		});
		t.start();

	}

	public static void sendHttpResponse(final Context context, final String url, final String data, final String sessionToken, final DataCallback callback) {
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					URL dataURL = new URL(url);
					HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();
					if(url.contains("PluginFlyvemdmGeolocation")) {
						conn.setRequestMethod("POST");
					} else {
						conn.setRequestMethod("PUT");
					}
					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(readtimeout);

					HashMap<String, String> header = new HashMap();
					header.put("Accept","application/octet-stream");
					header.put("Content-Type","application/json");
					header.put("Session-Token", sessionToken);

					StringBuilder logHeader = new StringBuilder();
					if(header != null) {
						for (Map.Entry<String, String> entry : header.entrySet()) {
							logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
							conn.setRequestProperty(entry.getKey(), entry.getValue());
						}
					} else {
						logHeader.append("Empty");
					}

					// Send post request
					conn.setDoOutput(true);

					DataOutputStream os = new DataOutputStream(conn.getOutputStream());
					os.writeBytes(data);
					os.flush();
					os.close();

					if(conn.getResponseCode() >= 400) {
						InputStream is = conn.getErrorStream();
						final String result = inputStreamToString(is);

						ConnectionHTTP.runOnUI(new Runnable()
						{
							public void run()
							{
								callback.callback(result);
							}
						});
						return;
					}

					InputStream is = conn.getInputStream();
					final String requestResponse = inputStreamToString(is);

					String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Data:\n" + data +"\n\n Response:\n" + requestResponse + "\n\n";
					LogDebug(response);

					ConnectionHTTP.runOnUI(new Runnable() {
						public void run() {
							callback.callback(requestResponse);
						}
					});

				}
				catch (final Exception ex)
				{
					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
							callback.callback(EXCEPTION_HTTP + ex.getMessage());
							FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getWebData",ex.getClass() + " : " + ex.getMessage());
						}
					});
				}
			}
		});
		t.start();
	}

	public static void sendHttpResponsePolicies(final Context context, final String taskId, final String data, final String sessionToken, final DataCallback callback) {
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					Routes routes = new Routes(context);
					MqttData cache = new MqttData(context);
					String url = routes.PluginFlyvemdmTaskstatusSearch(cache.getAgentId(), taskId);

					// First step get the taskstatus_id
					URL dataURL = new URL(url);
					HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

					conn.setRequestMethod("GET");
					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(readtimeout);

					HashMap<String, String> header = new HashMap();
					header.put("Content-Type","application/json");
					header.put("Session-Token", sessionToken);

					StringBuilder logHeader = new StringBuilder();
					if(header != null) {
						for (Map.Entry<String, String> entry : header.entrySet()) {
							logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
							conn.setRequestProperty(entry.getKey(), entry.getValue());
						}
					} else {
						logHeader.append("Empty");
					}

					if(conn.getResponseCode() >= 400) {
						InputStream is = conn.getErrorStream();
						final String result = inputStreamToString(is);

						ConnectionHTTP.runOnUI(new Runnable()
						{
							public void run()
							{
								callback.callback(result);
							}
						});
						return;
					}

					InputStream is = conn.getInputStream();
					final String requestResponse = inputStreamToString(is);

					String taskStatusId = "";
					try {
						JSONObject objResponse = new JSONObject(requestResponse);

						JSONArray arrayData = objResponse.getJSONArray("data");
						taskStatusId = arrayData.getJSONObject(0).getString("2");
					} catch (Exception ex) {
						FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getWebData",ex.getClass() + " : " + ex.getMessage() + " Data : " + data + "Route : " + url);
						return;
					}

					String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Data:\n" + data + "\n\n Response:\n" + requestResponse + "\n\n";
					LogDebug(response);

					try {
						// second step update the status task
						url = routes.PluginFlyvemdmTaskstatus(taskStatusId);

						dataURL = new URL(url);
						conn = (HttpURLConnection)dataURL.openConnection();

						conn.setRequestMethod("PUT");
						conn.setConnectTimeout(timeout);
						conn.setReadTimeout(readtimeout);

						header = new HashMap();
						header.put("Content-Type","application/json");
						header.put("Session-Token", sessionToken);

						logHeader = new StringBuilder();
						if(header != null) {
							for (Map.Entry<String, String> entry : header.entrySet()) {
								logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
								conn.setRequestProperty(entry.getKey(), entry.getValue());
							}
						} else {
							logHeader.append("Empty");
						}

						// Send post request
						conn.setDoOutput(true);

						DataOutputStream os = new DataOutputStream(conn.getOutputStream());
						os.writeBytes(data);
						os.flush();
						os.close();

						if(conn.getResponseCode() >= 400) {
							is = conn.getErrorStream();
							final String result = inputStreamToString(is);

							ConnectionHTTP.runOnUI(new Runnable()
							{
								public void run()
								{
									callback.callback(result);
								}
							});
							return;
						}

						is = conn.getInputStream();
						final String requestResponsePut = inputStreamToString(is);

						response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Data:\n" + data + "\n\n Response:\n" + requestResponse + "\n\n";
						LogDebug(response);

						ConnectionHTTP.runOnUI(new Runnable() {
							public void run() {
								callback.callback(requestResponsePut);
							}
						});


					} catch (final Exception ex) {
						ConnectionHTTP.runOnUI(new Runnable()
						{
							public void run()
							{
								callback.callback(ex.getMessage());
							}
						});
					}


				}
				catch (final Exception ex)
				{
					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
							callback.callback(EXCEPTION_HTTP + ex.getMessage());
							FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getWebData",ex.getClass() + " : " + ex.getMessage());
						}
					});
				}
			}
		});
		t.start();
	}


	/**
	 * Get the data in a synchronous way
	 * @param url the url
	 * @param data
	 * @param header
	 */
	public static String getSyncWebData(final String url, final JSONObject data, final Map<String, String> header) {
		try {
			URL dataURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

			conn.setRequestMethod("POST");
			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(readtimeout);
			StringBuilder logHeader = new StringBuilder();
			if(header != null) {
				for (Map.Entry<String, String> entry : header.entrySet()) {
					logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}
			} else {
				logHeader.append("Empty");
			}

			// Send post request
			conn.setDoOutput(true);

			DataOutputStream os = new DataOutputStream(conn.getOutputStream());
			os.writeBytes(data.toString());
			os.flush();
			os.close();

			if(conn.getResponseCode() >= 400) {
				InputStream is = conn.getErrorStream();
				return inputStreamToString(is);
			}

			InputStream is = conn.getInputStream();
			String requestResponse = inputStreamToString(is);
			String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Data:\n" + data.toString() + "\n\n Response:\n" + requestResponse + "\n\n";
			LogDebug(response);

			return requestResponse;
		}
		catch (final Exception ex) {
			String error = EXCEPTION_HTTP + ex.getMessage();
			FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncWebData", error);
			return error;
		}
	}

	/**
	 * Download and save files on device
	 * @param url String the url to download the file
	 * @param pathFile String place to save
	 * @return Boolean if file is write
	 */
	public static Boolean getSyncFile(final String url, final String pathFile, String sessionToken, final ProgressCallback callback) {

		OutputStream output = null;

		try {
			URL dataURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

			conn.setConnectTimeout(timeout);
			conn.setReadTimeout(readtimeout);
			conn.setInstanceFollowRedirects(true);

			HashMap<String, String> header = new HashMap();
			header.put("Accept","application/octet-stream");
			header.put("Content-Type","application/json");
			header.put("Session-Token", sessionToken);

			StringBuilder logHeader = new StringBuilder();
			for (Map.Entry<String, String> entry : header.entrySet()) {
				logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
				conn.setRequestProperty(entry.getKey(), entry.getValue());
			}

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
					callback.progress( Math.round(percent) );
				}

				output.write(data, 0, count);
			}

			String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n";
			LogDebug(response);

			FlyveLog.d( "Download complete size: " + totalDataRead);
			return true;
		}
		catch (final Exception ex) {
			FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getSyncFile", ex.getClass() +" : " + ex.getMessage());
			return false;
		}
		finally {
			if(output!=null){
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
	public static void getWebData(final String url, final JSONObject data, final Map<String, String> header, final DataCallback callback)
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
			try
			{
				URL dataURL = new URL(url);
				HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

				conn.setRequestMethod("POST");
				conn.setConnectTimeout(timeout);
				conn.setReadTimeout(readtimeout);

				StringBuilder logHeader = new StringBuilder();
				if(header != null) {
					for (Map.Entry<String, String> entry : header.entrySet()) {
						logHeader.append("- " + entry.getKey() + " : " + entry.getValue() + "\n");
						conn.setRequestProperty(entry.getKey(), entry.getValue());
					}
				} else {
					logHeader.append("Empty");
				}

				// Send post request
				conn.setDoOutput(true);

				DataOutputStream os = new DataOutputStream(conn.getOutputStream());
				os.writeBytes(data.toString());
				os.flush();
				os.close();

				if(conn.getResponseCode() >= 400) {
					InputStream is = conn.getErrorStream();
					final String result = inputStreamToString(is);

					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
							callback.callback(result);
						}
					});
					return;
				}

				InputStream is = conn.getInputStream();
				final String requestResponse = inputStreamToString(is);

				String response = "\n URL:\n" + url + "\n\n Method:\n" + conn.getRequestMethod() + "\n\n Code:\n" + conn.getResponseCode() + " " + conn.getResponseMessage() + "\n\n Header:\n" + logHeader + "\n\n Data:\n" + data.toString() + "\n\n Response:\n" + requestResponse + "\n\n";
				LogDebug(response);

				ConnectionHTTP.runOnUI(new Runnable() {
					public void run() {
						callback.callback(requestResponse);
					}
				});

			}
			catch (final Exception ex)
			{
				ConnectionHTTP.runOnUI(new Runnable()
				{
					public void run()
					{
						callback.callback(EXCEPTION_HTTP + ex.getMessage());
						FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", getWebData",ex.getClass() + " : " + ex.getMessage());
					}
				});
			}
			}
		});
		t.start();
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
		String line = null;
		while ((line = br.readLine()) != null) {
			sb.append(line + "\n");
		}
		br.close();
		return sb.toString();
	}

	private static void Log(String message){
		// write log file
		FlyveLog.e(ConnectionHTTP.class.getClass().getName() + ", Log", message);
	}

	private static void LogDebug(String message){
		// write log file
		FlyveLog.d(ConnectionHTTP.class.getClass().getName() + ", Log" + message);
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
