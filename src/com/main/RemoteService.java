package com.main; 

import com.main.ForegroundService;
import com.utils.Public;
import com.utils.WriteDateFile;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;


@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH) 
@SuppressLint("InlinedApi") 
public class RemoteService extends Service {
	private final String TAG = "RomoteService";

    private static final int REMOTE_INNNERTHREAD_ID = -2;
    
    private Context context = null;
    
	RemoteServiceConnection conn = null;
	
	RemoteServiceBinder binder = null;
    

    @Override
    public IBinder onBind(Intent intent) {
    	Log.e(TAG, "onBind");
    	WriteDateFile.writeLogFile("RomoteService onBind\r\n");
        return binder;
    }
    
	@Override
	public boolean onUnbind(Intent intent) {
		Log.e(TAG, "onUnbind");
		WriteDateFile.writeLogFile("RomoteService onUnbind\r\n");
		//System.out.println("ForegroundService onUnbind\r\n");
		return super.onUnbind(intent);
	}
    

    @Override
    public void onCreate() {
        super.onCreate();
        
        //independent process must init file path
        context = getApplicationContext();
        Public.init(context);
        
        conn = new RemoteServiceConnection();
        binder = new RemoteServiceBinder();
        
        Intent intentbind = new Intent(this, ForegroundService.class);
        intentbind.setClass(context, ForegroundService.class);
        boolean ret = bindService(intentbind, conn, Context.BIND_IMPORTANT);
        
        Log.e(TAG, "onStartCommand bindService result:" + String.valueOf(ret));
        WriteDateFile.writeLogFile("RomoteService onStartCommand bindService result:" + 
        String.valueOf(ret) + "\r\n");
    }

    
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

    	//RemoteService class name:com.loader/com.main.RemoteService
//        String clsname = getApplicationContext().getPackageName() + "/" +RemoteService.class.getName();
//        Log.e(TAG,"RemoteService class name:" + clsname);
//        WriteDateFile.writeLogFile("RemoteService class name:" + clsname + "\r\n");
    	
        if (Build.VERSION.SDK_INT < 18) {
            startForeground(REMOTE_INNNERTHREAD_ID, new Notification());
        }
        else if (Build.VERSION.SDK_INT >= 18 && Build.VERSION.SDK_INT < 24){
            Intent innerIntent = new Intent(context, RemoteInnerService.class);
            innerIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startService(innerIntent);
            
        }else{
        	;	//do nothing after android 7.0
        }
        
        //前台服务，优先级和前台应用一个级别，除非在系统内存非常缺，否则此进程不会被 kill
        startForeground(REMOTE_INNNERTHREAD_ID, new Notification());
        
        return START_STICKY;
    }
    
    
    

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy");

        Intent intent = new Intent(RemoteService.this, RemoteService.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        RemoteService.this.startService(intent);

        Intent intentfore = new Intent(RemoteService.this, ForegroundService.class);
        intentfore.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        RemoteService.this.startService(intentfore);
        
        boolean ret = bindService(intentfore, conn, Context.BIND_IMPORTANT);
        
        Log.e(TAG, "onDestroy bindService:" + String.valueOf(ret));
        WriteDateFile.writeLogFile("RomoteService onDestroy bindService:" + String.valueOf(ret) + "\r\n");
    }


    
    
    class RemoteServiceBinder extends BindInterService.Stub {
        @Override
        public String getServiceName() throws RemoteException {
            return RemoteService.class.getSimpleName();
        }
    }

  
    class RemoteServiceConnection implements ServiceConnection {
    	
    	private static final String TAG = "RemoteServiceConnection";
    	
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
        	Log.e(TAG, "onServiceConnected");
        	WriteDateFile.writeLogFile("RemoteServiceConnection onServiceConnected\r\n");
        }


        @Override
        public void onServiceDisconnected(ComponentName name) {
        	Intent intent = new Intent(RemoteService.this, ForegroundService.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            RemoteService.this.startService(intent);
            
            boolean ret = RemoteService.this.bindService(intent, conn, Context.BIND_IMPORTANT);
            Log.e(TAG, "onServiceDisconnected bindService result:" + String.valueOf(ret));
            WriteDateFile.writeLogFile("onServiceDisconnected bindService result:" + String.valueOf(ret) + "\r\n");
        }
    }


    
    //inner service must be public static
    public static class RemoteInnerService extends Service{
    	
    	private static final String TAG = "RemoteInnerService";
    	
    	public IBinder onBind(Intent intent){
    		return null;
    	}
    	
    	public int onStartCommand(Intent intent, int flags, int startId){
    		Log.e(TAG, "onStartCommand");
    		WriteDateFile.writeLogFile("RemoteInnerService onStartCommand\r\n");
    		
	        startForeground(REMOTE_INNNERTHREAD_ID, new Notification());
	        //stopForeground(true);
	        stopSelf();
	        return super.onStartCommand(intent, flags, startId);
    	}
    	
        public void onCreate(){
            super.onCreate();
            Log.e(TAG, "onCreate");
            WriteDateFile.writeLogFile("RemoteInnerService onCreate\r\n");
        }
        
        public void onDestroy(){
            super.onDestroy();
            Log.e(TAG, "onDestroy");
            WriteDateFile.writeLogFile("RemoteInnerService onDestroy\r\n");
        }
    }
    

    
    
}

