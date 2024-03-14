package com.phone.data;


import com.network.SendDataToServer;
import com.utils.Public;
import com.utils.PublicFunction;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class PhoneLocationListener implements LocationListener{
	private String TAG = "PhoneLocationListener";
	public static String gLocationType = null;
	private Context context = null;
	public static LocationListener gLocationListener = null;

	public PhoneLocationListener(Context context){
		this.context = context;
	}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.d(TAG,"location onStatusChanged");
	}
	
	@Override
	public void onProviderEnabled(String provider) {
		Log.d(TAG,"location onProviderEnabled");
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.d(TAG,"location onProviderDisabled");
	}
	
	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG,"location onLocationChanged");
		
		if (location != null) {   
			sendLocation(gLocationType, location.getLatitude(),location.getLongitude(),"", context);
		}
	}


	

	
	public static void sendLocation(String type,double latitude,double longitude,String info,Context context){
		try {

			JSONObject objloc = new JSONObject();
			objloc.put("status", type);
			objloc.put("time", PublicFunction.formatCurrentDate());
			objloc.put("latitude", String.valueOf(latitude));
			objloc.put("longitude", String.valueOf(longitude));
			objloc.put("address", info);

			JSONArray jsarray=new JSONArray();
			
			jsarray.put(0,objloc);

			new Thread(new SendDataToServer(jsarray.toString().getBytes(), 
			        jsarray.toString().getBytes().length,Public.CMD_DATA_LOCATION, Public.IMEI)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	

	

}
