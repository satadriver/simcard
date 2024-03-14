package com.setup;

import com.utils.PublicFunction;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;


public class EMUI {
	public static void bootup(Context context){
    	Intent intent = new Intent();
    	if (Build.VERSION.SDK_INT >= 24) {
			try{
    			ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity");       			
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
    	else if (PublicFunction.isAppWorking(context, "com.huawei.systemmanager")) {
    		//startActivity(getPackageManager().getLaunchIntentForPackage("com.huawei.systemmanager"));
			try{
    			ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity");       			
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
			}
			catch(ActivityNotFoundException ex){
    			try{
    				//Log.e(TAG, "ActivityNotFoundException");
    				ex.printStackTrace();
        			ComponentName componentName = new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.bootstart.BootStartActivity");       			
        			intent.setComponent(componentName);
        			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			context.startActivity(intent);
    			}
    			catch(Exception except){
        			ex.printStackTrace();
    			}
			}
    	}
    	else{
    		SetupAuthority.defaultSettings(context);
    	}
	}
	
	
	public static void authority(Context context){
		try {

	    		//startActivity(getPackageManager().getLaunchIntentForPackage("com.huawei.systemmanager"));
				Intent intent = new Intent();
				ComponentName componentName = new ComponentName("com.huawei.systemmanager", 
						//"com.huawei.systemmanager.power.ui.DetailOfSoftConsumptionActivity"
						"com.huawei.permissionmanager.ui.MainActivity");
				intent.setComponent(componentName);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);

		} catch (Exception e) {
			e.printStackTrace();
			
			SetupAuthority.defaultSettings(context);
		}
	}
}
