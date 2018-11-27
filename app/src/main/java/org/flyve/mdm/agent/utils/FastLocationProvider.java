/*
 * Copyright Teclib. All rights reserved.
 *
 * Flyve MDM is a mobile device management software.
 *
 * Flyve MDM is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * Flyve MDM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * ------------------------------------------------------------------------------
 * @author    Rafael Hernandez
 * @copyright Copyright Teclib. All rights reserved.
 * @license   GPLv3 https://www.gnu.org/licenses/gpl-3.0.html
 * @link      https://github.com/flyve-mdm/android-mdm-agent
 * @link      https://flyve-mdm.com
 * ------------------------------------------------------------------------------
 */

package org.flyve.mdm.agent.utils;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Get location fast and easy way
 */
public class FastLocationProvider {

    private Timer timer;
    private LocationManager locationManager;
    private LocationResult locationResult;
    private boolean gpsEnabled = false;
    private boolean networkEnabled = false;

    public boolean getLocation(Context context, LocationResult result) {
        //I use LocationResult callback class to pass location value from MyLocation to user code.
        locationResult = result;
        if(locationManager == null) {
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        //exceptions will be thrown if provider is not permitted.
        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);}
        catch(Exception ex){
            FlyveLog.e(this.getClass().getName() + ", getLocation",ex.getMessage());
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex){
            FlyveLog.e(this.getClass().getName() + ", getLocation",ex.getMessage());
        }

        //don't start listeners if no provider is enabled
        if(!gpsEnabled && !networkEnabled) {
            return false;
        }

        try {
            if (gpsEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
            }

            if (networkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);
            }
        } catch (SecurityException ex) {
            FlyveLog.e(this.getClass().getName() + ", getLocation", ex.getMessage());
        }

        timer = new Timer();
        timer.schedule(new GetLastLocation(), 3000);

        return true;
    }

    private LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        public void onProviderDisabled(String provider) {
            FlyveLog.d("onProviderDisabled");
        }

        public void onProviderEnabled(String provider) {
            FlyveLog.d("onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            FlyveLog.d("onStatusChanged");
        }
    };

    private LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
            FlyveLog.d("onProviderDisabled");
        }

        public void onProviderEnabled(String provider) {
            FlyveLog.d("onProviderEnabled");
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
            FlyveLog.d("onStatusChanged");
        }
    };

    class GetLastLocation extends TimerTask {
        @Override
        public void run() {
            Location netLocation = null;
            Location gpsLocation = null;

            try {
                if (gpsEnabled) {
                    gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }

                if (networkEnabled) {
                    netLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            } catch (SecurityException ex) {
                FlyveLog.e(this.getClass().getName() + ", GetLastLocation", ex.getMessage());
                locationResult.gotLocation(null);
            }

            //if there are both values use the latest one
            if(gpsLocation!=null && netLocation!=null){
                if(gpsLocation.getTime()>netLocation.getTime())
                    locationResult.gotLocation(gpsLocation);
                else
                    locationResult.gotLocation(netLocation);
                return;
            }

            if(gpsLocation!=null) {
                locationResult.gotLocation(gpsLocation);
                return;
            }

            if(netLocation!=null) {
                locationResult.gotLocation(netLocation);
                return;
            }

            locationResult.gotLocation(null);
        }
    }

    public abstract static class LocationResult {
        public abstract void gotLocation(Location location);
    }
}