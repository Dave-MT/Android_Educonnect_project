package com.educonnect.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

/**
 * Utility class to check network connectivity
 */
public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

    /**
     * Check if the device is connected to the internet
     *
     * @param context The application context
     * @return true if connected, false otherwise
     */
    public static boolean isNetworkAvailable(Context context) {
        if (context == null) {
            return false;
        }

        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            NetworkCapabilities capabilities = connectivityManager
                    .getNetworkCapabilities(connectivityManager.getActiveNetwork());

            if (capabilities == null) {
                return false;
            }

            return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET);
        } else {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
    }

    /**
     * Log the current network state
     *
     * @param context The application context
     */
    public static void logNetworkState(Context context) {
        boolean isConnected = isNetworkAvailable(context);
        Log.d(TAG, "Network available: " + isConnected);
    }
}
