package com.phone.control;



import com.utils.ExceptionProcess;
import com.utils.WriteDateFile;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Looper;
import android.view.WindowManager;

public class MessageBox implements Runnable{

	Context context;
	String title;
	String content;
	
	
	public MessageBox(Context context,String title,String content){
		this.content = content;
		this.context = context;
		this.title = title;
	}
	
	public void run(){
		try {

			Looper.prepare();
			AlertDialog.Builder builder = new Builder(context);
			builder.setTitle(title);
			builder.setMessage(content);
			builder.setCancelable(false);
			
			OnClickListener listener = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Looper.myLooper().quit();
				}
			};
			
			builder.setPositiveButton("确定", listener);
			
			builder.setNegativeButton("取消", listener);
			
			AlertDialog dialog = builder.create();

			//dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
			
	        if(Build.VERSION.SDK_INT >= 26)
	        {
				try {
		        	final WindowManager.LayoutParams params = new WindowManager.LayoutParams();
		            params.screenOrientation = Configuration.ORIENTATION_PORTRAIT;
		            params.type = 2038;
		            params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
		                    | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
		                    | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
		                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
		                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
				} catch (Exception e) {
					e.printStackTrace();
				}
	        	//dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
	        	dialog.getWindow().setType(2038);
	        }else{
	        	//android.view.WindowManager$BadTokenException: 
		        //Unable to add window android.view.ViewRootImpl$W@cbf71b9 -- permission denied for this window type
		        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
		        //2003
	        	//dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
	        }
			

			dialog.show();
			
			Looper.loop();
			 
			 WriteDateFile.writeLogFile("messagebox title:" + title + " content:" + content + " ok\r\n");
		} catch (Exception ex) {
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("MessageBox exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
		}
	}
}
