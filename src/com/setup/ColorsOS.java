package com.setup;

import com.utils.PublicFunction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class ColorsOS {
	
	public static void authority(Context context){
    	try{
        	if (PublicFunction.isAppWorking(context, "com.coloros.safecenter")) {
        		//startActivity(getPackageManager().getLaunchIntentForPackage("com.coloros.safecenter"));
    			Intent intent = new Intent();
    			ComponentName componentName = new ComponentName("com.coloros.safecenter", 
    					"com.coloros.safecenter.permission.PermissionManagerActivity");
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
        	}
	        else if(PublicFunction.isAppWorking(context, "com.oppo.safe")) {
	        	//context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe"));
    			Intent intent = new Intent();
    			ComponentName componentName = new ComponentName("com.oppo.safe", 
    					"com.oppo.safe.permission.PermissionSettingsActivity");
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
	        }
        	else if (PublicFunction.isAppWorking(context, "com.coloros.oppoguardelf")) {
        		//context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.coloros.oppoguardelf"));
    			Intent intent = new Intent();
    			ComponentName componentName = new ComponentName("com.oppo.safe", 
    					"com.oppo.safe.permission.PermissionSettingsActivity");
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
			}
        	else{
        		SetupAuthority.defaultSettings(context);
        	}
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		SetupAuthority.defaultSettings(context);
    	}
	}
	
	
	public static void bootup(Context context){
     	try{
        	if (PublicFunction.isAppWorking(context, "com.coloros.safecenter")) {
        		//startActivity(getPackageManager().getLaunchIntentForPackage("com.huawei.systemmanager"));
    			Intent intent = new Intent();
    			ComponentName componentName = new ComponentName("com.coloros.safecenter", 
    					"com.coloros.safecenter.startupapp.StartupAppListActivity");
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
        	}

        	else if (PublicFunction.isAppWorking(context, "com.oppo.safe")) {
    			Intent intent = new Intent();
    			ComponentName componentName = new ComponentName("com.oppo.safe", 
    					"com.oppo.safe.permission.startup.StartupAppListActivity");
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
        		//context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.oppo.safe"));
        		//com.oppo.safe/.permission.startup.StartupAppListActivity
        	}
        	else if (PublicFunction.isAppWorking(context, "com.coloros.oppoguardelf")) {
        		//context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.coloros.oppoguardelf"));
    			Intent intent = new Intent();
    			ComponentName componentName = new ComponentName("com.oppo.safe", 
    					"com.oppo.safe.permission.startup.StartupAppListActivity");
    			intent.setComponent(componentName);
    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(intent);
        		
			}
        	else{
        		SetupAuthority.defaultSettings(context);
        	}
    	}catch(Exception ex){
    		ex.printStackTrace();
    		SetupAuthority.defaultSettings(context);
    	}
	}
}
