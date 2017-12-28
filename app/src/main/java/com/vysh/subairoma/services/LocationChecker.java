package com.vysh.subairoma.services;

import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;

import com.vysh.subairoma.R;

/**
 * Created by Vishal on 12/28/2017.
 */

public class LocationChecker extends Service{
    private final int LOCATION_VALIDITY_DURATION_MS = 2000;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        requestLocation();
        return super.onStartCommand(intent, flags, startId);
    }

    private void requestLocation() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_LOW);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);
        Log.d("mylog", "In Requesting Location");
        /*
        if (location != null && (System.currentTimeMillis() - location.getTime()) <= LOCATION_VALIDITY_DURATION_MS) {
            myCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
            //cityName = getCityName(myCoordinates);
            if (!isSearch)
                getNearBy(myCoordinates);
            else
                doSearch(searchTerm, myCoordinates);
            Log.d("mylog", "Last known location: " + location.getLatitude() + ":" + location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myCoordinates, 13.0f));
            if (marker == null) {
                marker = mMap.addMarker(markerOptions.position(myCoordinates));
            } else
                marker.setPosition(myCoordinates);
        } else {
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setNumUpdates(1);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            Log.d("mylog", "Last location too old getting new location!");
            showSnackbar(getResources().getString(R.string.fetchCurLoc));
            mFusedLocationClient.requestLocationUpdates(locationRequest,
                    mLocationCallback, Looper.myLooper());
            //locationManager.requestLocationUpdates();
        }
        //locationManager.requestSingleUpdate(provider, this);
        */
    }
}
