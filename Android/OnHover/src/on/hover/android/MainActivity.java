package on.hover.android;

import on.hover.android.UsbService.ConnectionState;

import com.android.future.usb.UsbManager;

import on.hover.android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends Activity
{
	private UsbManager usbManager = UsbManager.getInstance(this);
	
	private String TAG = "JMMainActivity";
	private ImageView mStatusLed;
	private UsbService intentTestService;
	
	final Context context = this;
	
	public class newMessage extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{    
			String action = intent.getAction();
			if(action.equalsIgnoreCase("updateUSBConnectionState"))
			{
				Log.d(TAG,"USBconnectionState changed");
				updateUSBConnectionState();
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
		
		if(usbManager.getAccessoryList() == null)
		{
			AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
			 
			// set title
			alertDialogBuilder.setTitle("Automatisk start");
 
			// set dialog message
			alertDialogBuilder
				.setMessage("Denna app startas automatiskt när den monteras på svävaren.")
				.setCancelable(false)
				.setPositiveButton("Avsluta app",new DialogInterface.OnClickListener() 
				{
					public void onClick(DialogInterface dialog,int id)
					{
						// if this button is clicked, close application
						int pid = android.os.Process.myPid();
						android.os.Process.killProcess(pid); 
					}
				});
 
				// create alert dialog
				AlertDialog alertDialog = alertDialogBuilder.create();
 
				// show it
				alertDialog.show();
		}
		
		setContentView(R.layout.main);
		setupStatusLed();
		setupToggleButton();
		setupTestButton();

		startUsbService();

		Log.d(TAG,"onCreate stop");
	}

	private void startUsbService()
	{
		Intent intent = new Intent(this, UsbService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
		//startService(new Intent(this, UsbService.class));
	}
	
	private void stopUsbService()
	{
		stopService(new Intent(this, UsbService.class));
		//stopService(new Intent(this, UsbService.class));
	}

	@Override
	protected void onResume()
	{
		Log.d(TAG,"onResume start");
		super.onResume();
		newMessage messageReceiver = new newMessage();
		registerReceiver(messageReceiver, new IntentFilter("updateUSBConnectionState"));
		Log.d(TAG,"onResume stop");
	}
	
	@Override
	public void onPause()
	{
		Log.d(TAG,"onPause start");
		super.onPause();
		Log.d(TAG,"onPause stop");
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		Log.d(TAG,"onDestroy start");
		stopUsbService();
		Log.d(TAG,"onDestroy stop");
	}
	
	private void setupStatusLed() {
		mStatusLed = (ImageView) findViewById(R.id.status_led);
	}

	private void setupToggleButton()
	{
		Button button = (Button) findViewById(R.id.toggle_button);
		button.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View arg0) 
			{
				Log.d(TAG,"SendCommand Sent not");
				//sendCommand(TOGGLE_LED_COMMAND);
			}
		});
	}
	
	private void setupTestButton() 
	{
		Button button = (Button) findViewById(R.id.test_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) 
			{
				Log.d(TAG,"isConnected: " + UsbService.isActive);
				intentTestService = UsbService.getInstance();
				intentTestService.sendString();
			}
		});
	}	

	public void updateUSBConnectionState()
	{
		ConnectionState state = UsbService.connectionState; 
		switch(state)
		{
			case CONNECTED:
				mStatusLed.setImageResource(R.drawable.green_led);
			break;
			case WAITING:
				mStatusLed.setImageResource(R.drawable.yellow_led);
			break;
			case DISCONNECTED:
				mStatusLed.setImageResource(R.drawable.red_led);
			break;
		}
	}
}