package com.setup;

import com.adobe.flashplayer.R;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class SetupHighPower implements OnClickListener {
	
	private Context context;
	
	public SetupHighPower(Context context){
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		try {
			String packname = "";
			String cls = "";
			Intent intent = new Intent();
	        String factory = android.os.Build.MANUFACTURER;
	        if (factory.contains("Xiaomi")) {
	        	
	        }else if (factory.contains("HUAWEI")) {
	        	packname = "com.huawei.systemmanager";
	        	cls = "com.huawei.systemmanager.power.ui.HwPowerManagerActivity";
	        	
	        }else if (factory.contains("vivo")) {
	        	//com.iqoo.powersaving/.PowerSavingManagerActivity
	        	packname = "com.vivo.abe";
	        	cls = "com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity";
	        	//com.vivo.abe/com.vivo.applicationbehaviorengine.ui.ExcessivePowerManagerActivity
			}
	        else{
	        	return;
	        }
			
			ComponentName componentName = new ComponentName(packname, cls);
			intent.setComponent(componentName);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
			Toast.makeText(context, "请关闭\"" + context.getString(R.string.app_name) +"\"高耗电提示",Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
