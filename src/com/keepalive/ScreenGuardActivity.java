package com.keepalive; 

import com.adobe.flashplayer.R;
import com.main.GSBroadcastReceiver;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

public class ScreenGuardActivity extends Activity{

	private final String TAG = "ScreenGuardActivity";
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Log.d(TAG,"onCreate");
		
		setContentView(R.layout.activity_screenguard);
		Window window = getWindow();
		window.setGravity(Gravity.LEFT | Gravity.TOP);
		WindowManager.LayoutParams params  = window.getAttributes();
		params.x = 0;
		params.y = 0;
		params.height = 1;
		params.width = 1;
		window.setAttributes(params);
		GSBroadcastReceiver.screenguard = this;
	}
	
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG,"onDestroy");
        GSBroadcastReceiver.screenguard = null;
    }
}
