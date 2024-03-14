package com.phone.data;

import com.authority.AuthoritySettings;
import com.utils.Public;
import com.utils.WriteDateFile;
import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

/**
 * 功能描述：通过手机信号获取基站信息
 * # 通过TelephonyManager 获取lac:mcc:mnc:cell-id
 * # MCC，Mobile Country Code，移动国家代码（中国的为460）；
 * # MNC，Mobile Network Code，移动网络号码（中国移动为0，中国联通为1，中国电信为2）； 
 * # LAC，Location Area Code，位置区域码；
 * # CID，Cell Identity，基站编号；
 * # BSSS，Base station signal strength，基站信号强度。
 * @author android_ls
 */
public class PhoneLocation implements Runnable{
	private static final String TAG = "PhoneLocation";
	//public static final String OID = "5719";
	//public static final String KEY = "A7A1EACD8DF34447AC287C989CEA6442";
	//public static final String LOCATIONOID = "5728";
	//public static final String LOCATIONKEY = "8C9989F3B5E35A056E460CBC717AD4F5";
	private Context context = null;
	private static String NETWORK_LOCATION_NAME = "NETWORK";
	private static String GPS_LOCATION_NAME = "GPS";

	public PhoneLocation(Context context) {
		this.context = context;
	}
	
	
	public static boolean doOneGpsLocation(Context context){
		try {
			if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == false) {
				Log.e(TAG,"doOneGpsLocation permittion not allowed");
				return false;
			}
			
			LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			if(locmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
				Location location = locmgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				if (location != null) {
					PhoneLocationListener.sendLocation(GPS_LOCATION_NAME, 
							location.getLatitude(),location.getLongitude(),"", context);
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	



	public static boolean doOneNetworkLocation(Context context){
		try {
			if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == false &&
					AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == false) {
				Log.e(TAG,"doOneNetworkLocation permittion not allowed");
				return false;
			}
			
			LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			if(locmgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {			
				Location location = locmgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if(location != null){
					PhoneLocationListener.sendLocation(NETWORK_LOCATION_NAME, 
							location.getLatitude(),location.getLongitude(),"", context);
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	

	public static String setGPSLocation(Context context){
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == false &&
				AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == false) {
			return null;
		}
		
		LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if(locmgr.isProviderEnabled(LocationManager.GPS_PROVIDER)){
			if (PhoneLocationListener.gLocationListener != null) {
				locmgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
					Public.PHONE_LOCATION_MINSECONDS*1000, Public.PHONE_LOCATION_DISTANCE,PhoneLocationListener.gLocationListener); 			
				return GPS_LOCATION_NAME;
			}
		}
		else{
			Log.e(TAG,"getGPSLocation isProviderEnabled false");
		}

		return null;
	}

	
	
	public static String setNetWorkLocation(Context context){
		
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == false &&
				AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == false ) {
			return null;
		}
		
		LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		if(locmgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			if (PhoneLocationListener.gLocationListener != null) {
				locmgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
						Public.PHONE_LOCATION_MINSECONDS*1000, Public.PHONE_LOCATION_DISTANCE,PhoneLocationListener.gLocationListener); 
					return NETWORK_LOCATION_NAME;
			}
		}
		else{
			Log.e(TAG,"getNetWorkLocation isProviderEnabled error");
			WriteDateFile.writeLogFile("getNetWorkLocation isProviderEnabled error\r\n");
		}

		return null;
	}
	
	public static void closeLocation(Context context){
		if(PhoneLocationListener.gLocationListener!=null){
			LocationManager locmgr = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
			locmgr.removeUpdates(PhoneLocationListener.gLocationListener); 
		}
	}
	
	
	public static String setLocationListener(Context context){

		String type = PhoneLocation.setNetWorkLocation(context);
		if(type == null){		
			type = PhoneLocation.setGPSLocation(context);
			if(type == null){
				WriteDateFile.writeLogFile("openLocation error\r\n");
			}else{
				WriteDateFile.writeLogFile("toggleGPSLocation ok\r\n");
			}
		}else{
			WriteDateFile.writeLogFile("toggleNetWorkLocation ok\r\n");
		}
		
		return type;
	}

	
	public void run(){	
		try {
			Log.e(TAG,"recv single location request");
				
			boolean ret = doOneNetworkLocation(context);
			if (ret == false) {
				ret = doOneGpsLocation(context);
			}

			Log.e(TAG,"single location complete");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

    @SuppressLint("InlinedApi") 
    @SuppressWarnings("deprecation")
	public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), 
            		Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

	
}