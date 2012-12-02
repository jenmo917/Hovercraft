package on.hovercraft.android;

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
import android.util.Log;

//import on.hover.android.Command.DriveSignals;
//import on.hover.android.Command.Engines;

public class BtService extends IntentService
{
	private static String TAG = "JM";

	public BtService() 
	{
		super("BtService");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG,"BtService: start BtService");		
	}


	boolean listenBT = false;

	@Override
	protected void onHandleIntent(Intent arg0) 
	{
		Log.d(TAG, "BTService started");

		//IMPORTANT!
		registerReceiver(BtServiceReciever, new IntentFilter("callFunction"));

		while ( true )
		{
			try
			{
				Thread.sleep(10);
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}			
			if(listenBT)
			{
				readBuffer();
			}
		}
	}

	private void btConnectionLost()
	{
		listenBT = false;
		closeServerSocket();

		Intent i = new Intent("printMessage");
		i.putExtra("message", "Lost connection...");
		sendBroadcast(i);
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
			try 
			{
				mmServerSocket.close();

				Intent i = new Intent("printMessage");
				i.putExtra("message", "Server down...");
				sendBroadcast(i);
				serverUp = false;
			} 
			catch (IOException e) 
			{	
				btConnectionLost();

				Intent i = new Intent("printMessage");
				i.putExtra("message", "Faild to close server...");
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
			i.putExtra("message", "Server up...");
			sendBroadcast(i);
		} 
		catch (IOException e) 
		{ 
			btConnectionLost();

			serverUp = false;
			i.putExtra("message", "Fail...");
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
				i.putExtra("message", "Socket is up..");
				sendBroadcast(i);

				listen();
			} 
			catch (IOException e) 
			{
				btConnectionLost();

				socketUp = false;
				i.putExtra("message", "Connection timed out...");
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
				i.putExtra("message", "Input stream open...");
				sendBroadcast(i);
				listenBT = true;
			}
			catch (IOException e)
			{
				btConnectionLost();

				i.putExtra("message", "Failed to open input stream...");
				sendBroadcast(i);
				listenBT = false;
				return;
			}		

			btReader = new InputStreamReader(mmInStream);
		}
	}


	//			if(isProto)
	//			{
	//				if(coords.hasXCoor())
	//				{
	//					String coo = ("X: " + coords.getXCoor() + "\nY: " + coords.getYCoor() +  "\nZ: " + coords.getZCoor());
	//					Intent i = new Intent("printMessage");
	//					i.putExtra("on.hover.BluetoothServer.coordinates", coo);
	//					sendBroadcast(i);
	//				}
	//			}


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
				btConnectionLost();
				break;
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

		if(listenBT)
		{

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
				i.putExtra("coordinates", selectedPart);
				sendBroadcast(i);
			}
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
			btConnectionLost();
		}

		try 
		{
			mmOutStream.write(data);
		} 
		catch (IOException e) 
		{
			btConnectionLost();
		}
	}

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
				if(intent.hasExtra("setupServer"))
				{
					Log.d(TAG, "BtService: setupServer");
					setupServer();
				}	

				if(intent.hasExtra("waitToConnect"))
				{
					waitToConnect();
				}

				if(intent.hasExtra("listen"))
				{
					//	listen();
				}

				if(intent.hasExtra("sendDataBlinkyOn"))
				{
					String testString = "$Blinky On$";
					byte[] testByte = testString.getBytes();
					sendData(testByte);
				}
				if(intent.hasExtra("sendDataBlinkyOff"))
				{
					String testString = "$Blinky Off$";
					byte[] testByte = testString.getBytes();
					sendData(testByte);
				}				
			}
		}
	};
}
