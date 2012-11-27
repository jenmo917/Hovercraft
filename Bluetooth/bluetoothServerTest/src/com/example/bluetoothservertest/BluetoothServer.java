package com.example.bluetoothservertest;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class BluetoothServer extends Activity {

	Button btnSetup;
	Button btnClose;
	Button btnConnect;
	Button btnListen;
	Button btnRead;

	TextView textInfo;
	TextView textMessage;
	
	private static String TAG = "Server";

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		//Start activity
		
		Log.d(TAG, "Service not started yet...");
		//IMPORTANT!
		registerReceiver(serverReciever, new IntentFilter("printMessage"));
		
		//Start service
		startBtServerService();
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_server);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		btnSetup = (Button) findViewById(R.id.btn_setup);
		btnClose = (Button) findViewById(R.id.btn_close);
		btnConnect = (Button) findViewById(R.id.btn_connect);
		btnListen = (Button) findViewById(R.id.btn_collect);
		btnRead = (Button) findViewById(R.id.btn_read);
		
		textInfo = (TextView) findViewById(R.id.text_info);
		textMessage = (TextView) findViewById(R.id.text_messages);

		btnSetup.setOnClickListener( new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
				discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
				startActivity(discoverableIntent);
				
				//Call serverUp
	    		Intent i = new Intent("callFunction");
	    		i.putExtra("com.example.BtService.setupServer", "setupServer");
	    		sendBroadcast(i);
			}
		});

		btnConnect.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick( View v ) 
			{
				textInfo.setText("Waiting for connection...");
				
	    		Intent i = new Intent("callFunction");
	    		i.putExtra("com.example.BtService.waitToConnect", "waitToConnnect");
	    		sendBroadcast(i);
			}
		});

		btnClose.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				//BROADCAST FUNCTION CALL
				//closeServerSocket();
			}
		});

		btnListen.setOnClickListener( new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				textInfo.setText("Recieving coordinates...");
				
	    		Intent i = new Intent("callFunction");
	    		i.putExtra("com.example.BtService.listen", "listen");
	    		sendBroadcast(i);
			}
		});
		
		btnRead.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
	    		Intent i = new Intent("callFunction");
	    		i.putExtra("com.example.BtService.sendData", "sendData");
	    		sendBroadcast(i);
			}
		});
	}

	private void startBtServerService()
	{
		Intent intent = new Intent(this, BtService.class);
		intent.fillIn(getIntent(), 0);
		startService(intent);
	}

	private void stopBtServerService()
	{
		stopService(new Intent(this, BtService.class));
		//stopService(new Intent(this, UsbService.class));
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		stopBtServerService();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_bluetooth_server, menu);
		return true;
	}
	
	//Broadcast reciever
	private final BroadcastReceiver serverReciever = new BroadcastReceiver() 
	{
		
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			
			//function called
			if(action.equalsIgnoreCase("printMessage"))
			{
				
				if(intent.hasExtra("com.example.BluetoothServer.message"))
				{
					String message = intent.getStringExtra("com.example.BluetoothServer.message");
					textInfo.setText(message);
				}
				
				if(intent.hasExtra("com.example.BluetoothServer.coordinates"))
				{
					String coordinates = intent.getStringExtra("com.example.BluetoothServer.coordinates");
					textMessage.setText(coordinates);
				}
				
			}
		}
	};
}