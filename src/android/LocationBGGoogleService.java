package org.apache.cordova.locationbgservice;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class LocationBGGoogleService extends Service implements LocationListener {

    boolean isGPSEnable = false;
    boolean isNetworkEnable = false;
    LocationManager locationManager;
    Location location;
    private Handler mHandler = new Handler();

    public LocationBGGoogleService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        LocationBackgroundServiceUtil.mTimer = new Timer();
        LocationBackgroundServiceUtil.mTimer.scheduleAtFixedRate(new TimerTaskToGetLocation(), 5, LocationBackgroundServiceUtil.Interval);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        LocationBackgroundServiceUtil.wakelock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getCanonicalName());
        LocationBackgroundServiceUtil.wakelock.acquire();
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    private void getLocation() {
        LocationBackgroundServiceUtil.wakelock.acquire();
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        try {
            if (!isGPSEnable && !isNetworkEnable) {
                LocationBackgroundServiceUtil.IsEnabled = false;
                LocationBackgroundServiceUtil.callbackContext.error("GPS Location not enabled from phone.");
                LocationBackgroundServiceUtil.mTimer.cancel();
            } else {
                if (isNetworkEnable) {
                    location = null;
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            updateLocation(location);
                            return;
                        }
                    }
                }
                if (isGPSEnable) {
                    location = null;
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
                    if (locationManager != null) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            updateLocation(location);
                            return;
                        }
                    }
                }
            }
        } catch (SecurityException ex) {
            LocationBackgroundServiceUtil.IsEnabled = false;
            LocationBackgroundServiceUtil.callbackContext.error(ex.toString());
        }
    }

    private class TimerTaskToGetLocation extends TimerTask {
        @Override
        public void run() {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    getLocation();
                }
            });
        }
    }

    private void updateLocation(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        double bearing = location.getBearing();
        double accuracy = location.getAccuracy();
        double speed = location.getSpeed();
        String provider = location.getProvider();

        LocationBackgroundServiceUtil.IsEnabled = true;
        Log.d("latutide", String.valueOf(latitude));
        Log.d("longitude", String.valueOf(longitude));

        HttpURLConnection client = null;
        RequestQueue queue = Volley.newRequestQueue(this);
        try {
            StringRequest postRequest = new StringRequest(Request.Method.POST, LocationBackgroundServiceUtil.Url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.d("Response", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            LocationBackgroundServiceUtil.callbackContext.error(error.toString());
                        }
                    }
            ) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("clientId", LocationBackgroundServiceUtil.ClientId);
                    params.put("latitude", String.valueOf(latitude));
                    params.put("longitude", String.valueOf(longitude));
                    params.put("bearing", String.valueOf(bearing));
                    params.put("accuracy", String.valueOf(accuracy));
                    params.put("speed", String.valueOf(speed));
                    params.put("provider", String.valueOf(provider));
                    return params;
                }
            };
            queue.add(postRequest);
        } catch (Exception ex) {
            LocationBackgroundServiceUtil.callbackContext.error(ex.toString());
        }
    }
}