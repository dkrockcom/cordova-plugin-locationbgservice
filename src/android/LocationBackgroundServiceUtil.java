package org.apache.cordova.locationbgservice;

import android.os.PowerManager;

import org.apache.cordova.CallbackContext;

import java.util.Timer;

public class LocationBackgroundServiceUtil {
    public static String ClientId;
    public static String Url;
    public static int Interval;
    public static Boolean IsEnabled = false;
    public static CallbackContext callbackContext;
    public static Timer mTimer = null;
    public static PowerManager.WakeLock wakelock;
}