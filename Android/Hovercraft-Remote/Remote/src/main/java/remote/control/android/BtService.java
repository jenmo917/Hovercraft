package remote.control.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

import se.liu.ed.Constants;

public class BtService extends IntentService
{
	String TOGGLE_BLUETOOTH_STATE = "toggleBluetooth";
	String FIND_BLUETOOTH_DEVICES = "findDevices";
	String CHOOSE_BLUETOOTH_DEVICE = "chooseDevice";
	String CONNECT_WITH_BLUETOOTH_DEVICE = "connectDevice";
	String DISCONNECT_BLUETOOTH_DEVICE = "disconnectDevice";
	String TOGGLE_BT_BUTTON_TEXT = "toggleBtButtonText";
	String BT_STATUS = "btStatus";
	
	String BT_CONNECTION_STATUS = "btConnectionStatus";
	String BT_CONNECTION_STATUS_CALL = "btConnectionStatusCall";
	String BT_CONNECTION_STATUS_RESPONSE = "btConnectionStatusResponse";
	
	protected static final int REQUEST_ENABLE_BT = 1;
	
	private static String TAG = "JM";											/**< TAG name*/
	public List<String> devicesFound = new ArrayList<String>();					/**< List of found Bt devices*/
	
	boolean bluetoothSocketUp = false;											/**< Bt socket up when true*/
	boolean listenOnBtInputStream = false;										/**< Read Bt input stream when true*/

	String deviceAddress = null;												/**< Device address of chosen Bt device*/
	String deviceName = null;													/**< Device name of chosen Bt device*/
	int lengthOfDeviceArray = 0;												/**< Number of found devices*/

	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();         								/**< Bluetooth adapter*/
	private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");	/**< Bt profile UUID*/
	private BluetoothSocket mmSocket;																		/**< Bluetooth socket*/
	private OutputStream mmOutStream;																		/**< Bluetooth inputstream*/
	private InputStream mInputStream;																		/**< Bluetooth outputstream*/

	/**
	* \brief Constructor
	*
	*
	* \author Johan Gustafsson
	*/
	public BtService() 
	{
		super("BtService");
	}
	
	/**
	* \brief OnCreate
	*
	*
	* \author Johan Gustafsson
	*/
	@Override
	public void onCreate()
	{
        if (bluetooth == null) {
            Log.d(TAG,"BtService: Crap, Bluetooth does not seems to be supported.");
        }
        if(bluetooth.isEnabled())
		{
			updateBtButtonText(true);
		}
		else
		{
			updateBtButtonText(false);
		}

		super.onCreate();
		Log.d(TAG,"BtService: start BtService");
	}
	
	/**
	* \brief Update the text on the BT button
	*
	*
	* \author Johan Gustafsson
	*/
	void updateBtButtonText(boolean status)
	{
		Intent toggle = new Intent(TOGGLE_BT_BUTTON_TEXT);
		toggle.putExtra(BT_STATUS, status);
		sendBroadcast(toggle);
	}
	
	/**
	* \brief Enable/disable bluetooth for the device
	*
	*
	* \author Johan Gustafsson
	*/
	void toggleBtOnOff()
	{
		if(bluetooth.isEnabled())
		{
			bluetooth.disable();
			updateBtButtonText(false);
		}
		else
		{
			bluetooth.enable();
			updateBtButtonText(true);
		}	
	}
	
	/**
	* \brief BtService main function
	* 
	* Read the input stream when a device is connected
	*
	*
	* \author Johan Gustafsson
	*/
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
			if( listenOnBtInputStream )
			{
				checkInput();
			}
		}
	}

	/**
	* \brief Initiate the BtService broadcast receiver
	*
	* \author Johan Gustafsson
	*/
	private void initReceiver()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.ACTION);
		filter.addAction("callFunction");
		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_US_DATA);
		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_ACC_BRAIN_DATA);
		filter.addAction(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_STOP_ACC_BRAIN_DATA);
		filter.addAction(Constants.Broadcast.LiftFans.REQUEST);
		registerReceiver(BtRemoteServiceReceiver, filter);
	}
	
	/**
	* \brief Start a search for bluetooth devices
	*
	* \author Johan Gustafsson
	*/
	private void findBluetoothDevices()
	{
		//Clear list of devices
		devicesFound.clear();

		if(bluetooth.startDiscovery())	
		{
			sendBroadcastInfo("Starts searching...");
		}
		else
		{
			sendBroadcastInfo("Failed to start search...");
		}
	}

	/**
	* \brief Print a list of found bluetoth devices in the GUI
	*
	* \author Johan Gustafsson
	*/
	private void printBluetoothDevicesFound()
	{
		sendBroadcastInfo("Search finished...");
		Iterator<String> it = devicesFound.iterator();
		String devs = "";
		
		while(it.hasNext())
		{
				devs += (String)it.next() + "\n";
		}
		
		sendBroadcastInfo("Devices found:\n\n" + devs);
	}

	int i = 0;
	
	/**
	* \brief Choose a bluetooth device from the list of found devices
	* 
	* \author Johan Gustafsson
	*/
	private void chooseFoundBluetoothDevice()
	{
		bluetooth.cancelDiscovery();

		lengthOfDeviceArray = devicesFound.size(); 

		if( lengthOfDeviceArray > 0 )
		{
			if( i < ( lengthOfDeviceArray - 2 ) )
			{
				//Save address to selected device
				deviceAddress = devicesFound.get( 1 + i );
				deviceName = devicesFound.get( 0  + i );
				
				sendBroadcastInfo("Selected device:" + "\n\n" + devicesFound.get( 0  + i )
									+ "\n" + devicesFound.get(1 + i ));
				i += 2;
			}
			else if (i == (lengthOfDeviceArray - 2 ) )
			{
				//Save address to selected device
				deviceAddress = devicesFound.get( 1 + i );
				deviceName = devicesFound.get(0  + i );
				
				sendBroadcastInfo("Selected device:" + "\n\n" + devicesFound.get( 0  + i )
									+ "\n" + devicesFound.get(1 + i ));
				i = 0;
			}
			else
			{
				i = 0;
			}
		}
		else
		{
			sendBroadcastInfo("No devices found...");
		}
	}

	/**
	* \brief Connect to the selected bluetooth device
	*
	* \author Johan Gustafsson
	*/
	private void connectDevice() throws IOException 
	{
		if( !bluetoothSocketUp )
		{
			BluetoothDevice device = null;
			BluetoothSocket temp = null;
			mmSocket = null;


			if( deviceAddress != null )
			{
				device = bluetooth.getRemoteDevice(deviceAddress);

				try
				{
					// MY_UUID is the app's UUID string, also used by the server code
					temp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
					mmSocket = temp;
					bluetoothSocketUp = true;

				}
				catch (IOException e)
				{
					mmSocket = null;
					bluetoothSocketUp = false;
					sendBroadcastInfo("Failed to create socket...");
				}

				if( bluetoothSocketUp )
				{
					try
					{
						mmSocket.connect();
						sendBroadcastInfo("Connected to device...");
						startReadingBluetoothInputStream();
					}
					catch (IOException connectException)  
					{
						btConnectionLost("Connection failed...");
					}
				}
				
				//Send new status to Main
				Intent i = new Intent(BT_CONNECTION_STATUS_RESPONSE);
				i.putExtra(BT_CONNECTION_STATUS, bluetoothSocketUp);
				sendBroadcast(i);
			}
			else
			{
				sendBroadcastInfo("No selected device...");
			}
		}	
	}
	
	/**
	* \brief Read the bluetooth input stream
	*
	* Basic version
	*
	* \author Johan Gustafsson
	*/
	private void startReadingBluetoothInputStream()
	{
		if( bluetoothSocketUp )
		{
			try
			{
				mInputStream = mmSocket.getInputStream();
				sendBroadcastInfo("Connected to: " + deviceName + "\n" + "Input stream open...");
				listenOnBtInputStream = true;
			}
			catch (IOException e)
			{
				btConnectionLost("Connection lost...");
				sendBroadcastInfo("Failed to open input stream...");
				listenOnBtInputStream = false;
				return;
			}
		}
	}
	
	/**
	* \brief Read the bluetooth input stream
	*
	* Advanced version with message routing
	*/
	void checkInput()
	{
		byte[] bufferInfo = new byte[3];
		try
		{
			bufferInfo[0] = (byte) mInputStream.read(); // Command
			bufferInfo[1] = (byte) mInputStream.read(); // Target
			bufferInfo[2] = (byte) mInputStream.read(); // Message length
		} 
		catch (IOException e1)
		{
			btConnectionLost("Lost connection...");
			Log.d(TAG,"BtService: BufferInfo read failed");
			return;
		}
		
		byte[] bufferMessage = new byte[(int) bufferInfo[2]];
		try
		{
			mInputStream.read(bufferMessage, 0, (int) bufferInfo[2]);
		} 
		catch (IOException e1) 
		{
			btConnectionLost("Lost connection...");
			Log.d(TAG,"BtService: BufferMessage read failed");
			return;
		}

		Log.d(TAG, "BT Service received from remote.");
		Log.d(TAG, "bufferInfo[0]"+bufferInfo[0]);
		Log.d(TAG, "bufferInfo[1]"+bufferInfo[1]);
		Log.d(TAG, "bufferInfo[2]"+bufferInfo[2]);

		// commands from ADK to this device
		if(Constants.TARGET_BRAIN == bufferInfo[1])
		{
			//handleBrainCommands(bufferInfo, bufferMessage);
		}
		// commands from remote to remote. Never used?
		else if(Constants.TARGET_REMOTE == bufferInfo[1])
		{
			if(Constants.LOG_US_SENSOR_COMMAND == bufferInfo[0])
			{
				Intent usSensors = new Intent(Constants.Broadcast.LogService.Actions.ADK_US_RESPONSE);
				usSensors.putExtra(Constants.Broadcast.LogService.Actions.Intent.BYTES, bufferMessage);
				sendBroadcast(usSensors);
			}
			else if(Constants.LOG_ACC_BRAIN_SENSOR_COMMAND == bufferInfo[0])
			{
				String coords = new String(bufferMessage);
				
				String str = coords;
				String[] splitCoords;
				splitCoords = str.split(":");
				sendBroadcastMessage("Message received: " + String.valueOf(bufferInfo[0]) + "\n" + splitCoords[0] + "\n" + splitCoords[1] + "\n" + splitCoords[2]);
				
				Intent sendToLog = new Intent("brainAccResponse");
				sendToLog.putExtra("coords", coords);
				sendBroadcast(sendToLog);
			}
			else if (Constants.LIFT_FAN_RESPONSE_COMMAND == bufferInfo[0])
			{
				Intent intent = new Intent(
					Constants.Broadcast.LiftFans.RESPONSE);
				intent.putExtra(Constants.Broadcast.LiftFans.STATE,
					bufferMessage[0]);
				sendBroadcast(intent);
				
				String message = new String(bufferMessage);
				sendBroadcastMessage("Message received: " + bufferInfo[0] + "\n" + message);				
			}
						
			//sendCommand(bufferInfo[0], bufferInfo[1], bufferMessage);
		}
		// commands from remote to ADK.
		else if(Constants.TARGET_ADK == bufferInfo[1])
		{
			//broadcastBufferToUSBService(bufferInfo, bufferMessage);
		}
	}

	/**
	* \brief Send data over bluetooth
	*
	*
	* \author Johan Gustafsson
	*/
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

	/**
	* \brief Close bluetooth socket when a connection is lost
	*
	* \author Johan Gustafsson
	*/
	private void btConnectionLost(String message)
	{
		listenOnBtInputStream = false;
		
		if( bluetoothSocketUp ) //ADDED THIS??? check if lost connection does not work
		{
			try
			{
				mmSocket.close();
				bluetoothSocketUp = false;
			}
			catch (IOException e)
			{ 
				
			}
			
			//Send new status to Main
			Intent i = new Intent(BT_CONNECTION_STATUS_RESPONSE);
			i.putExtra(BT_CONNECTION_STATUS, bluetoothSocketUp);
			sendBroadcast(i);
		}

		//Disable transmission
		Intent disableMS = new Intent(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION);
		sendBroadcast(disableMS);
		sendBroadcastInfo(message);
	}

	/**
	* \brief Send a info message as a broadcast
	*
	*
	* \author Johan Gustafsson
	*/
	private void sendBroadcastInfo(String message)
	{
		Intent i = new Intent("printMessage");
		i.putExtra("message", message);
		sendBroadcast(i);
	}
	
	/**
	* \brief Send a text message as a broadcast
	*
	*
	* \author Johan Gustafsson
	*/
	private void sendBroadcastMessage(String message)
	{
		Intent i = new Intent("printMessage");
		i.putExtra("coordinates", message);
		sendBroadcast(i);
	}
	
	/**
	* \brief BtService broadcast receiver
	*
	* \author Johan Gustafsson
	*/
	private final BroadcastReceiver BtRemoteServiceReceiver = new BroadcastReceiver()
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
					sendBroadcastInfo("Found device:\n\n" + device.getName() + "\n" + device.getAddress());
				}
			}

			//When discovery is finished, change the Activity title
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
			{
				//Print list of found devices
				printBluetoothDevicesFound();
			}

			if(action.equalsIgnoreCase("callFunction"))
			{
				if(intent.hasExtra(TOGGLE_BLUETOOTH_STATE))
				{
					toggleBtOnOff();
				}
			
				if(intent.hasExtra(FIND_BLUETOOTH_DEVICES))
				{
					findBluetoothDevices();
				}

				if(intent.hasExtra(CHOOSE_BLUETOOTH_DEVICE))
				{
					chooseFoundBluetoothDevice();
				}
				
				if(intent.hasExtra(DISCONNECT_BLUETOOTH_DEVICE))
				{
					if( bluetoothSocketUp )
						btConnectionLost("Connection lost...");
				}

				if(intent.hasExtra(CONNECT_WITH_BLUETOOTH_DEVICE))
				{
					if( !bluetoothSocketUp && deviceAddress != null)
					{
						try
						{
							connectDevice();
						}
						catch (IOException e)
						{
							sendBroadcastInfo("Failed to connect...");
						}
					}
				}
				
				if(intent.hasExtra(BT_CONNECTION_STATUS_CALL))
				{
					Intent i = new Intent(BT_CONNECTION_STATUS_RESPONSE);
					i.putExtra(BT_CONNECTION_STATUS, bluetoothSocketUp);
					sendBroadcast(i);
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
			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_US_DATA))
			{
				byte[] requestUsAdk = new byte[1];
				requestUsAdk[0] = Constants.TARGET_REMOTE;
				
				if( bluetoothSocketUp )
					sendProtocol(Constants.US_SENSOR_REQ_COMMAND,Constants.TARGET_ADK,requestUsAdk);
			}
			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_ACC_BRAIN_DATA))
			{
				byte[] requestAccBrain = new byte[1];
				requestAccBrain[0] = Constants.TARGET_REMOTE;
				
				if( bluetoothSocketUp )
					sendProtocol(Constants.ACC_BRAIN_SENSOR_REQ_COMMAND,Constants.TARGET_BRAIN,requestAccBrain);
			}
			if(action.equals(Constants.Broadcast.BluetoothService.Actions.SendCommand.REQUEST_STOP_ACC_BRAIN_DATA))
			{
				byte[] requestAccBrain = new byte[1];
				requestAccBrain[0] = Constants.TARGET_REMOTE;
				
				if( bluetoothSocketUp )
					sendProtocol(Constants.ACC_BRAIN_SENSOR_STOP_REQ_COMMAND,Constants.TARGET_BRAIN, requestAccBrain);
			}
			if (action.equals(Constants.Broadcast.LiftFans.REQUEST))
			{
				this.liftFansRequest(intent);
			}
		}

		private void liftFansRequest(Intent intent)
		{
			if (intent.hasExtra(Constants.Broadcast.LiftFans.STATE))
			{
				byte[] requestStatus = new byte[1];
				requestStatus[0] = intent.getByteExtra(
					Constants.Broadcast.LiftFans.STATE,
					Constants.Broadcast.LiftFans.ERROR);
				if (requestStatus[0] != Constants.Broadcast.LiftFans.ERROR)
				{
					sendProtocol(Constants.LIFT_FAN_REQUEST_COMMAND,
						Constants.TARGET_ADK, requestStatus);
				}
			}
		}
	};

	/**
	* \brief Send a Protocol
	*
	*/
	public void sendProtocol(byte command, byte target, byte[] message)
	{
		int messageLength = message.length;
		byte[] dataTransmit = new byte[3 + messageLength];
		dataTransmit[0] = command;
		dataTransmit[1] = target;
		dataTransmit[2] = (byte) messageLength;
		System.arraycopy(message, 0, dataTransmit, 3, messageLength);
		sendData(dataTransmit);
	}
	
	/**
	* \brief Destroy Service
	* 
	* \author Johan Gustafsson
	*/
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "IntentService onDestroy");
		btConnectionLost("Destroy..");
		unregisterReceiver(BtRemoteServiceReceiver);
	}
}
