package org.fusioninventory.categories;

import java.util.Properties;

import android.content.Context;

public class Jvm
        extends Categories {


    /**
	 * 
	 */
	private static final long serialVersionUID = 3291981487537599599L;

	public Jvm(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub

        Category c = new Category(mCtx,"JVMS");
        Properties props = System.getProperties();
        /*
        for(Object prop: props.keySet() ) {
            FusionInventory.log(this, String.format("PROP %s = %s" , (String)prop, props.get(prop) ) , Log.VERBOSE);
        }
         */
        c.put("NAME", (String)props.getProperty("java.vm.name"));
        String language = (String)props.getProperty("user.language");
        language += '_';
        language += (String)props.getProperty("user.region");
        c.put("VENDOR", (String)props.getProperty("java.vm.vendor"));
        c.put("LANGUAGE", language);
        c.put("RUNTIME", (String)props.getProperty("java.runtime.version"));
        c.put("HOME", (String)props.getProperty("java.home"));
        c.put("VERSION", (String)props.getProperty("java.vm.version"));
        c.put("CLASSPATH", (String)props.getProperty("java.class.path"));
        this.add(c);
    }
}
