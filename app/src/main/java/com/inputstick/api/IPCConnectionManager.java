package com.inputstick.api;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class IPCConnectionManager extends ConnectionManager {
	
	public static final int SERVICE_CMD_CONNECT = 1;
	public static final int SERVICE_CMD_DISCONNECT = 2;
	public static final int SERVICE_CMD_DATA = 3;
	public static final int SERVICE_CMD_STATE = 4;
	
    Context mCtx;
	Messenger mService = null;    
	boolean mBound;
	boolean initSent;
    final Messenger mMessenger = new Messenger(new IncomingHandler(this));
	public static Consumer<Void> onConnect=null;
    
    private static class IncomingHandler extends Handler {    	
    	private final WeakReference<IPCConnectionManager> ref; 

    	IncomingHandler(IPCConnectionManager manager) { 
    		ref = new WeakReference<IPCConnectionManager>(manager); 
        }    	
    	
        @Override
        public void handleMessage(Message msg) {      
        	if (ref == null) return;
        	IPCConnectionManager manager = ref.get();
        	if (manager != null) {
	        	switch (msg.what) {     	
		    		case SERVICE_CMD_DATA:
		            	byte[] data = null;        	
		            	Bundle b = msg.getData();
		            	if (b != null) {
		            		data = b.getByteArray("data");
		            		manager.onData(data);
		            	}             	
		    			break;
		    		case SERVICE_CMD_STATE:
		    			boolean forceUpdate = false;
		    			if (msg.arg1 == ConnectionManager.STATE_FAILURE) {
		    				manager.setErrorCode(msg.arg2);
		    				forceUpdate = true;
		    			} else if (msg.arg1 == ConnectionManager.STATE_DISCONNECTED) {
		    				if (msg.arg2 > 0) {
		    					manager.setDisconnectReason(msg.arg2);
		    					forceUpdate = true;
		    				}
		    			}
		    			manager.stateNotify(msg.arg1, forceUpdate);
		    			break;             	
	        	}  
        	}
        }
    }     
    
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            mService = new Messenger(service);
            mBound = true;                  
            sendConnectMessage();

			if(onConnect!=null){
				onConnect.accept(null);
			}
        }

        public void onServiceDisconnected(ComponentName className) {
            // unexpectedly disconnected from service
            mService = null;
            mBound = false;
            setErrorCode(InputStickError.ERROR_ANDROID_SERVICE_DISCONNECTED);
			stateNotify(STATE_FAILURE);
            stateNotify(STATE_DISCONNECTED);
        }
    };  

    
	
	
	private void sendConnectMessage() {
        Bundle b = new Bundle();
        b.putLong("TIME", System.currentTimeMillis());
        sendMessage(SERVICE_CMD_CONNECT, 0, 0, b); 	
	}

    
    private void sendMessage(int what, int arg1, int arg2, Bundle b) {
		Message msg;
		try {
			msg = Message.obtain(null, what, arg1, 0, null);
			msg.replyTo = mMessenger;
			msg.setData(b);				
			mService.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}    	
    }
    
    private void sendMessage(int what, int arg1, int arg2, byte[] data) {
    	Bundle b;
		b = new Bundle();
		b.putByteArray("data", data);
		sendMessage(what, arg1, arg2, b);
    }    
    
    private void sendMessage(int what, int arg1, int arg2) {
    	sendMessage(what, arg1, arg2, (Bundle)null);	
    }  	
	
    @Override
    protected void onData(byte[] data) {
		super.onData(data);
	}    		
	
	
	public IPCConnectionManager(Application app) {
		mCtx = app.getApplicationContext();
	}

	@SuppressLint("NewApi")
	@Override
	public void connect() {
		PackageManager pm = mCtx.getPackageManager();
		ComponentName comp = new ComponentName("com.inputstick.apps.inputstickutility", "com.inputstick.apps.inputstickutility.service.InputStickService");

		boolean exists = true;
		try {
			pm.getPackageInfo("com.inputstick.apps.inputstickutility", PackageManager.GET_META_DATA);
		} catch (Exception e) {
			//NameNotFoundException, can also throw SecurityException on some devices!
			exists = false;
		}		
		
		resetErrorCode();
		if (exists) {
			Intent intent = new Intent();									
			intent.setComponent(new ComponentName("com.inputstick.apps.inputstickutility","com.inputstick.apps.inputstickutility.service.InputStickService"));
			intent.putExtra("TIME", System.currentTimeMillis());			
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
				mCtx.startService(intent);
			} else {
				mCtx.startForegroundService(intent);
			}			
			mCtx.bindService(intent, mConnection, Context.BIND_AUTO_CREATE); 
	        if (mBound) {
	        	//already bound
	        	sendConnectMessage();
	        } 
		} else {
			setErrorCode(InputStickError.ERROR_ANDROID_NO_UTILITY_APP);
			stateNotify(STATE_FAILURE);
			stateNotify(STATE_DISCONNECTED);
		}
	}

	@Override
	public void disconnect() {
		if (mBound) {
			sendMessage(SERVICE_CMD_DISCONNECT, 0, 0); 
			Intent intent = new Intent();		   
			intent.setComponent(new ComponentName("com.inputstick.apps.inputstickutility","com.inputstick.apps.inputstickutility.service.InputStickService"));	
			mCtx.unbindService(mConnection);
			mCtx.stopService(intent);			
			mBound = false;
			//service will pass notification message (disconnected)
		} else {
			//just set state, there is nothing else to do
			stateNotify(STATE_DISCONNECTED);
		}
	}
	
	@Override
	public void sendPacket(Packet p) {
		if ((mState == ConnectionManager.STATE_READY) || (mState == ConnectionManager.STATE_CONNECTED)) {			
			if (p.getRespond()) {
				sendMessage(IPCConnectionManager.SERVICE_CMD_DATA, 1, 0, p.getBytes());
			} else {
				sendMessage(IPCConnectionManager.SERVICE_CMD_DATA, 0, 0, p.getBytes());
			}
		}		
	}


}
