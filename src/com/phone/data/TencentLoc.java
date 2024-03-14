package com.phone.data;

/*
import com.tencent.map.geolocation.TencentLocation;
import com.tencent.map.geolocation.TencentLocationListener;
import com.tencent.map.geolocation.TencentLocationManager;
import com.tencent.map.geolocation.TencentLocationManagerOptions;
import com.tencent.map.geolocation.TencentLocationRequest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.util.Log;



public class TencentLoc implements Runnable,TencentLocationListener{
	
	private TencentLocationListener listener = null;
	
	private TencentLocationManager locationManager = null;
	
	public static final int REQUEST_LEVEL_POI = 4;
	
	private TencentLocationRequest request = null;
	
	private Context mContext;

	private String TAG = "TencentLoc";
	

	public TencentLoc(Context context){
		
		mContext = context;
				
	}
	
	
	public void stopLocation(Context context){
		if(listener != null){
			locationManager.removeUpdates(listener);
		}
	}





	@SuppressLint("SdCardPath") @Override
	public void run() {
		Looper.prepare();
		
		try {
		
			listener = this;
			
			request = TencentLocationRequest.create();
			
			locationManager = TencentLocationManager.getInstance(mContext);
			
			//request.setRequestLevel(REQUEST_LEVEL_POI);
			
			TencentLocationManagerOptions.setLoadLibraryEnabled(false);
			
	//		String libpath = context.getApplicationInfo().nativeLibraryDir();
	
			String libpath = "/data/data/" + mContext.getPackageName() + "/lib/libtencentloc.so";
			System.load(libpath);
			
			int error = locationManager.requestLocationUpdates(request, listener);

			Log.e(TAG,"result:" + error);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Looper.loop();
	}


    @Override
    public void onLocationChanged(TencentLocation location, int error, String reason) {
    	Log.e(TAG,"onLocationChanged");
        if (TencentLocation.ERROR_OK == error) {

        	PhoneLocationListener.sendLocation("NETWORK", 
        			location.getLatitude(), location.getLongitude(), location.getAddress(), mContext);
        } else {
            return;
        }
    }
  
    @Override
    public void onStatusUpdate(String name, int status, String desc) {
        Log.e(TAG,"onStatusUpdate");
    }   

}

*/
