package com.sample.kivic.hud;

import java.util.HashSet;

import com.kivic.network.HudNetworkService;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.util.Log;

public class HudApplication extends Application implements ActivityLifecycleCallbacks {
	private static final String PREFERENCES_NAME = "kivic_setting";
	public static final String KEY_INIT_PREFERENCE = "kivic_init_pref";
	public static final String KEY_KIVIC_MODE_PREFERENCE = "kivic_mode_pref";
	public static final String KEY_BLE_DEVICE_ADDRESS_PREFERENCE = "kivic_ble_device_address_pref";
	public static final String KEY_BLE_DEVICE_NAME_PREFERENCE = "kivic_ble_device_name_pref";
	public static final String KEY_OREO_HOTSPOT_SSID_PREFERENCE = "kivic_oreo_hotspot_ssid_pref";
	public static final String KEY_OREO_HOTSPOT_PASSWORD_PREFERENCE = "kivic_oreo_hotspot_password_pref";
	
	public static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 10000;
	public static final float MAX_KEYSTONE = 0.1f;
	public static final int DEFAULT_KIVICCAST_SCALE = 50;
	//public HudNetworkManager hudNetworkManager = null;
	public HudNetworkService hudNetworkManager = null;
	public SharedPreferences settings = null;
	public SharedPreferences.Editor editor = null;	

	private HashSet<Activity> aliveActivitySet = new HashSet<Activity>();
	
	private boolean mIsHudConnect = false;
	
	private boolean mIsTBTConnected = false;
	private int hudMode = 0;	
		
	Messenger castMessenger                                               = null;
	private OnHudNetworkServiceChanged hudNetworkServiceChangedListener = null;

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i("hudApplication", "HudApplication create");
		//hudNetworkManager = new HudNetworkManager(this);
		settings = getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE);
		editor = settings.edit();
		
		registerActivityLifecycleCallbacks(this);
		initializeSettings();
	}
		
	@Override
	public void onTerminate() {
		cleanUp();
		/*hudNetworkManager.unregisterActionStateChange();
		hudNetworkManager = null;*/
		super.onTerminate();
	}
	
	public void setHudConnect(boolean connect)
	{
		mIsHudConnect = connect;
	}
	
	public boolean isHudConnect()
	{
		return mIsHudConnect;
	}	
	
	@Override
	public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
		aliveActivitySet.add(activity);
	}
	
	@Override
	public void onActivityStarted(Activity activity) {
		
	}

	@Override
	public void onActivityResumed(Activity activity) {		
		
	}

	@Override
	public void onActivityPaused(Activity activity) {

	}

	@Override
	public void onActivityStopped(Activity activity) {

	}

	@Override
	public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

	}

	@Override
	public void onActivityDestroyed(Activity activity) {
		aliveActivitySet.remove(activity);
		if(aliveActivitySet.isEmpty()) {
			cleanUp();
		}
	}

	private void cleanUp() {
		//setGpsListener(false);
		//hudNetworkManager.disconnectGatt();
		//hudNetworkManager.closeGatt();
	}
	
	private void initializeSettings() {
		if(!settings.getBoolean(KEY_INIT_PREFERENCE, false)) {
			editor.putBoolean(KEY_INIT_PREFERENCE, true);
			editor.commit();
		}
	}
		
	public void setGattInfo(String address, String name)
	{		
		editor.putString(KEY_BLE_DEVICE_ADDRESS_PREFERENCE, address);
		editor.putString(KEY_BLE_DEVICE_NAME_PREFERENCE, name);
		editor.commit();
	}

	public String getDeviceAddress() {
		return settings.getString(KEY_BLE_DEVICE_ADDRESS_PREFERENCE, null);			
	}
	
	public String getDeviceName() {
		return settings.getString(KEY_BLE_DEVICE_NAME_PREFERENCE, null);
	}	
	
	public boolean isTBTConnected() {
		return mIsTBTConnected;
	}

	public void setTBTConnected(boolean mIsTBTConnected) {
		this.mIsTBTConnected = mIsTBTConnected;
	}

	public int getHUDMode() {
		return hudMode;
	}

	public void setHUDMode(int hudMode) {
		this.hudMode = hudMode;
	}
	
	public boolean isSupportingKivicCast() {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
			//TODO if the device is poor, you can return false.
			return true;
		}
		
		return false;
	}
	// Hud Network Service
	public void restartHudNetworkService()
	{
		startHudNetworkService();
	}
	public void closeHudNetworkService()
	{
		if(hudNetworkManager != null)
		{
			Log.e("hud", "unbindService hudNetworkService");
			unbindService(hudNetworkServiceConnection);
		}
	}

	public void setOnHudNetworkServiceChanged(OnHudNetworkServiceChanged listener) {
		hudNetworkServiceChangedListener = listener;
	}
	
	public interface OnHudNetworkServiceChanged {
		public void onHudNetworkServiceChanged(boolean isConnect);
	}
	
	ServiceConnection hudNetworkServiceConnection                               = null;
	private void startHudNetworkService()
	{
		hudNetworkServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            	
            	HudNetworkService.hudNetworkBinder mb = (HudNetworkService.hudNetworkBinder) service;
            	hudNetworkManager = mb.getService();
                
            	Log.e("hud", "hudNetworkServiceConnection onServiceConnected");
            	hudNetworkServiceChangedListener.onHudNetworkServiceChanged(true);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            	hudNetworkServiceChangedListener.onHudNetworkServiceChanged(false);
            	Log.e("hud", "hudNetworkServiceConnection onServiceDisconnected");
            }
        };

        bindService(new Intent(getApplicationContext(), HudNetworkService.class), hudNetworkServiceConnection, Context.BIND_AUTO_CREATE);
	}
}
