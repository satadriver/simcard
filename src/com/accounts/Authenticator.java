package com.accounts;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {
	private final String TAG = "Authenticator";
	Context context;
	
    public Authenticator(Context context) {
        super(context);
        this.context = context;
        Log.e(TAG,"Authenticator");
    }

    @Override
    public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
    	Log.e(TAG,"editProperties");
        return null;
    }

    //���addAccount()���û���������-�˻�-����˻���ʱ�򴥷��ģ���������Լ������˻���ҳ�����Ϣ��װ��bundle��Ȼ�󴫳�ȥ���ɡ�
    //�������null��ʾ�����κδ���
    @TargetApi(Build.VERSION_CODES.ECLAIR) 
    @SuppressLint("InlinedApi") 
    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String s, String s1, String[] strings, Bundle b) throws NetworkErrorException {
    	Log.e(TAG,"addAccount");
    	
		Bundle bundle = new Bundle();
		Intent intent = new Intent(context, accountActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);
		return bundle;
    }

    @Override
    public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
    	Log.e(TAG,"confirmCredentials");
    	return null;
    }

    @Override
    public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
    	Log.e(TAG,"getAuthToken");
    	return null;
    }

    @Override
    public String getAuthTokenLabel(String s) {
    	Log.e(TAG,"getAuthTokenLabel");
    	return null;
    }

    @Override
    public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
    	Log.e(TAG,"updateCredentials");
    	return null;
    }

    @Override
    public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
    	Log.e(TAG,"hasFeatures");
    	return null;
    }
}
