package com.inputstick.api.init;

import android.os.Handler;

import com.inputstick.api.InputStickError;
import com.inputstick.api.Packet;


public class BasicInitManager extends InitManager {	
	
	private int lastStatusParam;
	private Handler handler;
	private boolean cancelled;
	private boolean doNotSetUpdateInterval;

	public BasicInitManager(byte[] key) {
		super(key);
		lastStatusParam = 0;
	}
	

	@Override
	public void onConnected() {		
		lastStatusParam = 0;
		cancelled = false;
		initDone = false;
		sendPacket(new Packet(true, Packet.CMD_RUN_FW));	
		
		handler = new Handler();
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
				if ((!cancelled) && ( !initDone)) {
					sendPacket(new Packet(true, Packet.CMD_RUN_FW));
				}
		    }
		}, 1000);
		
		handler.postDelayed(new Runnable() {
		    @Override
		    public void run() {
				if ((!cancelled) && ( !initDone)) {
					mListener.onInitFailure(InputStickError.ERROR_INIT_TIMEDOUT);
				}
		    }
		}, 2000);
	}
	
	@Override
	public void onDisconnected() {
		cancelled = true;
	}
	
	@Override
	public void onData(byte[] data) {
		byte cmd = data[0];
		byte respCode = data[1];
		byte param = data[1];
		
		switch (cmd) {
			case Packet.CMD_RUN_FW:						
				sendPacket(new Packet(true, Packet.CMD_FW_INFO));
				break;
			case Packet.CMD_FW_INFO:
				onFWInfo(data, true, true, new Packet(true, Packet.CMD_INIT)); 
				break;
			case Packet.CMD_INIT:
				if (respCode == Packet.RESP_OK) {
					initDone = true;
					//sendPacket(new Packet(true, Packet.CMD_HID_STATUS_REPORT));
					
					if (mInfo.getFirmwareVersion() >= 100) {
						sendPacket(new Packet(true, Packet.CMD_SET_UPDATE_INTERVAL, (byte)5)); //request 500ms update interval
					} else {
						if ( !doNotSetUpdateInterval) {
							setStatusUpdateInterval(100);
						}
					}
					
				} else {
					mListener.onInitFailure(respCode);
				}				
				break;
			case Packet.CMD_SET_UPDATE_INTERVAL:
				if (respCode == Packet.RESP_OK) {
					setStatusUpdateInterval(500);
				}
				break;
			case Packet.CMD_INIT_AUTH:
			case Packet.CMD_INIT_AUTH_HMAC:
				initDone = onAuth(data, true, new Packet(true, Packet.CMD_INIT)); 
				break;
			case Packet.CMD_HID_STATUS:
				if (mKey == null) {
					initDone = true;
				}
				
				if (initDone) {
					if (param != lastStatusParam) {
						lastStatusParam = param;
						if (param == 0x05) {						
							mListener.onInitReady();
						} else {
							mListener.onInitNotReady();
						}
					}
				}
				break;
		}
	}
	
	public void disableStatusUpdateInterval() {
		doNotSetUpdateInterval = true;
	}
	

}
