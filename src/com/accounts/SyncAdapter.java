package com.accounts;


import com.main.MainUtils;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


public class SyncAdapter extends AbstractThreadedSyncAdapter {
	private final String TAG = "SyncAdapter";
	
    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        Log.e(TAG, "SyncAdapter");
       
    }

    //参数ContentProviderClient provider就是配置的Contentprivder==AccountStubProvider
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
       try{
	    	Context context = getContext();
	    	MainUtils.checkStartForegroundService(context);

	    	Log.e(TAG, "onPerformSync");
	        WriteDateFile.writeLogFile(PublicFunction.formatCurrentDate() + " onPerformSync\r\n");
       }
       catch(Exception ex){
    	   ex.printStackTrace();
       }
    }
    
}