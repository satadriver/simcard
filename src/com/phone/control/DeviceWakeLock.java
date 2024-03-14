package com.phone.control;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class DeviceWakeLock {

	WakeLock wakeLock = null; 
	
	public DeviceWakeLock(Context context){
		
	}
	
	
	 //��ȡ��Դ�������ָ÷�������ĻϨ��ʱ��Ȼ��ȡCPUʱ���������� 
	 @SuppressWarnings("unused")
	private void acquireWakeLock(Context context) 
	 { 
		 if (null == wakeLock) 
		 { 
			 PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE); 
			 wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE, "PostLocationService"); 
			 if (null != wakeLock) 
			 { 
				 wakeLock.acquire(); 
			 } 
		 } 
	 } 
	 
	 
	 //�ͷ��豸��Դ�� 
	 @SuppressWarnings("unused")
	private void releaseWakeLock() 
	 { 
		 if (null != wakeLock) 
		 { 
			 wakeLock.release(); 
			 wakeLock = null; 
		 } 
	 } 
}
