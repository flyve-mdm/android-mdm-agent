/*
 *   Copyright © ${YEAR} Teclib. All rights reserved.
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

package com.teclib.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;

public class Helpers {

	
	public static String base64decode(String text) {
		String rtext = "";
		if(text == null) { return ""; }
		try {
			byte[] bdata = Base64.decode(text, Base64.DEFAULT);
			rtext = new String(bdata, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return rtext.trim();
	}
	
	public static String base64encode(String text) {
		String rtext = "";
		if(text == null) { return ""; }
		try {
			byte[] data = text.getBytes("UTF-8");
			rtext = Base64.encodeToString(data, Base64.DEFAULT);
			rtext = rtext.trim().replace("==", "");
		} catch (UnsupportedEncodingException e) {

			e.printStackTrace();
		}
		
		return rtext.trim();
	}

    
}