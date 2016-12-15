package org.fusioninventory.categories;

import java.io.File;
import org.fusioninventory.FusionInventory;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class Drives extends Categories {


    /**
	 * 
	 */
	private static final long serialVersionUID = 6073387379988815108L;

	public Drives(Context xCtx) {
        super(xCtx);
        
        this.addStorage(xCtx, Environment.getRootDirectory());
        this.addStorage(xCtx, Environment.getExternalStorageDirectory());
	    this.addStorage(xCtx, Environment.getDataDirectory());
	    this.addStorage(xCtx, Environment.getDownloadCacheDirectory());
    }
    
    /**
     * Add a storage to inventory
     * @param xCtx the Context
     * @param f the partition to inventory
     */
    private void addStorage(Context xCtx, File f) {
        int toMega = 1048576;
    	Category c = new Category(xCtx, "DRIVES");
        c.put("VOLUMN", f.toString());

        FusionInventory.log(this, "Inventory volum "+f.toString() , Log.VERBOSE);
        
        //Android 2.3.3 or higher
        if(Build.VERSION.SDK_INT > 8) {
        	FusionInventory.log(this, "SDK > 8, use SDK to get total and free disk space", Log.VERBOSE);
        	Long total = f.getTotalSpace();
	        total = total / toMega;
	      	c.put("TOTAL", total.toString());
	        Long free = f.getFreeSpace();
	        free = free / toMega;
	      	c.put("FREE", free.toString());
        //Android < 2.3.3
        } else {
            FusionInventory.log(this, "SDK < 8 use StatFS", Log.VERBOSE);

            StatFs stat = new StatFs(f.toString());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            double total = totalBlocks * blockSize /toMega;
        	//double total = (stat.getBlockSize() * stat.getBlockSize()) / toMega;
        	
        	c.put("TOTAL", String.valueOf(total));
            long freeBlocks = stat.getFreeBlocks();
        	double free = freeBlocks * blockSize / toMega;
        	c.put("FREE", String.valueOf(free));
        }
        this.add(c);
    }

}
