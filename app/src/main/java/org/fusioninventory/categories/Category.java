package org.fusioninventory.categories;

import java.io.IOException;
import java.util.LinkedHashMap;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.os.Build;

public class Category extends LinkedHashMap<String, String>{

    /**
     * 
     */
    private static final long serialVersionUID = 6443019125036309325L;
    public Context mCtx;
    public String mType;

    public Category(Context xCtx, String xType) {
        mCtx = xCtx;
        mType = xType;
    }

    public String put(String key, String value) {
       //Do not add value if it's null, blank or "unkown"
       if (value != null && !value.equals("") && !value.equals(Build.UNKNOWN)) {
    	   return super.put(key, value);
       } else {
    	   return "";
       }
    }
    public void toXML(XmlSerializer serializer) throws IllegalArgumentException, IllegalStateException, IOException {
        
            serializer.startTag(null, mType);

            for (String prop : this.keySet()) {

                serializer.startTag(null, prop);
                serializer.text(String.valueOf(this.get(prop)));
                serializer.endTag(null, prop);
            }

            serializer.endTag(null, mType);
        

    }
}
