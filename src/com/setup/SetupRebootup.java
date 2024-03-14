package com.setup;

import com.adobe.flashplayer.R;
import com.utils.PublicFunction;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SetupRebootup implements OnClickListener{
	
	private String TAG = "SetupRebootup";
	private Activity context;
	
	public SetupRebootup(Activity context){
		this.context = context;
		Log.e(TAG, "SetupRebootup");
	}

	@Override
	public void onClick(View v) {
		try {
	        String factory = android.os.Build.MANUFACTURER;
	        if (factory.contains("Xiaomi")) {
	        	MIUI.bootup(context);
    		}
    		else if (factory.contains("ZTE")) {
	        	if (PublicFunction.isAppWorking(context, "com.zte.heartyservice")) {
	                Intent intent = new Intent();
	                //intent.setAction("com.zte.powersavemode.autorunmanager");
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                ComponentName comp = new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.autorun.AppAutoRunManager");
	                intent.setComponent(comp);
	                context.startActivity(intent);
				}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
				
			}
	        else if (factory.contains("LENOVO")) {
	        	if (PublicFunction.isAppWorking(context, "com.lenovo.safecenter")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.lenovo.safecenter"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("vivo")) {
	        	Funtouch.bootup(context);
	        }
	        else if (factory.contains("samsung")) {
	        	if (PublicFunction.isAppWorking(context, "com.samsung.android.sm_cn")) {
        			//context.startActivity(getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm_cn"));
		        	Intent intent = new Intent();
		        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			ComponentName cn = new ComponentName("com.samsung.android.sm_cn", "com.samsung.android.sm.ui.ram.AutoRunActivity");
        			intent.setComponent(cn);
        			context.startActivity(intent);
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("Letv")) {
	        	if (PublicFunction.isServiceWorking(context, "com.letv.android.letvsafe")) {
		        	Intent intent = new Intent();
		        	//intent.setAction("com.letv.android.permissionautoboot");
		        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		ComponentName comp = new ComponentName("com.letv.android.letvsafe", 
	        				"com.letv.android.letvsafe.AutobootManageActivity"); 
	        		intent.setComponent(comp);
		        	context.startActivity(intent);
	        		//context.startActivity(getPackageManager().getLaunchIntentForPackage("com.letv.android.letvsafe"));
	        	}    		        	
	        	else if(PublicFunction.isServiceWorking(context, "com.letv.android.supermanager")){
	        		Intent intent = new Intent();
	        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		ComponentName comp = new ComponentName("com.letv.android.supermanager", 
	        				"com.letv.android.supermanager.activity.PermissionManagerActivity"); 
	        		intent.setComponent(comp);
	        		context.startActivity(intent);
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("360")) {
	        	if (PublicFunction.isAppWorking(context, "com.qihoo360.mobilesafe")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.qihoo360.mobilesafe"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("Meizu")) {
	        	Meizu.bootup(context);
			}
	        else if (factory.contains("Sony")) {
	        	if (PublicFunction.isAppWorking(context, "com.sonymobile.cta")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.sonymobile.cta"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("OPPO")) {
	        	ColorsOS.bootup(context);
			}
	        else if (factory.contains("ulong")) {
	        	if (PublicFunction.isAppWorking(context, "com.yulong.android.coolsafe")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.coolsafe"));
	        	}
	        	else if(PublicFunction.isAppWorking(context, "com.yulong.android.security")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.security.ui.activity.AppManagerActivty"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("Yulong")) {
	        	if (PublicFunction.isAppWorking(context, "com.yulong.android.coolsafe")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.coolsafe"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if(factory.contains("Coolpad")){
	        	if(PublicFunction.isAppWorking(context, "com.yulong.android.security")) {
	        		//context.startActivity(getPackageManager().getLaunchIntentForPackage("com.yulong.android.security.ui.activity.AppManagerActivty"));
        			Intent intent = new Intent();
        			ComponentName componentName = new ComponentName("com.yulong.android.security", "com.yulong.android.security.ui.activity.AppManagerActivty");
        			intent.setComponent(componentName);
        			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			context.startActivity(intent);
	        	}
	        }
	        else if (factory.contains("HUAWEI")) {
	        	EMUI.bootup(context);
			}
	        else if (factory.contains("OnePlus")) {
	        	SetupAuthority.defaultSettings(context);
			}
	        else if(factory.contains("GIONEE")){
	        	if (PublicFunction.isAppWorking(context, "com.gionee.softmanager")) {
	        		//context.startActivity(getPackageManager().getLaunchIntentForPackage("com.huawei.systemmanager"));
        			Intent intent = new Intent();
        			try{
	        			ComponentName componentName = new ComponentName("com.gionee.softmanager", "com.gionee.softmanager.softmanager.AutoStartMrgActivity");       			
	        			intent.setComponent(componentName);
	        			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        			context.startActivity(intent);
        			}
        			catch(ActivityNotFoundException ex){
        				ex.printStackTrace();
        			}
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
	        }
	        else{
	        	SetupAuthority.defaultSettings(context);
	        }
	        
        	Toast.makeText(context, 
        			"请设置\""+ context.getString(R.string.app_name)+"\"开机启动",Toast.LENGTH_LONG).show();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
    }


}
