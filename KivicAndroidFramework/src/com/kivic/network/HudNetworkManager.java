package com.kivic.network;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.UUID;

import com.kivic.network.packet.HudNetwork;
import com.kivic.network.packet.HudPacket;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HudNetworkManager extends HudNetwork {
	private static final String TAG                                           = "hud HudNetworkManager";
	private static final int WHAT_GATT_CONNECTION_STATE                       = 10000;
	private static final int WHAT_GATT_UNSUPPORT_UART                         = 10001;
	private static final int WHAT_GATT_PACKET_RECEIVED                        = 10002;
	private static final int WHAT_GATT_ACTION_STATE                           = 10003;
	
	private static final int WHAT_REMAIN_PACKET_SEND                          = 20000;
	private static final int SEND_MSG_MAX_LENGTH                              = 19;
	private static final UUID CCCD                                            = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	private static final UUID IO_SERVICE_UUID                                 = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
	private static final UUID TX_CHAR_UUID                                    = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
	private static final UUID RX_CHAR_UUID                                    = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

	private final Context context;
	private Object sendLock                                                   = new Object();
	private boolean isPacketComing                                            = false;
	private String targetDeviceAddress                                        = "";
	private ByteArrayOutputStream recvPacketBuffer                            = new ByteArrayOutputStream();
	private final BluetoothAdapter bluetoothAdapter;
	private int gattConnectionState                                           = BluetoothProfile.STATE_DISCONNECTED;
	private int gattWriteState = BluetoothGatt.GATT_SUCCESS;
	private BluetoothGatt bluetoothGatt                                       = null;
	private BluetoothGattCharacteristic writeCharacteristic                   = null;
	private HashSet<OnGattStateChangeListener> gattStateChangeListener        = new HashSet<OnGattStateChangeListener>();
    private ArrayList<byte[]> mRemainCommand = new ArrayList<byte[]>();
    private byte[] mCurrentSendPacket = null;
    private int mPacketCount = 0;
    private boolean isPacketSend = false;
	
	private Handler internalHandler                                           = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_GATT_CONNECTION_STATE:
				fireGattConnectionStateChanged((msg.arg1 == BluetoothProfile.STATE_CONNECTED), msg.arg2);				
				break;
			case WHAT_GATT_UNSUPPORT_UART:
				fireGattUnsupportUART();
				break;
			case WHAT_GATT_PACKET_RECEIVED:
				try {
					handleRawPacket((byte[]) msg.obj);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				break;
			case WHAT_GATT_ACTION_STATE:				
				fireGattActionStateChange(msg.arg1 == BluetoothAdapter.STATE_ON);
				break;
			case WHAT_REMAIN_PACKET_SEND:
				sendRemainPacket();
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
	private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {

		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {			
			gattConnectionState = newState;
			if (newState == BluetoothProfile.STATE_DISCONNECTED) {
				writeCharacteristic = null;
				Message.obtain(internalHandler, WHAT_GATT_CONNECTION_STATE, BluetoothProfile.STATE_DISCONNECTED, status).sendToTarget();
			}
			else if (status != BluetoothGatt.GATT_SUCCESS || !gatt.discoverServices()) {
				//gatt.disconnect();
				Message.obtain(internalHandler, WHAT_GATT_CONNECTION_STATE, BluetoothProfile.STATE_DISCONNECTED, status).sendToTarget();
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {			 
			if (status == BluetoothGatt.GATT_SUCCESS) {				
				if (findUartService(gatt)) {
					return;
				}
			}

			writeCharacteristic = null;
			internalHandler.sendEmptyMessage(WHAT_GATT_UNSUPPORT_UART);
			super.onServicesDiscovered(gatt, status);
		};

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			//LogWrapper.i(TAG, "onCharacteristicRead() status : " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
				handleGattData(characteristic);
			}
			super.onCharacteristicRead(gatt, characteristic, status);
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			
			//LogWrapper.i(TAG, "onCharacteristicWrite() status : " + status);
			internalHandler.sendEmptyMessage(WHAT_REMAIN_PACKET_SEND);
			
			
			super.onCharacteristicWrite(gatt, characteristic, status);
				
		}
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			//LogWrapper.i(TAG, "onCharacteristicChanged()");
			handleGattData(characteristic);
			super.onCharacteristicChanged(gatt, characteristic);
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {			
			if (status == BluetoothGatt.GATT_SUCCESS) {
				
				synchronized (mRemainCommand) {
					mRemainCommand.clear();	
				}
				
				gattConnectionState = BluetoothProfile.STATE_CONNECTED;
				Message.obtain(internalHandler, WHAT_GATT_CONNECTION_STATE, BluetoothProfile.STATE_CONNECTED, 0)
						.sendToTarget();
			} else {
				writeCharacteristic = null;
				internalHandler.sendEmptyMessage(WHAT_GATT_UNSUPPORT_UART);
			}
			super.onDescriptorWrite(gatt, descriptor, status);
		}

		private boolean findUartService(BluetoothGatt gatt) {
			boolean handled = false;
			BluetoothGattService RxService = gatt.getService(IO_SERVICE_UUID);
			if (RxService != null) {
				writeCharacteristic = RxService.getCharacteristic(TX_CHAR_UUID);
				if (writeCharacteristic != null) {
					BluetoothGattCharacteristic readChar = RxService.getCharacteristic(RX_CHAR_UUID);
					if (readChar != null) {
						gatt.setCharacteristicNotification(readChar, true);
						BluetoothGattDescriptor descriptor = readChar.getDescriptor(CCCD);
						if (descriptor != null) {
							handled = Arrays.equals(descriptor.getValue(),
									BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
							if (!handled) {
								if (descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
									handled = gatt.writeDescriptor(descriptor);
								}
							}
						}
					}
				}
			}			
			return handled;
		}
	};

	private BroadcastReceiver mBluetoothReceiver = null;
	private IntentFilter makeIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		return intentFilter;
	}
	
	public HudNetworkManager(Context context) {
		super(context);

		this.context = context;
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		//setDumpPacket(TAG, true, true);
	}
	
	public void resetPacket(){
		synchronized (mRemainCommand) {
			mRemainCommand.clear();	
		}
		mPacketCount = 0;
		mCurrentSendPacket = null;
		isPacketSend = false;		
	}

	public void registerActionStateChange()
	{
		if(mBluetoothReceiver == null) {
			mBluetoothReceiver = new BroadcastReceiver() {		
				@Override
				public void onReceive(Context context, Intent intent) {
					final String action = intent.getAction();
					if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
						int actualState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
						// 10, 12
						if(actualState == BluetoothAdapter.STATE_OFF)
						{											
							Message.obtain(internalHandler, WHAT_GATT_ACTION_STATE, BluetoothAdapter.STATE_OFF, 0).sendToTarget();
						}
						else if(actualState == BluetoothAdapter.STATE_ON)
						{
							Message.obtain(internalHandler, WHAT_GATT_ACTION_STATE, BluetoothAdapter.STATE_ON, 0).sendToTarget();					
						}
					}

				}
			};
			this.context.registerReceiver(mBluetoothReceiver, makeIntentFilter());
		}
	}

	public void unregisterActionStateChange()
	{
		if(mBluetoothReceiver != null) {
			this.context.unregisterReceiver(mBluetoothReceiver);
			mBluetoothReceiver = null;
		}
	}

	public void registerGattStateChangeListener(OnGattStateChangeListener listener) {
		synchronized (gattStateChangeListener) {
			gattStateChangeListener.add(listener);
		}
	}

	public void unregisterGattStateChangeListener(OnGattStateChangeListener listener) {
		synchronized (gattStateChangeListener) {
			gattStateChangeListener.remove(listener);
		}
	}

	public final boolean isGattConnected() {
		return (gattConnectionState == BluetoothProfile.STATE_CONNECTED);
	}
	
	public final boolean isGattConnecting() {
		return (gattConnectionState == BluetoothProfile.STATE_CONNECTING);
	}
	
	public final boolean isGattConnectedOrConnecting() {
		return (gattConnectionState == BluetoothProfile.STATE_CONNECTED || gattConnectionState == BluetoothProfile.STATE_CONNECTING);
	}
	
	public final boolean isGattDisconnected() {
		return (gattConnectionState == BluetoothProfile.STATE_DISCONNECTED);
	}
	
	public final boolean isGattDisconnecting() {
		return (gattConnectionState == BluetoothProfile.STATE_DISCONNECTING);
	}
	
	public final boolean isGattDisconnectedOrDisconnecting() {
		return (gattConnectionState == BluetoothProfile.STATE_DISCONNECTED || gattConnectionState == BluetoothProfile.STATE_DISCONNECTING);
	}
	
	@TargetApi(23)
	public synchronized HudError connectGatt(String address) {
		if (address == null) {
			return HudError.INVALID_PARAM;
		}
		
		if(gattConnectionState != BluetoothProfile.STATE_CONNECTED) {
			if(targetDeviceAddress != null && targetDeviceAddress.equals(address) && bluetoothGatt != null) {
				
				if (!bluetoothGatt.connect()) {
					gattConnectionState = BluetoothProfile.STATE_DISCONNECTED;
					return HudError.CAN_NOT_CONNECT;
				}				
				return HudError.SUCCESS;
			}
			
			BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
			if (device == null) {
				return HudError.NOT_EXIST;
			}
			
			gattConnectionState = BluetoothProfile.STATE_CONNECTING;
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				bluetoothGatt = device.connectGatt(context, true, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
			}
			else {
				bluetoothGatt = device.connectGatt(context, true, bluetoothGattCallback);
			}
			if (bluetoothGatt == null) {
				gattConnectionState = BluetoothProfile.STATE_DISCONNECTED;
				return HudError.CAN_NOT_CONNECT;
			}
	
			targetDeviceAddress = address;
		}
		return HudError.SUCCESS;
	}
	
	public synchronized	HudError disconnectGatt() {
		resetPacket();
		if (bluetoothGatt != null) {
			targetDeviceAddress = null;
			bluetoothGatt.disconnect();
			recvPacketBuffer.reset();
			internalHandler.removeMessages(WHAT_GATT_PACKET_RECEIVED);
			gattConnectionState = BluetoothProfile.STATE_DISCONNECTING;
			return HudError.SUCCESS;
		}

		return HudError.IS_DISCONNECTED;
	}

	public synchronized HudError closeGatt() {
		resetPacket();
		if (bluetoothGatt != null) {						
			writeCharacteristic = null;
			//bluetoothGatt.disconnect();
			bluetoothGatt.close();
			recvPacketBuffer.reset();
			internalHandler.removeMessages(WHAT_GATT_PACKET_RECEIVED);			
			gattConnectionState = BluetoothProfile.STATE_DISCONNECTED;			
			bluetoothGatt = null;
		}

		return HudError.SUCCESS;
	}

	/*
	 * HUD로 Command를 보내는 함수.
	 * HUD로 보내는 중에 Command가 발생 할 경우 저장을 한 후 순차적으로 처리.
	 * 20byte를 보내면 onCharacteristicWrite 이벤트가 발생되어 남은 Command를 보냄.
	 */
	@Override
	protected synchronized boolean sendPacketImpl(byte[] packed) {
		if (bluetoothGatt == null || writeCharacteristic == null
				|| gattConnectionState != BluetoothProfile.STATE_CONNECTED) {
			return false;
		}
		
		if(isPacketSend)
		{
			synchronized (mRemainCommand) {
				mRemainCommand.add(packed);
			}			
			return true;
		}
		
		gattWriteState = BluetoothGatt.GATT_SUCCESS;

		int available = SEND_MSG_MAX_LENGTH;
		
		if(packed.length < SEND_MSG_MAX_LENGTH)
			available = packed.length;
		
		byte[] sendMsg = new byte[available];
		isPacketSend = true;
		mCurrentSendPacket = packed;
		System.arraycopy(mCurrentSendPacket, 0, sendMsg, 0, available);
		mPacketCount++;
		writeCharacteristic.setValue(sendMsg);
		
		writeCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
		bluetoothGatt.writeCharacteristic(writeCharacteristic);
		return true;
		
	}
	
	/*
	 * 스택에 쌓여있는 Commnad를 순차적으로 보냄.
	 */
	private void sendRemainPacket(){
		
		if (bluetoothGatt == null || writeCharacteristic == null
				|| gattConnectionState != BluetoothProfile.STATE_CONNECTED) {
			return;
		}
		
		byte[] sendMsg;
		
		if(mCurrentSendPacket != null && mCurrentSendPacket.length > SEND_MSG_MAX_LENGTH)
		{			
			int available = mCurrentSendPacket.length - mPacketCount * SEND_MSG_MAX_LENGTH;
			if(available > 0)
			{
				if(available > SEND_MSG_MAX_LENGTH)
					available = SEND_MSG_MAX_LENGTH;
				
				sendMsg = new byte[available];
				System.arraycopy(mCurrentSendPacket, mPacketCount * SEND_MSG_MAX_LENGTH, sendMsg, 0, available);
				mPacketCount++;
				writeCharacteristic.setValue(sendMsg);
				//writeCharacteristic.setWriteType(BluetoothGattCharacteristic.PROPERTY_NOTIFY);
				bluetoothGatt.writeCharacteristic(writeCharacteristic);
			}
			else
			{
				mPacketCount = 0;
				mCurrentSendPacket = null;
			}
		}
		else
		{
			mPacketCount = 0;
			mCurrentSendPacket = null;
		}

		if(mCurrentSendPacket == null)
		{
			//Log.e(TAG, "mRemainCommand.size() : " + mRemainCommand.size() + ", isEmpty : " + mRemainCommand.isEmpty() + ", isPacketSend : " + isPacketSend);
			if(mRemainCommand != null && !mRemainCommand.isEmpty())
			{
				Iterator<byte[]> list = mRemainCommand.iterator();
				synchronized (mRemainCommand) {
					try {

						while(list.hasNext()){
							mCurrentSendPacket = list.next();
							int available = SEND_MSG_MAX_LENGTH;

							if(mCurrentSendPacket.length < SEND_MSG_MAX_LENGTH)
								available = mCurrentSendPacket.length;

							sendMsg = new byte[available];
							System.arraycopy(mCurrentSendPacket, 0, sendMsg, 0, available);
							mPacketCount++;
							writeCharacteristic.setValue(sendMsg);
							//writeCharacteristic.setWriteType(BluetoothGattCharacteristic.PROPERTY_NOTIFY);
							bluetoothGatt.writeCharacteristic(writeCharacteristic);

							list.remove();

							break;
						}
					} catch (Exception e) {
						// TODO: handle exception
					}
				}
			}
			else
				isPacketSend = false;
		}
	}
	
	private BluetoothGatt connectGattImpl(BluetoothDevice device, boolean autoConnect) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N || Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT || !autoConnect) {
			return connectGattCompat(device, autoConnect);
		}
		
		try {
			Object iBluetoothGatt = getIBluetoothGatt(getIBluetoothManager());
			
			if(iBluetoothGatt == null) {
				return connectGattCompat(device, true);
			}
			
			BluetoothGatt bluetoothGatt = createBluetoothGatt(iBluetoothGatt, device);

			if (bluetoothGatt == null) {
				return connectGattCompat(device, true);
			}

			if(!connectUsingReflection(bluetoothGatt, true)) {
				bluetoothGatt.close();
				return null;
			}
			
			return bluetoothGatt;
		}
		catch (Exception e) {
			e.printStackTrace();
			return connectGattCompat(device, true);
		}
	}
	
	private Method getMethodFromClass(Class<?> cls, String methodName) throws NoSuchMethodException {
        Method method = cls.getDeclaredMethod(methodName);
        method.setAccessible(true);
        return method;
    }  
	
	private Object getIBluetoothManager() throws Exception {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            return null;
        }

        Method getBluetoothManagerMethod = getMethodFromClass(bluetoothAdapter.getClass(), "getBluetoothManager");
        return getBluetoothManagerMethod.invoke(bluetoothAdapter);
	}
	
	private Object getIBluetoothGatt(Object iBluetoothManager) throws Exception {
		if (iBluetoothManager == null) {
			return null;
		}

		Method getBluetoothGattMethod = getMethodFromClass(iBluetoothManager.getClass(), "getBluetoothGatt");
		return getBluetoothGattMethod.invoke(iBluetoothManager);
	}

	@TargetApi(Build.VERSION_CODES.M)
	private BluetoothGatt createBluetoothGatt(Object iBluetoothGatt, BluetoothDevice device) throws Exception {
		Constructor bluetoothGattConstructor = BluetoothGatt.class.getDeclaredConstructors()[0];
		bluetoothGattConstructor.setAccessible(true);		

		if (bluetoothGattConstructor.getParameterTypes().length == 4) {
			return (BluetoothGatt) (bluetoothGattConstructor.newInstance(context, iBluetoothGatt, device, BluetoothDevice.TRANSPORT_LE));
		}
		
		return (BluetoothGatt) (bluetoothGattConstructor.newInstance(context, iBluetoothGatt, device));
	}

	@TargetApi(Build.VERSION_CODES.M)
	private BluetoothGatt connectGattCompat(BluetoothDevice device, boolean autoConnect) {
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			return device.connectGatt(context, autoConnect, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE);
		}
		
		return device.connectGatt(context, autoConnect, bluetoothGattCallback);
	}
	
	private void setAutoConnectValue(BluetoothGatt bluetoothGatt, boolean autoConnect) throws Exception {
		Field autoConnectField = bluetoothGatt.getClass().getDeclaredField("mAutoConnect");
		autoConnectField.setAccessible(true);
		autoConnectField.setBoolean(bluetoothGatt, autoConnect);
	}
	
	private boolean connectUsingReflection(BluetoothGatt bluetoothGatt, boolean autoConnect) throws Exception {		
		setAutoConnectValue(bluetoothGatt, autoConnect);
		Method connectMethod = bluetoothGatt.getClass().getDeclaredMethod("connect", Boolean.class, BluetoothGattCallback.class);
		connectMethod.setAccessible(true);
		return (Boolean) (connectMethod.invoke(bluetoothGatt, true, bluetoothGattCallback));
	}
	
	private void refreshDeviceCache(final BluetoothGatt gatt, final boolean force) {
		if (force || gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
			try {
				final Method refresh = gatt.getClass().getMethod("refresh");
				final boolean success = (Boolean) refresh.invoke(gatt);
			
			}
			catch (Exception e) {
			
			}
		}
	}

	private void fireGattConnectionStateChanged(boolean connected, int status) {
		synchronized (gattStateChangeListener) {
			for (OnGattStateChangeListener client : gattStateChangeListener) {
				client.onConnectionStateChange(connected, status);
			}
		}
	}

	private void fireGattUnsupportUART() {
		synchronized (gattStateChangeListener) {
			for (OnGattStateChangeListener client : gattStateChangeListener) {
				client.onGattUnsupportUART();
			}
		}
	}
	
	private void fireGattActionStateChange(boolean enabled) {
		synchronized (gattStateChangeListener) {
			for (OnGattStateChangeListener client : gattStateChangeListener) {
				client.onActionStateChange(enabled);
			}
		}
	}

	private void handleGattData(BluetoothGattCharacteristic characteristic) {
		if (characteristic.getUuid().equals(RX_CHAR_UUID)) {
			byte[] datas = characteristic.getValue();
			if (datas != null) {
				for (byte data : datas) {
					if (isPacketComing) {
						recvPacketBuffer.write(data);
						if (data == HudPacket.ETX) {
							Message.obtain(internalHandler, WHAT_GATT_PACKET_RECEIVED, recvPacketBuffer.toByteArray())
									.sendToTarget();
							isPacketComing = false;
						}
					} else if (data == HudPacket.STX) {
						isPacketComing = true;
						recvPacketBuffer.reset();
						recvPacketBuffer.write(data);
					} else {
					}
				}
			}
		}
	}

	public interface OnGattStateChangeListener {
		public void onConnectionStateChange(boolean connected, int status);
		public void onGattUnsupportUART();
		public void onActionStateChange(boolean enabled);
	}
}
