package org
.fusioninventory.categories;

import android.content.Context;

public class Processes
        extends Category {

    public Processes(Context xCtx, String xType) {
        super(xCtx, xType);
        // TODO Auto-generated constructor stub
/*
        Category c = new Category(mCtx, "PROCESSES");
        ActivityManager  activityManager = (ActivityManager) mFusionApp.getSystemService(Service.ACTIVITY_SERVICE);

        List<RunningAppProcessInfo> ps = activityManager.getRunningAppProcesses();
        for(RunningAppProcessInfo process : ps) {
              content.add(new Processes(mFusionApp,process));
          }
          */
    }

    /**
     * 
     */
    private static final long serialVersionUID = 5399654900099889897L;

   

}
