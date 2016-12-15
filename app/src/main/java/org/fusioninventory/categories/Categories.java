package org.fusioninventory.categories;

import java.io.IOException;
import java.util.ArrayList;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

public class Categories extends ArrayList<Category>{

    /**
     * 
     */
    private static final long serialVersionUID = 2278660715848751766L;
    public Context mCtx;
    
    public Categories(Context xCtx) {
        // TODO Auto-generated constructor stub
        mCtx = xCtx;
    }

    
    public void toXML(XmlSerializer xSerializer) {
        for( Category c : this) {
            try {
                c.toXML(xSerializer);
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }


}
