package on.hovercraft.android;

import com.android.future.usb.UsbManager;

import on.hovercraft.android.R;
import common.files.android.Constants;
import common.files.android.Constants.ConnectionState;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private String TAG = "JM";
	private ImageView mUSBStatusLed;
	private ImageView mBTStatusLed;
	TextView textInfo;
	TextView textMessage;

	private final BroadcastReceiver messageReceiver = new newMessage();
	final Context context = this;

	private void restart()
	{
		AppRestart.doRestart(this);
	}

	private class newMessage extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
			{
				Log.d(TAG,"finish!");
			}
			else if (Constants.Broadcast.System.POWER_CONNECTED.equals(action))
			{
				Log.d(TAG,"power connected!");
				restart();
			}
			else if(action.equalsIgnoreCase(
						Constants.Broadcast.UsbService.UPDATE_CONNECTION_STATE))
			{
				ConnectionState state = (ConnectionState) bundle.get(
						Constants.Broadcast.ConnectionStates.CONNECTION_STATE);
				Log.d(TAG,"Update USBconnectionState: " + state.name());
				updateUSBConnectionState(state);
			}
			else if(action.equalsIgnoreCase(
				Constants.Broadcast.BluetoothService.UPDATE_CONNECTION_STATE))
			{
				ConnectionState state = (ConnectionState)bundle.get(
						Constants.Broadcast.ConnectionStates.CONNECTION_STATE);
				Log.d(TAG,"Update BTconnectionState: " + state.name());
				updateBTConnectionState(state);
			}
			else if(action.equalsIgnoreCase("printMessage")) //TODO: Fix to Constants
			{
				if(intent.hasExtra("message"))
				{
					String message = intent.getStringExtra("message");
					textInfo.setText(message);
				}

				if(intent.hasExtra("coordinates"))
				{
					String coordinates = intent.getStringExtra("coordinates");

					if( (coordinates.equalsIgnoreCase("up")) )
					{
						textMessage.setText("Blinky on");
						
						Intent i = new Intent("sendBlinkyOnCommand");
						sendBroadcast(i);

						//Call serverUp
						Intent i2 = new Intent("callFunction");
						i2.putExtra("sendDataBlinkyOn", "sendDataBlinkyOn");
						sendBroadcast(i2);
					}
					else if( (coordinates.equalsIgnoreCase("down")) )
					{
						textMessage.setText("Blinky off");

						Intent i = new Intent("sendBlinkyOffCommand");
						sendBroadcast(i);

						//Call serverUp
						Intent i2 = new Intent("callFunction");
						i2.putExtra("sendDataBlinkyOff", "sendDataBlinkyOff");
						sendBroadcast(i2);
					}
				}
			}
		}
	}
	
	@Override
	public void onBackPressed()
	{

	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{				
		Log.d(TAG,"onCreate start");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Setup USB and Bluetooth status LED:s
		setupStatusLeds();

		// Setup buttons, onClick etc and init text views
		setupButtons();
		initTextViews();

		// Start USB and Blue service
		startUsbService();
		startBtServerService();

		Log.d(TAG,"onCreate stop");
	}

	private void setupButtons()
	{
		setupADKTestButton();
		setupBTSetupButton();
		setupBTListenButton();
	}

	private void initTextViews()
	{
		textInfo = (TextView) findViewById(R.id.textInfo);
		textMessage = (TextView) findViewById(R.id.textMessage);
	}

	private void startUsbService()
	{
		Intent intent = new Intent(this, UsbService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}
	
	private void stopUsbService()
	{
		stopService(new Intent(this, UsbService.class));
	}

	private void startBtServerService()
	{
		Log.d(TAG,"start BtService");
		Intent intent = new Intent(this, BtService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}

	private void stopBtServerService()
	{
		stopService(new Intent(this, BtService.class));
	}

	private void initReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction("updateUSBConnectionState");
		filter.addAction("updateBTConnectionState");
		filter.addAction("printMessage");
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
		registerReceiver(messageReceiver, filter);		
	}

	@Override
	protected void onResume()
	{
		Log.d(TAG,"onResume start");
		super.onResume();
		initReceiver();
		Log.d(TAG,"onResume stop");
	}

	@Override
	public void onPause()
	{
		Log.d(TAG,"onPause start");
		super.onPause();
		unregisterReceiver(messageReceiver);
		Log.d(TAG,"onPause stop");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG,"onDestroy start");
		stopUsbService();
		stopBtServerService();
		Log.d(TAG,"onDestroy stop");
	}

	private void setupStatusLeds()
	{
		mUSBStatusLed = (ImageView) findViewById(R.id.usb_connection_status_led);
		mBTStatusLed = (ImageView) findViewById(R.id.bt_connection_status_led);
	}

	private void setupADKTestButton() 
	{
		Button button = (Button) findViewById(R.id.adk_test_button);
		button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				Log.d(TAG,"setupADKTestButton pushed");
				Intent i = new Intent("sendADKTestCommand");
				sendBroadcast(i);
			}
		});
	}

	private void setupBTSetupButton()
	{
		Button setupButton = (Button) findViewById(R.id.bt_setup_button);
		setupButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "setupBTSetupButton pushed");

				//TODO: Visible mode turn on by user 

				//Call serverUp
				Intent i = new Intent("callFunction");
				i.putExtra("setupServer", "setupServer");
				sendBroadcast(i);

				textInfo.setText("Waiting for connection...");

				Intent i2 = new Intent("callFunction");
				i2.putExtra("waitToConnect", "waitToConnnect");
				sendBroadcast(i2);
			}
		});
	}

	private void setupBTListenButton()
	{
		Button listenButton = (Button) findViewById(R.id.bt_listen_button);
		listenButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				textInfo.setText("Recieving coordinates...");
				
				Intent i = new Intent("callFunction");
				i.putExtra("listen", "listen");
				sendBroadcast(i);
			}
		});
	}

	private void updateUSBConnectionState(ConnectionState state)
	{
		switch(state)
		{
			case CONNECTED:
				mUSBStatusLed.setImageResource(R.drawable.green_led);
			break;
			case WAITING:
				mUSBStatusLed.setImageResource(R.drawable.yellow_led);
			break;
			case DISCONNECTED:
				mUSBStatusLed.setImageResource(R.drawable.red_led);
			break;
		}
	}

	private void updateBTConnectionState(ConnectionState state)
	{
		switch(state)
		{
			case CONNECTED:
				mBTStatusLed.setImageResource(R.drawable.green_led);
			break;
			case WAITING:
				mBTStatusLed.setImageResource(R.drawable.yellow_led);
			break;
			case DISCONNECTED:
				mBTStatusLed.setImageResource(R.drawable.red_led);
			break;
		}
	}
}
