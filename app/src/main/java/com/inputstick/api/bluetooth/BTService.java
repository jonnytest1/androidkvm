package com.inputstick.api.bluetooth;

import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;

import com.inputstick.api.InputStickError;
import com.inputstick.api.Util;

public class BTService {
	
	public static final int DEFAULT_CONNECT_TIMEOUT = 30000;
	
	public static final int EVENT_NONE = 0;
	public static final int EVENT_DATA = 1;
	public static final int EVENT_CONNECTED = 2;
	public static final int EVENT_CANCELLED = 3;
	public static final int EVENT_ERROR = 4;	
    
    
    private final Handler mHandler;
    private int mLastEvent;
    
    private String mMac;
    private final Application mApp;
    private final Context mCtx; 
    
    private boolean mUseReflection;
    private int mConnectTimeout;        
       
    private long timeout;
    private int retryCnt;    
    
    private boolean disconnecting;
    private boolean connected;
    
    private PacketReader mPacketReader;
    private BTConnection mBTConnection;        
    
    private boolean turnBluetoothOn;
    private boolean receiverRegistered;    
    
    private Handler mDelayHandler = new Handler();
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();	
			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
				if ((state == BluetoothAdapter.STATE_ON)  && (turnBluetoothOn)) {					
					turnBluetoothOn = false;
					//wait 250ms after adapter is enabled, otherwise it may not connect properly
					mDelayHandler.removeCallbacksAndMessages(null);
					mDelayHandler.postDelayed(new Runnable() {
						@Override
						public void run() {
							doConnect(false);
						}
					}, 250);
				}
			}			
		}
	};
    
    
    
    public BTService(Application app, Handler handler) {        
        mLastEvent = EVENT_NONE;
        mHandler = handler;
        mApp = app;
        mCtx = app.getApplicationContext();
        mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
    }    
    
    public void setConnectTimeout(int timeout) {
    	mConnectTimeout = timeout;
    }
    
    public void enableReflection(boolean enabled) {
    	mUseReflection = enabled;
    }
    
    public void setStatusUpdateInterval(int interval) {
        if (connected) {
        	mBTConnection.setStatusUpdateInterval(interval);
        }
	}
    

    protected synchronized void event(int event, int arg1) {    	
    	Util.log(Util.FLAG_LOG_BT_SERVICE, "event() " + mLastEvent + " -> " + event);	
        mLastEvent = event;        
        Message msg = Message.obtain(null, mLastEvent, arg1, 0);
        mHandler.sendMessage(msg);        
    }        
    
    public synchronized int getLastEvent() {
        return mLastEvent;
    }    
    
    private void doConnect(boolean reconnecting) {
    	if (reconnecting) {
    		retryCnt++;
    	} else {	
    		retryCnt = 0;
			timeout = System.currentTimeMillis() + mConnectTimeout;
    	}
        
    	mBTConnection.connect();
    }
    
    
    public synchronized void connect(String mac) {
    	connect(mac, false);
    }       
    
    public synchronized void connect(String mac, boolean doNotAsk) {
    	connect(mac, doNotAsk, false);
	}    
    
    public synchronized void connect(String mac, boolean doNotAsk, boolean bt40) {
    	try {
    		Util.log(Util.FLAG_LOG_BT_SERVICE, "connect to: " + mac + " REFLECTION: " + mUseReflection);
			disconnecting = false;
			connected = false;
			mMac = mac;
			if (BluetoothAdapter.checkBluetoothAddress(mac)) {
				BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
				if (mBluetoothAdapter == null) {
					event(BTService.EVENT_ERROR, InputStickError.ERROR_BLUETOOTH_NOT_SUPPORTED);
				} else {
					if (bt40) {
						mBTConnection = new BT40Connection(mApp, this, mMac, mUseReflection);
					} else {
						mBTConnection = new BT20Connection(mApp, this, mMac, mUseReflection);
					}			
					
					if (mBluetoothAdapter.isEnabled()) {	
						doConnect(false);					
					} else {											
				    	if (mApp != null) {
					    	turnBluetoothOn = true;					    	
					    	if ( !receiverRegistered) {
					    		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
					    		mCtx.registerReceiver(mReceiver, filter);
					    		receiverRegistered = true;
					    	}	    	
					    	
					    	if (doNotAsk) {
					    		BluetoothAdapter.getDefaultAdapter().enable();
					    	} else {
						    	Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
						    	enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						    	mApp.startActivity(enableBtIntent);
						    	//if BT is not enabled before timeout, show error
								mDelayHandler.postDelayed(new Runnable() {
									@Override
									public void run() {
										event(BTService.EVENT_ERROR, InputStickError.ERROR_BLUETOOTH_NOT_ENABLED);	
									}
								}, mConnectTimeout - 500);
					    	}
				    	}
					}
				}
			} else {
				event(BTService.EVENT_ERROR, InputStickError.ERROR_BLUETOOTH_INVALID_MAC);
			}	
		} catch (NoClassDefFoundError e) {
			event(BTService.EVENT_ERROR, InputStickError.ERROR_BLUETOOTH_BT40_NOT_SUPPRTED);		
		}
    }  
    
    public synchronized void disconnect() {
    	Util.log(Util.FLAG_LOG_BT_SERVICE, "disconnect");
    	
    	mDelayHandler.removeCallbacksAndMessages(null);
    	removeReceiver();
    	
        disconnecting = true;
        if (mBTConnection != null) { 
        	mBTConnection.disconnect();
        }
        event(EVENT_CANCELLED, 0);
    }


    public synchronized void write(byte[] out) {    	
        if (connected) {
        	mBTConnection.write(out);
        }    	
    }          
    
    

    
    protected synchronized void connectionEstablished() {
    	mDelayHandler.removeCallbacksAndMessages(null);
    	removeReceiver(); 

    	mPacketReader = new PacketReader(this, mHandler);    	
        timeout = 0;
        connected = true;       		
        event(EVENT_CONNECTED, 0);        
    }    
    
    
    protected void connectionFailed(boolean canRetry, int errorCode) {    	
    	mDelayHandler.removeCallbacksAndMessages(null);
    	removeReceiver(); 
    	
    	connected = false;
    	if (disconnecting) {
    		disconnecting = false;
    	} else {	
    		if (canRetry) {
		    	if ((timeout > 0) && (System.currentTimeMillis() < timeout)) {
		    		Util.log(Util.FLAG_LOG_BT_SERVICE, "RETRY: "+retryCnt + " time left: " + (timeout - System.currentTimeMillis()));    		
		    		doConnect(true);
		    	} else {    	
		    		event(EVENT_ERROR, InputStickError.ERROR_BLUETOOTH_CONNECTION_FAILED);
		    	}     
    		} else {    			
    			event(EVENT_ERROR, errorCode);
    		}
    	}
    }      
    
    
    protected synchronized void onByteRx(int rxByte) {
    	mPacketReader.rxByte((byte)rxByte);
    }
    
    //returns true if at least one complete packet was processed 
    protected synchronized boolean onByteRx(byte[] rxBytes) {
    	boolean result = false;
    	for (int i = 0; i < rxBytes.length; i++) {
    		if (mPacketReader.rxByte(rxBytes[i])) {
    			result = true;
    		}
    	}
    	return result;
    }
    
    
    private void removeReceiver() {
        if (receiverRegistered) {
        	mCtx.unregisterReceiver(mReceiver);
        	receiverRegistered = false;
        }    	
    }        
         
    
    
    public static boolean isBT40Supported() {
    	return (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2);
    }

}
