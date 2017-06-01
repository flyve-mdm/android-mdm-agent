package org.fusioninventory.categories;

import java.util.List;

import android.app.Service;
import android.content.Context;
import android.location.LocationManager;
import android.location.LocationProvider;

public class LocationProviders extends Categories {

    /**
     * 
     */
    private static final long serialVersionUID = 6066226866162586918L;

    public LocationProviders(Context ctx) {
        super(ctx);
        // TODO Auto-generated constructor stub
        LocationManager lLocationMgr = (LocationManager) mCtx.getSystemService(Service.LOCATION_SERVICE);
        
        List<String> lProvidersName = lLocationMgr.getAllProviders(); 
        
        for (String p : lProvidersName) {
            LocationProvider lProvider = lLocationMgr.getProvider(p);
            Category c = new Category(mCtx,"LOCATION_PROVIDERS");
            c.put("NAME" , lProvider.getName());
//            c.put("COST", String.valueOf(lProvider.hasMonetaryCost()) );
            this.add(c);
        }
        
        
        
    }

}
