package com.example.home.superwheather;

/**
 * Created by Home on 04.12.2014.
 */

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

class MyLocationListener implements LocationListener {

    static Location imHere;

    public static void SetUpLocationListener(Context context)
    {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new MyLocationListener();

        locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                10,
                locationListener);

        imHere = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location loc) {
        imHere = loc;
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
}
