package com.setup;

import com.utils.PublicFunction;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class MIUI {
	public static void bootup(Context context){
       	if (PublicFunction.isAppWorking(context,"com.miui.securitycenter") ||
       			PublicFunction.isServiceWorking(context, "com.miui.securitycenter")) {
        	Intent intent;
        	intent = new Intent(Intent.ACTION_VIEW);
        	intent.setClassName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity");
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	context.startActivity(intent);
		}else{
        	Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            ComponentName componentName = new ComponentName("com.miui.securitycenter", 
            		"com.miui.appmanager.AppManagerMainActivity");
            intent.setComponent(componentName);
            intent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(intent);
		}
       	
       
	}
	
	
	public static void authority(Context context){
    	if (PublicFunction.isServiceWorking(context, "com.miui.securitycenter")||
    			PublicFunction.isAppWorking(context, "com.miui.securitycenter")) {
    		try{
        		Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
	            ComponentName componentName = new ComponentName("com.miui.securitycenter", 
	            		"com.miui.permcenter.permissions.PermissionsEditorActivity");
	            intent.setComponent(componentName);
	            intent.putExtra("extra_pkgname", context.getPackageName());
	            context.startActivity(intent);
        	}catch(Exception ex){
        		try{
		        	Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
		            ComponentName componentName = new ComponentName("com.miui.securitycenter", 
		            		"com.miui.permcenter.permissions.AppPermissionsEditorActivity");
		            intent.setComponent(componentName);
		            intent.putExtra("extra_pkgname", context.getPackageName());
		            context.startActivity(intent);
        		}catch(Exception e){
        			
	        		try{
			        	Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
			            ComponentName componentName = new ComponentName("com.miui.securitycenter", 
			            		"com.miui.appmanager.AppManagerMainActivity");
			            intent.setComponent(componentName);
			            intent.putExtra("extra_pkgname", context.getPackageName());
			            context.startActivity(intent);
	        		}catch(Exception ee){
	        			ee.printStackTrace();
	        		}
        			
        			//Log.e(TAG,"xiaomi app permission open error");
        		}
        	}
    	}
    	else{
        	Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            ComponentName componentName = new ComponentName("com.miui.securitycenter", "com.miui.appmanager.AppManagerMainActivity");
            intent.setComponent(componentName);
            intent.putExtra("extra_pkgname", context.getPackageName());
            context.startActivity(intent);
    	}
	}
}
