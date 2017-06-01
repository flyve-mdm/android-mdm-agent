package org.fusioninventory.categories;

import android.content.Context;

public class PhoneStatus
        extends Category {

 
    /**
     * 
     */
    private static final long serialVersionUID = 872718741270132229L;

    public PhoneStatus(Context xCtx, String xType) {
        super(xCtx, xType);
        // TODO Auto-generated constructor stub
        //final TelephonyManager mTM = (TelephonyManager) xCtx
        //.getSystemService(Context.TELEPHONY_SERVICE);
        
        //MyPhoneStateListener mPhoneState = new MyPhoneStateListener();
        //mTM.listen(mPhoneState, PhoneStateListener.LISTEN_SERVICE_STATE);
        //this.add(mPhoneState.getCategory());
    }
/*
    
    // TODO Implements a proper way to retrieve those informations
     
    public class MyPhoneStateListener extends PhoneStateListener {
        private Category c = null;

        @Override
        public void onServiceStateChanged(ServiceState serviceState) {
            // TODO Auto-generated method stub
            c = new Category(mCtx, "PHONE_STATUS");
        	super.onServiceStateChanged(serviceState);
            int iState = serviceState.getState();
            String sState = null;
            switch(iState) {
            case ServiceState.STATE_EMERGENCY_ONLY:
                sState = "STATE_EMERGENCY_ONLY";
                break;
            case ServiceState.STATE_IN_SERVICE:
                sState = "STATE_IN_SERVICE";
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                sState = "STATE_OUT_OF_SERVICE";
                break;
            case ServiceState.STATE_POWER_OFF:
                sState = "STATE_POWER_OFF";
                break;
            }
            
            c.put("STATE", sState  );
            c.put("OPERATOR_ALPHA", serviceState.getOperatorAlphaLong());
            c.put("OPERATOR_NUMERIC", serviceState.getOperatorNumeric());
            c.put("ROAMING", new Boolean(serviceState.getRoaming()).toString());
            c.put("NETWORK_SELECTION", serviceState.getIsManualSelection()?"MANUAL":"AUTO");
        }
        
        public Category getCategory() {
        	return c;
        }
        
    }
    
    public PhoneStatus(FusionInventoryApp app) {
        super(app);
        
//        
        final TelephonyManager mTM = (TelephonyManager) ctx
        .getSystemService(Context.TELEPHONY_SERVICE);
//        
//
        MyPhoneStateListener mPhoneState = new MyPhoneStateListener();
//        
        mTM.listen(mPhoneState, PhoneStateListener.LISTEN_SERVICE_STATE);
        this.type = "PHONESTATUS";
//        
//        
        
        this.content.put("", );
        
    //}*/
}
