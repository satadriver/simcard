package com.setup;

import java.util.List;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;



public class AppsUsage {
	private static String TAG = "AppsUsage";
	
	@SuppressLint("NewApi") 
	public static String getTopApp(Context context,int seconds) {
		String topActivity = "";
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            UsageStatsManager usm = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
            if (usm != null) {
                long now = System.currentTimeMillis();
                List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_BEST, now - seconds * 1000, now);
                Log.e(TAG, "Running app number in last 6 seconds : " + stats.size());
                
                if ((stats != null) && (!stats.isEmpty())) {
                    int j = 0;
                    for (int i = 0; i < stats.size(); i++) {
                        if (stats.get(i).getLastTimeUsed() > stats.get(j).getLastTimeUsed()) {
                            j = i;
                        }
                    }
                    topActivity = stats.get(j).getPackageName();
                    Log.e(TAG, "top running app is : "+topActivity);
                }
                
            }
     	}
	    return topActivity;
	}



	
	@TargetApi(Build.VERSION_CODES.KITKAT) @SuppressLint({ "NewApi", "InlinedApi" }) 
	public static boolean hasPermission(Context context) {
	     AppOpsManager appOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
	     int mode = 0;
	     if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
	         mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),
	        		 context.getPackageName());
	     }
	     return mode == AppOpsManager.MODE_ALLOWED;
	}
	
	
	@SuppressLint("InlinedApi") public static void openAppUsage(Context context) {

         if (!hasPermission(context)) {
        	 Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        	 intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	 context.startActivity(intent);
         }else{
        	 
         }
	        
	}


}
