package org.fusioninventory.categories;

import java.util.Map;

import android.content.Context;

public class Envs
        extends Categories {


    /**
	 * 
	 */
	private static final long serialVersionUID = -6210390594988309754L;

	public Envs(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub


        Map<String,String> envs = System.getenv();
        for( String env : envs.keySet()) {
           Category c = new Category(mCtx,"ENVS");
           c.put("KEY", env);
           c.put("VAL", envs.get(env));
           this.add(c);
        }
    }
}
