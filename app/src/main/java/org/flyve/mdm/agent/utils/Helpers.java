/*
 *   Copyright (C) 2017 Teclib. All rights reserved.
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
 * @copyright Copyright (C) ${YEAR} Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/flyve-mdm-android-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Base64;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.ui.MainActivity;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicInteger;

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


	public static Drawable getApplicationImage(Context context, String packageApp) {
		try {
			return context.getPackageManager().getApplicationIcon(packageApp);
		}
		catch (PackageManager.NameNotFoundException ex) {
			FlyveLog.e(ex.getMessage());
			return null;
		}
	}
	public static void hideKeyboard(Activity activity) {
		// Hide keyboard
		View view = activity.getCurrentFocus();
		if (view != null) {
			try {
				InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			} catch (NullPointerException ex) {
				FlyveLog.e(ex.getMessage());
			}
		}
	}

	/**
	 * If the user selects the image with the option from the gallery
	 */
	public static void galleryIntent(Activity activity, int requestFile) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);//
		activity.startActivityForResult(Intent.createChooser(intent, activity.getResources().getString(R.string.select_file) ),requestFile);
	}

	/**
	 * If the user selects the image with the option take photo
	 */
	public static void cameraIntent(Activity activity, int requestCamera) {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getImageUri());
			activity.startActivityForResult(takePictureIntent, requestCamera);
		}
	}

	/**
	 * Get the URI of the image
	 * @return the URI
	 */
	public static Uri getImageUri() {
		// Store image in dcim
		File filePhoto = new File(Environment.getExternalStorageDirectory() + "/DCIM/", "flyveUser.jpg");
		return Uri.fromFile(filePhoto);
	}


	public static void deleteMQTTCache(Context context) {
		File path = context.getExternalFilesDir("MqttConnection");
		FlyveLog.d(path.getAbsolutePath());
		deleteFolder(path.getAbsolutePath());
	}

	public static void deleteFolder(String path) {

		File file = new File(path);

		if (file.exists()) {
			String deleteCmd = "rm -r " + path;
			Runtime runtime = Runtime.getRuntime();
			try {
				runtime.exec(deleteCmd);
			} catch (IOException ex) {
				FlyveLog.e(ex.getMessage());
			}
		}
	}

	public static void sendToNotificationBar(Context context, String message) {
		Intent resultIntent = new Intent(context, MainActivity.class);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent piResult = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
				.setContentTitle(context.getResources().getString(R.string.app_name))
				.setContentText(message)
				.setSound(defaultSoundUri)
				.setAutoCancel(true)
				.setContentIntent(piResult)
				.setPriority(Notification.PRIORITY_HIGH);

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setSmallIcon(R.drawable.ic_notification_white);
		} else {
			builder.setSmallIcon(R.drawable.icon);
		}


		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(getIntID(), builder.build());
	}

	private final static AtomicInteger c = new AtomicInteger(0);
	public static int getIntID() {
		return c.incrementAndGet();
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
	 * @param context
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
			rtext = Base64.encodeToString(data, Base64.NO_WRAP | Base64.URL_SAFE);
			rtext = rtext.replaceAll("-", "+");
		} catch (UnsupportedEncodingException e) {
			FlyveLog.e(e.getMessage());
		}
		
		return rtext;
	}

	/**
	 * Get Device Serial to work with simulator and real devices
	 * @return String with Device Serial
	 */
	public static String getDeviceSerial() {
		String serial;
		if(Build.SERIAL.equalsIgnoreCase("unknown")) {
			serial = "Unknown";
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
	 * @param type of the message
	 * @param title of the message
	 * @param body of the message
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
	 * https://developer.android.com/reference/android/support/design/widget/Snackbar.html Documentation of the Sanackbars
	 * @param activity the View to show
	 * @param message to display
	 */
	public static void snack(Activity activity, String message) {
		Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG)
				.setActionTextColor(activity.getResources().getColor(R.color.snackbar_action))
				.show();
	}

	/**
	 * Generate a snackbar with the given arguments
	 * @param activity the View to show
	 * @param message to display
	 * @param action the text to display for the action
	 * @param callback to be invoked when the action is clicked
	 */
	public static void snack(Activity activity, String message, String action,  View.OnClickListener callback) {
		Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE)
				.setActionTextColor(activity.getResources().getColor(R.color.snackbar_action))
				.setAction(action, callback)
				.show();
	}

	public static void deleteAllSMS(Context context) {
		Cursor cursor = context.getContentResolver().query(
				Uri.parse("content://sms/"),new String[] {
						"_id", "thread_id", "address", "person", "date","body" }, null, null, null);

		try {
			while (cursor.moveToNext()) {
				int id = cursor.getInt(0);
				int threadId = cursor.getInt(1); // get the thread_id
				int rowsDeleted = context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
				FlyveLog.d("Rows deleted: " + rowsDeleted);
			}
		} catch (Exception ex) {
			FlyveLog.e(ex.getMessage());
		}
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
		context.sendBroadcast(in);
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
	 * @param bitmap the image to convert
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
	 * @param bitmap
	 * @param imageAbsolutePath the path to the image
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
	 * @param bitmap the image to rotate
	 * @param degrees to rotate
	 * @return Bitmap the image rotated
	 */
	public static Bitmap rotate(Bitmap bitmap, float degrees) {
		Matrix matrix = new Matrix();
		matrix.postRotate(degrees);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}

	/**
	 * Flip the Bitmap according the given arguments
	 * @param bitmap the image to flip
	 * @param horizontal true for the x-axis, false for the y-axis
	 * @param vertical true for the y-axis, false for the x-axis
	 * @return Bitmap the flipped image
	 */
	public static Bitmap flip(Bitmap bitmap, boolean horizontal, boolean vertical) {
		Matrix matrix = new Matrix();
		matrix.preScale(horizontal ? -1 : 1, vertical ? -1 : 1);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
	}
}