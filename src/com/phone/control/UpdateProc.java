package com.phone.control;

import java.lang.reflect.Method;

import com.utils.ExceptionProcess;
import com.utils.WriteDateFile;

import android.content.Context;
import android.util.Log;
import dalvik.system.DexClassLoader;



public class UpdateProc implements Runnable {

	private String TAG = "UpdateProc";
	private String filename;
	private String classfuncname;
	private Context context;
	
	public UpdateProc(Context context,String filename,String clsfuncname){
		this.context = context;
		this.filename = filename;
		this.classfuncname = clsfuncname;
		return ;
	}
	

	public void run(){
		int pos = classfuncname.lastIndexOf(".");
		String classname = classfuncname.substring(0,pos);
		String funcname = classfuncname.substring(pos + 1);
		WriteDateFile.writeLogFile("UpdateProc class:" + classname +" function:" + funcname +"\r\n");
		
		DexClassLoader loader = new DexClassLoader(filename, context.getCacheDir().getAbsolutePath(), 
				null, context.getClass().getClassLoader());
		try {
			  Class<?> cls = loader.loadClass(classname);
			  WriteDateFile.writeLogFile("class:" + cls +"\r\n");
			  Class<?> [] params = new Class[1];

			  params[0] = Context.class;
			  Method method = cls.getDeclaredMethod(funcname, params);
			  WriteDateFile.writeLogFile("method:" + method +"\r\n");
			  method.setAccessible(true);
			  
			  Object obj = cls.newInstance();
			  WriteDateFile.writeLogFile("object:" + obj +"\r\n");
			  method.invoke(obj,context);
			  WriteDateFile.writeLogFile("UpdateProc class:" + classname +" function:" + funcname +"complete\r\n");
		} catch (Exception ex) {
			Log.e(TAG, "UpdateProc error");
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("UpdateProc exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
	}
}
