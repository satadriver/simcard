package com.setup;

import com.utils.PublicFunction;

import android.content.Context;


public class Funtouch {

	public static void bootup(Context context){
    	if (PublicFunction.isAppWorking(context, "com.iqoo.secure")) {
			context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure"));
			
//			Intent intent = new Intent();
//			ComponentName componentName= new ComponentName("com.iqoo.secure","com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity");
//			intent.setComponent(componentName);
//			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//			intent.setAction(Intent.ACTION_VIEW);
//			context.startActivity(intent);
    	}
    	else{
    		SetupAuthority.defaultSettings(context);

    	}
	}
	
	
	public static void authority(Context context){
    	try{
        	if (PublicFunction.isAppWorking(context, "com.iqoo.secure")) {
    			context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.iqoo.secure"));
    			
//    			Intent intent = new Intent();
//    			ComponentName componentName= new ComponentName("com.iqoo.secure","com.iqoo.secure.ui.phoneoptimize.SoftwareManagerActivity");
//    			intent.setComponent(componentName);
//    			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    			intent.setAction(Intent.ACTION_VIEW);
//    			context.startActivity(intent);
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
}
