package on.hover.android;

import on.hover.android.UsbService.ConnectionState;
import on.hover.android.R;
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

public class MainActivity extends Activity
{	
	private String TAG = "JMMainActivity";
	private ImageView mStatusLed;
	
	private final BroadcastReceiver messageReceiver = new newMessage();
	  
	final Context context = this;
	
	public class newMessage extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{    
			String action = intent.getAction();
			Bundle bundle = intent.getExtras();
			if(action.equalsIgnoreCase("updateUSBConnectionState"))
			{
				ConnectionState state = (ConnectionState) bundle.get("connectionState");		
				Log.d(TAG,"Update USBconnectionState: " + state.name());
				updateUSBConnectionState(state);
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
		setupStatusLed();
		
		// setupTestButton();

		startUsbService();

		Log.d(TAG,"onCreate stop");
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

	@Override
	protected void onResume()
	{
		Log.d(TAG,"onResume start");
		super.onResume();
		registerReceiver(messageReceiver, new IntentFilter("updateUSBConnectionState"));
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
		Log.d(TAG,"onDestroy stop");
	}
	
	private void setupStatusLed() 
	{
		mStatusLed = (ImageView) findViewById(R.id.status_led);
	}

/*	
	private void setupTestButton() 
	{
		Button button = (Button) findViewById(R.id.test_button);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) 
			{
				// Send command
		    	Intent i = new Intent("sendString");
		    	sendBroadcast(i);
			}
		});
	}	
*/
	
	public void updateUSBConnectionState(ConnectionState state)
	{
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