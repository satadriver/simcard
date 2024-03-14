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


//Doze �������豸��ʱ�䲻ʹ��ʱ��ͨ���ӳٺ�̨CPU������Ļ�����ٵ�ص�����,App Standby���ӳ�û�н�����app������ 
//Doze��App Standby��Android6.0���ϵİ汾�Ϲ�������App����Ϊ

//Doze������
//������ʹ���
//ϵͳ���Ի�������wake locks.��
//��׼��AlarmManager���ӱ��Ƴٵ���һ��Maintenance Window����
//ϵͳ�������WIFIɨ��
//ϵͳ������sync adapters����
//ϵͳ������JobScheduler����
//ps:ÿ��Appÿ15���ӻ��Ѵ���Ҳ���ܳ���һ��

//�û���ʾ������app
//app��ǰ̨��һ������һ��Activity��һ��ǰ̨��service���򱻵��õ�Activity��ǰ̨service��
//app�����������֪ͨ������һ��notification 
//���豸���ʱ��ϵͳ��app��Standby״̬���ͷŵ������������ɷ������磬ִ������������ͬ������
//����豸��ʱ����У�ϵͳ��������е�appһ�����һ������

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
	                //��������������Ӧ������ҳ���û��Լ�ȥ����
	                //activity.startActivity(new Intent("android.settings.IGNORE_BATTERY_OPTIMIZATION_SETTINGS")); 
	                //������������Ȩ��
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

