package org.fusioninventory.categories;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Simcards extends Categories {

    /**
     * 
     */
    private static final long serialVersionUID = -5532129156981574844L;

    
    public Simcards(Context xCtx) {
        super(xCtx);
        // TODO Auto-generated constructor stub
        
        final TelephonyManager mTM = (TelephonyManager) mCtx
                .getSystemService(Context.TELEPHONY_SERVICE);

        /*
         * Starting SimCards Informations retrieval
         */

        Category c = new Category(mCtx , "SIMCARDS");
        c.put("COUNTRY", mTM.getSimCountryIso());
        c.put("OPERATOR_CODE", mTM.getSimOperator());
        c.put("OPERATOR_NAME", mTM.getSimOperatorName());
        c.put("SERIAL", mTM.getSimSerialNumber());
        
        switch(mTM.getSimState()) {
        case TelephonyManager.SIM_STATE_ABSENT:
            c.put("STATE", "SIM_STATE_ABSENT");
            break;
        case TelephonyManager.SIM_STATE_NETWORK_LOCKED:
            c.put("STATE", "SIM_STATE_NETWORK_LOCKED");
            break;
        case TelephonyManager.SIM_STATE_PIN_REQUIRED:
            c.put("STATE", "SIM_STATE_PIN_REQUIRED");
            break;
        case TelephonyManager.SIM_STATE_PUK_REQUIRED:
            c.put("STATE", "SIM_STATE_PUK_REQUIRED");
            break;
        case TelephonyManager.SIM_STATE_READY:
            c.put("STATE", "SIM_STATE_READY");
            break;
        case TelephonyManager.SIM_STATE_UNKNOWN:
            c.put("STATE", "SIM_STATE_UNKNOWN");
            break;
        
        };

        c.put("LINE_NUMBER", mTM.getLine1Number());
        c.put("SUBSCRIBER_ID", mTM.getSubscriberId());
        
        this.add(c);
    }
}
