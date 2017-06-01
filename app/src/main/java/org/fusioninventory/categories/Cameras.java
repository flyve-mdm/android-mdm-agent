package org.fusioninventory.categories;

import java.util.List;

import android.content.Context;
import android.hardware.Camera;

public class Cameras
        extends Categories {


    /**
	 * 
	 */
	private static final long serialVersionUID = 6791259866128400637L;

	public Cameras(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub
        Category c = new Category(mCtx , "CAMERAS");
        Camera cam = Camera.open();
        
        Camera.Parameters params = cam.getParameters();
        List<Camera.Size> list = params.getSupportedPictureSizes();
        int width = 0,height = 0;
        for (Camera.Size size : list) {
        	if( (size.width * size.height) > (width * height) ) {
                width  = size.width;
                height = size.height;
            }
        }
        c.put("RESOLUTIONS",String.format("%dx%d" , width, height) );
        cam.release();
        this.add(c);
    }

}
