package org.fusioninventory.categories;

import android.app.Service;
import android.content.Context;
import android.view.Display;
import android.view.WindowManager;

public class Videos
        extends Categories {

    /**
     * 
     */
    private static final long serialVersionUID = 6953895287405000489L;

    public Videos(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub
        WindowManager lWinMgr = (WindowManager) mCtx.getSystemService(Service.WINDOW_SERVICE);
        
        Category c = new Category(mCtx , "VIDEOS");
        
        Display d = lWinMgr.getDefaultDisplay();
        
        c.put("RESOLUTION" , String.format("%dx%d" , d.getWidth(),d.getHeight()) );
         this.add(c);
    }

}
