package on.hovercraft.android;

import com.android.future.usb.UsbManager;

import se.liu.ed.Constants;
import se.liu.ed.Constants.ConnectionState;
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
/**
* \brief MainActivity
* This is the main activity of the application. 
*
* \author Jens Moser
*/
public class MainActivity extends Activity
{
	private String TAG = "JM"; /**< LogCat TAG */
	private ImageView mUSBStatusLed; /**< Connection state LED */
	private ImageView mBTStatusLed; /**< Connection state LED */
	TextView textInfo; /**< Info from BTservice */
	TextView textMessage; /**< Message from BTservice */

	private final BroadcastReceiver messageReceiver = new newMessage(); /**< BroadcastReceiver */
	final Context context = this;
	
	/**
	* \brief Restart app
	* This function will restart the app 
	*
	* \author Jens Moser
	*/
	private void restart()
	{
		AppRestart.doRestart(this);
	}

	/**
	* \brief Broadcast receiver
	*
	* \author Jens Moser
	*/	
	private class newMessage extends BroadcastReceiver
	{
		/**
		* \brief onReceive
		* This function will execute when broadcast is received.
		*
		* @param intent Information sent with the broadcast
		*
		* \author Jens Moser
		*/	
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
			}
		}
	}

	/**
	* \brief Back button
	*
	* This function disables the back button on the phone
	*
	* \author Jens Moser
	*/	
	@Override
	public void onBackPressed()
	{

	}

	/**
	* \brief onCreate
	*
	* Executes when app starts
	*
	* \author Jens Moser
	*/		
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
		startBtServerService();
		startControlSystemService();
		startUsbService();
		startLogServiceBrain();
		
		Log.d(TAG,"onCreate stop");
	}
	
	/**
	* \brief Setup buttons
	*
	* \author Jens Moser
	*/	
	private void setupButtons()
	{
		setupBTSetupButton();
	}
	
	/**
	* \brief Init text views
	*
	* Setup text views
	*
	* \author Jens Moser
	*/	
	private void initTextViews()
	{
		textInfo = (TextView) findViewById(R.id.textInfo);
		textMessage = (TextView) findViewById(R.id.textMessage);
	}
	
	/**
	* \brief Start USB service
	*
	* \author Jens Moser
	*/	
	private void startUsbService()
	{
		Log.d(TAG,"start UsbService");
		Intent intent = new Intent(this, UsbService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}
	
	/**
	* \brief stop USB-service
	*
	* \author Jens Moser
	*/		
	private void stopUsbService()
	{
		stopService(new Intent(this, UsbService.class));
	}

	/**
	* \brief Start Log-service
	*
	* \author Emil Andersson
	*/		
	private void startLogServiceBrain()
	{
		Log.d(TAG,"start LogServiceBrain");
		Intent intent = new Intent(this, LogServiceBrain.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}
	
	/**
	* \brief Stop Log-service
	*
	* \author Emil Andersson
	*/		
	private void stopLogServiceBrain()
	{
		stopService(new Intent(this, LogServiceBrain.class));
	}

	/**
	* \brief Start Controlsystem-service.
	*
	* \author Daniel Josefsson
	*/		
	private void startControlSystemService()
	{
		Log.d(TAG,"start ControlSystemService");
		Intent intent = new Intent(this, ControlSystemService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}
	
	/**
	* \brief Stop Controlsystem-service.
	*
	* \author Daniel Josefsson
	*/			
	private void stopControlSystemService()
	{
		stopService(new Intent(this, ControlSystemService.class));
	}
	
	/**
	* \brief Start Bt-service.
	*
	* \author Johan Gustafsson
	*/	
	private void startBtServerService()
	{
		Log.d(TAG,"start BtService");
		Intent intent = new Intent(this, BtService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}
	
	/**
	* \brief Stop Bt-service.
	*
	* \author Johan Gustafsson
	*/	
	private void stopBtServerService()
	{
		stopService(new Intent(this, BtService.class));
	}
	
	/**
	* \brief Init broadcast receiver
	* This class will listen on below specified filters.
	*
	* \author Jens Moser
	*/	
	private void initReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.Broadcast.UsbService.UPDATE_CONNECTION_STATE);
		filter.addAction(Constants.Broadcast.BluetoothService.UPDATE_CONNECTION_STATE);
		filter.addAction("printMessage");
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
		registerReceiver(messageReceiver, filter);		
	}
	
	/**
	* \brief Executes when app is resumed.
	*
	* \author Jens Moser
	*/	
	@Override
	protected void onResume()
	{
		Log.d(TAG,"onResume start");
		super.onResume();
		initReceiver();
		Log.d(TAG,"onResume stop");
	}
	
	/**
	* \brief Executes when app is paused.
	*
	* \author Jens Moser
	*/	
	@Override
	public void onPause()
	{
		Log.d(TAG,"onPause start");
		super.onPause();
		unregisterReceiver(messageReceiver);
		Log.d(TAG,"onPause stop");
	}
	
	/**
	* \brief Executes when app is destroyed.
	*
	* \author Jens Moser
	*/	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG,"onDestroy start");
		stopUsbService();
		stopControlSystemService();
		stopBtServerService();
		stopLogServiceBrain();
		Log.d(TAG,"onDestroy stop");
	}
	
	/**
	* \brief Setup communication leds
	*
	* \author Jens Moser
	*/	
	private void setupStatusLeds()
	{
		mUSBStatusLed = (ImageView) findViewById(R.id.usb_connection_status_led);
		mBTStatusLed = (ImageView) findViewById(R.id.bt_connection_status_led);
	}
	
	/**
	* \brief Setup BT-setup-button.
	*
	* \author Johan Gustafsson
	*/		
	private void setupBTSetupButton()
	{
		Button setupButton = (Button) findViewById(R.id.bt_setup_button);
		setupButton.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "setupBTSetupButton pushed");

				//Call serverUp
				Intent i = new Intent("callFunction");
				i.putExtra("setupServer", "setupServer");
				sendBroadcast(i);

				//TODO move to BTservice so not printed when already connected
				textInfo.setText("Waiting for connection...");

				Intent i2 = new Intent("callFunction");
				i2.putExtra("waitToConnect", "waitToConnnect");
				sendBroadcast(i2);
			}
		});
	}

    // TODO: One (not two very similar) function to update connectionState.
	
	/**
	* \brief Set USB LED
	*
	* \author Jens Moser
	*/	
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
	
	/**
	* \brief Set BT LED
	*
	* \author Jens Moser
	*/	
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
