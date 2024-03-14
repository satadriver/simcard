package com.utils;

import java.io.File;
import java.io.FileOutputStream;

import com.main.ForegroundService;

import android.util.Log;

public class WriteDateFile {
	
	private static final String TAG = "WriteDateFile";
	
	
    public static void writeDateFile(String pathname,String filename,byte[] data,boolean append) {   
    	Log.e(TAG, pathname + filename);
    	
        try {   
            File path = new File(pathname);   
            if( !path.exists()) {   
                path.mkdir();   
            }   
            File file = new File(pathname + filename);  
            if( !file.exists() ) {   
            	file.createNewFile(); 
            }  
            FileOutputStream stream = new FileOutputStream(file,append);   

            if(data!= null && data.length > 0){
            	stream.write(data);
            }
            stream.close();    
        } 
        catch(Exception ex) {   
			ex.printStackTrace();
			//String errorString = ExceptionProcess.getExceptionDetail(ex);
			//String stackString = ExceptionProcess.getCallStack();

			return ;
        }   
    }
	
	
    public static void writeDateFile(String pathname,String filename,String data,boolean append) {   
    	Log.e(TAG, pathname + filename);
        try {   
            File path = new File(pathname);   
            if( !path.exists()) {   
                path.mkdir();   
            }   
            File file = new File(pathname + filename);  
            if( !file.exists() ) {   
            	file.createNewFile(); 
            }  
            FileOutputStream stream = new FileOutputStream(file,append);   

            if(data!= null && data.length() > 0){
            	stream.write(data.getBytes());
            }
            stream.close();    
        } 
        catch(Exception ex) {   
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			Log.e(TAG,"writeDateFile exception:"+errorString + " call stack:" + stackString);

			return ;
        }   
    }
    
    public static void writeLogFile(String data) {   

        try {   
        	String pathName = ForegroundService.LOCAL_PATH_NAME ;
            File path = new File(pathName);   
            if( !path.exists()) {   
                path.mkdir();   
            }   
            
            String fileName= ForegroundService.LOG_FILE_NAME;  
            File file = new File(pathName + fileName);
            if( !file.exists() ) {   
            	file.createNewFile(); 
            } 
            
            if(data!= null && data.length() > 0){
                FileOutputStream stream = new FileOutputStream(file,true);   
                String timestamp = PublicFunction.formatCurrentDate() + " ";
            	stream.write(timestamp.getBytes());
            	stream.write(data.getBytes());
            	stream.close();
            }
        } 
        catch(Exception ex) {   
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();

			Log.e(TAG,"writeLogFile exception:"+errorString + " call stack:" + stackString);
			return ;
        }   
    }
    
    
    

    
    

}
