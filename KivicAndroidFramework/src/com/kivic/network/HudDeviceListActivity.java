/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kivic.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sample.kivic.hud.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class HudDeviceListActivity extends Activity {
	private static final String TAG                   = "hud DeviceListActivity";
	private static final String DEVICE_NAME           = "KIVIC HUD";

	private static final long SCAN_PERIOD             = 10000; // 10 seconds
	private static final int WHAT_STOP_LE_SCAN        = 1;
	private static final int WHAT_STOP_NORMAL_SCAN    = 2;
	private static final int BLUETOOTH_TYPE_LE        = 0;
	private static final int BLUETOOTH_TYPE_NORMAL    = 1;

	private BluetoothAdapter mBluetoothAdapter;
	private TextView mEmptyList;
	private Button cancelButton;
	List<BluetoothDevice> deviceList;
	private DeviceAdapter deviceAdapter;
	Map<String, Integer> devRssiValues;
	
	private boolean mScanning;
	private int mBtType                               = BLUETOOTH_TYPE_LE;
	
	private Handler mHandler                          = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_STOP_LE_SCAN:
				scanLeDevice(false);
				break;
			case WHAT_STOP_NORMAL_SCAN:
				scanNormalDevice(false);
			default:
				break;
			}
		};
	};
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		if(intent != null) {
			mBtType = intent.getIntExtra("bluetooth_type", 0);
		}
		
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
		setContentView(R.layout.device_list);
		android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
		layoutParams.gravity = Gravity.CENTER;

		if (mBtType == BLUETOOTH_TYPE_LE && !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		if (mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		if (mBtType == BLUETOOTH_TYPE_NORMAL) {
			registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		}
		
		mEmptyList = (TextView) findViewById(R.id.empty);
		cancelButton = (Button) findViewById(R.id.btn_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mScanning) {
					finish();
				} 
				else {
					if (mBtType == BLUETOOTH_TYPE_LE) {
						scanLeDevice(true);
					} else {
						scanNormalDevice(true);
					}
				}
			}
		});
		
		populateList(mBtType);
	}

	private void populateList(int type) {
		deviceList = new ArrayList<BluetoothDevice>();
		deviceAdapter = new DeviceAdapter(this, deviceList);
		devRssiValues = new HashMap<String, Integer>();

		ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
		newDevicesListView.setAdapter(deviceAdapter);
		newDevicesListView.setOnItemClickListener(mDeviceClickListener);

		if (type == BLUETOOTH_TYPE_LE) {
			scanLeDevice(true);
		} else {
			scanNormalDevice(true);
		}
	}
	
	/*
	 * ble 스캔을 시작 합니다.
	 */
	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			if(!mScanning) {
				boolean status = false;
				
				status = mBluetoothAdapter.startLeScan(mLeScanCallback);
				
				if (status) {
					mScanning = true;
					cancelButton.setText(R.string.cancel);
	
					// Stops scanning after a pre-defined scan period.
					mHandler.sendEmptyMessageDelayed(WHAT_STOP_LE_SCAN, SCAN_PERIOD);
				}
				else {
					Log.w(TAG, "Can't startLeScan");
				}
			}
		} 
		else {
			mHandler.removeMessages(WHAT_STOP_LE_SCAN);
			if(mScanning) {
				mScanning = false;
				mBluetoothAdapter.stopLeScan(mLeScanCallback);

				cancelButton.setText(R.string.scan);
			}
		}
	}

	
	private void scanNormalDevice(final boolean enable) {
		if(enable) {
			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
			if (pairedDevices.size() > 0) {
				boolean deviceFound = false;
	
				for (BluetoothDevice device : pairedDevices) {
					deviceList.add(device);
					devRssiValues.put(device.getAddress(), 10);
					deviceAdapter.notifyDataSetChanged();
				}
			}
	
			if (!mBluetoothAdapter.isDiscovering()) {
				if (mBluetoothAdapter.startDiscovery()) {
					mScanning = true;
					cancelButton.setText(R.string.cancel);
					mHandler.sendEmptyMessageDelayed(WHAT_STOP_NORMAL_SCAN, SCAN_PERIOD);
				}
			}
		}
		else {
			mHandler.removeMessages(WHAT_STOP_NORMAL_SCAN);
			if (mScanning) {
				if (mBluetoothAdapter.isDiscovering()) {
					mBluetoothAdapter.cancelDiscovery();
				}
				mScanning = false;
				cancelButton.setText(R.string.scan);
			}
		}
	}

	/*
	 * 스캔되는 결과를 콜백으로 받습니다.
	 */
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					addDevice(device, rssi);
				}
			});
		}
	};

	/*
	 * 스캔 된 device에서 KIVIC HUD 만을 리스트에 추가 합니다.
	 */
	private void addDevice(BluetoothDevice device, int rssi) {
		boolean deviceFound = false;

		for (BluetoothDevice listDev : deviceList) {
			if (listDev.getAddress().equals(device.getAddress())) {
				deviceFound = true;
				break;
			}
		}

		if (!deviceFound) {
			if (device.getName() != null && device.getName().contains(DEVICE_NAME)) {
				devRssiValues.put(device.getAddress(), rssi);
				deviceList.add(device);
				mEmptyList.setVisibility(View.GONE);

				deviceAdapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onStop() {
		super.onStop();

		if (mBtType == BLUETOOTH_TYPE_LE) {			
			scanLeDevice(false);
		} 
		else {			
			scanNormalDevice(false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (mBtType == BLUETOOTH_TYPE_NORMAL) {
			unregisterReceiver(discoveryResult);
		}
	}

	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			BluetoothDevice device = deviceList.get(position);
			if (mBtType == BLUETOOTH_TYPE_LE) {			
				scanLeDevice(false);
			} 
			else {			
				scanNormalDevice(false);
			}
			
			Bundle b = new Bundle();
			b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList.get(position).getAddress());
			Intent result = new Intent();
			result.putExtras(b);
			setResult(Activity.RESULT_OK, result);
			finish();
		}
	};

	BroadcastReceiver discoveryResult = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			// When discovery finds a device

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {

				// Get the BluetoothDevice object from the Intent

				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				deviceList.add(device);
				devRssiValues.put(device.getAddress(), 10);
				deviceAdapter.notifyDataSetChanged();
			}

		}
	};

	class DeviceAdapter extends BaseAdapter {
		Context context;
		List<BluetoothDevice> devices;
		LayoutInflater inflater;

		public DeviceAdapter(Context context, List<BluetoothDevice> devices) {
			this.context = context;
			inflater = LayoutInflater.from(context);
			this.devices = devices;
		}

		@Override
		public int getCount() {
			return devices.size();
		}

		@Override
		public Object getItem(int position) {
			return devices.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup vg;

			if (convertView != null) {
				vg = (ViewGroup) convertView;
			} else {
				vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
			}

			BluetoothDevice device = devices.get(position);
			final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
			final TextView tvname = ((TextView) vg.findViewById(R.id.name));
			final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);
			final TextView tvrssi = (TextView) vg.findViewById(R.id.rssi);

			tvrssi.setVisibility(View.VISIBLE);
			byte rssival = (byte) devRssiValues.get(device.getAddress()).intValue();
			if (rssival != 0) {
				tvrssi.setText("Rssi = " + String.valueOf(rssival));
			}

			tvname.setText(device.getName());
			tvadd.setText(device.getAddress());
			if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
				tvname.setTextColor(Color.WHITE);
				tvadd.setTextColor(Color.WHITE);
				tvpaired.setTextColor(Color.GRAY);
				tvpaired.setVisibility(View.VISIBLE);
				tvpaired.setText(R.string.paired);
				tvrssi.setVisibility(View.VISIBLE);
				tvrssi.setTextColor(Color.WHITE);

			} else {
				tvname.setTextColor(Color.WHITE);
				tvadd.setTextColor(Color.WHITE);
				tvpaired.setVisibility(View.GONE);
				tvrssi.setVisibility(View.VISIBLE);
				tvrssi.setTextColor(Color.WHITE);
			}
			return vg;
		}
	}

	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
}
