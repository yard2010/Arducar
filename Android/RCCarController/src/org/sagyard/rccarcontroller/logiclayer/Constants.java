package org.sagyard.rccarcontroller.logiclayer;

public class Constants
{
	public static final int maxVelocity = 100;
	public static final int minVelocity = -100;
	public static final int DEFAULT_UPDATE_INTERVAL = 5;
	public static final char SEPERATOR = ':';
	
	public static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
	
	public static final String SHARED_PREFS_TAG = "ArduCarSharedPrefs";
	public static final String IP_TAG = "IP";
}
