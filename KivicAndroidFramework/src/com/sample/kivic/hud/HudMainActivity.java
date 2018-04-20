package com.sample.kivic.hud;

import java.util.Calendar;
import java.util.HashMap;

import com.kivic.network.HudDeviceListActivity;
import com.kivic.network.HudError;
import com.kivic.network.HudNetworkManager;
import com.kivic.network.packet.HudNetwork;
import com.kivic.network.packet.HudPacket;
import com.kivic.network.packet.HudPacketFilter;
import com.kivic.network.packet.command.DisplayTimeCommandPacket;
import com.kivic.network.packet.command.HudDisconnectCommandPacket;
import com.kivic.network.packet.command.KeepAliveCommandPacket;
import com.kivic.network.packet.command.KivicModeCommandPacket;
import com.kivic.network.packet.command.SystemTimeCommandPacket;
import com.kivic.network.packet.command.UartConnectionCheckCommandPacket;
import com.kivic.network.packet.event.UartConnectionEventPacket;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

public class HudMainActivity extends Activity implements View.OnClickListener,
																	HudNetworkManager.OnGattStateChangeListener,
																	HudNetwork.OnPacketReceiveListener {
	
	protected static final String TAG = "hud Main";
	private static final int REQUEST_SELECT_DEVICE      = 1;
	private static final int REQUEST_ENABLE_BT          = 2;
	
	private HudApplication hudApplication = null;
	private RelativeLayout hudMainLayout;
	private RelativeLayout hudConnectLayout;
	
	private TextView mHud_enable_txt;
	private ToggleButton mBT_sw;	
	
	// Hud packet handler
	private HashMap<Class, IPacketHandle> hudPacketHandler = new HashMap<Class, IPacketHandle>();	
	
	// init settings
	private Handler mHUDCheckHandler;
	
	private Runnable mHudHeartBeatTask = new Runnable(){
		@Override
		public void run() {
			updateHudStatus(false);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		hudApplication = (HudApplication)getApplication();
		
		overridePendingTransition(0,0);
		initView();		
		setupPacketReceiver();

		hudApplication.hudNetworkManager.registerActionStateChange();
		
    }
	
	private void initView() {
		setContentView(R.layout.activity_hud_main);

		hudMainLayout = (RelativeLayout) findViewById(R.id.main_layout);
		hudConnectLayout = (RelativeLayout) findViewById(R.id.connection_layout);
		
		RelativeLayout hudConnectLayout = (RelativeLayout) findViewById(R.id.hud_connect_layout);
		hudConnectLayout.setOnClickListener(this);

		RelativeLayout hudCommandLayout = (RelativeLayout) findViewById(R.id.hud_command_layout);
		hudCommandLayout.setOnClickListener(this);
		
		mHud_enable_txt = (TextView) findViewById(R.id.hud_enable_txt);
		mBT_sw = (ToggleButton) findViewById(R.id.hud_sw);
		mBT_sw.setOnClickListener(this);
		
		Button connectBacketBtn = (Button) findViewById(R.id.connection_back_btn);
		connectBacketBtn.setOnClickListener(this);
		

		mHUDCheckHandler = new Handler();
	}
	
	@Override
	protected void onResume() {
		if(!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
			// bluetooth Off일 경우 On 시키기 위해 처리.
			startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
		}
		else if(hudApplication.getDeviceAddress() != null && hudApplication.hudNetworkManager.isGattDisconnectedOrDisconnecting())
		{
			mHud_enable_txt.setEnabled(true);
			mBT_sw.setChecked(true);
			connectBLE();
		}
		else {			
		}
		super.onResume();
	}

	@Override
	public void onBackPressed() {		
		if(hudConnectLayout.getVisibility() == View.VISIBLE)
		{			
			hudConnectLayout.setVisibility(View.INVISIBLE);
			hudMainLayout.setVisibility(View.VISIBLE);			
		}
		else
			super.onBackPressed();
		
	}
	@Override
	protected void onDestroy() {
		Log.i(TAG, "onDestroy()");
		
		HudError hudError = hudApplication.hudNetworkManager.closeGatt();
		hudApplication.setHudConnect(false);
		
		hudApplication.hudNetworkManager.unregisterGattStateChangeListener(this);
		hudApplication.hudNetworkManager.unregisterOnPacketReceiveListener(this);
		hudApplication.hudNetworkManager.unregisterActionStateChange();

		mHUDCheckHandler.removeCallbacks(mHudHeartBeatTask);

		super.onDestroy();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {
		case REQUEST_SELECT_DEVICE:
			if (resultCode == Activity.RESULT_OK && data != null) {
				String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
				BluetoothDevice device = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
				hudApplication.setGattInfo(deviceAddress, device.getName());
				connectBLE();
			}
			else
			{
				mHud_enable_txt.setEnabled(false);
				mBT_sw.setChecked(false);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode != Activity.RESULT_OK) {
				finish();
			}
			break;
		default:
			break;
		}
	}
	
	/*
	 * BLE와 연결 함. Device Address는 HudDeviceListActivity에서 받습니다.
	 */
	private boolean connectBLE() {
		Log.i(TAG, "connectBLE ");
		HudError hudError = hudApplication.hudNetworkManager.connectGatt(hudApplication.getDeviceAddress());
		if (hudError != HudError.SUCCESS) {
			try {
				BluetoothDevice device = BluetoothAdapter.getDefaultAdapter()
						.getRemoteDevice(hudApplication.getDeviceAddress());
				Log.w(TAG, "Can't connect to bluetooth gatt. deviceName[" + device.getName()
						+ "], deviceAddress[" + hudApplication.getDeviceAddress() + "] hudError : " + hudError);
			} catch (Exception e) {

			}

		} else {
			return true;
		}

		return false;
	}
	
	/*
	 * HUD와 연결 후 초기 설정값을 HUD로 보냄.
	 */
	private boolean applyHUDSettings() {
		final HudNetworkManager hudNetworkManager = hudApplication.hudNetworkManager;
		
		// Time
		Calendar calendar = Calendar.getInstance();
		SystemTimeCommandPacket systemTimeCommandPacket = new SystemTimeCommandPacket();
		systemTimeCommandPacket.setTimeInMillis(calendar.getTimeInMillis());
		systemTimeCommandPacket.setTimeZoneId(calendar.getTimeZone().getID());
		if(!hudNetworkManager.sendPacket(systemTimeCommandPacket)) {
			return false;
		}
		// Time Hide
		DisplayTimeCommandPacket displayTimeCommandPacket = new DisplayTimeCommandPacket();		
		displayTimeCommandPacket.setEnable(true);
		if(!hudNetworkManager.sendPacket(displayTimeCommandPacket)) {
			return false;
		}
		// Kivic Mode
		KivicModeCommandPacket kivicModeCommandPacket = new KivicModeCommandPacket();
		kivicModeCommandPacket.setMode(KivicModeCommandPacket.ANDROID_TBT_MODE);
		if(!hudNetworkManager.sendPacket(kivicModeCommandPacket)) {
			return false;
		}

		return true;
	}

	/*
	 * function : setupPacketReceiver
	 * HUD에서 앱으로 보낸 데이터를 받는 부분
	 */
	private void setupPacketReceiver() {
		hudPacketHandler.clear();

		/*
		 * HUD와 정상적으로 연결 되었을 경우 받는 이벤트로 이 곳에서 초기값을 설정 합니다.
		 * UartConnectionEventPacket는 주기적으로 HUD에서 보내는 이벤트로 초기 연결 뿐만 아니라 
		 * 정상적으로 HUD와 연결이 되었는지 확인 하는 용도로도 사용 합니다.
		 */
		hudPacketHandler.put(UartConnectionEventPacket.class, new IPacketHandle<UartConnectionEventPacket>() {
			@Override
			public void onPacketReceived(UartConnectionEventPacket receivePacket, Context context) {

				if(hudApplication.isHudConnect()) {
					mHUDCheckHandler.removeCallbacks(mHudHeartBeatTask);
					mHUDCheckHandler.postDelayed(mHudHeartBeatTask, 12000);
					KeepAliveCommandPacket keepAlive = new KeepAliveCommandPacket();										
					hudApplication.hudNetworkManager.sendPacket(keepAlive);
				}
				else {
					boolean status = applyHUDSettings();
					if(status) {
						updateHudStatus(true);
					}
					else {
						hudApplication.hudNetworkManager.disconnectGatt();
						updateHudStatus(false);
					}
				}
			}
		});
		
		HudPacketFilter hudPacketFilter = new HudPacketFilter();
		for(Class clazz : hudPacketHandler.keySet()) {
			hudPacketFilter.addFilter(clazz);
		}
		
		hudApplication.hudNetworkManager.registerGattStateChangeListener(this);
		hudApplication.hudNetworkManager.registerOnPacketReceiveListener(this, hudPacketFilter);		
	}
	
	@Override
	public void onPacketReceived(HudPacket hudPacket) {
		IPacketHandle packetHandle = hudPacketHandler.get(hudPacket.getClass());
		
		if(packetHandle != null) {
			packetHandle.onPacketReceived(hudPacket, this);
		}
	}
	
	private void updateHudStatus(boolean isAlive) {
		Log.i(TAG, "updateHudStatus " + isAlive);
		if(isAlive) {
			hudApplication.setHudConnect(true);
			mHud_enable_txt.setEnabled(true);
			mBT_sw.setChecked(true);
			
			mHUDCheckHandler.removeCallbacks(mHudHeartBeatTask);
			mHUDCheckHandler.postDelayed(mHudHeartBeatTask, HudApplication.DEFAULT_KEEP_ALIVE_TIMEOUT);
			KeepAliveCommandPacket keepAlive = new KeepAliveCommandPacket();
			hudApplication.hudNetworkManager.sendPacket(keepAlive);
			
		}
		else {
			hudApplication.setHudConnect(false);			
			mHUDCheckHandler.removeCallbacks(mHudHeartBeatTask);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		
		case R.id.hud_connect_layout:
			hudMainLayout.setVisibility(View.INVISIBLE);
			hudConnectLayout.setVisibility(View.VISIBLE);
			break;
		case R.id.hud_command_layout:
			startActivity(new Intent(HudMainActivity.this, CommandActivity.class));
			break;
		case R.id.hud_sw:
			synchronized (this) {
				if(mBT_sw.isChecked()) {
					if(hudApplication.hudNetworkManager.isGattDisconnectedOrDisconnecting()) {
						// HUD를 찾아 팝업 리스트로 보여주는 부분. 
						Intent newIntent = new Intent(HudMainActivity.this, HudDeviceListActivity.class);
						newIntent.putExtra("bluetooth_type", 0);
						startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
					}
				}
				else {					
					HudDisconnectCommandPacket HudDisconnectCommandPacket = new HudDisconnectCommandPacket();					
					hudApplication.hudNetworkManager.sendPacket(HudDisconnectCommandPacket);
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
					HudError hudError = hudApplication.hudNetworkManager.disconnectGatt();
					Log.d(TAG, "invoked closeGatt() result : " + hudError);
					hudApplication.setGattInfo(null, null);
					
					updateHudStatus(false);
				}
			}
			break;
		case R.id.connection_back_btn:
			hudConnectLayout.setVisibility(View.INVISIBLE);
			hudMainLayout.setVisibility(View.VISIBLE);
			break;
		}
	}

	/******************* HudNetworkManager(BLE) Status Event *******************/
	@Override
	public void onConnectionStateChange(boolean connected, int status) {
		Log.d(TAG, "got onConnectionStateChange() connected : " + connected + ", status : " + status);
		if(!connected) {
			updateHudStatus(false);
			hudApplication.hudNetworkManager.closeGatt();
			if(mBT_sw.isChecked()) {
				connectBLE();
			}
		}
		else {
			hudApplication.hudNetworkManager.resetPacket();
			mHud_enable_txt.setEnabled(connected);
			mBT_sw.setChecked(connected);
			
			UartConnectionCheckCommandPacket uartConnectionCheckCommandPacket = new UartConnectionCheckCommandPacket();
			Log.d(TAG, "sendPacket() UartConnectionCheckCommandPacket");
			if(!hudApplication.hudNetworkManager.sendPacket(uartConnectionCheckCommandPacket)) {
				updateHudStatus(false);
				hudApplication.hudNetworkManager.disconnectGatt();
			}
		}
	}

	@Override
	public void onGattUnsupportUART() {
		Log.e(TAG, "onGattUnsupportUART");
		
		updateHudStatus(false);
		hudApplication.hudNetworkManager.disconnectGatt();
	}
	
	@Override
	public void onActionStateChange(boolean enabled) {
		if(!enabled)
			hudApplication.hudNetworkManager.closeGatt();
		else
		{
			if(mBT_sw.isChecked()) {
				connectBLE();
			}	
		}
		
	}
		
}
