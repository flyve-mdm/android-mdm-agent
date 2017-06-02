package com.teclib.utils;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ConnectionHTTP {

	public static Handler UIHandler;

	static {
		UIHandler = new Handler(Looper.getMainLooper());
	}

	private static int timeout = 18000;
	private static int readtimeout = 6000;

	private static void runOnUI(Runnable runnable) {
		UIHandler.post(runnable);
	}

	public static void getWebData(final String url, final String method, final DataCallback callback) throws IOException
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					URL dataURL = new URL(url);
					HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(readtimeout);
					conn.setInstanceFollowRedirects(true);

					InputStream is = conn.getInputStream();

					final String result = inputStreamToString(is);

					ConnectionHTTP.runOnUI(new Runnable() {
						public void run() {
							callback.callback(result);
						}
					});					

				}
				catch (final Exception ex)
				{
					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
						callback.callback("Exception (" + ex.getClass() + "): " + ex.getMessage());
						}
					});
				}
			}
		});
		t.start();
	}	
	
	
	public static void getWebData(final String url, final String method, final HashMap<String, String> header, final DataCallback callback) throws IOException
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					URL dataURL = new URL(url);
					HttpURLConnection conn = (HttpURLConnection)dataURL.openConnection();

					conn.setConnectTimeout(timeout);
					conn.setReadTimeout(readtimeout);
					conn.setInstanceFollowRedirects(true);
					conn.setRequestMethod(method);

					for (Map.Entry<String, String> entry : header.entrySet()) {
						conn.setRequestProperty(entry.getKey(), entry.getValue());
					}

					InputStream is = conn.getInputStream();

					final String result = inputStreamToString(is);

					ConnectionHTTP.runOnUI(new Runnable() {
						public void run() {
							callback.callback(result);
						}
					});					

				}
				catch (final Exception ex)
				{
					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
						callback.callback("Exception (" + ex.getClass() + "): " + ex.getMessage());
						}
					});
				}
			}
		});
		t.start();
	}

	public static void getWebData(final String url, final JSONObject data, final HashMap<String, String> header, final DataCallback callback) throws IOException
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
				//conn.setInstanceFollowRedirects(true);

				for (Map.Entry<String, String> entry : header.entrySet()) {
					conn.setRequestProperty(entry.getKey(), entry.getValue());
				}

				// Send post request
				conn.setDoOutput(true);

				DataOutputStream os = new DataOutputStream(conn.getOutputStream());
				os.writeBytes(data.toString());
				os.flush();
				os.close();

				if(conn.getResponseCode() == 400) {
					ConnectionHTTP.runOnUI(new Runnable()
					{
						public void run()
						{
							callback.callback("Invitation is not pending");
						}
					});
				}

				InputStream is = conn.getInputStream();

				final String result = inputStreamToString(is);

				ConnectionHTTP.runOnUI(new Runnable() {
					public void run() {
						callback.callback(result);
					}
				});

			}
			catch (final Exception ex)
			{
				ConnectionHTTP.runOnUI(new Runnable()
				{
					public void run()
					{
						callback.callback("Exception (" + ex.getClass() + "): " + ex.getMessage());
					}
				});
			}
			}
		});
		t.start();
	}

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

	public interface DataCallback {

		void callback(String data);

	}

}
