package com.kivic.network;

import com.kivic.network.HudNetworkManager.OnGattStateChangeListener;
import com.kivic.network.packet.HudNetwork.OnPacketReceiveListener;
import com.kivic.network.packet.HudPacket;
import com.kivic.network.packet.HudPacketFilter;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Binder;
import android.os.IBinder;

public class HudNetworkService extends Service {
	public HudNetworkManager hudNetworkManager = null;
	private final IBinder mBinder = new hudNetworkBinder();
	
    public class hudNetworkBinder extends Binder {
    	public HudNetworkService getService() {
            return HudNetworkService.this;
        }
    }

	@Override
	public void onCreate() {
		
		hudNetworkManager = new HudNetworkManager(this); 
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		hudNetworkManager.disconnectGatt();
		hudNetworkManager.closeGatt();
		super.onDestroy();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onUnbind(Intent intent) {
		hudNetworkManager.disconnectGatt();
		hudNetworkManager.closeGatt();
		return super.onUnbind(intent);
	}

	
	public HudNetworkService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		
		return mBinder;
	}
	
	public void resetPacket()
	{
		hudNetworkManager.resetPacket();
	}

	public void registerActionStateChange()
	{
		hudNetworkManager.registerActionStateChange();
	}
	
	public void unregisterActionStateChange()
	{
		hudNetworkManager.unregisterActionStateChange();
	}
	

	public void registerGattStateChangeListener(OnGattStateChangeListener listener) {
		hudNetworkManager.registerGattStateChangeListener(listener);
	}

	public void unregisterGattStateChangeListener(OnGattStateChangeListener listener) {
		hudNetworkManager.unregisterGattStateChangeListener(listener);
	}


	public final boolean isGattConnected() {
		return hudNetworkManager.isGattConnected();
	}
	
	public final boolean isGattConnecting() {
		return hudNetworkManager.isGattConnecting();
	}
	
	public final boolean isGattConnectedOrConnecting() {
		return hudNetworkManager.isGattConnectedOrConnecting();
	}
	
	public final boolean isGattDisconnected() {
		return hudNetworkManager.isGattDisconnected();
	}
	
	public final boolean isGattDisconnecting() {
		return hudNetworkManager.isGattDisconnecting();
	}
	
	public final boolean isGattDisconnectedOrDisconnecting() {
		return hudNetworkManager.isGattDisconnectedOrDisconnecting();
	}
	
	public synchronized HudError connectGatt(String address, boolean isAuto) {
		return hudNetworkManager.connectGatt(address, isAuto);
	}
	
	public synchronized	HudError disconnectGatt() {
		return hudNetworkManager.disconnectGatt();
	}
	
	public synchronized HudError closeGatt() {
		return hudNetworkManager.closeGatt();
	}
	
	public boolean sendPacket(HudPacket hudPacket)
	{
		return hudNetworkManager.sendPacket(hudPacket);
	}
	
	public void registerOnPacketReceiveListener(OnPacketReceiveListener listener, HudPacketFilter filter)
	{
		hudNetworkManager.registerOnPacketReceiveListener(listener, filter);
	}
	
	public void unregisterOnPacketReceiveListener(OnPacketReceiveListener listener)
	{
		hudNetworkManager.unregisterOnPacketReceiveListener(listener);
	}
}
