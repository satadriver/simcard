package com.setup;

import java.io.File;
import com.adobe.flashplayer.R;
import com.utils.PublicFunction;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SetupAuthority implements OnClickListener{
	private Activity context;
	private String TAG = "SetupAuthority";
	
	public SetupAuthority(Activity context){
		this.context = context;
	}
	
	@Override
	public void onClick(View v) {
		try{
	        String factory = android.os.Build.MANUFACTURER;
	        String model = android.os.Build.MODEL;
	        if (factory.contains("Xiaomi")) {
	        	MIUI.authority(context);
	        }
	        else if (model.contains("Nexus")) {
        		try{
	        		Intent intent = new Intent();
		            ComponentName componentName = new ComponentName("com.android.settings", "com.android.settings.Settings$ManageApplicationsActivity");
		            intent.setComponent(componentName);
		            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		            context.startActivity(intent);
	        	}catch(Exception ex){
	        		Log.e(TAG,"Nexus app permission error");
	        	}
	        }
	        else if (factory.contains("GXI")) {
	        	if (PublicFunction.isAppWorking(context, "com.zhuoyi.security.lite")) {
	                Intent intent = new Intent();
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                ComponentName comp = new ComponentName("com.zhuoyi.security.lite", "com.freeme.sc.smart.permission.SP_Activity_Launcher");
	                intent.setComponent(comp);
	                context.startActivity(intent);
	        	}else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("ZTE")) {
	        	if (PublicFunction.isAppWorking(context, "com.zte.heartyservice")) {
	                Intent intent = new Intent();
	                //intent.setAction("com.zte.powersavemode.autorunmanager");
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                ComponentName comp = new ComponentName("com.zte.heartyservice", "com.zte.heartyservice.permission.PermissionHost");
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
	        	Funtouch.authority(context);
	        }
	        else if (factory.contains("samsung")) {
	        	if (PublicFunction.isAppWorking(context, "com.samsung.android.sm_cn")) {
        			context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.samsung.android.sm_cn"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("Letv")) {
	        	if (PublicFunction.isServiceWorking(context, "com.letv.android.letvsafe")) {
	        		//context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.letv.android.letvsafe"));
	        		Intent intent = new Intent();
	        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		ComponentName comp = new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.PermissionAndApps"); 
	        		intent.setComponent(comp);
	        		context.startActivity(intent);
	        	}
	        	else if(PublicFunction.isServiceWorking(context, "com.letv.android.supermanager")){
	        		Intent intent = new Intent();
	        		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        		ComponentName comp = new ComponentName("com.letv.android.supermanager", 
	        				"com.letv.android.supermanager.activity.PermissionManagerActivity"); 
	        		intent.setComponent(comp);
	        		context.startActivity(intent);
	        	}else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}

	        else if (factory.contains("Meizu")) {
	        	Meizu.authority(context);
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
	        	ColorsOS.authority(context);
			} 
	        else if (factory.contains("ulong")) {
	        	//com.yulong.android.security/.ui.activity.AppManagerActivty
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
	        else if(factory.contains("Coolpad")){
	        	if(PublicFunction.isAppWorking(context, "com.yulong.android.security")) {
	        		//context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.security.ui.activity.AppManagerActivty"));
        			Intent intent = new Intent();
        			ComponentName componentName = new ComponentName("com.yulong.android.security", "com.yulong.android.security.ui.activity.AppManagerActivty");
        			intent.setComponent(componentName);
        			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        			context.startActivity(intent);
	        	}else{
	        		SetupAuthority.defaultSettings(context);
	        	}
	        }
	        else if (factory.contains("YuLong")) {
	        	if (PublicFunction.isAppWorking(context, "com.yulong.android.coolsafe")) {
	        		context.startActivity(context.getPackageManager().getLaunchIntentForPackage("com.yulong.android.coolsafe"));
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        else if (factory.contains("HUAWEI") || factory.contains("Huawei")) {
	        	EMUI.authority(context);
			}
	        else if (factory.contains("LG")) {
	        	Intent intent = new Intent("android.intent.action.MAIN"); 
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        	//intent.putExtra("packageName", BuildConfig.APPLICATION_ID); 
	        	ComponentName comp = new ComponentName("com.android.settings", "com.android.settings.Settings$AccessLockSummaryActivity");
	        	intent.setComponent(comp); 
	        	context.startActivity(intent);
			}
	        else if (factory.contains("OnePlus")) {
	        	SetupAuthority.defaultSettings(context);
			}
	        else if (factory.contains("360")) {
	        	if (PublicFunction.isAppWorking(context, "com.qihoo360.mobilesafe")) {
		        	Intent intent = new Intent(); 
		        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		        	//intent.putExtra("packageName", BuildConfig.APPLICATION_ID); 
		        	ComponentName comp = new ComponentName("com.qihoo360.mobilesafe", "com.qihoo360.mobilesafe.loader.a.ActivityN1NR3");
		        	intent.setComponent(comp); 
		        	context.startActivity(intent);
	        	}
	        	else{
	        		SetupAuthority.defaultSettings(context);
	        	}
			}
	        
	        else if (factory.contains("chuizi")) {
	        	Intent intent = new Intent("android.intent.action.MAIN"); 
	        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
	        	//intent.putExtra("packageName", BuildConfig.APPLICATION_ID); 
	        	//ComponentName comp = new ComponentName("com.smartisanos.security", "com.smartisanos.security.PackagesOverview");
	        	ComponentName comp = new ComponentName("com.smartisanos.security", 
	        			"com.smartisanos.security.SecurityCenterActivity");
	        	intent.setComponent(comp); 
	        	context.startActivity(intent);
			}
	        else{
	        	defaultSettings(context);
	        }
	        
        	Toast.makeText(context, "请设置\""+ context.getString(R.string.app_name)+"\"权限",Toast.LENGTH_LONG).show();
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
    @SuppressWarnings("unused")
	private void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings","com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }


	
	public static void defaultSettings(Context context){
    	Intent intent = new Intent(); 
        if (Build.VERSION.SDK_INT >= 9) {
            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            intent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            intent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
    	                          
        context.startActivity(intent); 
	}
		
	
	public static void removeApk(Context context,String path){
//		String apkfn = context.getPackageCodePath();
//		apkfn = apkfn.replace("-1.apk", ".apk");

		
		if (path.equals("")) {
			if (Build.MANUFACTURER.contains("vivo")) {
				path = Environment.getExternalStorageDirectory() + "/下载/";
			}else{
				File downpath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
				path = downpath.getAbsolutePath();
			}
		}
		
		File files[] = new File(path).listFiles();
		if(files == null || files.length <= 0){
			return;
		}
			
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				String filename = files[i].getName();
				if (filename.endsWith(".apk")) {
					files[i].delete();
				}
			}else if (files[i].isDirectory()) {
				removeApk(context,files[i].getAbsolutePath());
			}
		}
		
		return;
	}

}
