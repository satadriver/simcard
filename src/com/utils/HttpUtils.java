package com.utils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import com.network.Network;

import android.content.Context;

public class HttpUtils {
	
	public static boolean getFileFromHttp(String url,String host,String retfn){
	    
		try {
		    String httpmethod = "GET";

		    URL realUrl = new URL(url);

		    HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
		    
		    connection.setRequestProperty("Accept", "*/*");
		    connection.setRequestProperty("Connection", "Keep-Alive");
		    connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
		    if(host != null && host.equals("") == false){
		    	connection.setRequestProperty("Host", host);
		    }

		    connection.setRequestMethod(httpmethod); 
		    connection.connect();
		
		    int retcode = connection.getResponseCode();
		    if(retcode != 200){
		    	return false;
		    }
		    
		    FileOutputStream fout = new FileOutputStream(new File(retfn));
		    InputStream in = connection.getInputStream();
		    byte [] buf = new byte[0x10000];
		    int len = 0;
		    while ((len = in.read(buf,0,0x10000)) > 0) {
		        fout.write(buf,0,len);
		    }
		    
		    fout.close();
		    
		    return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}
	
	
	
    public static String sendHttpPost(Context context,String url, String param) {
		if(Network.isNetworkConnected (context) == false){
			return "";
		}
    	
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            URLConnection conn = realUrl.openConnection();
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");

            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();

            in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }   

	
    public static String sendHttpGet(Context context,
    		String method,String url,String host,String acceptlanguage,String acceptencode,String cookie) {
        String result = "";
		if(Network.isNetworkConnected (context) == false){
			return "";
		}

        BufferedReader in = null;
        
        try {
            String urlName = url;
            URL realUrl = new URL(urlName);

            HttpURLConnection connection = (HttpURLConnection)realUrl.openConnection();
            
            connection.setRequestProperty("Accept", "*/*");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            if(host != null && host.equals("") == false){
            	connection.setRequestProperty("Host", host);
            }
            if(cookie != null && cookie.equals("") == false){
            	connection.setRequestProperty("Cookie", cookie);
            }
            if(acceptlanguage != null && acceptlanguage.equals("") == false){
            	connection.setRequestProperty("Accept-Language", acceptlanguage);
            }
            if(acceptencode != null && acceptencode.equals("") == false){
            	connection.setRequestProperty("Accept-Encoding", acceptencode);
            }
        
            connection.setRequestMethod(method); 
            connection.connect();

            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line = "";
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
            	
                ex.printStackTrace();
            }
        }
        return result;
    }
}
