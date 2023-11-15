package com.inputstick.api.basic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;

import com.inputstick.api.BTConnectionManager;
import com.inputstick.api.ConnectionManager;
import com.inputstick.api.DownloadDialog;
import com.inputstick.api.HIDInfo;
import com.inputstick.api.IPCConnectionManager;
import com.inputstick.api.InputStickDataListener;
import com.inputstick.api.InputStickError;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.OnEmptyBufferListener;
import com.inputstick.api.Packet;
import com.inputstick.api.hid.HIDTransaction;
import com.inputstick.api.hid.HIDTransactionQueue;
import com.inputstick.api.init.BasicInitManager;
import com.inputstick.api.init.DeviceInfo;
import com.inputstick.api.init.InitManager;

public class InputStickHID implements InputStickStateListener, InputStickDataListener  {
	
	public static final int INTERFACE_KEYBOARD = 0;
	public static final int INTERFACE_CONSUMER = 1;
	public static final int INTERFACE_MOUSE = 2;
	public static final int INTERFACE_RAW_HID = 3;
	
	private static ConnectionManager mConnectionManager;
	
	private static Vector<InputStickStateListener> mStateListeners = new Vector<InputStickStateListener>();
	protected static Vector<OnEmptyBufferListener> mBufferEmptyListeners = new Vector<OnEmptyBufferListener>();	
	
	private static InputStickHID instance = new InputStickHID();
	private static HIDInfo mHIDInfo;
	private static DeviceInfo mDeviceInfo;
	
	private static HIDTransactionQueue keyboardQueue;
	private static HIDTransactionQueue mouseQueue;
	private static HIDTransactionQueue consumerQueue;
	private static HIDTransactionQueue rawHIDQueue;
	
	private InputStickHID() {
	}
	
	public static InputStickHID getInstance() {
		return instance;
	}
	
	private static void init() {
		mHIDInfo = new HIDInfo();	
		keyboardQueue = new HIDTransactionQueue(INTERFACE_KEYBOARD, mConnectionManager, 32, 32);
		mouseQueue = new HIDTransactionQueue(INTERFACE_MOUSE, mConnectionManager, 32, 32);
		consumerQueue = new HIDTransactionQueue(INTERFACE_CONSUMER, mConnectionManager, 32, 32);
		rawHIDQueue = new HIDTransactionQueue(INTERFACE_RAW_HID, mConnectionManager, 2, 1);
		
		mConnectionManager.addStateListener(instance);
		mConnectionManager.addDataListener(instance);
		mConnectionManager.connect();		
	}
	
	
	/*
	 * Returns download InputStickUtility AlertDialog if InputStickUtility is not installed. Returns null is InputStickUtility application is installed.
	 * Should be called when your application is started or before InputStick functionality is about to be used. 
	 * 
	 * @return download InputStickUtility AlertDialog or null
	 */
	public static AlertDialog getDownloadDialog(final Context ctx) {
		if (mConnectionManager.getErrorCode() == InputStickError.ERROR_ANDROID_NO_UTILITY_APP) {
			return DownloadDialog.getDialog(ctx, DownloadDialog.NOT_INSTALLED);
		} else {
			return null;
		}
	}	
	
	
	/*
	 * Connect using InputStickUtility application.
	 * IN MOST CASES THIS METHOD SHOULD BE USED TO INITIATE CONNECTION!
	 * 
	 * @param app	Application 
	 */
	public static void connect(Application app) {
		mConnectionManager = new IPCConnectionManager(app);
		init();
	}			
	
	
	/*
	 * Close connection
	 */
	public static void disconnect() { 
		if (mConnectionManager != null) {
			mConnectionManager.disconnect();
		}
	}


	/*
	 * Direct connection to InputStick (BT2.1 only!). InputStickUtility application is not required in this case.
	 * TIP: use Util.getPasswordBytes(plainText) to get key.
	 * 
	 * @param app	Application
	 * @param mac	Bluetooth MAC address
	 * @param key	MD5(password) - must be provided if InputStick is password protected. Use null otherwise
	 * @param initManager	custom init manager	 
	 */
	public static void connect(Application app, String mac, byte[] key, InitManager initManager) {
		connect(app, mac, key, initManager, false);
	}	
	
	
	/*
	 * Direct connection to InputStick. InputStickUtility application is not required in this case.
	 * TIP: use Util.getPasswordBytes(plainText) to get key.
	 * 
	 * @param app	Application
	 * @param mac	Bluetooth MAC address
	 * @param key	MD5(password) - must be provided if InputStick is password protected. Use null otherwise
	 * @param initManager	custom init manager	 
	 * @param isBT40	specify Bluetooth version. Must match your hardware (InputStick BT2.1 or BT4.0)!
	 */	
	public static void connect(Application app, String mac, byte[] key, InitManager initManager, boolean isBT40) {
		mConnectionManager = new BTConnectionManager(initManager, app, mac, key, isBT40);		
		init();
	}
	

	/*
	 * Direct connection to InputStick. InputStickUtility application is not required in this case.
	 * TIP: use Util.getPasswordBytes(plainText) to get key.
	 * 
	 * @param app	Application
	 * @param mac	Bluetooth MAC address
	 * @param key	MD5(password) - must be provided if InputStick is password protected. Use null otherwise
	 * @param initManager	custom init manager	 
	 * @param isBT40	specify Bluetooth version. Must match your hardware (InputStick BT2.1 or BT4.0)!
	 */	
	public static void connect(Application app, String mac, byte[] key, boolean isBT40) {
		mConnectionManager = new BTConnectionManager(new BasicInitManager(key), app, mac, key, isBT40);
		init();
	}
	

	/*
	 * Direct connection to InputStick (BT2.1 only!). InputStickUtility application is not required in this case.
	 * TIP: use Util.getPasswordBytes(plainText) to get key.
	 * 
	 * @param app	Application
	 * @param mac	Bluetooth MAC address
	 * @param key	MD5(password) - must be provided if InputStick is password protected. Use null otherwise
	 */	
	public static void connect(Application app, String mac, byte[] key) {
		connect(app, mac, key, false);
	}	


	/*
	 * Requests USB host to resume from sleep / suspended state. Feature must be supported and enabled by USB host.
	 * Note 1: when USB host is suspended, device state will be STATE_CONNECTED.
	 * Note 2: some USB hosts may cut off USB power when suspended.	 
	 */
	public static void wakeUpUSBHost() {
		if (isConnected()) {
			Packet p = new Packet(false, Packet.CMD_USB_RESUME);
            InputStickHID.sendPacket(p);
			mConnectionManager.sendPacket(p);
		}
	}
	
	
	/*
	 * Returns ConnectionManager
	 * 
	 * @return ConnectionManager
	 */
	public static ConnectionManager getConnectionManager() {
		return mConnectionManager;
	}
	
	
	/*
	 * Get device info of connected device
	 * 
	 * @return Device info of connected device. Null if info is not available
	 */
	public static DeviceInfo getDeviceInfo() {
		if ((isReady()) && (mDeviceInfo != null)) {
			return mDeviceInfo;
		} else {
			return null;
		}
	}
	
	
	/*
	 * Get latest status update received from InputStick.
	 * 
	 * @return	latest status update
	 */
	public static HIDInfo getHIDInfo() {
		return mHIDInfo;
	}
	

	/*
	 * Returns current state of the connection.
	 * 
	 * @return state of the connection
	 */
	public static int getState() {
		if (mConnectionManager != null) {
			return mConnectionManager.getState();
		} else {
			return ConnectionManager.STATE_DISCONNECTED;
		}
	}
	
	
	/*
	 * Returns last error code. See class InputStickError.
	 * 
	 * @return last error code	 
	 */
	public static int getErrorCode() {
		if (mConnectionManager != null) {
			return mConnectionManager.getErrorCode();
		} else {
			return InputStickError.ERROR_UNKNOWN;
		} 
	}
	
	
	/*
	 * Returns reason code for last disconnect event. See ConnectionManager for list of disconnect reason codes 
	 * 
	 * @return last disconnect reason code
	 */
	public static int getDisconnectReason() {
		if (mConnectionManager != null) {
			return mConnectionManager.getDisconnectReason();
		} else {
			return ConnectionManager.DISC_REASON_UNKNOWN;
		} 
	}	
	

	/*
	 * Checks if Bluetooth connection between Android device and InputStick is established.
	 * Note -  InputStick may be not ready yet to accept keyboard/mouse data.
	 * 
	 * @return true if Bluetooth connection is established
	 */
	public static boolean isConnected() {
		if ((getState() == ConnectionManager.STATE_READY) ||  (getState() == ConnectionManager.STATE_CONNECTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	
	/*
	 * Checks if InputStick is ready to accept keyboard/mouse/etc. data.
	 * 
	 * @return true if InputStick is ready to accept data
	 */
	public static boolean isReady() {
		if (getState() == ConnectionManager.STATE_READY) {
			return true;
		} else {
			return false;
		}
	}


	/*
	 * Adds InputStickStateListener. Listener will be notified when connection state changes. 
	 * 
	 * @param listener	listener to add
	 */
	public static void addStateListener(InputStickStateListener listener) {
		if (listener != null) {
			if ( !mStateListeners.contains(listener)) {
				synchronized (mStateListeners) {
					mStateListeners.add(listener);
				}
			}
		}
	}
	
	
	/*
	 * Removes InputStickStateListener. Listener will no longer be notified when connection state changes. 
	 * 
	 * @param listener	listener to remove
	 */	
	public static void removeStateListener(InputStickStateListener listener) {
		if (listener != null) {
			synchronized (mStateListeners) {
				mStateListeners.remove(listener);
			}
		}
	}


	/*
	 * Adds OnEmptyBufferListener.  Listeners will be notified when local (application) or remote (InputStick) HID report buffer is empty.
	 * 
	 * @param listener	listener to add
	 */
	public static void addBufferEmptyListener(OnEmptyBufferListener listener) {
		if (listener != null) {
			synchronized(mBufferEmptyListeners) {
				if ( !mBufferEmptyListeners.contains(listener)) {
					mBufferEmptyListeners.add(listener);
				}
			}
		}
	}
	
	
	/*
	 * Removes OnEmptyBufferListener.
	 * 
	 * @param listener	listener to remove
	 */	
	public static void removeBufferEmptyListener(OnEmptyBufferListener listener) {
		if (listener != null) {
			synchronized(mBufferEmptyListeners) {
				mBufferEmptyListeners.remove(listener);
			}
		}		
	}
	
	

	
	public static void sendEmptyBufferNotifications(int bufferType, int interfaceType) {		
		if (bufferType == 1) { //remote buffer
			synchronized(mBufferEmptyListeners) {
				for (OnEmptyBufferListener listener : mBufferEmptyListeners) {
					listener.onRemoteBufferEmpty(interfaceType);
				}			
			}
		} else if (bufferType == 2) { //local buffer
			synchronized(mBufferEmptyListeners) {
				for (OnEmptyBufferListener listener : mBufferEmptyListeners) {
					listener.onLocalBufferEmpty(interfaceType);
				}			
			}
		}		
	}
	

	/*
	 * Adds transaction to keyboard queue. 
	 * If possible, all reports form a single transactions will be sent in a single packet.
	 * This should prevent from key being stuck in pressed position when connection is suddenly lost.
	 * 
	 * @param transaction	transaction to be queued
	 * @param sendNow	if true transaction will be sent now (if possible), if false, when next status update is received or when buffer is flushed
	 */
	public static void addKeyboardTransaction(HIDTransaction transaction, boolean sendNow) {
		if ((transaction != null) && (keyboardQueue != null)) {
			keyboardQueue.addTransaction(transaction, sendNow);
		}
	}
	
	public static void addKeyboardTransaction(HIDTransaction transaction) {
		if ((transaction != null) && (keyboardQueue != null)) {
			keyboardQueue.addTransaction(transaction, true);
		}
	}
	
	/*
	 * Send all reports currently stored in keyboard queue
	 */
	public static void flushKeyboardBuffer() {
		if (keyboardQueue != null) {
			keyboardQueue.sendFromQueue();
		}
	}
	

	/*
	 * Adds transaction to mouse queue. 
	 * If possible, all reports form a single transactions will be sent in a single packet.
	 * 
	 * @param transaction	transaction to be queued	 
	 * @param sendNow	if true transaction will be sent now (if possible), if false, when next status update is received or when buffer is flushed
	 */
	public static void addMouseTransaction(HIDTransaction transaction, boolean sendNow) {
		if ((transaction != null) && (mouseQueue != null)) {
			mouseQueue.addTransaction(transaction, sendNow);
		}
	}
	
	public static void addMouseTransaction(HIDTransaction transaction) {
		if ((transaction != null) && (mouseQueue != null)) {
			mouseQueue.addTransaction(transaction, true);
		}
	}
	
	/*
	 * Send all reports currently stored in mouse queue
	 */
	public static void flushMouseBuffer() {
		if (mouseQueue != null) {
			mouseQueue.sendFromQueue();
		}
	}
	
	/*
	 * Adds transaction to consumer control queue. 
	 * If possible, all reports form a single transactions will be sent in a single packet.
	 * 
	 * @param transaction	transaction to be queued	 
	 * @param sendNow	if true transaction will be sent now (if possible), if false, when next status update is received or when buffer is flushed
	 */
	public static void addConsumerTransaction(HIDTransaction transaction, boolean sendNow) {
		if ((transaction != null) && (consumerQueue != null)) {
			consumerQueue.addTransaction(transaction, sendNow);
		}
	}
	
	public static void addConsumerTransaction(HIDTransaction transaction) {
		if ((transaction != null) && (consumerQueue != null)) {
			consumerQueue.addTransaction(transaction, true);
		}
	}
	
	/*
	 * Send all reports currently stored in consumer control queue
	 */
	public static void flushConsumerBuffer() {
		if (consumerQueue != null) {
			consumerQueue.sendFromQueue();
		}
	}
	
	
	/*
	 * Adds transaction to raw HID queue. 
	 * If possible, all reports form a single transactions will be sent in a single packet.
	 * 
	 * @param transaction	transaction to be queued	 
	 * @param sendNow	if true transaction will be sent now (if possible), if false, when next status update is received or when buffer is flushed
	 */
	public static void addRawHIDTransaction(HIDTransaction transaction, boolean sendNow) {
		if ((transaction != null) && (rawHIDQueue != null)) {
			rawHIDQueue.addTransaction(transaction, sendNow);
		}
	}
	
	public static void addRawHIDTransaction(HIDTransaction transaction) {
		if ((transaction != null) && (rawHIDQueue != null)) {
			rawHIDQueue.addTransaction(transaction, true);
		}
	}
	
	/*
	 * Send all reports currently stored in raw HID queue
	 */
	public static void flushRawHIDBuffer() {
		if (rawHIDQueue != null) {
			rawHIDQueue.sendFromQueue();
		}
	}


	/*
	 * Removes all reports from keyboard buffer.	 
	 */
	public static void clearKeyboardBuffer() {
		if (keyboardQueue != null) {
			keyboardQueue.clearBuffer();
		}
	}
	
	
	/*
	 * Removes all reports from mouse buffer.	 
	 */
	public static void clearMouseBuffer() {
		if (mouseQueue != null) {
			mouseQueue.clearBuffer();
		}
	}
	

	/*
	 * Removes all reports from consumer control buffer.	 
	 */
	public static void clearConsumerBuffer() {
		if (consumerQueue != null) {
			consumerQueue.clearBuffer();
		}
	}
	
	
	/*
	 * Removes all reports from consumer control buffer.	 
	 */
	public static void clearRawHIDBuffer() {
		if (rawHIDQueue != null) {
			rawHIDQueue.clearBuffer();
		}
	}
	
	
	/*
	 * Removes all reports from all buffers.	 
	 */
	public static void clearAllBuffers() {
		clearKeyboardBuffer();
		clearMouseBuffer();
		clearConsumerBuffer();
		clearRawHIDBuffer();
	}
	

	/*
	 * Sends custom packet to InputStick.
	 * 
	 * @param p	packet to send.	 
	 */
	public static boolean sendPacket(Packet p) {
		if (mConnectionManager != null) {
			mConnectionManager.sendPacket(p);
			return true;
		} else {
			return false;
		}
	}					

	
	/*
	 * Checks if local (Android device) keyboard report buffer is empty. It is possible that there are reports queued remote (InputStick device) buffer.
	 *
	 * @return true if local keyboard buffer is empty, false otherwise
	 */
	public static boolean isKeyboardLocalBufferEmpty() {
		if (keyboardQueue != null) {
			return keyboardQueue.isLocalBufferEmpty();
		} else {
			return true;
		}
	}
	
	
	/*
	 * Checks if local (Android device) mouse report buffer is empty. It is possible that there are reports queued in remote (InputStick device) buffer.
	 *
	 * @return true if local mouse buffer is empty, false otherwise
	 */
	public static boolean isMouseLocalBufferEmpty() {
		if (mouseQueue != null) {
			return mouseQueue.isLocalBufferEmpty();
		} else {
			return true;
		}
	}
	
	
	/*
	 * Checks if local (Android device) consumer control report buffer is empty. It is possible that there are reports queued remote (InputStick device) buffer.
	 *
	 * @return true if local consumer control buffer is empty, false otherwise
	 */
	public static boolean isConsumerLocalBufferEmpty() {
		if (consumerQueue != null) {
			return consumerQueue.isLocalBufferEmpty();
		} else {
			return true;
		}
	}
	
	
	/*
	 * Checks if local (Android device) raw HID report buffer is empty. It is possible that there are reports queued in remote (InputStick device) buffer.
	 *
	 * @return true if local raw HID buffer is empty, false otherwise
	 */
	public static boolean isRawHIDLocalBufferEmpty() {
		if (rawHIDQueue != null) {
			return rawHIDQueue.isLocalBufferEmpty();
		} else {
			return true;
		}
	}
	
	
	/*
	 * Checks if all local (Android device) report buffers are empty. It is possible that there are reports queued in remote (InputStick device) buffers.
	 *
	 * @return true if local raw HID buffer is empty, false otherwise
	 */
	public static boolean areAllLocalBuffersEmpty() {
		return (isKeyboardLocalBufferEmpty() && isMouseLocalBufferEmpty() && isConsumerLocalBufferEmpty() && isRawHIDLocalBufferEmpty());
	}
	
	
	/*
	 * Checks if local (Android device) AND remote (InputStick device) keyboard report buffers are empty.
	 *
	 * @return true if local and remote keyboard buffers are empty, false otherwise
	 */
	public static boolean isKeyboardRemoteBufferEmpty() {
		if (keyboardQueue != null) {
			return keyboardQueue.isRemoteBufferEmpty();
		} else {
			return true;
		}		
	}
	
	
	/*
	 * Checks if local (Android device) AND remote (InputStick device) mouse report buffers are empty.
	 *
	 * @return true if local and remote mouse buffers are empty, false otherwise
	 */
	public static boolean isMouseRemoteBufferEmpty() {
		if (mouseQueue != null) {
			return mouseQueue.isRemoteBufferEmpty();
		} else {
			return true;
		}
	}
	
	
	/*
	 * Checks if local (Android device) AND remote (InputStick device) consumer control report buffers are empty.
	 *
	 * @return true if local and remote consumer control buffers are empty, false otherwise
	 */
	public static boolean isConsumerRemoteBufferEmpty() {
		if (consumerQueue != null) {
			return consumerQueue.isRemoteBufferEmpty();
		} else {
			return true;
		}
	}

	
	/*
	 * Checks if local (Android device) AND remote (InputStick device) raw HID report buffers are empty.
	 *
	 * @return true if local and remote raw HID buffers are empty, false otherwise
	 */
	public static boolean isRawHIDRemoteBufferEmpty() {
		if (rawHIDQueue != null) {
			return rawHIDQueue.isRemoteBufferEmpty();
		} else {
			return true;
		}
	}
	
	
	/*
	 * Checks if all local (Android device) AND remote (InputStick device) report buffers are empty.
	 *
	 * @return true if local raw HID buffer is empty, false otherwise
	 */
	public static boolean areAllRemoteBuffersEmpty() {
		return (isKeyboardRemoteBufferEmpty() && isMouseRemoteBufferEmpty() && isConsumerRemoteBufferEmpty() && isRawHIDRemoteBufferEmpty());
	}
	
	
	@Override
	public void onStateChanged(int state) {		
		synchronized (mStateListeners) {
			ArrayList<InputStickStateListener> tmp = new ArrayList<InputStickStateListener>();
			for (InputStickStateListener listener : mStateListeners) {
				tmp.add(listener);
			}
			
			for (InputStickStateListener listener : tmp) {
				listener.onStateChanged(state);
			}
		}
	}

	@Override
	public void onInputStickData(byte[] data) {
		byte cmd = data[0];
		if (cmd == Packet.CMD_FW_INFO) {
			mDeviceInfo = new DeviceInfo(data);								
			if (mDeviceInfo.getFirmwareVersion() >= 100) {
				keyboardQueue.setCapacity(128);
				mouseQueue.setCapacity(64);
				consumerQueue.setCapacity(64);		
			} 				
		}
		
		if (cmd == Packet.CMD_HID_DATA_RAW) {
			if (data.length > 65) {
				InputStickRawHID.notifyRawHIDListeners(Arrays.copyOfRange(data, 1, 65));
			}
		}
		
		if (cmd == Packet.CMD_HID_STATUS) {
			mHIDInfo.update(data);
			
			InputStickKeyboard.setReportProtocol(mHIDInfo.isKeyboardReportProtocol());
			InputStickMouse.setReportProtocol(mHIDInfo.isMouseReportProtocol());
			
			if (keyboardQueue != null) {
				keyboardQueue.update(mHIDInfo);
			}
			if (mouseQueue != null) {
				mouseQueue.update(mHIDInfo);
			}
			if (consumerQueue != null) {
				consumerQueue.update(mHIDInfo);
			}
			if (rawHIDQueue != null) {
				rawHIDQueue.update(mHIDInfo);
			}
			
			InputStickKeyboard.setLEDs(mHIDInfo.getNumLock(), mHIDInfo.getCapsLock(), mHIDInfo.getScrollLock());			
		}
	}		
	
	

}
