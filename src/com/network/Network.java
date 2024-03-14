package com.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {
	
	public static final int WIFI_CONNECTION 			= 8;
	public static final int WIRELESS_CONNECTION 		= 4;
	public static final int NONE_CONNECTION 			= 0;
	
	public static int getNetworkType(Context context) {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            int type = networkInfo.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return WIFI_CONNECTION;
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                return WIRELESS_CONNECTION;
            }
            else{
            	return NONE_CONNECTION;
            }
        }
        else{
        	return NONE_CONNECTION;
        }
    }
    
    
    public static boolean isNetworkConnected(Context context) {  
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);  
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();  
        if (networkInfo != null && networkInfo.isConnected()) {  
            return true;  
        }  
         
        return false;  
    }
}
