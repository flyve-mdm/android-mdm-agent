/*
 *   Copyright © 2017 Teclib. All rights reserved.
 *
 * This file is part of flyve-mdm-android-agent
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
 * @copyright Copyright © ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.view.View;

import org.flyve.mdm.agent.R;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This class content some helpers function
 */
public class Helpers {

	public static final String BROADCAST_LOG = "flyve.mqtt.log";
	public static final String BROADCAST_MSG = "flyve.mqtt.msg";
	public static final String BROADCAST_STATUS = "flyve.mqtt.status";

	/**
	 * private construtor
	 */
	private Helpers() {
	}

	public static String isSystemApp(Context context) {

		try {
			ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getApplicationContext().getPackageName(), 0);

			if (appInfo.sourceDir.startsWith("/data/app/")) {
				//Non-system app
				return "0";
			} else {
				//System app
				return "1";
			}
		} catch (Exception ex) {
			FlyveLog.e(ex.getMessage());
			return "0";
		}
	}

	public static boolean isOnline(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
			//should check null because in airplane mode it will be null
			return (netInfo != null && netInfo.isConnected());
		} catch (Exception ex) {
			return false;
		}
	}

	/**
	 * Check if the service is running
	 * @param serviceClass Class
	 * @return boolean
	 */
	public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				FlyveLog.i ("isMyServiceRunning? %s", Boolean.toString( true ));
				return true;
			}
		}
		FlyveLog.i ("isMyServiceRunning? %s", Boolean.toString( false ));
		return false;
	}

	/**
	 * Convert Base64 String in to plain String
	 * @param text String to convert
	 * @return String with a plain text
	 */
	public static String base64decode(String text) {
		String rtext = "";
		if(text == null) { return ""; }
		try {
			byte[] bdata = Base64.decode(text, Base64.DEFAULT);
			rtext = new String(bdata, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			FlyveLog.e(e.getMessage());
		}
		return rtext.trim();
	}

	/**
	 * Convert String in to Base64 encode
	 * @param text String to convert
	 * @return String with a base64 encode
	 */
	public static String base64encode(String text) {
		String rtext = "";
		if(text == null) { return ""; }
		try {
			byte[] data = text.getBytes("UTF-8");
			rtext = Base64.encodeToString(data, Base64.DEFAULT);
			rtext = rtext.trim().replace("==", "");
		} catch (UnsupportedEncodingException e) {
			FlyveLog.e(e.getMessage());
		}
		
		return rtext.trim();
	}

	/**
	 * Get Device Serial to work with simulator and real devices
	 * @return String with Device Serial
	 */
	public static String getDeviceSerial() {
		String serial;
		if(Build.SERIAL.equalsIgnoreCase("unknown")) {
			serial = "ABCEDFF012345678";
		} else {
			serial = Build.SERIAL;
		}

		return serial;
	}

	/**
	 * get Unix time
	 * @return int unix time
	 */
	public static int getUnixTime() {
		TimeZone timeZone = TimeZone.getTimeZone("UTC");
		Calendar calendar = Calendar.getInstance(timeZone);
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
		simpleDateFormat.setTimeZone(timeZone);

		return ((int) (calendar.getTimeInMillis() / 1000));
	}

	/**
	 * Open url on browser
 	 * @param context Context where is working
	 * @param url String the url to display
	 */
	public static void openURL(Context context, String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		context.startActivity(browserIntent);
	}

	/**
	 * Generates a JSONOBject with the message
	 * @param string the type of the message
	 * @param string the title of the message
	 * @param string the body of the message
	 * @return string the message to broadcast
	 */
	public static String broadCastMessage(String type, String title, String body) {
		try {
			JSONObject json = new JSONObject();
			json.put("type", type);
			json.put("title", title);
			json.put("body", body);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String currentDateTime = sdf.format(new Date());
			json.put("date", currentDateTime);

			return json.toString();

		} catch(Exception ex) {
			return null;
		}
	}

	/**
	 * Generate a snackbar with the given arguments
	 * @param Activity the view to show
	 * @param string the message to display
	 * @see https://developer.android.com/reference/android/support/design/widget/Snackbar.html Documentation of the Sanackbars
	 */
	public static void snack(Activity activity, String message) {
		Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
				.setActionTextColor(activity.getResources().getColor(R.color.snackbar_action))
				.show();
	}

	/**
	 * Generate a snackbar with the given arguments
	 * @param Activity the view to show
	 * @param string the message to display
	 * @param string the text to display for the action
	 * @param View.OnClickListener the callback to be invoked when the action is clicked
	 */
	public static void snack(Activity activity, String message, String action,  View.OnClickListener callback) {
		Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
				.setActionTextColor(activity.getResources().getColor(R.color.snackbar_action))
				.setAction(action, callback)
				.show();
	}

	/**
	 * Send broadcast
	 * @param message String to send
	 * @param action String action
	 * @param context String context
	 */
	public static void sendBroadcast(String message, String action, Context context) {
		FlyveLog.i(message);
		//send broadcast
		Intent in = new Intent();
		in.setAction(action);
		in.putExtra("message", message);
		LocalBroadcastManager.getInstance(context).sendBroadcast(in);
	}

	/**
	 * Send broadcast
	 * @param message Boolean to send
	 * @param action String action
	 * @param context String context
	 */
	public static void sendBroadcast(Boolean message, String action, Context context) {
		//send broadcast
		Intent in = new Intent();
		in.setAction(action);
		in.putExtra("message", Boolean.toString( message ) );
		LocalBroadcastManager.getInstance(context).sendBroadcast(in);
	}

	/**
	 * Convert the Bitmap to string
	 * @param Bitmap the image to convert
	 * @return string the image Bitmap
	 */
	public static String bitmapToString(Bitmap bitmap){
		ByteArrayOutputStream baos=new  ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
		byte [] b=baos.toByteArray();
		return Base64.encodeToString(b, Base64.DEFAULT);
	}

	/**
	 * @param encodedString
	 * @return bitmap (from given string)
	 */
	public static Bitmap stringToBitmap(String encodedString){
		try {
			byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
			return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
		} catch(Exception e) {
			FlyveLog.e(e.getMessage());
			return null;
		}
	}

	/**
	 * Modify the orientation according the rotation selected
	 * @param Bitmap the bitmap
	 * @param string the path to the image
	 * @return Bitmap the modificated image
	 */
	public static Bitmap modifyOrientation(Bitmap bitmap, String imageAbsolutePath) throws IOException {
		ExifInterface ei = new ExifInterface(imageAbsolutePath);
		int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

		switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				return rotate(bitmap, 90);

			case ExifInterface.ORIENTATION_ROTATE_180:
				return rotate(bitmap, 180);

			case ExifInterface.ORIENTATION_ROTATE_270:
				return rotate(bitmap, 270);

			case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
				return flip(bitmap, true, false);

			case ExifInterface.ORIENTATION_FLIP_VERTICAL:
				return flip(bitmap, false, true);

			default:
				return bitmap;
		}
	}

	/**
	 * Rotate the Bitmap according the degrees
	 * @param Bitmap the image to rotate
	 * @param float the degrees to rotate
	 * @return Bitmap the image rotated
	 */
	public static Bitmap rotate(Bitmap bitmap, float degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	/**
	 * Flip the Bitmap according the given arguments
	 * @param Bitmap the image to flip
	 * @param boolean true for the x-axis, false for the y-axis
	 * @param boolean true for the y-axis, false for the x-axis
	 * @return Bitmap the flipped image
	 */
	public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
		Matrix matrix = new Matrix();
		matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}
}