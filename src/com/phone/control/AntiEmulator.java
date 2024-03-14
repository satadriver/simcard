package com.phone.control;

import com.phone.data.PhoneInformation;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.text.TextUtils;

public class AntiEmulator {
	
	public static boolean isEmulator(Context context){
		if (isFeatures()) {
			//WriteDateFile.writeLogFile("find emulator running in features\r\n");
			return true;
		}
		//else if (notHasBlueTooth()) {
		//	return true;
		//}
//		else if(notHasLightSensorManager(context)){
//			WriteDateFile.writeLogFile("find emulator running in light sensor\r\n");
//			return true;
//		}
		
		//WriteDateFile.writeLogFile("not find emulator running\r\n");
		return false;
	}

	public static boolean notHasBlueTooth() {
		
	    BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
	    if (ba == null) {
	        return true;
	    } else {
	        // �����������һ������Ч�ġ���ȡ�������ƣ���Ϊnull ��Ĭ��Ϊģ����
	        String name = ba.getName();
	        if (TextUtils.isEmpty(name)) {
	            return true;
	        } else {
	            return false;
	        }
	    }
		
	}
	
	public static boolean notHasLightSensorManager(Context context) {
	    SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
	    Sensor sensor8 = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT); //��
	    if (null == sensor8) {
	        return true;
	    } else {
	        return false;
	    }
	}
	
	public static boolean isFeatures() {
		String kernelver = PhoneInformation.getKernelVersion();
	    return kernelver.contains("qemu") ||
	    		Build.FINGERPRINT.toLowerCase().contains("vbox")|| 
	    		//Build.FINGERPRINT.toLowerCase().contains("test-keys")|| 
	    		//Build.FINGERPRINT.toLowerCase().contains("user\\/dev-keys")|| 
	    		//Build.FINGERPRINT.toLowerCase().startsWith("google")|| 
	    		Build.MODEL.contains("Emulator")|| 
	    		Build.MANUFACTURER.equals("Genymotion")|| 
	    		Build.BRAND.equals("generic") || 
	    		Build.PRODUCT.contains("vbox")|| 
	    		Build.DISPLAY.contains("vbox");
	}
	
	

}
