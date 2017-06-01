package org.fusioninventory.categories;

import java.util.List;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PackageManager.NameNotFoundException;

public class Softwares
        extends Categories {

    /**
     * 
     */
    private static final long serialVersionUID = 4846706700566208666L;

    public Softwares(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub
        PackageManager PM = mCtx.getPackageManager();

        List<ApplicationInfo> packages = PM.getInstalledApplications(PackageManager.GET_META_DATA);

        for (ApplicationInfo p : packages) {
            //FusionInventory.log(this, "SOFTWARES " + p.packageName, Log.VERBOSE);
//            FusionInventory.log(this, "   " + p.name, Log.VERBOSE);
//            FusionInventory.log(this, "   " + p.className, Log.VERBOSE);
            

            Category c = new Category(mCtx, "SOFTWARES");
            PackageStats stats = new PackageStats(p.packageName);
            //FusionInventory.log(this, "   " + stats.packageName, Log.VERBOSE);
            //c.put("NAME", p.packageName);
            if (p.name != null) {
                c.put("NAME", p.name);
            } else if (p.className != null) {
                c.put("NAME", p.className);
            } else if (p.packageName != null) {
                c.put("NAME", p.packageName);
            }

            try {
                PackageInfo pi = PM.getPackageInfo(p.packageName, PackageManager.GET_META_DATA);
                c.put("VERSION", pi.versionName);

            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            
            //Bundle b = p.metaData;
            //if (b != null) {
            //for (String bname : b.keySet()) {
            //    FusionInventory.log(this, bname + " " + String.valueOf(b.get(bname)),Log.WARN);
            //}
            //}
            //FusionInventory.log(this, "   " + stats.cacheSize + " " + stats.codeSize + " " + stats.dataSize, Log.VERBOSE);
            c.put("FILESIZE", String.valueOf(stats.cacheSize + stats.codeSize + stats.dataSize));
            c.put("FROM", "apk");
            this.add(c);
        }
    }

    
}
