package com.phone.data;

import com.root.rootDevice;
import com.utils.ExceptionProcess;
import com.utils.WriteDateFile;
import java.io.DataOutputStream;
import java.io.OutputStream;
import android.util.Log;

public class ShellCmd implements Runnable{
	private static final String TAG = "ShellCommand";
	private String cmd;
	
	public static int execShell(String user,String cmd)
	{
		Log.e(TAG,"run cmd:" + cmd + " with user:" + user);
		int result = -1;
		try{  		
            Process p = Runtime.getRuntime().exec(user);  //su为root用户,sh普通用户
           
            OutputStream outputStream = p.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);
            if(cmd != null && cmd.equals("") == false){
	            dataOutputStream.writeBytes(cmd + "\n");
	            dataOutputStream.flush();
            }
            dataOutputStream.writeBytes("exit\n");
            dataOutputStream.flush();
            
            p.waitFor();
            result = p.exitValue();
            
            dataOutputStream.close();
            outputStream.close();
            p.destroy();
		}  
		catch(Exception e)  
		{  
			e.printStackTrace();  
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("execShell exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		} 
		return result;
    }
	
	
	public ShellCmd(String cmd){
		this.cmd = cmd;
	}
	
	public void run(){
		int ret = rootDevice.requestSU();
		if (ret == 0) {
			execShell("su",cmd);
		}else{
			execShell("sh",cmd);
		}
		
	}
}
