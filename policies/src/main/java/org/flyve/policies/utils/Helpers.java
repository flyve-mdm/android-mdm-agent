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

package org.flyve.policies.utils;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import org.flyve.policies.R;


/**
 * This class content some helpers function
 */
public class Helpers {

	/**
	 * private construtor
	 */
	private Helpers() {
	}

	public static void sendToNotificationBar(Context context, int id, String title, String message, boolean isPersistence, Class<?> cls, String from) {

		Intent resultIntent = new Intent(context, cls);
		resultIntent.putExtra("From", from);
		resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent piResult = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_ONE_SHOT);

		Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

		NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
				.setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.icon))
				.setContentTitle(title)
				.setContentText(message)
				.setSound(defaultSoundUri)
				.setContentIntent(piResult)
				.setPriority(Notification.PRIORITY_HIGH);

		if(isPersistence) {
			builder.setOngoing(true);
		} else {
			builder.setAutoCancel(true);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			// Notification Channel
			String notificationChannelId = "1122";
			String channelName = "Flyve MDM Notifications";
			int importance = NotificationManager.IMPORTANCE_LOW;
			NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, importance);
			notificationChannel.enableLights(true);
 			notificationChannel.setLightColor(Color.GREEN);

			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

			try {
				notificationManager.createNotificationChannel(notificationChannel);
				builder.setChannelId(notificationChannelId);
			} catch (Exception ex) {
				FlyveLog.e(Helpers.class.getClass().getName() + ", sendToNotificationBar", ex.getMessage());
			}
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			builder.setSmallIcon(R.drawable.ic_notification_white);
		} else {
			builder.setSmallIcon(R.drawable.icon);
		}

		builder.setStyle(new NotificationCompat.BigTextStyle().bigText(message));

		NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

		try {
			notificationManager.notify(id, builder.build());
		} catch (Exception ex) {
			FlyveLog.e(Helpers.class.getClass().getName() + ", deleteFolder", ex.getMessage());
		}
	}

	public static void sendToNotificationBar(Context context, String message, Class<?> mainActivity) {
		sendToNotificationBar(context, 1010, context.getResources().getString(R.string.app_name), message, false, mainActivity, "");
	}



}