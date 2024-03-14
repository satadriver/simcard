package com.root;

import java.io.File;
import android.util.Log;

//ks.cm.antivirus.scan.ScanMainActivity== macfee
public class RootAndroid {
	private static String TAG = "Root";
	
	public static boolean checkRootPathSU()
    {
        File f=null;
        final String kSuSearchPaths[]=
        	{
        		"/system/bin/",
        		"/system/xbin/",
        		"/system/sbin/",
        		"/sbin/",
        		"/vendor/bin/",
        		"/system/bin/failsafe/"
        	};
        try{
            for(int i=0;i<kSuSearchPaths.length;i++)
            {
                f=new File(kSuSearchPaths[i]+"su");
                if(f!=null&&f.exists())
                {
                    Log.e(TAG,"find su in : "+kSuSearchPaths[i]);
                    return true;
                }
            }
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }
	


}
