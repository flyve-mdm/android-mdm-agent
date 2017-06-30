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

package com.teclib.utils;

import android.os.Build;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;

/**
 * This class content some helpers function
 */
public class Helpers {

<<<<<<< HEAD
	/**
	 * private construtor
	 */
	private Helpers() {
	}

	/**
	 * Convert Base64 String in to plain String
	 * @param text String to convert
	 * @return String with a plain text
	 */
=======
	private Helpers() {
	}
	
>>>>>>> 4959d8279a378008c355841710a6040797b112a2
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
			serial = "ABCDEFGHIJ1234";
		} else {
			serial = Build.SERIAL;
		}

		return serial;
	}

	/**
	 * get Unix time
	 * @return int unix time
	 */
	public static int GetUnixTime() {
		Calendar calendar = Calendar.getInstance();
		long now = calendar.getTimeInMillis();
		int utc = (int) (now / 1000);
		return (utc);
	}
}