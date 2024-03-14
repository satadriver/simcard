package com.phone.data;

import com.utils.WriteDateFile;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.media.projection.MediaProjectionManager;



@SuppressLint("NewApi") public class ScreenSnapshotActivity extends Activity{
	
	private final String TAG = "ScreenSnapshotActivity";
	private static final int REQUEST_MEDIA_PROJECTION = 0x12345678;
	private static long scrnstarttime;
	
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	
        super.onActivityResult(requestCode, resultCode, intent);

        finish();
       
        if (requestCode == REQUEST_MEDIA_PROJECTION) {

            if (resultCode == Activity.RESULT_OK && intent != null) {

                new Thread(new ScreenSnapshot(ScreenSnapshotActivity.this,intent)).start();

            	long scrnendtime = System.currentTimeMillis();
            	long scrnusetime = scrnendtime - ScreenSnapshotActivity.scrnstarttime;
            	WriteDateFile.writeLogFile("screen snapshot photo cost milliseconds:" + String.valueOf(scrnusetime) + "\r\n");
            }
            else{
            	WriteDateFile.writeLogFile("ScreenSnapshot result code is:" + resultCode + "\r\n");
            	Log.e(TAG,"result code is:" + resultCode);
            }
        }
        
        Log.e(TAG, "onActivityResult complete");
    }
	
    
    
    @Override
	public void onCreate(Bundle savedInstanceState){

		super.onCreate(savedInstanceState);

		scrnstarttime = System.currentTimeMillis();
		
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        //getWindow().setDimAmount(0f);
        //setContentView(R.layout.activity_screensnapshot);

        /*
		Window window = getWindow();
		window.setGravity(Gravity.LEFT | Gravity.TOP);
		WindowManager.LayoutParams params  = window.getAttributes();
		params.x = 0;
		params.y = 0;
		params.height = 1;
		params.width = 1;
		window.setAttributes(params);
		*/

    	MediaProjectionManager mediamgr = (MediaProjectionManager)getSystemService("media_projection");
    	//Context.MEDIA_PROJECTION_SERVICE
        Intent intent = mediamgr.createScreenCaptureIntent();
    	startActivityForResult(intent,REQUEST_MEDIA_PROJECTION);
    	
    	Log.e(TAG,"onCreate");
        return;
	}
	

	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e(TAG,"onDestroy");
	}
	
}
