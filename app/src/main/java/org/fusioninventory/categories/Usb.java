package org.fusioninventory.categories;

import java.util.HashMap;
import java.util.Iterator;

import org.fusioninventory.FusionInventory;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.util.Log;

public class Usb
        extends Categories {

    /**
     * 
     */
    private static final long serialVersionUID = 4846706700566208666L;

    public Usb(Context xCtx) {
        super(xCtx);
        
        //USB inventory comes with SDK level 12 !
        if(Build.VERSION.SDK_INT > 12) {
	
	        UsbManager manager = (UsbManager) xCtx.getSystemService(Context.USB_SERVICE);
	        HashMap<String, UsbDevice> devices = manager.getDeviceList();
	        Iterator<String> iterator = devices.keySet().iterator();
	        while (iterator.hasNext()) {
	        	String key = (String) iterator.next();
	        	FusionInventory.log(this,key, Log.VERBOSE);
	        	UsbDevice mydevice = devices.get(key);
	        	Category c = new Category(mCtx, "USBDEVICES");
	        	c.put("CLASS", Integer.toString(mydevice.getDeviceClass()));
	        	c.put("PRODUCTID", Integer.toString(mydevice.getProductId()));
	        	c.put("VENDORID", Integer.toString(mydevice.getVendorId()));
	        	c.put("SUBCLASS", Integer.toString(mydevice.getDeviceSubclass()));
	        	this.add(c);
	        }
        }
    }
}
