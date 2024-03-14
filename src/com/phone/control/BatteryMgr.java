package com.phone.control;




import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;


//Doze 可以在设备长时间不使用时，通过延迟后台CPU和网络的活动来减少电池的消耗,App Standby将延迟没有交互的app网络活动。 
//Doze和App Standby在Android6.0以上的版本上管理所有App的行为

//Doze的限制
//网络访问挂起
//系统忽略唤醒锁（wake locks.）
//标准的AlarmManager闹钟被推迟到下一次Maintenance Window运行
//系统不会进行WIFI扫描
//系统不允许sync adapters运行
//系统不允许JobScheduler工作
//ps:每个App每15分钟唤醒次数也不能超过一次

//用户显示的启动app
//app在前台有一个任务（一个Activity或一个前台的service，或被调用的Activity或前台service）
//app在锁屏界面或通知栏生成一个notification 
//当设备充电时，系统将app从Standby状态中释放掉，允许它自由访问网络，执行任意的任务和同步操作
//如果设备长时间空闲，系统将允许空闲的app一天访问一次网络

@TargetApi(23) public class BatteryMgr {

	private static String TAG = "BatteryMgr";
	
	public static void reboot(Context context){
		PowerManager pManager=(PowerManager) context.getSystemService(Context.POWER_SERVICE);  
		pManager.reboot(null);
	}


    public static void ignoreBatteryOpt(Activity activity,int code) {
    	 try {
        	if (Build.VERSION.SDK_INT >= 23){
        		String packname = activity.getPackageName();
        		PowerManager powerManager = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        		boolean hasIgnored = powerManager.isIgnoringBatteryOptimizations(packname);

        		Intent intent = new Intent();
        	
	            if (hasIgnored) {
	                intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                activity.startActivity(intent);

	                hasIgnored = powerManager.isIgnoringBatteryOptimizations(packname);
	                Log.e(TAG,"request ignore battery result:" + hasIgnored );
	            }else{
	                //方法二，跳到相应的设置页面用户自己去设置
	                //activity.startActivity(new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS")); 
	                //方法二，请求权限
	                //activity.requestPermissions(new String[]{"android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS"}, 0);

	            	intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
	            	Uri uri = Uri.parse("package:" + packname);
	                intent.setData(uri);
	                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	                activity.startActivityForResult(intent,code);
	                Log.e(TAG,"request ignore battery return:" + code );
	                
	                hasIgnored = powerManager.isIgnoringBatteryOptimizations(packname);
	                Log.e(TAG,"request ignore battery result:" + hasIgnored );
	            }
	            
	        }
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    	
	    	Log.e(TAG,"exception");
	    }
	}

	
	public static void noDoze(Context context){
		
	}
}

