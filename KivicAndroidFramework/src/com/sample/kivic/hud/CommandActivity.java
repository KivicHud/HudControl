package com.sample.kivic.hud;

import com.kivic.network.packet.command.DisplayBrightnessCommandPacket;
import com.kivic.network.packet.command.DisplaySpeedColorCommandPacket;
import com.kivic.network.packet.command.DisplaySpeedUintsCommandPacket;
import com.kivic.network.packet.command.FullScreenCommandPacket;
import com.kivic.network.packet.command.KeyStoneCommandPacket;
import com.kivic.network.packet.command.KivicModeCommandPacket;
import com.kivic.network.packet.command.WifiSTAModeCommandPacket;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class CommandActivity extends Activity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
	protected static final String TAG = "hud CommandActivity";
	private HudApplication hudApplication = null;
	private Button mbBack_btn;
	
	private static final int REQUEST_MEDIA_PROJECTION                     = 5;
	
	private static final int SPEED_COLOR_WHITE       = 0;
	private static final int SPEED_COLOR_CYAN        = 1;
	private static final int SPEED_COLOR_YELLOW      = 2;
	private static final int SPEED_COLOR_GREEN       = 3;
	
	private static final int SPEED_UNIT_KMH          = 0;
	private static final int SPEED_UNIT_MPH          = 1;
			
	private static final int ITEM_MODE_HUD                = 0;
	private static final int ITEM_MODE_NAVIGATION         = 1;	
	private static final int ITEM_MODE_KIVICCAST          = 2;
	
	private TextView mKivicScreenTxt;
	private ToggleButton mKivicScreen_sw;
	private TextView mBrightnessEnableTxt;
	private TextView mSpeedColorSelectTxt;
	private TextView mSpeedUnitSelectTxt;
	private TextView mModeSelectTxt;
	private EditText mHotspotSsidEtx;
	private EditText mHotspotPWEtx;	 
	private ToggleButton mBrightnessEnableSW;
	private int mSpeedColor = 0;
	private int mSpeedUnit = 0;
	private int mHudMode = 0;
	private int mModeIdx = 0;
	
	ServiceConnection castServiceConnection               = null;
	private int intentResultCode                          = Activity.RESULT_CANCELED;
	private Intent intentResultData                       = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    hudApplication = (HudApplication)getApplication();
	    overridePendingTransition(0,0);
	    initView();
	    loadSetting();
	    startKivicCast();
	}
	
	@Override
	protected void onDestroy() {
		saveSetting();
		setCast(false);
		if(hudApplication.castMessenger != null) {
			unbindService(castServiceConnection);
		}
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId())
		{		
		case R.id.command_back_btn:
			finish();
			break;
		case R.id.brightness_enable_sw:			
			setBrightnessEnable(mBrightnessEnableSW.isChecked());			
			break;
		case R.id.speed_color_layout:
			showSpeedColor();
			break;
		case R.id.speed_units_layout:
			showSpeedUnit();
			break;
		case R.id.kivic_mode_layout:
			showHudMode();
			break;
		case R.id.kivic_screen_sw:
			setFullScreen(mKivicScreen_sw.isChecked());
			break;
		}

	}
	
	private void loadSetting() {
		SharedPreferences settings = hudApplication.settings;		
		mHotspotSsidEtx.setText(settings.getString(HudApplication.KEY_OREO_HOTSPOT_SSID_PREFERENCE, null));
		mHotspotPWEtx.setText(settings.getString(HudApplication.KEY_OREO_HOTSPOT_PASSWORD_PREFERENCE, null));				
	}
	
	private void saveSetting() {
		SharedPreferences.Editor editor = hudApplication.editor;
		editor.putString(HudApplication.KEY_OREO_HOTSPOT_SSID_PREFERENCE, mHotspotSsidEtx.getText().toString());
		editor.putString(HudApplication.KEY_OREO_HOTSPOT_PASSWORD_PREFERENCE, mHotspotPWEtx.getText().toString());
		editor.commit();

	}
	private void initView() {
		setContentView(R.layout.activity_hud_command);


		// brightness on/off
		mBrightnessEnableTxt = (TextView)findViewById(R.id.brightness_enable_txt);
		mBrightnessEnableSW = (ToggleButton)findViewById(R.id.brightness_enable_sw);
		mBrightnessEnableSW.setOnClickListener(this);
		
		// speed Color
		RelativeLayout speedColorLayout = (RelativeLayout) findViewById(R.id.speed_color_layout);		
		speedColorLayout.setOnClickListener(this);
		mSpeedColorSelectTxt = (TextView) findViewById(R.id.speed_color_select_txt);
		
		
		
		// speed Unit
		RelativeLayout speedUnitsLayout = (RelativeLayout) findViewById(R.id.speed_units_layout);
		speedUnitsLayout.setOnClickListener(this);
		mSpeedUnitSelectTxt = (TextView) findViewById(R.id.speed_units_select_txt);
		
		// Keystone
		SeekBar keystoneSeekBar = (SeekBar) findViewById(R.id.keystone_seekbar);
		keystoneSeekBar.setOnSeekBarChangeListener(this);
		
		// Mode
		RelativeLayout kivicModeLayout = (RelativeLayout) findViewById(R.id.kivic_mode_layout);
		kivicModeLayout.setOnClickListener(this);
		mModeSelectTxt = (TextView) findViewById(R.id.kivic_mode_select_txt);
		
		mHotspotSsidEtx = (EditText) findViewById(R.id.hotspot_ssid_etx);
		mHotspotPWEtx = (EditText) findViewById(R.id.hotspot_password_etx);

		mKivicScreenTxt = (TextView) findViewById(R.id.kivic_screen_txt);
		mKivicScreen_sw = (ToggleButton) findViewById(R.id.kivic_screen_sw);
		mKivicScreen_sw.setOnClickListener(this);
		
		mbBack_btn = (Button) findViewById(R.id.command_back_btn);
		mbBack_btn.setOnClickListener(this);

		showSetting();
		
	}

	private void showSetting()
	{
		// speed color
		if(mSpeedColor == SPEED_COLOR_CYAN)
			mSpeedColorSelectTxt.setText(R.string.cyan);
		else if(mSpeedColor == SPEED_COLOR_YELLOW)
			mSpeedColorSelectTxt.setText(R.string.yellow);
		else if(mSpeedColor == SPEED_COLOR_GREEN)
			mSpeedColorSelectTxt.setText(R.string.green);
		else
			mSpeedColorSelectTxt.setText(R.string.white);
		
		// speed unit
		if(mSpeedUnit == SPEED_UNIT_MPH)
			mSpeedUnitSelectTxt.setText(R.string.kph);
		else
			mSpeedUnitSelectTxt.setText(R.string.mph);

		// mode		
		if(mModeIdx == ITEM_MODE_KIVICCAST)
			mModeSelectTxt.setText(R.string.kivic_cast_title);
		else if(mModeIdx == ITEM_MODE_NAVIGATION)
			mModeSelectTxt.setText(R.string.navigation_name);
		else			
			mModeSelectTxt.setText(R.string.hud_ble);
		
	}
	
	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		if(fromUser) {
			
			if(seekBar.getId() == R.id.keystone_seekbar) {
				float progressLevel = (float)progress/(float)seekBar.getMax();
				float keyStoneValue = progressLevel*HudApplication.MAX_KEYSTONE;
				setKeystone(keyStoneValue);
			}
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}
	

	private void showSpeedColor()
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setIcon(R.drawable.ic_launcher);
		alertBuilder.setTitle(getString(R.string.speed_color));

		// List Adapter 생성
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
		adapter.add(getString(R.string.white));
		adapter.add(getString(R.string.cyan));
		adapter.add(getString(R.string.yellow));
		adapter.add(getString(R.string.green));

		// cancel
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		// ok
		alertBuilder.setSingleChoiceItems(adapter, mSpeedColor, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				int selectColor;
				mSpeedColor = id;

				if(id == SPEED_COLOR_CYAN)
					selectColor = 0xff00ffff;
				else if(id == SPEED_COLOR_YELLOW)
					selectColor = 0xffffff00;
				else if(id == SPEED_COLOR_GREEN)
					selectColor = 0xff00ff00;
				else
					selectColor = 0xffffffff;
				
				DisplaySpeedColorCommandPacket normalSpeedColorSendPacket = new DisplaySpeedColorCommandPacket();
				normalSpeedColorSendPacket.setSpeedColor(selectColor);
				hudApplication.hudNetworkManager.sendPacket(normalSpeedColorSendPacket);
				
				showSetting();
				dialog.dismiss();
			}
		});
		alertBuilder.show();
	}
	
	private void showSpeedUnit()
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setIcon(R.drawable.ic_launcher);
		alertBuilder.setTitle(getString(R.string.speed_unit));

		// List Adapter 생성
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
		adapter.add(getString(R.string.kph));
		adapter.add(getString(R.string.mph));

		// cancel
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		// ok
		alertBuilder.setSingleChoiceItems(adapter, mSpeedUnit, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				mSpeedUnit = id;
				
				DisplaySpeedUintsCommandPacket displaySpeedUintsCommandPacket = new DisplaySpeedUintsCommandPacket();
				displaySpeedUintsCommandPacket.setType(id);
				hudApplication.hudNetworkManager.sendPacket(displaySpeedUintsCommandPacket);
			
				showSetting();
				dialog.dismiss();
			}
		});
		alertBuilder.show();
	}
	
	private void showHudMode()
	{
		AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
		alertBuilder.setIcon(R.drawable.ic_launcher);
		alertBuilder.setTitle(getString(R.string.kivic_mode));

		// List Adapter 생성
		final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_singlechoice);
		adapter.add(getString(R.string.hud_ble));		
		adapter.add(getString(R.string.navigation_name));
		adapter.add(getString(R.string.kivic_cast_title));
		
		// cancel
		alertBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		// ok
		alertBuilder.setSingleChoiceItems(adapter, mModeIdx, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {

				if(mModeIdx == id)
				{
					dialog.dismiss();
					return;
				}
				mModeIdx = id;

				if(mModeIdx == ITEM_MODE_KIVICCAST)
				{
					mHudMode = KivicModeCommandPacket.ANDROID_KIVICCAST_STA_MODE;					
					applyKivicCast(true);
				}
				else if(mModeIdx == ITEM_MODE_NAVIGATION)
				{
					mHudMode = KivicModeCommandPacket.ANDROID_TBT_MODE;
					applyKivicCast(false);
				}
				else
				{
					mHudMode = KivicModeCommandPacket.ANDROID_HUD_MODE;
					applyKivicCast(false);
				}

				KivicModeCommandPacket kivicModeCommandPacket = new KivicModeCommandPacket();
				kivicModeCommandPacket.setMode(mHudMode);
				hudApplication.hudNetworkManager.sendPacket(kivicModeCommandPacket);

				showSetting();
				dialog.dismiss();
			}
		});
		
		alertBuilder.show();
	}
	
	/********************** new KivicCast ***************************/
	private void applyKivicCast(boolean isEnable)
	{
		if(isEnable)
		{
			if(TextUtils.isEmpty(mHotspotSsidEtx.getText().toString()) || TextUtils.isEmpty(mHotspotPWEtx.getText().toString()))
			{			
				return;
			}
			
			WifiSTAModeCommandPacket wifiSTAModeCommandPacket = new WifiSTAModeCommandPacket();
			wifiSTAModeCommandPacket.setSsid(mHotspotSsidEtx.getText().toString());
			wifiSTAModeCommandPacket.setPassword(mHotspotPWEtx.getText().toString());
			wifiSTAModeCommandPacket.setSecurity(2);	
			hudApplication.hudNetworkManager.sendPacket(wifiSTAModeCommandPacket);
			
			ensureMediaProjectionPermission();
		}
		else
		{
			setCast(false);
		}
	}
    
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void ensureMediaProjectionPermission() {

		if(intentResultData == null) {
			MediaProjectionManager mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
			Intent intent = mediaProjectionManager.createScreenCaptureIntent();			
			startActivityForResult(intent, REQUEST_MEDIA_PROJECTION);
		}
		else {
			setCast(true);
		}
	}
	
	private void startKivicCast()
	{
        castServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
            	hudApplication.castMessenger = new Messenger(service);                
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
            	hudApplication.castMessenger = null;
            }
        };

        bindService(new Intent(getApplicationContext(), SampleKivicCastService.class), castServiceConnection, Context.BIND_AUTO_CREATE);
	}
	
	
    private void setCast(boolean enable) {
        if(hudApplication.castMessenger == null) {
            return;
        }
        if(intentResultData == null)
        	return;
        Message message = Message.obtain();

        
        if(enable) {
            Bundle parameter = new Bundle();

            parameter.putInt(SampleKivicCastService.EXTRA_RESULT_CODE, intentResultCode);
            parameter.putParcelable(SampleKivicCastService.EXTRA_RESULT_DATA, intentResultData);
            parameter.putParcelable(SampleKivicCastService.EXTRA_NOTIFICATION_HANDLING_INTENT, new Intent(this, CommandActivity.class));
            parameter.putString(SampleKivicCastService.EXTRA_NOTIFICATION_CONTENTS_TITLE, "Sample KivicCast is running");
            parameter.putInt(SampleKivicCastService.EXTRA_NOTIFICATION_ICON, R.drawable.ic_launcher);            
            parameter.putFloat(SampleKivicCastService.EXTRA_PORTRAIT_SCALE, HudApplication.DEFAULT_KIVICCAST_SCALE);
            message.what = SampleKivicCastService.REQUEST_CONNECT_CAST;
            message.setData(parameter);
        }
        else {
            message.what = SampleKivicCastService.REQUEST_DISCONNECT_CAST;
        }

        try {
        	hudApplication.castMessenger.send(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setPortraitScale(float portraitScale) {
        if(hudApplication.castMessenger == null) {
            return;
        }

        Message message = Message.obtain();
        Bundle parameter = new Bundle();

        parameter.putFloat(SampleKivicCastService.EXTRA_PORTRAIT_SCALE, 0);
        message.what = SampleKivicCastService.REQUEST_PORTRAIT_SCALE_SETTING;
        message.setData(parameter);

        try {
        	hudApplication.castMessenger.send(message);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
	
    
	/***************************************** send HUD packet ***********************************************/
	
	private void setBrightnessEnable(boolean isEnable)
	{
		if(isEnable)
			mBrightnessEnableTxt.setEnabled(true);
		else
			mBrightnessEnableTxt.setEnabled(false);

		DisplayBrightnessCommandPacket displayBrightnessCommandPacket = new DisplayBrightnessCommandPacket();
		displayBrightnessCommandPacket.setBacklightEnabled(isEnable);
		hudApplication.hudNetworkManager.sendPacket(displayBrightnessCommandPacket);
	}
	
	private void setFullScreen(boolean isFullScreen)
	{
		if(isFullScreen)
			mKivicScreenTxt.setEnabled(true);
		else
			mKivicScreenTxt.setEnabled(false);

		FullScreenCommandPacket fullScreenCommandPacket = new FullScreenCommandPacket();
		fullScreenCommandPacket.setFullScreen(isFullScreen);		
		hudApplication.hudNetworkManager.sendPacket(fullScreenCommandPacket);
	}
	
	
	private void setKeystone(float value)
	{	
		KeyStoneCommandPacket keyStoneCommandPacket = new KeyStoneCommandPacket();
		keyStoneCommandPacket.setKeyStone(value);
		hudApplication.hudNetworkManager.sendPacket(keyStoneCommandPacket);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		switch (requestCode) {	
		case REQUEST_MEDIA_PROJECTION:			
			if(resultCode == Activity.RESULT_OK) {
				intentResultCode = resultCode;
				intentResultData = data;
				setCast(true);
			}			
			break;
		default:
			break;
		}
	}
}
