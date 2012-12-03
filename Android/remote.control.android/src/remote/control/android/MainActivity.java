package remote.control.android;

import common.files.android.Constants;

import android.app.Activity;
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

	Button buttonToggleBT;
	Button buttonFind;
	Button buttonPair;
	Button buttonChoose;
	Button buttonUp;
	Button buttonDown;
	Button start;
	Button stop;
	Button settings;

	TextView xCoordinate;
	TextView yCoordinate;
	TextView zCoordinate;
	TextView infoText;
	TextView messageText;

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
		xCoordinate = (TextView) findViewById(R.id.textX);
		yCoordinate = (TextView) findViewById(R.id.textY);
		zCoordinate = (TextView) findViewById(R.id.textZ);
		infoText = (TextView) findViewById(R.id.text_info);
		messageText = (TextView) findViewById(R.id.text_message);
	}

	private void initReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("printMessage");
		registerReceiver(mReceiver, filter);
	}

	private void initButtons()
	{
		// log buttons
		start = (Button) findViewById(R.id.startButton);
		stop = (Button) findViewById(R.id.stopButton);
		settings = (Button) findViewById(R.id.settingsButton);

		// bluetooth buttons
		buttonToggleBT = (Button) findViewById(R.id.btn_toggleBT);
		buttonFind = (Button) findViewById(R.id.btn_find);
		buttonPair = (Button) findViewById(R.id.btn_pair);
		buttonChoose = (Button) findViewById(R.id.btn_choose);

		// other buttons
		buttonUp = (Button) findViewById(R.id.btn_up);
		buttonDown = (Button) findViewById(R.id.btn_down);
	}

	private void initOnClickListners()
	{
		start.setOnClickListener(this);
		stop.setOnClickListener(this);
		settings.setOnClickListener(this);
		buttonUp.setOnClickListener(this);
		buttonDown.setOnClickListener(this);
		buttonChoose.setOnClickListener(this);
		buttonPair.setOnClickListener(this);
		buttonFind.setOnClickListener(this);
		buttonToggleBT.setOnClickListener(this);
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.btn_toggleBT:

			// TODO

			break;

		case R.id.btn_find:

			Intent find = new Intent("callFunction");
			find.putExtra("findDevices", "findDevices");
			sendBroadcast(find);

			break;

		case R.id.btn_pair:

			Intent pair = new Intent("callFunction");
			pair.putExtra("connectDevice", "connectDevice");
			sendBroadcast(pair);

			break;

		case R.id.btn_choose:

			Intent choose = new Intent("callFunction");
			choose.putExtra("chooseDevice", "chooseDevice");
			sendBroadcast(choose);

			break;

		case R.id.btn_up:
			
			Intent enableMS = new Intent(Constants.Broadcast.MotorSignals.Remote.ENABLE_TRANSMISSION);
			sendBroadcast(enableMS);
			
		break;

		case R.id.btn_down:

			Intent disableMS = new Intent(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION);
			sendBroadcast(disableMS);

			break;

		case R.id.startButton:
			Log.d(TAG, "startButton pushed");
			if (LogService.accSensor == false && LogService.accBrainSensor == false) {
				Context context = getApplicationContext();
				Toast.makeText(context, "No Sensors Chosen", Toast.LENGTH_SHORT)
						.show();
			} else if (LogService.logStarted == true) {
				Context context = getApplicationContext();
				Toast.makeText(context, "Log Already Started",
						Toast.LENGTH_SHORT).show();
			} else {
				// Skicka broadcast till service och s�ga start log
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
				// Skicka broadcast till service och s�ga stop log
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
				}

				if (intent.hasExtra("coordinates")) {
					String coordinates = intent.getStringExtra("coordinates");
					messageText.setText(coordinates);
				}
			}
		}
	};

	@Override
	public void onResume()
	{
		Log.d(TAG, "onResume Main");
		initReceiver();
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