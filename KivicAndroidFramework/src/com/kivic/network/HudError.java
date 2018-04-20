package com.kivic.network;

public enum HudError {
	UNKNOWN(0x0),
	SUCCESS(0x1),
	INTERNAL(0x2),
	OUT_OF_MEMORY(0x3),
	INVALID_PARAM(0x4),
	NOT_EXIST(0x5),
	IS_CONNECTED(0x6),
	IS_DISCONNECTED(0x7),
	CAN_NOT_CONNECT(0x8),
	IO(0x9);
	
	public final int value;
	
	private HudError(int value) {
		this.value = value;
	}
	
	public static HudError convertFrom(byte value) {
		if(value >= HudError.values().length) {
			return HudError.UNKNOWN;
		}
		
		return HudError.values()[value];
	}
}
