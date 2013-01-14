package remote.control.android;

import common.files.android.Constants;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {
	private static final String TAG = "REMOTE";
	protected static final int REQUEST_ENABLE_BT = 1;

	String TOGGLE_BLUETOOTH_STATE = "toggleBluetooth";
	String FIND_BLUETOOTH_DEVICES = "findDevices";
	String CHOOSE_BLUETOOTH_DEVICE = "chooseDevice";
	String CONNECT_WITH_BLUETOOTH_DEVICE = "connectDevice";
	String DISCONNECT_BLUETOOTH_DEVICE = "disconnectDevice";
	String TOGGLE_BT_BUTTON_TEXT = "toggleBtButtonText";
	String BT_STATUS = "btStatus";
	
	String BT_CONNECTION_STATUS = "btConnectionStatus";
	String BT_CONNECTION_STATUS_CALL = "btConnectionStatusCall";
	String BT_CONNECTION_STATUS_RESPONS = "btConnectionStatusRespons";
	
	Button buttonToggleBT;
	Button buttonFindBtDevice;
	Button buttonConnectBtDevice;
	Button buttonChooseBtDevice;
	Button buttonToggleTransmission;
	Button buttonToggleLiftFans;
	Button buttonStartLog;
	Button buttonStopLog;
	Button buttonLogSettings;

	TextView infoText;
	TextView messageText;
	

	boolean transmittingMotorSignals = false;
	boolean liftFansOn = false;
	boolean btON = false;
	boolean btConnected = false;
	
	String currentInfo = "Info...";
	int length = 0;
	int i = 0;

	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetest);

		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		// Start log service
		startService(new Intent(this, LogService.class));

		// Start BTRemoteService
		startRemoteBtServerService();

		// Start MotorSignalsService
		startService(new Intent(this, MotorSignalsService.class));

		initButtons();
		initTextViews();
		initOnClickListners();
		initReceiver();
		
		Intent checkTransmissionSatus = new Intent(Constants.Broadcast.ControlSystem.Status.Query.ACTION);
		checkTransmissionSatus.putExtra(Constants.Broadcast.ControlSystem.Status.Query.TYPE, Constants.Broadcast.ControlSystem.Status.TRANSMISSION);
		sendBroadcast(checkTransmissionSatus);
	}

	private void startRemoteBtServerService()
	{
		Log.d(TAG, "start BtService");
		Intent intent = new Intent(this, BtService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}

	private void stopRemoteBtServerService()
	{
		stopService(new Intent(this, BtService.class));
	}

	private void initTextViews()
	{
		infoText = (TextView) findViewById(R.id.text_info);
		messageText = (TextView) findViewById(R.id.text_message);
	}

	private void initReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("printMessage");
		filter.addAction(Constants.Broadcast.ControlSystem.Status.Response.ACTION);
		filter.addAction(TOGGLE_BT_BUTTON_TEXT);
		filter.addAction(Constants.Broadcast.LiftFans.RESPONSE);
		filter.addAction(BT_CONNECTION_STATUS_RESPONS);
		registerReceiver(mReceiver, filter);
	}

	/**
	 * \brief Create button objects for log, BT, motor signals and lift fans.
	 */
	private void initButtons()
	{
		// log buttons
		buttonStartLog = (Button) findViewById(R.id.startButton);
		buttonStopLog = (Button) findViewById(R.id.stopButton);
		buttonLogSettings = (Button) findViewById(R.id.settingsButton);

		// bluetooth buttons
		buttonToggleBT = (Button) findViewById(R.id.toggleBluetoothOnOff);
		buttonFindBtDevice = (Button) findViewById(R.id.findBtDevicesButton);
		buttonConnectBtDevice = (Button) findViewById(R.id.connectBtDeviceButton);
		buttonChooseBtDevice = (Button) findViewById(R.id.chooseBtDeviceButton);

		// other buttons
		buttonToggleTransmission = (Button) findViewById(R.id.toggleTransmissionButton);
		buttonToggleLiftFans = (Button) findViewById(R.id.toggleLiftFansButton);
	}

	/**
	 * \brief Setup buttons' on-click listeners .
	 */
	private void initOnClickListners()
	{
		buttonStartLog.setOnClickListener(this);
		buttonStopLog.setOnClickListener(this);
		buttonLogSettings.setOnClickListener(this);
		buttonToggleTransmission.setOnClickListener(this);
		buttonToggleLiftFans.setOnClickListener(this);
		buttonChooseBtDevice.setOnClickListener(this);
		buttonConnectBtDevice.setOnClickListener(this);
		buttonFindBtDevice.setOnClickListener(this);
		buttonToggleBT.setOnClickListener(this);
	}
	
	public void callBtFunction(String function)
	{
		Intent callFunc = new Intent("callFunction");
		callFunc.putExtra(function, function);
		sendBroadcast(callFunc);
	}
	
	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.toggleBluetoothOnOff:
			
			callBtFunction(TOGGLE_BLUETOOTH_STATE);

			//Semigood but may work on the server
			//Intent testIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			//startActivity(testIntent);
			break;

		case R.id.findBtDevicesButton:

			callBtFunction(FIND_BLUETOOTH_DEVICES);
			break;

		case R.id.connectBtDeviceButton:

			callBtFunction(CONNECT_WITH_BLUETOOTH_DEVICE);
			break;

		case R.id.chooseBtDeviceButton:

			callBtFunction(CHOOSE_BLUETOOTH_DEVICE);
			break;
		// Check if the BT-link is up and send the toggle signal to the ADK.
		// The button is updated on response from the ADK.
		case R.id.toggleTransmissionButton:
			
			callBtFunction(BT_CONNECTION_STATUS_CALL);
			
			if( btConnected )
			{
				if(transmittingMotorSignals)
				{
					Intent disableMS = new Intent(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION);
					sendBroadcast(disableMS);
				}
				else
				{
					Intent enableMS = new Intent(Constants.Broadcast.MotorSignals.Remote.ENABLE_TRANSMISSION);
					sendBroadcast(enableMS);
				}
			}
			else
			{
				Context context = getApplicationContext();
				Toast.makeText(context, "No Bluetooth Connection",
				Toast.LENGTH_SHORT).show();
			}

			Intent checkTransmissionSatus = new Intent(Constants.Broadcast.ControlSystem.Status.Query.ACTION);
			checkTransmissionSatus.putExtra(Constants.Broadcast.ControlSystem.Status.Query.TYPE, Constants.Broadcast.ControlSystem.Status.TRANSMISSION);
			sendBroadcast(checkTransmissionSatus);
			
			break;
			// Check if the BT-link is up and send the toggle signal to the ADK.
			// The button is updated on response from the ADK.
			case R.id.toggleLiftFansButton:
				callBtFunction(BT_CONNECTION_STATUS_CALL);
				
				if(btConnected)
				{
					Intent liftFans = new Intent(
							Constants.Broadcast.LiftFans.REQUEST);
						if (liftFansOn)
						{
							liftFans.putExtra(Constants.Broadcast.LiftFans.STATE,
								Constants.Broadcast.LiftFans.DISABLE);
						}
						else
						{
							liftFans.putExtra(Constants.Broadcast.LiftFans.STATE,
								Constants.Broadcast.LiftFans.ENABLE);
						}
						sendBroadcast(liftFans);
				}
				else
				{
					Context context = getApplicationContext();
					Toast.makeText(context, "No Bluetooth Connection",
					Toast.LENGTH_SHORT).show();
				}
				break;

		case R.id.startButton:
			Log.d(TAG, "startButton pushed");
			if (LogService.accSensor == false && LogService.accBrainSensor == false && LogService.usAdkSensor == false) {
				Context context = getApplicationContext();
				Toast.makeText(context, "No Sensors Chosen", Toast.LENGTH_SHORT)
						.show();
			} else if (LogService.logStarted == true) {
				Context context = getApplicationContext();
				Toast.makeText(context, "Log Already Started",
						Toast.LENGTH_SHORT).show();
			} else {
					// Skicka broadcast till service och säga start log
				Intent startLogIntent = new Intent("StartLogAction");
				startLogIntent.putExtra("logDelay", 5000);
				sendBroadcast(startLogIntent);
				Context context = getApplicationContext();
				Toast.makeText(context, "Log Started", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.stopButton:
			if (LogService.logStarted == false) {
				Context context = getApplicationContext();
				Toast.makeText(context, "No Log Started", Toast.LENGTH_SHORT)
						.show();
			} else {
				Log.d(TAG, "stopButton pushed");
					// Skicka broadcast till service och säga stop log
				Intent stopLogIntent = new Intent("StopLogAction");
				sendBroadcast(stopLogIntent);
				Context context = getApplicationContext();
				Toast.makeText(context, "Log Stopped", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case R.id.settingsButton:
			if (LogService.logStarted == true) {
				Context context = getApplicationContext();
				Toast.makeText(context,
						"Settings not available when log is started",
						Toast.LENGTH_SHORT).show();
			} else if (LogService.logStarted == false) {
				Log.d(TAG, "Settings button pushed");
				Intent settingIntent = new Intent(this, settingActivity.class);
				startActivity(settingIntent);
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		getMenuInflater().inflate(R.menu.activity_bluetest, menu);
		return true;
	}

	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equalsIgnoreCase("printMessage")) {
				if (intent.hasExtra("message")) {
					String message = intent.getStringExtra("message");
					infoText.setText(message);
					currentInfo = message;
				}

				if (intent.hasExtra("coordinates")) {
					String coordinates = intent.getStringExtra("coordinates");
					messageText.setText(coordinates);
				}
			}
			else if(action.equalsIgnoreCase(TOGGLE_BT_BUTTON_TEXT))
			{
				boolean state = intent.getBooleanExtra(BT_STATUS, false);
				if(state)
					buttonToggleBT.setText(R.string.btnToggleBtON);
				else
					buttonToggleBT.setText(R.string.btnToggleBtOFF);	
			}
			// Updated toggle button text.
			else if(Constants.Broadcast.ControlSystem.Status.Response.ACTION.equals(action))
			{
				if(intent.getStringExtra(Constants.Broadcast.ControlSystem.Status.Response.TYPE).equals(Constants.Broadcast.ControlSystem.Status.TRANSMISSION))
				{
					transmittingMotorSignals = intent.getBooleanExtra(Constants.Broadcast.ControlSystem.Status.Response.STATUS, false);
					
					if(transmittingMotorSignals)
						buttonToggleTransmission.setText(R.string.btnStopMS);
					else
						buttonToggleTransmission.setText(R.string.btnSendMS);
				}
			}
			// Updated toggle button text
			else if (action.equals(Constants.Broadcast.LiftFans.RESPONSE))
			{
				liftFansResponse(intent);
			}
			
			else if (action.equalsIgnoreCase(BT_CONNECTION_STATUS_RESPONS)) 
			{
				btConnected = intent.getBooleanExtra(BT_CONNECTION_STATUS, false);
			}
		}

		/**
		 * \brief Updates the lift fans toggle button text depending on status
		 * from ADK.
		 * \author Daniel Josefsson
		 * @param intent
		 */
		private void liftFansResponse(Intent intent)
		{
			if (intent.hasExtra(Constants.Broadcast.LiftFans.STATE))
			{
				byte state = intent.getByteExtra(Constants.Broadcast.LiftFans.STATE, Constants.Broadcast.LiftFans.ERROR);
				if (Constants.Broadcast.LiftFans.ENABLE == state)
				{
					MainActivity.this.liftFansOn = true;
					buttonToggleLiftFans.setText(R.string.btnStopLF);
				}
				else if (Constants.Broadcast.LiftFans.DISABLE == state)
				{
					MainActivity.this.liftFansOn = false;
					buttonToggleLiftFans.setText(R.string.btnSendLF);
				}
			}
		}
	};

	@Override
	public void onResume()
	{
		Log.d(TAG, "onResume Main");
		initReceiver();
		infoText.setText(currentInfo);
		super.onResume();
	}

	@Override
	public void onPause()
	{
		Log.d(TAG, "onPause Main");
		unregisterReceiver(mReceiver);
		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		Log.d(TAG, "onDestroy Main");
		super.onDestroy();
		stopService(new Intent(this, LogService.class));
		// Must be called before ending of BT service.
		stopService(new Intent(this, MotorSignalsService.class));
		// StopremoteBTService
		stopRemoteBtServerService();
	}
}