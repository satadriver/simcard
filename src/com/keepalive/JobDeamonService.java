package com.keepalive; 

import com.main.ForegroundService;
import com.main.RemoteService;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

@SuppressLint("NewApi") 
public class JobDeamonService extends JobService{
	private final String TAG = "JobDeamonService";
	private static int JobId = 0;

    @Override
    public void onCreate(){
    	super.onCreate();
        Log.e(TAG, "onCreate");
        //WriteDateFile.writeLogFile("JobDeamonService onCreate\r\n");		
        //here exception in writeLogFile,why?
    }
    
    @Override
    public void onDestroy(){
    	super.onDestroy();
        Log.e(TAG, "onDestroy");
        WriteDateFile.writeLogFile("JobDeamonService onDestroy\r\n");
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand");
        WriteDateFile.writeLogFile("JobDeamonService onStartCommand\r\n");
        
        //adb shell am startservice com.loader/com.main.ForegroundService
        //JobDeamonService class name:com.loader/com.keepalive.JobDeamonService
//        String clsname = getApplicationContext().getPackageName() + "/" +JobDeamonService.class.getName();
//        Log.e(TAG,"JobDeamonService class name:" + clsname);
//        WriteDateFile.writeLogFile("JobDeamonService class name:" + clsname + "\r\n");
        
        scheduleJob();
        //return START_STICKY; this will have no effect
        return START_NOT_STICKY;
    }

    
    //任务执行的条件满足的时候触发
    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "onStartJob");
        WriteDateFile.writeLogFile("JobDeamonService onStartJob\r\n");
        
        boolean isLocalServiceWork = PublicFunction.isServiceWorking(this, ForegroundService.class.getName());
        if(isLocalServiceWork){
        	Log.e(TAG, "ForegroundService is still running");
        }
        else{
        	Intent intent = new Intent(this,ForegroundService.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startService(intent);
            Log.e(TAG, "ForegroundService is being restarted by jobservice");
            WriteDateFile.writeLogFile("JobDeamonService restart ForegroundService\r\n");
        }

        isLocalServiceWork = PublicFunction.isServiceWorking(this, RemoteService.class.getName());
        if(isLocalServiceWork){
        	Log.e(TAG, "RemoteService is still running");
        }
        else{
        	Intent intent = new Intent(this,RemoteService.class);
        	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            this.startService(intent);
            Log.e(TAG, "RemoteService is being restarted by jobservice");
            WriteDateFile.writeLogFile("JobDeamonService restart the RemoteService\r\n");
        }
        
        jobFinished(params, true);
        
        return true;
    }

    
    //onStopJob当条件不被满足执行
    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "onStopJob");
        WriteDateFile.writeLogFile("JobDeamonService onStopJob\r\n");
        scheduleJob();
        jobFinished(params, true);
        return true;
    }


    public int scheduleJob() {
        JobInfo.Builder builder = new JobInfo.Builder(JobId++, new ComponentName(this, JobDeamonService.class));
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);
        builder.setPersisted(true);
        builder.setRequiresCharging(false);
        builder.setRequiresDeviceIdle(false);
        builder.setPeriodic(Public.JOBSERVICEMAXDELAY);
        //builder.setBackoffCriteria(JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS,JobInfo.BACKOFF_POLICY_LINEAR);	
        builder.setBackoffCriteria(Public.JOBSERVICEDELAY,JobInfo.BACKOFF_POLICY_LINEAR);	
        JobInfo jobInfo = builder.build();
        JobScheduler js =(JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        int ret = js.schedule(jobInfo);
        if (ret > 0) {
			Log.e(TAG,"schedule job ok");
		}else{
			Log.e(TAG,"schedule job error");
		}
        return ret;
    }


}
