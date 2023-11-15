package com.inputstick.api.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.example.jonathan.androidkvm.Key;
import com.inputstick.api.ConnectionManager;
import com.inputstick.api.Packet;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.hid.HIDKeycodes;
import com.inputstick.api.hid.HIDTransaction;
import com.inputstick.api.hid.ShortKeyboardReport;
import com.inputstick.api.utils.remote.ModifiersSupport;

public abstract class KeyboardLayout {
	
	public static final int MAX_SCANCODE = 0x60;
	
	public static final byte[] scanCodeToHID = {
		/* 0x00 */ 0,
		/* 0x01 */ HIDKeycodes.KEY_ESCAPE,
		/* 0x02 */ HIDKeycodes.KEY_1,
		/* 0x03 */ HIDKeycodes.KEY_2,
		/* 0x04 */ HIDKeycodes.KEY_3,
		/* 0x05 */ HIDKeycodes.KEY_4,
		/* 0x06 */ HIDKeycodes.KEY_5,
		/* 0x07 */ HIDKeycodes.KEY_6,
		/* 0x08 */ HIDKeycodes.KEY_7,
		/* 0x09 */ HIDKeycodes.KEY_8,
		/* 0x0a */ HIDKeycodes.KEY_9,
		/* 0x0b */ HIDKeycodes.KEY_0,
		/* 0x0c */ HIDKeycodes.KEY_MINUS,
		/* 0x0d */ HIDKeycodes.KEY_EQUALS,
		/* 0x0e */ HIDKeycodes.KEY_BACKSPACE,
		/* 0x0f */ HIDKeycodes.KEY_TAB,
			
		
		/* 0x10 */ HIDKeycodes.KEY_Q,
		/* 0x11 */ HIDKeycodes.KEY_W,
		/* 0x12 */ HIDKeycodes.KEY_E,
		/* 0x13 */ HIDKeycodes.KEY_R,
		/* 0x14 */ HIDKeycodes.KEY_T,
		/* 0x15 */ HIDKeycodes.KEY_Y,
		/* 0x16 */ HIDKeycodes.KEY_U,
		/* 0x17 */ HIDKeycodes.KEY_I,
		/* 0x18 */ HIDKeycodes.KEY_O,
		/* 0x19 */ HIDKeycodes.KEY_P,
		/* 0x1a */ HIDKeycodes.KEY_LEFT_BRACKET,
		/* 0x1b */ HIDKeycodes.KEY_RIGHT_BRACKET,
		/* 0x1c */ HIDKeycodes.KEY_ENTER,
		/* 0x1d */ 0, //RL CTRL
		/* 0x1e */ HIDKeycodes.KEY_A,
		/* 0x1f */ HIDKeycodes.KEY_S,	
		
		/* 0x20 */ HIDKeycodes.KEY_D,
		/* 0x21 */ HIDKeycodes.KEY_F,
		/* 0x22 */ HIDKeycodes.KEY_G,
		/* 0x23 */ HIDKeycodes.KEY_H,
		/* 0x24 */ HIDKeycodes.KEY_J,
		/* 0x25 */ HIDKeycodes.KEY_K,
		/* 0x26 */ HIDKeycodes.KEY_L,
		/* 0x27 */ HIDKeycodes.KEY_SEMICOLON,
		/* 0x28 */ HIDKeycodes.KEY_APOSTROPHE,
		/* 0x29 */ HIDKeycodes.KEY_GRAVE,
		/* 0x2a */ 0, //L SHIFT
		/* 0x2b */ HIDKeycodes.KEY_BACKSLASH,
		/* 0x2c */ HIDKeycodes.KEY_Z,
		/* 0x2d */ HIDKeycodes.KEY_X,
		/* 0x2e */ HIDKeycodes.KEY_C,
		/* 0x2f */ HIDKeycodes.KEY_V,		
		
		/* 0x30 */ HIDKeycodes.KEY_B,
		/* 0x31 */ HIDKeycodes.KEY_N,
		/* 0x32 */ HIDKeycodes.KEY_M,
		/* 0x33 */ HIDKeycodes.KEY_COMA,
		/* 0x34 */ HIDKeycodes.KEY_DOT,
		/* 0x35 */ HIDKeycodes.KEY_SLASH,
		/* 0x36 */ 0, //R SHIFT
		/* 0x37 */ HIDKeycodes.KEY_PRINT_SCREEN,
		/* 0x38 */ 0, //RL ALT
		/* 0x39 */ HIDKeycodes.KEY_SPACEBAR,
		/* 0x3a */ HIDKeycodes.KEY_CAPS_LOCK,
		/* 0x3b */ HIDKeycodes.KEY_F1,
		/* 0x3c */ HIDKeycodes.KEY_F2,
		/* 0x3d */ HIDKeycodes.KEY_F3,
		/* 0x3e */ HIDKeycodes.KEY_F4,
		/* 0x3f */ HIDKeycodes.KEY_F5,
		
		/* 0x40 */ HIDKeycodes.KEY_F6,
		/* 0x41 */ HIDKeycodes.KEY_F7,
		/* 0x42 */ HIDKeycodes.KEY_F8,
		/* 0x43 */ HIDKeycodes.KEY_F9,
		/* 0x44 */ HIDKeycodes.KEY_F10,
		/* 0x45 */ HIDKeycodes.KEY_NUM_LOCK,
		/* 0x46 */ HIDKeycodes.KEY_SCROLL_LOCK,
		/* 0x47 */ HIDKeycodes.KEY_HOME,
		/* 0x48 */ HIDKeycodes.KEY_ARROW_UP,
		/* 0x49 */ HIDKeycodes.KEY_PAGE_UP,
		/* 0x4a */ 0, //-
		/* 0x4b */ HIDKeycodes.KEY_ARROW_LEFT,
		/* 0x4c */ 0, //CENTER
		/* 0x4d */ HIDKeycodes.KEY_ARROW_RIGHT,
		/* 0x4e */ 0, //+
		/* 0x4f */ HIDKeycodes.KEY_END,
		
		/* 0x50 */ HIDKeycodes.KEY_ARROW_DOWN,
		/* 0x51 */ HIDKeycodes.KEY_PAGE_DOWN,
		/* 0x52 */ HIDKeycodes.KEY_INSERT,
		/* 0x53 */ HIDKeycodes.KEY_DELETE,
		/* 0x54 */ 0,
		/* 0x55 */ 0,
		/* 0x56 */ HIDKeycodes.KEY_BACKSLASH_NON_US,  //GERMAN LAYOUT!
		/* 0x57 */ HIDKeycodes.KEY_F11,
		/* 0x58 */ HIDKeycodes.KEY_F12,
		/* 0x59 */ 0,
		/* 0x5a */ 0,
		/* 0x5b */ 0,
		/* 0x5c */ 0,
		/* 0x5d */ 0,
		/* 0x5e */ 0,
		/* 0x5f */ 0,		
		
	};	
	
	public static final KeyboardLayout[] keyboardLayouts = {		
		BelgianLayout.getInstance(),
		CanadianFrenchLayout.getInstance(),
		CroatianLayout.getInstance(),
		CzechLayout.getInstance(),
		CzechLinuxLayout.getInstance(),
		CzechProgrammersLayout.getInstance(),
		DanishLayout.getInstance(),
		DutchLayout.getInstance(),
		DvorakLayout.getInstance(),
		FinnishLayout.getInstance(),
		FrenchLayout.getInstance(),
		FrenchLinuxLayout.getInstance(),		
		GermanLayout.getInstance(),
		GermanMacLayout.getInstance(),
		GreekLayout.getInstance(),
		HebrewLayout.getInstance(),
		HungarianLayout.getInstance(),
		ItalianLayout.getInstance(),
		NorwegianLayout.getInstance(),
		PolishLayout.getInstance(),
		PolishLinuxLayout.getInstance(),
		PortugueseBrazilianLayout.getInstance(),
		PortugueseLayout.getInstance(),
		RussianLayout.getInstance(),
		SlovakLayout.getInstance(),
		SpanishLayout.getInstance(),		
		SwedishLayout.getInstance(),
		SwissFrenchLayout.getInstance(),
		SwissGermanLayout.getInstance(),
		UnitedKingdomLayout.getInstance(),
		UnitedStatesInternationalLayout.getInstance(),	
		UnitedStatesLayout.getInstance(),
	};
	
	public static final int LAYOUT_CODE = 0;
	
	public abstract int[][] getLUT();
	public abstract int[][] getFastLUT();
	public abstract int[][] getDeadkeyLUT();
	public abstract int[] 	getDeadkeys();
	public abstract String 	getLocaleName();	
	public abstract String 	getNativeName();	
	public abstract String 	getEnglishName();
	public abstract String 	getVariant();	
	
	/*
	 * Type text using InputStick. Assumes that USB host uses matching keyboard layout.
	 * 
	 *  @param text	text to type
	 */
	public abstract void type(String text);
	
	
	/*
	 * Type text using InputStick. Assumes that USB host uses matching keyboard layout.
	 * 
	 *  @param text	text to type
	 *  @param typingSpeed	use 0 for fastest typing speed (may not work with some USB hosts), use 1 for default typing speed, higher values decrease typing speed
	 */
	public abstract void type(String text, int typingSpeed);	
	
	
	/*
	 * Type text using InputStick. Assumes that USB host uses matching keyboard layout.
	 * Note: use only if you are certain that specified modifier keys will not cause any side effects during typing.
	 * 
	 *  @param text	text to type
	 *  @param modifiers	state of keyboard modifier keys (CTRL_LEFT .. GUI_RIGHT, see HIDKeycodes)
	 */
	public abstract void type(String text, byte modifiers);
	
	
	/*
	 * Type text using InputStick. Assumes that USB host uses matching keyboard layout.
	 * Note: use only if you are certain that specified modifier keys will not cause any side effects during typing.
	 * 
	 *  @param text	text to type
	 *  @param modifiers	state of keyboard modifier keys (CTRL_LEFT .. GUI_RIGHT, see HIDKeycodes)
	 *  @param typingSpeed	use 0 for fastest typing speed (may not work with some USB hosts), use 1 for default typing speed, higher values decrease typing speed
	 */
	public abstract void type(String text, byte modifiers, int typingSpeed);
	
	
	public abstract char getChar(int scanCode, boolean capsLock, boolean shift, boolean altGr);
	
	
	public void type(int[][] fastLUT, String text) {
		type(fastLUT, text, (byte)0, 1);
	}
	
	
	public void type(int[][] fastLUT, String text, byte modifiers) {
		type(fastLUT, text, modifiers, 1);
	}
	
	
	public void type(int[][] fastLUT, String text, byte modifiers, int typingSpeed) {
		if ((InputStickHID.getState() == ConnectionManager.STATE_READY) && (text != null)) {			
			char[] chars = text.toCharArray();
			HIDTransaction t;
			prevKey = 0; //static var, used only when typingSpeed == 0
			for (char c : chars) {
				t = getHIDTransaction(fastLUT, c, modifiers, typingSpeed);				
				if (t != null) {
					InputStickHID.addKeyboardTransaction(t, false);
				}				
			}
			//release key
			if (typingSpeed == 0) {
				t = new HIDTransaction(Packet.CMD_HID_DATA_KEYB_FAST);	
				t.addReport(new ShortKeyboardReport());
				InputStickHID.addKeyboardTransaction(t, false);
			}		
			InputStickHID.flushKeyboardBuffer();
		}
	}

	
	public static int hidToScanCode(byte key) {
		for (int scanCode = 0; scanCode < MAX_SCANCODE; scanCode++) {
			if (scanCodeToHID[scanCode] == key) {
				return scanCode;
			}
		}
		return -1;
	}
	
	public static char getChar(int[][] lut, int scanCode, boolean capsLock, boolean shift, boolean altGr) {
		if ((scanCode >= MAX_SCANCODE) || (scanCode < 0)) {
			return (char)0;
		}
		
		int index = 1;
		
		if ((capsLock) && (lut[scanCode][0] > 0)) {
			//capslock is on and it affects current key						
			if (lut[scanCode][0] == 1) {
				if (shift) {
					index = 1; //caps + shift = default
				} else {
					index = 2; //shift
				}
			} else {
				// >1
				if (shift) {
					if (altGr) {
						index = 4; //caps + shift + alt = alt
					} else {					
						index = 1; //caps + shift = default
					}
				} else {
					if (altGr) {
						index = 5; //caps + alt = shift + alt
					} else {
						index = 2; //caps = shift
					}
				}					
			}
		} else {				
			if (shift) {
				index = 2;
			}		
			if (altGr) {
				if (shift) {
					index = 5;
				} else {
					index = 4;
				}
			} 
		}
		
		if (lut[scanCode][index] == -1) {
			index = 1;
		} 
		return (char)lut[scanCode][index];		
	}
	
	public static int getScanCode(int[][] lut, char c) {		
		for (int scanCode = 0; scanCode < MAX_SCANCODE; scanCode++) {
			if (lut[scanCode][0] == -1) {
				continue;
			} else {
				for (int i = 1; i < 6; i++) {
					if (lut[scanCode][i] == (int)c) {
						return scanCode;
					}
				}
			}
		}
		return -1;
	}
	
	public static byte getKey(int scanCode) {	
		return scanCodeToHID[scanCode];
	}
	
	public static byte getModifiers(int[][] lut, int scanCode, char c) {
		if (lut[scanCode][1] == (int)c) {
			return 0;
		}
		if (lut[scanCode][2] == (int)c) {
			return HIDKeycodes.SHIFT_LEFT;
		}
		if (lut[scanCode][3] == (int)c) {
			return HIDKeycodes.CTRL_LEFT;
		}
		if (lut[scanCode][4] == (int)c) {
			return HIDKeycodes.ALT_RIGHT;
		}
		if (lut[scanCode][5] == (int)c) {
			return HIDKeycodes.SHIFT_LEFT | HIDKeycodes.ALT_RIGHT;
		}
		
		return 0;
	}
	
	
	public static boolean isDeadkey(int[] deadkeys, char c) {
		if (deadkeys != null) {
			for (int key : deadkeys) {
				if (key == (int)c) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static int searchLUT(int[][] deadkeyLUT, char c, int returnIndex) {
		if (deadkeyLUT != null) {
			for (int i = 0; i < deadkeyLUT.length; i++) {
				if (deadkeyLUT[i][2] == (int)c) {
					return deadkeyLUT[i][returnIndex];
				}
			}
		}
		return -1;
	}
	
	public static int findDeadKey(int[][] deadkeyLUT, char c) {
		return searchLUT(deadkeyLUT, c, 0);
	}
	
	public static int findFollowingKey(int[][] deadkeyLUT, char c) {
		return searchLUT(deadkeyLUT, c, 1);
	}
	
	
	private static byte prevKey; //used when typingSpeed == 0 (fastest), allows to "release" keys when two identical characters are to be typed consecutively 
		
	public static HIDTransaction getHIDTransaction(int[][] fastLUT, char c, byte additionalModifierKeys, int typingSpeed) {
		byte modifiers, key, deadKey, deadKeyModifiers;
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_KEYB_FAST);		
		
		for (int i = 0; i < fastLUT.length; i++) {
			if (fastLUT[i][0] == c) {					
				modifiers = (byte)fastLUT[i][1];
				modifiers |= additionalModifierKeys;
				key = (byte)fastLUT[i][2];
				deadKeyModifiers = (byte)fastLUT[i][3];
				deadKey = (byte)fastLUT[i][4];
									
				if (deadKey > 0) {
					if (typingSpeed == 0) {
						addPressAndReleaseReportsToHIDTransaction(t, deadKeyModifiers, deadKey, 1); //speed 1 to force releasing deadkey
					} else {
						addPressAndReleaseReportsToHIDTransaction(t, deadKeyModifiers, deadKey, typingSpeed);
					}
				}
				
				if (typingSpeed == 0) {
					if (key == prevKey) {
						t.addReport(new ShortKeyboardReport());
					}
					addPressAndReleaseReportsToHIDTransaction(t, modifiers, key, 0);
				} else {
					addPressAndReleaseReportsToHIDTransaction(t, modifiers, key, typingSpeed);
				}
				
				prevKey = key;
			}
		}			
		return t;
	}
	
	
	private static void addPressAndReleaseReportsToHIDTransaction(HIDTransaction t, byte modifiers, byte key, int typingSpeed) {
		if (typingSpeed == 0) {
			t.addReport(new ShortKeyboardReport(modifiers, key));
		} else {		
			for (int i = 0; i < typingSpeed; i++) {
				t.addReport(new ShortKeyboardReport(modifiers, (byte)0));
			}
			for (int i = 0; i < typingSpeed; i++) {
				t.addReport(new ShortKeyboardReport(modifiers, key));
			}
			for (int i = 0; i < typingSpeed; i++) {
				t.addReport(new ShortKeyboardReport());
			}
		}
	}
	
	
	public static HIDTransaction getHIDTransaction(int[][] lut, int[][] deadkeyLUT, int[] deadkeys, char c, byte additionalModifierKeys) {
		byte modifiers, key;
		int scanCode;
		
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_KEYB_FAST);		
		scanCode = getScanCode(lut, c);
		if (scanCode > 0) {			
			key = getKey(scanCode);
			modifiers = getModifiers(lut, scanCode, c);
			modifiers |= additionalModifierKeys;
			
			t.addReport(new ShortKeyboardReport(modifiers, (byte)0));
			t.addReport(new ShortKeyboardReport(modifiers, key));
			t.addReport(new ShortKeyboardReport());
			
			//add space after deadkey!
			if (isDeadkey(deadkeys, c)) {
				t.addReport(new ShortKeyboardReport((byte)0, HIDKeycodes.KEY_SPACEBAR)); //this won't work if modifiers are present!
				t.addReport(new ShortKeyboardReport());
			}
			
		} else {
			//check if character can be obtained using deadkey:
			int deadkey = findDeadKey(deadkeyLUT, c);
			if (deadkey > 0) { 				
				//yes it can
				int following = findFollowingKey(deadkeyLUT, c);								
				
				scanCode = getScanCode(lut, (char)deadkey);
				key = getKey(scanCode);
				modifiers = getModifiers(lut, scanCode, (char)deadkey);
				t.addReport(new ShortKeyboardReport(modifiers, (byte)0));
				t.addReport(new ShortKeyboardReport(modifiers, key));
				t.addReport(new ShortKeyboardReport());
				
				scanCode = getScanCode(lut, (char)following);
				key = getKey(scanCode);
				modifiers = getModifiers(lut, scanCode, (char)following);
				t.addReport(new ShortKeyboardReport(modifiers, (byte)0));
				t.addReport(new ShortKeyboardReport(modifiers, key));
				t.addReport(new ShortKeyboardReport());
			}
			
		}
		return t;
	}		
	
	
	/*
	 * Returns layout specified by locale (example: "de-DE"). If specified layout is not available, en-US layout is returned.
	 * 
	 *  @param locale	locale of requested layout
	 */		
	public static KeyboardLayout getLayout(String locale) {		
		if (locale != null) {
			for (KeyboardLayout layout : keyboardLayouts) {
				if (locale.equalsIgnoreCase(layout.getLocaleName())) {
					return layout;
				}
			}			
		}

		return UnitedStatesLayout.getInstance();
	}
	


	/*
	 * Returns array with layout names sorted by English names of layouts 
	 * 
	 * @param locale	use true if native version of layouts name should be added
	 */		
	public static CharSequence[] getLayoutNames(final boolean addNativeNames) {		
		ArrayList<LayoutInfo> infoArray = getLayoutInfoSortedByLayoutName();
		CharSequence[] result = new CharSequence[infoArray.size()];
		for (int i = 0; i < infoArray.size(); i++) {
			LayoutInfo info = infoArray.get(i);
			String tmp = info.englishName;			
			if ((addNativeNames) && ( !info.nativeName.equalsIgnoreCase("english"))) {
				tmp += " (" + info.nativeName + ")";
			}			
			tmp += " [" + info.variant + "]";			
			result[i] = tmp;
		}
		return result;
	}
	
	
	
	/*
	 * Returns array with layout codes (locale) sorted by English names of layouts, to match array returned by getLayoutNames method (example: index of "German" will be the same as of "de-DE")
	 * 
	 */	
	public static CharSequence[] getLayoutCodes() {
		ArrayList<LayoutInfo> tmp = getLayoutInfoSortedByLayoutName();
		CharSequence[] result = new CharSequence[tmp.size()];
		for (int i = 0; i < tmp.size(); i++) {
			result[i] = tmp.get(i).localeName;
		}
		return result;
	}

	public Key getText(ModifiersSupport mods, int[] key){
		int charc = key[1];
		boolean altgrEnabled=mods.toggleButtonAlt.isChecked()||(mods.toggleButtonCtrl.isChecked()&& mods.toggleButtonAlt.isChecked());

		if(altgrEnabled){
			charc=key[5];
		}else if(mods.toggleButtonCtrl.isChecked()){
			charc=key[3];
		}else if(mods.toggleButtonShift.isChecked()){
			charc=key[2];
		}
		if(charc==-1){
			return new Key(new String(new char[]{(char)  key[1]}),"");
		}
		return new Key(new String(new char[]{(char) charc}));
	}


	
	
	/*
	 * Returns description (locale, name in native language and English, keyboard layout variant) of all supported keyboard layouts
	 * 	 
	 */		
	
	private static ArrayList<LayoutInfo> getLayoutInfoSortedByLayoutName() {
		ArrayList<LayoutInfo> result = new ArrayList<LayoutInfo>();
		LayoutInfo info;
		for (int i = 0; i < keyboardLayouts.length; i++) {
			info = new LayoutInfo();
			info.localeName = keyboardLayouts[i].getLocaleName();
			info.nativeName = keyboardLayouts[i].getNativeName();
			info.englishName = keyboardLayouts[i].getEnglishName();
			info.variant = keyboardLayouts[i].getVariant();	
			result.add(info);
		}
		Collections.sort(result, new Comparator<LayoutInfo>() {
	        @Override
	        public int compare(LayoutInfo i1, LayoutInfo i2) {
	        	return i1.englishName.compareToIgnoreCase(i2.englishName);
	        }
	    });
		return result;
	}
			
	static private class LayoutInfo {		
		public String localeName;
		public String nativeName;
		public String englishName;
		public String variant;
	}		
	
}
