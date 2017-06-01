package org.fusioninventory.categories;

import android.content.Context;

import android.bluetooth.*;
public class BluetoothAdapterCategory
        extends Categories {

    /**
     * 
     */
    private static final long serialVersionUID = 3252750764653173048L;

    public BluetoothAdapterCategory(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub
        
        Category c = new Category(mCtx, "BLUETOOTH_ADAPTER");
        
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        c.put("HMAC" , adapter.getAddress());
        c.put("NAME" , adapter.getName());

        this.add(c);

    }

}
