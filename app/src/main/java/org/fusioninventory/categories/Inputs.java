package org.fusioninventory.categories;

import android.content.Context;
import android.content.res.Configuration;

public class Inputs extends Categories {

	/**
     * 
     */
	private static final long serialVersionUID = 4846706700566208666L;

	public Inputs(Context xCtx) {
		super(xCtx);
		// TODO Auto-generated constructor stub

		Category c = new Category(mCtx, "INPUTS");
		Configuration config = mCtx.getResources().getConfiguration();
		switch (config.keyboard) {
			case Configuration.KEYBOARD_QWERTY:
			case Configuration.KEYBOARD_12KEY:
				c.put("KEYBOARD", "YES");
				break;
			case Configuration.KEYBOARD_NOKEYS:
			default:
				break;
		}

		switch (config.touchscreen) {
			case Configuration.TOUCHSCREEN_STYLUS:
				c.put("TOUCHSCREEN", "STYLUS");
				break;
			case Configuration.TOUCHSCREEN_FINGER:
				c.put("TOUCHSCREEN", "FINGER");
				break;
			case  Configuration.TOUCHSCREEN_NOTOUCH:
			default:
				break;
		}

		this.add(c);

	}
}
