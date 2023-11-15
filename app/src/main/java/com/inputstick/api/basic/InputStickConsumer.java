package com.inputstick.api.basic;

import com.inputstick.api.hid.ConsumerReport;
import com.inputstick.api.hid.HIDTransaction;

public class InputStickConsumer {
	
	//CONSUMER PAGE (consumerAction)
	public static final int VOL_UP = 0x00E9;
	public static final int VOL_DOWN = 0x00EA;
	public static final int VOL_MUTE = 0x00E2;
	public static final int TRACK_NEXT = 0x00B5;
	public static final int TRACK_PREV = 0x00B6;
	public static final int STOP = 0x00B7;
	public static final int PLAY_PAUSE = 0x00CD;
	
	public static final int LAUNCH_BROWSER = 0x0196;
	public static final int LAUNCH_EMAIL = 0x018A;
	public static final int LAUNCH_CALC = 0x0192;
	
	//Android OS (consumer):
	public static final int HOME = 0x0223;
	public static final int BACK = 0x0224;
	public static final int FORWARD = 0x0225;
	public static final int REFRESH = 0x0227;
	public static final int SEARCH = 0x0221;
	
	
	//SYSTEM PAGE (systemAction)
	public static final byte SYSTEM_POWER_DOWN = 0x01;
	public static final byte SYSTEM_SLEEP = 0x02;
	public static final byte SYSTEM_WAKEUP = 0x03;		
	
	private InputStickConsumer() {
		
	}
	
	
	/*
	 * Use only for system actions SYSTEM_POWER_DOWN, SYSTEM_SLEEP and SYSTEM_WAKEUP
	 * 
	 * @param action	code of system action
	 */
	public static void systemAction(byte action) {
		HIDTransaction t = new HIDTransaction();
		t.addReport(new ConsumerReport(ConsumerReport.SYSTEM_REPORT_ID, action, (byte)0));
		t.addReport(new ConsumerReport(ConsumerReport.SYSTEM_REPORT_ID, (byte)0, (byte)0));
		InputStickHID.addConsumerTransaction(t, true);	
	}	
	
	
	/*
	 * Requests USB host to power down. Must be supported and enabled by USB host.
	 */
	public static void systemPowerDown() {
		systemAction(SYSTEM_POWER_DOWN);	
	}
	
	
	/*
	 * Requests USB host to go into sleep/standby mode. Must be supported and enabled by USB host.
	 */
	public static void systemSleep() {
		systemAction(SYSTEM_SLEEP);	
	}
	
	
	/*
	 * Requests USB host to resume from sleep/standby mode. Must be supported and enabled by USB host.
	 * Note: USB host must supply USB power when suspended. Otherwise InputStick will not work.
	 */
	public static void systemWakeUp() {
		systemAction(SYSTEM_WAKEUP);	
	}
	
	
	/*
	 * Consumer control action: media playback, volume etc. 
	 * See http://www.usb.org/developers/hidpage/Hut1_12v2.pdf (consumer page).
	 * USB host may not support certain action codes
	 * 
	 * @param action	code of consumer control action
	 */
	public static void consumerAction(int action) {
		HIDTransaction t = new HIDTransaction();
		t.addReport(new ConsumerReport(action));
		t.addReport(new ConsumerReport());
		InputStickHID.addConsumerTransaction(t, true);		
	}


	/*
	 * Returns human-readable description for action 
	 * 
	 * @param action	code of consumer control action (HID usage ID)
	 */
	public static String actionToString(int action) {
		switch (action) {
			case VOL_UP:
				return "Volume up";	
			case VOL_DOWN:
				return "Volume down";
			case VOL_MUTE:
				return "Mute/Unmute";		
			case TRACK_NEXT:
				return "Next track";		
			case TRACK_PREV:
				return "Previous track";		
			case STOP:
				return "Stop";		
			case PLAY_PAUSE:
				return "Play/Pause";		
				
			case LAUNCH_BROWSER:
				return "Launch web browser";		
			case LAUNCH_EMAIL:
				return "Launch email client";		
			case LAUNCH_CALC:
				return "Launch calculator";		
				
			case HOME:
				return "Home button";		
			case BACK:
				return "Back button";		
			case FORWARD:
				return "Forward";		
			case REFRESH:
				return "Refresh";		
			case SEARCH:
				return "Search";		
				
			case SYSTEM_POWER_DOWN:
				return "Power down";		
			case SYSTEM_SLEEP:
				return "Sleep";		
			case SYSTEM_WAKEUP:
				return "Wake up";		
				
			default:
				return String.valueOf(action);					
		}
	
	}
}
