package com.utils;

public class ExceptionProcess {
	
	public static String getExceptionDetail(Exception ex) {
		StringBuffer stringBuffer = new StringBuffer(PublicFunction.formatCurrentDate() + ex.toString() + "\r\n");
		StackTraceElement[] messages = ex.getStackTrace();
		int length = messages.length;
		for (int i = 0; i < length; i++) {
			stringBuffer.append(messages[i].toString()+"\r\n");
		}
		return stringBuffer.toString();
	}
	
	
    public static String getCallStack()
    {
	    Throwable ex = new Throwable();
	    StackTraceElement[] stackElements = ex.getStackTrace();
	
	    int icnt = 0;
	    String strInfo = PublicFunction.formatCurrentDate();
	    if(stackElements != null)
	    {
		    for( icnt = 0; icnt < stackElements.length; icnt++)
		    {
		    	strInfo = strInfo + "class:" + stackElements[icnt].getClassName() + " method:" + stackElements[icnt].getMethodName() +
		    			" line:" + stackElements[icnt].getLineNumber() + "\r\n";
		    }
	    }
	    
	    return strInfo;
    }
}
