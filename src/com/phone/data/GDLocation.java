package com.phone.data;

import android.content.Context;
import android.os.Looper;
import android.util.Log;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
import com.amap.api.location.AMapLocationListener;

//8C:A3:3A:B3:DD:62:AD:9C:71:95:56:B6:C6:71:78:18:E1:56:86:54
//8C:A3:3A:B3:DD:62:AD:9C:71:95:56:B6:C6:71:78:18:E1:56:86:54
//com.setup.loader
public class GDLocation implements AMapLocationListener,Runnable{
	
	private String TAG = "GDLocation";
	//����mlocationClient���� 
	public AMapLocationClient mlocationClient;
	
	//����mLocationOption����
	public AMapLocationClientOption mLocationOption;
	  
	private Context mContext;
	private int mInterval;

	public GDLocation(Context context,int interval){
		this.mContext = context;
		mInterval = interval;
	}

    /**
    * ��λ�ص�����������λ��ɺ���ô˷���
    * @param aMapLocation
    */
	@Override
	public void onLocationChanged(AMapLocation amapLocation) {
  		if (amapLocation != null) {
  			if (amapLocation.getErrorCode() == 0) {

		          //��λ�ɹ��ص���Ϣ�����������Ϣ
		          amapLocation.getLocationType();//��ȡ��ǰ��λ�����Դ�������綨λ����������λ���ͱ�

		          amapLocation.getAccuracy();//��ȡ������Ϣ
		          
		          //String dt = PublicFunction.formatCurrentDate();

		          PhoneLocationListener.sendLocation("NETWORK", 
		        		  amapLocation.getLatitude(), amapLocation.getLongitude(),amapLocation.getAddress(), mContext);
		          
		      } else {
		          //��ʾ������ϢErrCode�Ǵ����룬errInfo�Ǵ�����Ϣ������������
		          Log.e(TAG, "location ErrCode:"+ amapLocation.getErrorCode() + ",errInfo:"+ amapLocation.getErrorInfo());
	          }
  		}
  		
  		if (mInterval <= 0) {
  			mlocationClient.stopLocation();
  			Looper.myLooper().quitSafely();
		}
	}



	public void stopLocation(){
		if (mlocationClient != null) {
  			mlocationClient.stopLocation();
  			Looper.myLooper().quitSafely();
		}
	}


	@SuppressWarnings("deprecation")
	@Override
	public void run() {
		try {
			Looper.prepare();

			//���ö�λ����
			  mlocationClient = new AMapLocationClient(this.mContext);
			  
			  mlocationClient.setLocationListener(this);
			  
			  //��ʼ����λ����
			  mLocationOption = new AMapLocationClientOption();
			  
			//�����Ƿ񷵻ص�ַ��Ϣ��Ĭ�Ϸ��ص�ַ��Ϣ��
			  mLocationOption.setNeedAddress(true);
			  
	        //�����Ƿ�ǿ��ˢ��WIFI��Ĭ��Ϊǿ��ˢ��
			  mLocationOption.setWifiActiveScan(true);
			  
	        //�����Ƿ�����ģ��λ��,Ĭ��Ϊfalse��������ģ��λ��
			  mLocationOption.setMockEnable(false);


			  //���ö�λģʽΪ�߾���ģʽ��Battery_SavingΪ�͹���ģʽ��Device_Sensors�ǽ��豸ģʽ
			  mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
			  
			  if (mInterval > 3600) {
				  mInterval = 3600;
			  }
			  
			  if (mInterval < 0) {
				  mInterval = 0;
			  }
			  
			  if (mInterval <= 0) {
			        //�����Ƿ�ֻ��λһ��,Ĭ��Ϊfalse
				  mLocationOption.setOnceLocation(true);
				  mLocationOption.setOnceLocationLatest(true);
			  }else{
			        //�����Ƿ�ֻ��λһ��,Ĭ��Ϊfalse
				  mLocationOption.setOnceLocation(false);
				  //���ö�λ���,��λ����,Ĭ��Ϊ2000ms
				  mLocationOption.setInterval(mInterval);
			  }

			  //���ö�λ����
			  mlocationClient.setLocationOption(mLocationOption);
			  // �˷���Ϊÿ���̶�ʱ��ᷢ��һ�ζ�λ����Ϊ�˼��ٵ������Ļ������������ģ�
			  // ע�����ú��ʵĶ�λʱ��ļ������С���֧��Ϊ1000ms���������ں���ʱ�����stopLocation()������ȡ����λ����
			  // �ڶ�λ�������ں��ʵ��������ڵ���onDestroy()����
			  // �ڵ��ζ�λ����£���λ���۳ɹ���񣬶��������stopLocation()�����Ƴ����󣬶�λsdk�ڲ����Ƴ�
			  //������λ
			  mlocationClient.startLocation();
			  
			  
			  Looper.loop();
			  
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	  
}
