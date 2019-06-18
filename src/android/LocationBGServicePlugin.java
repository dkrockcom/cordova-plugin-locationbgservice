package org.apache.cordova.locationbgservice;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * This class exposes methods in Cordova that can be called from JavaScript.
 */
public class LocationBGServicePlugin extends CordovaPlugin {

    public static final int PERMISSION_DENIED_ERROR = 20;
    public static final int TAKE_LOCATION = 0;

    protected final static String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    public CallbackContext _callbackContext;

    public void callsServiceStart() {
        boolean locationPermission = PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                && PermissionHelper.hasPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        try {
            if (!locationPermission) {
                locationPermission = true;
                try {
                    PackageManager packageManager = this.cordova.getActivity().getPackageManager();
                    String[] permissionsInPackage = packageManager.getPackageInfo(this.cordova.getActivity().getPackageName(), PackageManager.GET_PERMISSIONS).requestedPermissions;
                    if (permissionsInPackage != null) {
                        for (String permission : permissionsInPackage) {
                            if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                                locationPermission = false;
                                break;
                            }

                            if (permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                locationPermission = false;
                                break;
                            }
                        }
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    // We are requesting the info for our package, so this should
                    // never be caught
                }
            }

            if (locationPermission) {
                serviceStart();
            } else if (!locationPermission) {
                PermissionHelper.requestPermission(this, TAKE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION);
            }
        } catch (IllegalArgumentException e) {
            this._callbackContext.error("Illegal Argument Exception");
            PluginResult r = new PluginResult(PluginResult.Status.ERROR);
            this._callbackContext.sendPluginResult(r);
        }
    }

    public void serviceStart() {
        cordova.getActivity().startService(new Intent(cordova.getContext(), LocationBGGoogleService.class));
        _callbackContext.success("Location Background service started.");
        LocationBackgroundServiceUtil.IsEnabled = true;
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback context from which we were invoked.
     */
    @SuppressLint("NewApi")
    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {
        _callbackContext = callbackContext;
        LocationBackgroundServiceUtil.callbackContext = _callbackContext;
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(cordova.getContext());
        if (action.equals("start")) {
            if (LocationBackgroundServiceUtil.IsEnabled) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("enabled", true);
                jsonObject.put("message", "Service is already in running mode.");
                this._callbackContext.success(jsonObject);
            } else {
                LocationBackgroundServiceUtil.ClientId = args.getString(0);
                LocationBackgroundServiceUtil.Url = args.getString(1);
                LocationBackgroundServiceUtil.Interval = args.getInt(2);
                callsServiceStart();
            }
        } else if (action.equals("stop")) {
            LocationBackgroundServiceUtil.IsEnabled = false;
            cordova.getActivity().stopService(new Intent(cordova.getContext(), LocationBGGoogleService.class));
            LocationBackgroundServiceUtil.mTimer.cancel();
            LocationBackgroundServiceUtil.wakelock.release();
        } else if (action.equals("isRunning")) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("enabled", true);
            jsonObject.put("message", "Service is " + (LocationBackgroundServiceUtil.IsEnabled ? "" : "not ") + " running.");
            this._callbackContext.success(jsonObject);
        } else {
            return false;
        }
        return true;
    }

    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_DENIED) {
                this._callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
                return;
            }
        }

        switch (requestCode) {
            case TAKE_LOCATION:
                serviceStart();
                break;
        }
    }
}