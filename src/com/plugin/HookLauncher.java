package com.plugin;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.Handler.Callback;
import android.widget.Toast;



public class HookLauncher {
	public static class CustomHandler  implements Callback {
	    //���100һ����������Ҳ�����ȡ����Ȼ����Ҳ����ֱ��д������ϵͳ�ı���һ�¾ͺ���
	    public static final int LAUNCH_ACTIVITY = 100;
	    private Handler origin;

	    private Context context;

	    public CustomHandler(Context context , Handler origin) {
	        this.context = context;
	        this.origin =  origin;
	    }

	    @Override
	    public boolean handleMessage(Message msg) {
	        if (msg.what == LAUNCH_ACTIVITY) {
	        //����ÿ��������ʱ���ᵯ����˾��
	            Toast.makeText(
	                    context.getApplicationContext(),
	                    "hello,I am going to launch", Toast.LENGTH_SHORT).show();
	        }
	        origin.handleMessage(msg);
	        return false;
	    }
	}
	
	public static void hookHandler(Context context)  {
		try {
	        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
	        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
	        currentActivityThreadMethod.setAccessible(true);
	        //��ȡ���̶߳���
	        Object activityThread = currentActivityThreadMethod.invoke(null);
	        //��ȡmH�ֶ�
	        Field mH = activityThreadClass.getDeclaredField("mH");
	        mH.setAccessible(true);
	        //��ȡHandler
	        Handler handler = (Handler) mH.get(activityThread);
	        //��ȡԭʼ��mCallBack�ֶ�
	        Field mCallBack = Handler.class.getDeclaredField("mCallback");
	        mCallBack.setAccessible(true);
	        //���������������Լ�ʵ���˽ӿڵ�CallBack����
	        mCallBack.set(handler, new CustomHandler(context, handler));
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
}
