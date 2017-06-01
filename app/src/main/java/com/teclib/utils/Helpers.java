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