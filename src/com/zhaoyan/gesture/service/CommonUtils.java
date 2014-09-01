package com.zhaoyan.gesture.service;

import java.util.HashMap;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;

public class CommonUtils {
	private static final String TAG = CommonUtils.class.getSimpleName();
	
	private static HashMap<Context, ServiceBinder> sConnectionMap = new HashMap<Context, ServiceBinder>();
	private static CommonService mService;
	
	 public static class ServiceToken {
	        ContextWrapper mWrappedContext;
	        ServiceToken(ContextWrapper context) {
	            mWrappedContext = context;
	        }
	    }

	    public static ServiceToken bindToService(Activity context) {
	        return bindToService(context, null);
	    }

	    public static ServiceToken bindToService(Activity context, ServiceConnection callback) {
	        Activity realActivity = context.getParent();
	        if (realActivity == null) {
	            realActivity = context;
	        }
	        ContextWrapper cw = new ContextWrapper(realActivity);
	        cw.startService(new Intent(cw, CommonService.class));
	        ServiceBinder sb = new ServiceBinder(callback);
	        if (cw.bindService((new Intent()).setClass(cw, CommonService.class), sb, 0)) {
	            sConnectionMap.put(cw, sb);
	            return new ServiceToken(cw);
	        }
	        Log.e(TAG, "Failed to bind to service");
	        return null;
	    }

	    public static void unbindFromService(ServiceToken token) {
	        if (token == null) {
	            Log.e("MusicUtils", "Trying to unbind with null token");
	            return;
	        }
	        ContextWrapper cw = token.mWrappedContext;
	        ServiceBinder sb = sConnectionMap.remove(cw);
	        if (sb == null) {
	            Log.e("MusicUtils", "Trying to unbind for unknown Context");
	            return;
	        }
	        cw.unbindService(sb);
	        if (sConnectionMap.isEmpty()) {
	            // presumably there is nobody interested in the service at this point,
	            // so don't hang on to the ServiceConnection
	            mService = null;
	        }
	    }

	    private static class ServiceBinder implements ServiceConnection {
	        ServiceConnection mCallback;
	        ServiceBinder(ServiceConnection callback) {
	            mCallback = callback;
	        }
	        
	        public void onServiceConnected(ComponentName className, android.os.IBinder service) {
	        	mService = ((CommonService.LocalBinder) service)
						.getService();
	            if (mCallback != null) {
	                mCallback.onServiceConnected(className, service);
	            }
	        }
	        
	        public void onServiceDisconnected(ComponentName className) {
	            if (mCallback != null) {
	                mCallback.onServiceDisconnected(className);
	            }
	            mService = null;
	        }
	    }
	    
	    public static void invisiableMainNotification(){
	    	if (mService != null) {
				mService.invisibleNotification();
			}
	    }
}
