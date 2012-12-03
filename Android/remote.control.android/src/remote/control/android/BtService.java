package remote.control.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import common.files.android.Constants;

//import on.hover.android.Command.DriveSignals;
//import on.hover.android.Command.Engines;

public class BtService extends IntentService
{
	private static String TAG = "JM";

	protected static final int REQUEST_ENABLE_BT = 1;

	public List<String> devicesFound = new ArrayList<String>();
	boolean sendCoordinates = false;

	String Dev;
	String adress = null;

	int length = 0;

	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
	private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothSocket mmSocket;
	private OutputStream mmOutStream;
	private InputStream mmInStream;

	private InputStreamReader btReader;

	boolean socketUp = false;

	byte[] buffer = new byte[2048]; // buffer store for the stream
	int bytes; // bytes returned from read()

	boolean listenBT = false;

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

	@Override
	protected void onHandleIntent(Intent arg0)
	{
		Log.d(TAG, "BTService started");
		initReceiver();

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

	private void initReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter
			.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.ACTION);
		filter.addAction("callFunction");
		registerReceiver(BtRemoteServiceReciever, filter);
	}
	
	private void findDevices()
	{
		//Clear list of devices
		devicesFound.clear();

		if(bluetooth.startDiscovery())	
		{
			Intent i = new Intent("printMessage");
			i.putExtra("message", "Starts searching...");
			sendBroadcast(i);
		}
		else
		{
			Intent i = new Intent("printMessage");
			i.putExtra("message", "Failed to start search...");
			sendBroadcast(i);
		}
	}

	private void printDevicesFound()
	{
		Intent i = new Intent("printMessage");
		i.putExtra("message", "Search finished...");
		sendBroadcast(i);

		//TODO infoText.setText("Devices found:" + "\n\n");

		//GuR OM STRuNG OCH BROADCASTA!!!

		Iterator<String> it = devicesFound.iterator();
		String devs = "";

		while(it.hasNext())
		{
			devs += (String)it.next() + "\n";
		}

		Intent i2 = new Intent("printMessage");
		i2.putExtra("message", devs);
		sendBroadcast(i2);
	}

	private void chooseDevice()
	{
		bluetooth.cancelDiscovery();

		length = devicesFound.size(); 

		if( length > 0 )
		{
			if( i < ( length - 2 ) )
			{
				//Save adress to selected device
				adress = devicesFound.get( 1 + i );
				Intent dev = new Intent("printMessage");
				dev.putExtra("message", "Selected device:" + "\n\n" + devicesFound.get(0  + i )
						+ "\n" + devicesFound.get(1 + i ));

				sendBroadcast(dev);

				i += 2;
			}
			else if (i == (length - 2 ) )
			{
				//Save adress to selected device
				adress = devicesFound.get( 1 + i );
				Intent dev = new Intent("printMessage");
				dev.putExtra("message", "Selected device:" + "\n\n" + devicesFound.get(0  + i )
						+ "\n" + devicesFound.get(1 + i ));
				sendBroadcast(dev);

				i = 0;
			}
			else
			{
				i = 0;
			}
		}
		else
		{
			Intent dev = new Intent("printMessage");
			dev.putExtra("message", "No devices found...");
			sendBroadcast(dev);
		}
	}

	private void listen()
	{
		if( socketUp )
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
				btConnectionLost("Connection lost...");

				i.putExtra("message", "Failed to open input stream...");
				sendBroadcast(i);
				listenBT = false;
				return;
			}

			btReader = new InputStreamReader(mmInStream);
		}
	}
	
	//TODO change to Jens checkInput

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
				btConnectionLost("Connection lost...");
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

	//*******change to Jens checkInput****************


	private void sendData(byte[] data)
	{	
		try 
		{
			mmOutStream = mmSocket.getOutputStream();
		} 
		catch (IOException e) 
		{
			btConnectionLost("Connection lost...");
		}

		try 
		{
			mmOutStream.write(data);
		} 
		catch (IOException e) 
		{
			btConnectionLost("Connection lost...");
		}
	}

	private void connectDevice() throws IOException 
	{
		if(!socketUp)
		{
			BluetoothDevice device = null;
			BluetoothSocket temp = null;
			mmSocket = null;


			if( adress != null )
			{
				device = bluetooth.getRemoteDevice(adress);

				try
				{
					// MY_UUID is the app's UUID string, also used by the server code
					temp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
					mmSocket = temp;
					socketUp = true;

				}
				catch (IOException e)
				{
					mmSocket = null;
					socketUp = false;

					Intent i = new Intent("printMessage");
					i.putExtra("message", "Failed to create socket...");
					sendBroadcast(i);
				}

				if( socketUp )
				{
					try
					{
						mmSocket.connect();

						Intent i = new Intent("printMessage");
						i.putExtra("message", "Connected to device...");
						sendBroadcast(i);

						listen();
					}
					catch (IOException connectException)  
					{
						btConnectionLost("Connection failed...");
					}
				}
			}
			else
			{
				Intent i = new Intent("printMessage");
				i.putExtra("message", "No selected device...");
				sendBroadcast(i);
			}
		}	
	}

	private void btConnectionLost(String message)
	{
		listenBT = false;
		socketUp = false;
		try
		{
			mmSocket.close();
		}
		catch (IOException e)
		{ 
			
		}

		Intent i = new Intent("printMessage");
		i.putExtra("message", message);
		sendBroadcast(i);
	}

	private final BroadcastReceiver BtRemoteServiceReciever = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();

			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				if(!devicesFound.contains(device.getName()))
				{
					devicesFound.add(device.getName());
					devicesFound.add(device.getAddress());

					Intent i = new Intent("printMessage");
					i.putExtra("message", device.getName() + "\n" + device.getAddress());
					sendBroadcast(i);
				}
			}

			//When discovery is finished, change the Activity title
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				printDevicesFound();
			}

			//function called
			if(action.equalsIgnoreCase("callFunction"))
			{

				if(intent.hasExtra("findDevices"))
				{
					findDevices();
				}

				if(intent.hasExtra("printDevices"))
				{
					printDevicesFound();
				}

				if(intent.hasExtra("chooseDevice"))
				{
					chooseDevice();
				}

				if(intent.hasExtra("connectDevice"))
				{
					if( !socketUp && adress != null)
					{
						try
						{
							connectDevice();
						}
						catch (IOException e)
						{
							Intent i = new Intent("printMessage");
							i.putExtra("message", "Failed to connect...");
							sendBroadcast(i);
						}
					}
				}

				if(intent.hasExtra("sendData") && socketUp)
				{
					String up = "$up$";
					sendData(up.getBytes());
				}

				if(intent.hasExtra("sendProto") && socketUp)
				{
					String up = "$down$";
					sendData(up.getBytes());
				}
			}
			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.ACTION))
			{
				byte command = intent
					.getByteExtra(
						Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND,
						(byte) 0);
				byte target = intent
					.getByteExtra(
						Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET,
						(byte) 0);
				byte[] bytes = intent
					.getByteArrayExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES);
				sendProtocol(command, target, bytes);
			}
		}
	};

	// TODO: Should it give any status as return?
	public void sendProtocol(byte command, byte target, byte[] message)
	{
		int messageLength = message.length;
		byte[] dataTransmitt = new byte[3 + messageLength];
		dataTransmitt[0] = command;
		dataTransmitt[1] = target;
		dataTransmitt[2] = (byte) messageLength;
		System.arraycopy(message, 0, dataTransmitt, 3, messageLength);
		sendData(dataTransmitt);
	}
}
