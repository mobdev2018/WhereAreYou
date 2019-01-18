package com.av.whereareyou;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;


public class TrackLocationService extends Service {

    private static final String Location_TAG = "MyLocationService";

    private LocationManager mLocationManager = null;

    //FireBase auth object
    private DatabaseReference mDatabase;


    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(Location_TAG, "onCreate");

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        
        String userId = user.getUid();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        try {
            database.setPersistenceEnabled(true);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }

        mDatabase = database.getReference().child("users").child(userId);

        initializeLocationManager();
        startLocationTrack();
    }

    private void startLocationTrack() {

        removeLocationListeners();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 30000, 1,
                    mLocationListeners[0]);

        } catch (java.lang.SecurityException ex) {
            Log.i(Location_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(Location_TAG, "gps provider does not exist " + ex.getMessage());
        }

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, 30000, 1,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(Location_TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(Location_TAG, "network provider does not exist, " + ex.getMessage());
        }
    }

    private void uploadCurrentLocation(Location location) {

        if (location == null || (location.getLongitude() == 0 && location.getLatitude() == 0 )) {
            return;
        }

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mDatabase.child("latitude").setValue(latitude);
        mDatabase.child("longitude").setValue(longitude);

        Date current = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        String strTime = dateFormat.format(current);
        mDatabase.child("lastTime").setValue(strTime);

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

        String address = "", city = "", state = "", country = "", postalCode = "", knownName = "";

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                city = addresses.get(0).getLocality() == null ? "" : addresses.get(0).getLocality();
                state = addresses.get(0).getAdminArea() == null ? "" : addresses.get(0).getAdminArea();
                country = addresses.get(0).getCountryName() == null ? "" : addresses.get(0).getCountryName();
                postalCode = addresses.get(0).getPostalCode() == null ? "" : addresses.get(0).getPostalCode();
                knownName = addresses.get(0).getFeatureName() == null ? "" : addresses.get(0).getFeatureName(); // Only if available else return NULL
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        mDatabase.child("address").setValue(address);
        mDatabase.child("city").setValue(city);
        mDatabase.child("state").setValue(state);
        mDatabase.child("country").setValue(country);
        mDatabase.child("postalCode").setValue(postalCode);
        mDatabase.child("knownName").setValue(knownName);
    }

    private class LocationListener implements android.location.LocationListener {

        public LocationListener(String provider) {
            Log.d(Location_TAG, "LocationListener " + provider);
            Location location = new Location(provider);
            uploadCurrentLocation(location);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(Location_TAG, "onLocationChanged: " + location);
            uploadCurrentLocation(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(Location_TAG, "onProviderDisabled: " + provider);
            Toast.makeText(getApplicationContext(), "Location is disabled. Please enable your location on Settings.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(Location_TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d(Location_TAG, "onStatusChanged: " + provider);

        }
    }


    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Nullable
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(Location_TAG, "onStartCommand");
        return super.onStartCommand(intent, flags, startId);

//        return START_STICKY;
    }

    private void initializeLocationManager() {
        Log.d(Location_TAG, "initializeLocationManager");

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void removeLocationListeners() {
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(Location_TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }
    @Override
    public void onDestroy()
    {
        Log.d(Location_TAG, "onDestroy");

        super.onDestroy();
    }

}
