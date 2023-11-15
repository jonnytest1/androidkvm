package com.inputstick.api;

import java.lang.ref.WeakReference;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.inputstick.api.bluetooth.BTService;
import com.inputstick.api.init.InitManager;
import com.inputstick.api.init.InitManagerListener;

public class BTConnectionManager extends ConnectionManager implements InitManagerListener {
	
	//private static final String mTag = "BTConnectionManager";
	
	private String mMac;
	private byte[] mKey;		
	private boolean mIsBT40;
	
	private InitManager mInitManager;
	private Application mApp;
	protected BTService mBTService;
	private PacketManager mPacketManager;
	private final BTHandler mBTHandler = new BTHandler(this);				
	
	
	
    private static class BTHandler extends Handler {    	
    	private final WeakReference<BTConnectionManager> ref; 

    	BTHandler(BTConnectionManager manager) { 
    		ref = new WeakReference<BTConnectionManager>(manager); 
        }    	
    	
		@Override
		public void handleMessage(Message msg) {
			BTConnectionManager manager = ref.get();
			if (manager != null) {
				switch (msg.what) {
					case BTService.EVENT_DATA:
						manager.onData((byte[])msg.obj);
						break;			
					case BTService.EVENT_CONNECTED:
						manager.onConnected();
						break;
					case BTService.EVENT_CANCELLED:
						manager.onDisconnected();
						break;														
					case BTService.EVENT_ERROR:					
						manager.onFailure(msg.arg1); 
						break;
					default:
						manager.onFailure(InputStickError.ERROR_BLUETOOTH); 
				}
			}
		}
    } 		
    
    private void onConnecting() {
    	stateNotify(ConnectionManager.STATE_CONNECTING);
    }
	
	private void onConnected() {		
		stateNotify(ConnectionManager.STATE_CONNECTED);
		mInitManager.onConnected();
	}
	
	private void onDisconnected() {
		stateNotify(ConnectionManager.STATE_DISCONNECTED);
		mInitManager.onDisconnected();
	}
	
	private void onFailure(int code) {
		setErrorCode(code);		
		stateNotify(ConnectionManager.STATE_FAILURE);		
		disconnect();
	}
	
	@Override
	protected void onData(byte[] rawData) {
		byte[] data;
		data = mPacketManager.bytesToPacket(rawData);		
		if (data == null) {
			//TODO failure?
			return;
		}
		
		mInitManager.onData(data);		
		super.onData(data);			
	}	
	
	public BTConnectionManager(InitManager initManager, Application app, String mac, byte[] key, boolean isBT40) {		
		mInitManager = initManager;		
		mMac = mac;		
		mKey = key;
		mApp = app;
		mIsBT40 = isBT40;
	}

	public BTConnectionManager(InitManager initManager, Application app, String mac, byte[] key) {		
		this(initManager, app, mac, key, false);
	}
	
	@Override
	public void connect() {
		connect(false, BTService.DEFAULT_CONNECT_TIMEOUT);
	}

	public void connect(boolean reflection, int timeout, boolean doNotAsk) {
		resetErrorCode();
		if (mBTService == null) {
			mBTService = new BTService(mApp, mBTHandler);
			mPacketManager = new PacketManager(mBTService, mKey);
			mInitManager.init(this, mPacketManager);
		}
		mBTService.setConnectTimeout(timeout);
		mBTService.enableReflection(reflection);
		mBTService.connect(mMac, doNotAsk, mIsBT40);
		onConnecting();		
	}
		
	public void connect(boolean reflection, int timeout) {
		connect(reflection, timeout, false);
	}

	@Override
	public void disconnect() {
		if (mBTService != null) {
			mBTService.disconnect();
		}
	}
	
	public void disconnect(int failureCode) {
		onFailure(failureCode);
	}
	
	public String getMac() {
		return mMac;
	}
	
	public void changeKey(byte[] key) {
		mKey = key;
		mPacketManager.changeKey(mKey);
	}
	

	@Override
	public void sendPacket(Packet p) {
		mPacketManager.sendPacket(p); 
	}
	

	@Override
	public void onInitReady() {
		stateNotify(ConnectionManager.STATE_READY);
	}

	@Override
	public void onInitNotReady() {
		stateNotify(ConnectionManager.STATE_CONNECTED);
	}

	@Override
	public void onInitFailure(int code) {
		onFailure(code);		
	}	

}
