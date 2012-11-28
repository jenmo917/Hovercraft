package on.hover.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;

//import on.hover.android.Command.DriveSignals;
//import on.hover.android.Command.Engines;


public class BtService extends IntentService
{
	private static String TAG = "JMMainActivity";
	
	public BtService() 
	{
		super("BtService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) 
	{
		Log.d(TAG, "Service started");
		
		//IMPORTANT!
		registerReceiver(BtServiceReciever, new IntentFilter("callFunction"));
		
		while ( true )
		{
			
		}
	}
	
	//Protocol coords;
	BluetoothSocket mmSocket;
	BluetoothServerSocket mmServerSocket;
	BluetoothAdapter mBluetoothAdapter;

	private static final UUID  MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String NAME = "Bluetooth SPP";

	int timeout = 30000;
	boolean serverUp = false;
	boolean socketUp = false;


	private void closeServerSocket()
	{
		if(serverUp)
		{
			Intent i = new Intent("printMessage");
			
			try 
			{
				mmServerSocket.close();
				
				i.putExtra("com.example.BluetoothServer.message", "Server down...");
				sendBroadcast(i);
				serverUp = false;
			} 
			catch (IOException e) 
			{
				i.putExtra("com.example.BluetoothServer.message", "Faild to close server...");
				sendBroadcast(i);
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
		Intent i = new Intent("printMessage");
		
		try 
		{
			// MY_UUID is the app's UUID string, also used by the client code
			tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME,  MY_UUID_INSECURE);
			
			serverUp = true;
			i.putExtra("com.example.BluetoothServer.message", "Server up...");
			sendBroadcast(i);
		} 
		catch (IOException e) 
		{ 
			serverUp = false;
			i.putExtra("com.example.BluetoothServer.message", "Fail...");
			sendBroadcast(i);
		}

		mmServerSocket = tmp;
		return serverUp;
	}

	private boolean waitToConnect()
	{

		if(serverUp)
		{
			mmSocket = null;
			
			Intent i = new Intent("printMessage");
			
			try 
			{
				mmSocket = mmServerSocket.accept(timeout);
				
				socketUp = true;
				i.putExtra("com.example.BluetoothServer.message", "Socket is up..");
				sendBroadcast(i);
			} 
			catch (IOException e) 
			{
				socketUp = false;
				i.putExtra("com.example.BluetoothServer.message", "Failed to establish socket...");
				sendBroadcast(i);
				
				closeServerSocket();	
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
			Intent i = new Intent("printMessage");
			try
			{
				mmInStream = mmSocket.getInputStream();
				i.putExtra("com.example.BluetoothServer.message", "Input stream open...");
				sendBroadcast(i);
			}
			catch (IOException e)
			{
				i.putExtra("com.example.BluetoothServer.message", "Failed to open input stream...");
				sendBroadcast(i);
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
			/*
			if(isProto)
			{
				if(coords.hasXCoor())
				{
					String coo = ("X: " + coords.getXCoor() + "\nY: " + coords.getYCoor() +  "\nZ: " + coords.getZCoor());
					Intent i = new Intent("printMessage");
					i.putExtra("com.example.BluetoothServer.coordinates", coo);
					sendBroadcast(i);
				}
			}
			*/
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
					sleep(50);
				}
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				
				readBuffer();
				handler.post(r);
			}
		}
	};

	int tempInt = 0;
	int i = 0;
	boolean firstChar = true;
	boolean isProto = false;
	
	char[] tempCharArray = new char[1024];
	String tempString = null;
	String selectedPart = null;
	
	void readBuffer()
	{
		firstChar = true;
		tempInt = 0;
		i = 0;
		
		while( true )
		{
			try 
			{
				tempInt = btReader.read();
			} 
			catch (IOException e1) 
			{
				
			}
			
			if(tempInt == 36 && firstChar == false)
				break;
			
			if(firstChar)
			{
				firstChar = false;
				
				//First char not \n
				if(tempInt == 10)
					isProto = true;
				else if(tempInt == 36)
					isProto = false;
				else
					break;
			}
			
			tempCharArray[i] = (char)tempInt;

			i++;	
		}
		
		if(isProto)
		{
			tempString = new String(tempCharArray);
			selectedPart = tempString.substring(0, i);
			
			//byte[] protoByte = selectedPart.getBytes();			
			/*
			try
			{
				coords = protocolbufferjava.Test.Protocol.parseFrom(protoByte);
			}
			
			catch (IOException e)
			{
				//WHAT TO DO???
			}	
			*/
		}
		else
		{
			tempString = new String(tempCharArray);
			selectedPart = tempString.substring(1, i);
			
			Intent i = new Intent("printMessage");
			i.putExtra("com.example.BluetoothServer.coordinates", selectedPart);
			sendBroadcast(i);
		}
	}
	
	
	
	private OutputStream mmOutStream;
	
	private void sendData(byte[] data)
	{			
		try 
	    {
			mmOutStream = mmSocket.getOutputStream();
	    } 
	    catch (IOException e) 
	    {
	      //Do something?
	    }
	 
	    try 
	    {
	    	mmOutStream.write(data);
	    } 
	    catch (IOException e) 
	    {
	    	//Do something?
	    }
	}
	
	int testInt = 0;
	
	//Broadcast reciever
	private final BroadcastReceiver BtServiceReciever = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			
			//function called
			if(action.equalsIgnoreCase("callFunction"))
			{
				//witch function
				if(intent.hasExtra("com.example.BtService.setupServer"))
				{
					Log.d(TAG, "BtService: setupServer");
					setupServer();
				}	
				
				if(intent.hasExtra("com.example.BtService.waitToConnect"))
				{
					waitToConnect();
				}
				
				if(intent.hasExtra("com.example.BtService.listen"))
				{
					listen();
				}
				
				if(intent.hasExtra("com.example.BtService.sendData"))
				{
					String testString = "Blinky" + String.valueOf(testInt);
					byte[] testByte = testString.getBytes();
					
					sendData(testByte);
					testInt++;
				}
			}
		}
	};
}
