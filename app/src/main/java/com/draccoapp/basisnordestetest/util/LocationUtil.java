package com.draccoapp.basisnordestetest.util;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class LocationUtil {

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationUtil(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public void getCurrentLocation(final LocationCallback callback) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            callback.onLocationResult(null);
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> callback.onLocationResult(location))
                    .addOnFailureListener(e -> callback.onLocationResult(null));
        } catch (Exception e) {
            callback.onLocationResult(null);
        }
    }

    public interface LocationCallback {
        void onLocationResult(Location location);
    }
}

