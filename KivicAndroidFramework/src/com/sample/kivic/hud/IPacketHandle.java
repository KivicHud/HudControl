package com.sample.kivic.hud;

import android.content.Context;

public interface IPacketHandle<T> {
	public void onPacketReceived(T receivePacket, Context context);
}
