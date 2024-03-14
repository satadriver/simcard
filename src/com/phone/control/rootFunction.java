package com.phone.control;

import android.content.Context;
import android.content.Intent;

public class rootFunction {
	
	public static void restart(Context context){
		try{

			Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			context.startActivity(intent);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	public static void shutdown(Context context){
		try{
	        Intent intent = new Intent("com.android.settings.action.REQUEST_POWER_OFF");
	        //Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
	        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(intent);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
