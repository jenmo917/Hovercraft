package com.example.bluetoothservertest;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
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

import protocolbufferjava.Test.Protocol;

public class BluetoothServer extends Activity {

	Button btnSetup;
	Button btnClose;
	Button btnConnect;
	Button btnListen;
	Button btnRead;

	TextView textInfo;
	TextView textMessage;
	
	Protocol coords;

	

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
		

		
		
		
		btnRead.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{

				char[] tempCharArray = new char[1024];
				String tempString = null;
				
				int tempInt = 0;
				int i = 0;
				
				while( true )
				{
					try 
					{
						tempInt = btReader.read();
					} 
					catch (IOException e1) 
					{
						
						textMessage.setText("Failed ones..");
					}
					
					if(tempInt == 36)
						break;

					tempCharArray[i] = (char)tempInt;;

					i++;	
				}
				
				tempString = new String(tempCharArray);
				String selectedPart = tempString.substring(0, i);
				
				textMessage.setText(selectedPart);
							
				byte[] protoByte = selectedPart.getBytes();			
		
				try
				{
						Protocol coords = protocolbufferjava.Test.Protocol.parseFrom(protoByte);
						
						if(coords.hasXCoor())
						{
							textMessage.setText("Proto: " + coords.getXCoor());
						}
				}
				
				catch (IOException e)
				{
					textMessage.setText(">" + selectedPart + "<"+ "Failed...");
				}				
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
	private InputStreamReader btReader;

	byte[] buffer = new byte[2048]; // buffer store for the stream
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
			
			btReader = new InputStreamReader(mmInStream);
			
			
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
			if(coords.hasXCoor())
			{
				textMessage.setText("X: " + coords.getXCoor() + "\nY: " + coords.getYCoor() +  "\nZ: " + coords.getZCoor());
			}
		}
	};

	String str = "ost";
	
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
				
				readBuff();
				
				
				handler.post(r);
			}
		}
	};

	int tempInt = 0;
	int i = 0;
	char[] tempCharArray = new char[1024];
	String tempString = null;
	String selectedPart = null;
	
	void readBuff()
	{
		tempInt = 0;
		i = 0;
		
		//Ytterst obra!!!
		while( true )
		{
			try 
			{
				tempInt = btReader.read();
			} 
			catch (IOException e1) 
			{
				
			}
			
			//Ytterst obra!
			if(tempInt == 36)
				break;

			tempCharArray[i] = (char)tempInt;;

			i++;	
		}
		
		
		
		tempString = new String(tempCharArray);
		selectedPart = tempString.substring(0, i);
		
		
		byte[] protoByte = selectedPart.getBytes();			

		try
		{
				coords = protocolbufferjava.Test.Protocol.parseFrom(protoByte);
		}
		
		catch (IOException e)
		{
		}				
	}
}