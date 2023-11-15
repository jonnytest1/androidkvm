package com.inputstick.api.init;

import com.inputstick.api.InputStickError;
import com.inputstick.api.Packet;
import com.inputstick.api.PacketManager;

public class InitManager {
	
	public static final int DEFAULT_INIT_TIMEOUT = 60000; //60s init timeout 
	
	protected PacketManager mPacketManager;
	protected InitManagerListener mListener;	
	protected byte[] mKey;
	protected DeviceInfo mInfo;
	protected boolean initDone;
	
	public InitManager(byte[] key) {
		mKey = key;					
	}
	

	public DeviceInfo getDeviceInfo() {
		return mInfo;
	}
	
	public boolean isEncrypted() {
		return mPacketManager.isEncrypted();
	}
	
	
	public void init(InitManagerListener listener, PacketManager packetManager) {		
		mListener = listener;
		mPacketManager = packetManager;
		
		initDone = false;			
	}	
	
	public void onConnected() {
		mListener.onInitReady();
	}
	
	public void onDisconnected() {
	}
		
	public void onData(byte[] data) {
		//byte cmd = data[0];
		//byte param = data[1];				
	}
	
	public void sendPacket(Packet p) {
		mPacketManager.sendPacket(p);
	}
	
	public void setStatusUpdateInterval(int interval) {
		mPacketManager.setStatusUpdateInterval(interval);
	}
	
	public void onFWInfo(byte[] data, boolean authenticate, boolean enableEncryption, Packet sendNext) {
		mInfo = new DeviceInfo(data);			
		
		if (authenticate) {
			if (mInfo.isPasswordProtected()) {
				if (mKey != null) {
					//authenticate
					boolean hmac = (mInfo.getFirmwareVersion() >= 100);
					sendPacket(mPacketManager.encPacket(enableEncryption, hmac));
				} else {
					mListener.onInitFailure(InputStickError.ERROR_SECURITY_NO_KEY);
				}
			} else {
				if (mKey != null) {
					//possible scenarios: FW upgrade / password removed using other device/app / tampering! 
					mListener.onInitFailure(InputStickError.ERROR_SECURITY_NOT_PROTECTED);
				} 				
				sendPacket(sendNext);
			}		
		} else {
			sendPacket(sendNext);
		}
	}
	
	public boolean onAuth(byte[] data, boolean enableOutEncryption, Packet sendNext) {
		byte respCode = data[1];
		
		switch (respCode) {
			case Packet.RESP_OK:
				//TODO check length!
				byte[] cmp = new byte[16];
				byte[] hmacKey = null;
				System.arraycopy(data, 2, cmp, 0, 16);
				
				if (mInfo.getFirmwareVersion() >= 100) {
					hmacKey = new byte[32];
					System.arraycopy(data, 18, hmacKey, 0, 32);
				}
				
				if (mPacketManager.setEncryption(cmp, enableOutEncryption, hmacKey)) {
					sendPacket(sendNext);
					return true;
				} else {
					mListener.onInitFailure(InputStickError.ERROR_SECURITY_CHALLENGE);
				}	
				break;
				
			case 0x20:
				mListener.onInitFailure(InputStickError.ERROR_SECURITY_INVALID_KEY);
				break;
				
			case 0x21:
				mListener.onInitFailure(InputStickError.ERROR_SECURITY_NOT_PROTECTED);
				break;	
				
			case Packet.RESP_UNKNOWN_CMD:
				mListener.onInitFailure(InputStickError.ERROR_SECURITY_NOT_SUPPORTED);
				break;
				
			default:
				mListener.onInitFailure(InputStickError.ERROR_SECURITY);
		}
		return false;

	}
	
}
