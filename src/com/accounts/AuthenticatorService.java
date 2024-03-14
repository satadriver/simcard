package com.accounts;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


public class AuthenticatorService extends Service {
	private final String TAG = "AuthenticatorService";
	
	////mAuthenticatorĿ������Ϊ�˺���֤
	private Authenticator mAuthenticator = null;
    
    public AuthenticatorService() {
    	Log.e(TAG,"AuthenticatorService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mAuthenticator =  new Authenticator(this);
        Log.e(TAG,"onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
    	Log.e(TAG,"onBind");
        return mAuthenticator.getIBinder();
    }
}


/*
1 AuthenticationService��
AuthenticationService��һ���̳�Service�ķ���Ŀ�����ṩ����̵��ã���ϵͳͬ����ܵ��á�
�̶���ActionΪandroid.accounts.AccountAuthenticator

2 Authenticator��һ���̳���AbstractAccountAuthenticator���࣬AbstractAccountAuthenticator��һ�����࣬
�����崦���ֻ������á���˺���ͬ������Account����ӡ�ɾ������֤�ȹ��ܵĻ����ӿڣ���ʵ����һЩ�������ܡ�
AbstractAccountAuthenticator�����и��̳���IAccountAuthenticator.Stub���ڲ��࣬
��������AbstractAccountAuthenticator��Զ�̽ӿڵ��ý��а�װ��
���ǿ���ͨ��AbstractAccountAuthenticator��getIBinder���������������ڲ����IBinder��ʽ��
�Ա�Դ������Զ�̵��ã����������onBind�����еĵ��á�
���бȽ���Ҫ��Ҫ���صķ�����addAccount()��
//���addAccount()���û���������-�˻�-����˻���ʱ�򴥷��ģ���������Լ������˻���ҳ�����Ϣ��װ��bundle��Ȼ�󴫳�ȥ���ɡ�
//�������null��ʾ�����κδ���


3 sync���Ƶ�ʹ�ú��˺Ź�������ƣ�Ҳ�ǻ���binder���ƵĿ����ͨ�š���������Ҫһ��Service����������ṩһ��Action��ϵͳ�Ա�ϵͳ���ҵ�����
Ȼ����Ǽ̳к�ʵ��AbstractThreadedSyncAdapter�������а���ʵ����ISyncAdapter.Stub�ڲ��࣬
����ڲ����װ��Զ�̽ӿڵ��ã������getSyncAdapterBinder()�����������ڲ����IBinder��ʽ��
�Ա��AbstractThreadedSyncAdapte����Զ�̵��ã�
��manifest����Ҫ��Serviceע�ᣬ����ָ��meta-data�����meta-data��һ��xml�ļ���
��SampleSyncAdapterʵ���У�����������syncadapter.xml������ļ�ָ�����˺źͱ�������contentprovider

*/
