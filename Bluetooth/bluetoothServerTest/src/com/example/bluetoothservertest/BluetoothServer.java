package com.example.bluetoothservertest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

@SuppressLint("HandlerLeak")
public class BluetoothServer extends Activity {

	Button btnSetup;
	Button btnClose;
	Button btnConnect;
	Button btnListen;

	TextView textInfo;
	TextView textMessage;


	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetooth_server);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		btnSetup = (Button) findViewById(R.id.btn_setup);
		btnClose = (Button) findViewById(R.id.btn_close);
		btnConnect = (Button) findViewById(R.id.btn_connect);
		btnListen = (Button) findViewById(R.id.btn_collect);

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
				
				if(setupServer())
				{
					textInfo.setText("Server is up");
				}

				else
					textInfo.setText("Failed to set up server");

			}
		});


		btnConnect.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick( View v ) 
			{
				textInfo.setText("Waiting for connection...");

				waitToConnect();

			}
		});


		btnClose.setOnClickListener(new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				closeServerSocket();
				socketUp = false;
			}
		});


		btnListen.setOnClickListener( new OnClickListener() 
		{

			@Override
			public void onClick(View v) 
			{
				listen();
			}
		});

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_bluetooth_server, menu);
		return true;
	}

	BluetoothSocket mmSocket;
	BluetoothServerSocket mmServerSocket;
	BluetoothAdapter mBluetoothAdapter;

	private static final UUID  MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String NAME = "Bluetooth SPP";

	int timeout = 15000;

	boolean serverUp = false;
	boolean socketUp = false;


	private void closeServerSocket()
	{
		if(serverUp)
		{
			//thread.stop();
			try 
			{
				mmServerSocket.close();
				textInfo.setText("Server is down");
				serverUp = false;

			} 

			catch (IOException e) 
			{ 
			}
		}
	}

	private boolean setupServer()
	{
		// Use a temporary object that is later assigned to mmServerSocket,
		// because mmServerSocket is final
		mBluetoothAdapter = null;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		BluetoothServerSocket tmp = null;

		try 
		{
			// MY_UUID is the app's UUID string, also used by the client code
			tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME,  MY_UUID_INSECURE);

		} 

		catch (IOException e) 
		{ 
			serverUp = false;
			return serverUp;
		}

		mmServerSocket = tmp;
		serverUp = true;

		return serverUp;
	}

	private boolean waitToConnect()
	{

		if(serverUp)
		{
			mmSocket = null;
			
			try 
			{
				
				mmSocket = mmServerSocket.accept(timeout);
				textInfo.setText("Socket is up...");
				socketUp = true;
				
			} 
			catch (IOException e) 
			{
				
				closeServerSocket();
				textInfo.setText("Failed to establish socket...");
				socketUp = false;
				
			}
		}


		else
		{
			socketUp = false;
		}


		return socketUp;      
	}


	private InputStream mmInStream;

	byte[] buffer = new byte[1024];  // buffer store for the stream
	int bytes; // bytes returned from read()


	
	@SuppressLint("HandlerLeak")
	private void listen()
	{

		if(socketUp)
		{
			try
			{
				mmInStream = mmSocket.getInputStream();
			}
			catch (IOException e)
			{
				//TODO
				return;
			}		
		}
		//Start thread to read bluetooth
		if(!thread.isAlive())
		{
			thread.start();
		}
	}

	private final Handler handler = new Handler();

	final Runnable r = new Runnable()
	{
		public void run() 
		{
			//textInfo.append("..WOHO..");

			textMessage.setText(str);
		}
	};

	String str;
	
	Thread thread = new Thread()
	{
		@Override
		public void run() 
		{
			while(true) 
			{
				try
				{
					sleep(100);
				}
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				try
				{
					bytes = mmInStream.read(buffer);

					if(bytes != -1)
					{
						str = new String(buffer);
					}

				}
				catch (IOException e)
				{

				}
				
				handler.post(r);
			}
		}
	};










}