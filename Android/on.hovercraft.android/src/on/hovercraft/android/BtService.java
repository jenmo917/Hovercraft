package on.hovercraft.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import common.files.android.Constants;
import common.files.android.Constants.ConnectionState;

import android.annotation.SuppressLint;
import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class BtService extends IntentService implements SensorEventListener
{
	private InputStream mInputStream;
	private OutputStream mmOutStream;
	
	private static final UUID  MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String NAME = "Bluetooth SPP";	
	
	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; // BT connection state	
	
	private boolean listenBT = false;	
	
	private BluetoothSocket mmSocket;
	private BluetoothServerSocket mmServerSocket;
	private BluetoothAdapter mBluetoothAdapter;
	
	private SensorManager sensorManager;
	private double accX;
	private double accY;
	private double accZ;
	 
	private int timeout = 5000;
	private boolean serverUp = false;
	private boolean socketUp = false;	
	
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
		
        sensorManager=(SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private void updateConnectionState(ConnectionState state)
	{
		if(connectionState != state)
		{
			connectionState = state;
			Intent i = new Intent(Constants.Broadcast.UsbService.UPDATE_CONNECTION_STATE);
			i.putExtra("connectionState", state);
			sendBroadcast(i);
		}
	}	

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) 
	{

	}

	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		if (event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
		{
			accX = event.values[0];
			accY = event.values[1];
			accZ = event.values[2];
		}		
	}	

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
				checkInput();
			}
		}
	}

	private void btConnectionLost(String message)
	{
		listenBT = false;
		closeServerSocket();

		if(mInputStream != null)
		{
			try 
			{
				mInputStream.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		broadcastMessage(message);
		updateConnectionState(ConnectionState.DISCONNECTED);
	}
	
	private void closeServerSocket()
	{
		if(serverUp)
		{
			try 
			{
				mmServerSocket.close();
				broadcastMessage("Server down...");
				serverUp = false;
			} 
			catch (IOException e) 
			{	
				broadcastMessage("Faild to close server...");
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
			broadcastMessage("Server up...");
			serverUp = true;
			
		} 
		catch (IOException e) 
		{ 
			btConnectionLost("Failed to create setup server...");
			serverUp = false;
		}

		mmServerSocket = tmp;
		return serverUp;
	}

	private boolean waitToConnect()
	{
		updateConnectionState(ConnectionState.WAITING);
		if(serverUp)
		{
			mmSocket = null;
			try 
			{
				mmSocket = mmServerSocket.accept(timeout);
				
				socketUp = true;
				broadcastMessage("Socket is up..");
				updateConnectionState(ConnectionState.CONNECTED);
				
				listen();
			} 
			catch (IOException e) 
			{
				btConnectionLost("Failed to connect...");
			}
		}
		else
		{
			socketUp = false;
		}

		return socketUp;      
	}

	@SuppressLint("HandlerLeak")

	private void listen()
	{
		if(socketUp)
		{
			try
			{
				mInputStream = mmSocket.getInputStream();
				broadcastMessage("Input stream open...");
				listenBT = true;
			}
			catch (IOException e)
			{
				btConnectionLost("Failed to open input stream...");
				listenBT = false;
				return;
			}
		}
	}

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

		Log.d(TAG, "bufferInfo[0]"+bufferInfo[0]);
		Log.d(TAG, "bufferInfo[1]"+bufferInfo[1]);
		Log.d(TAG, "bufferInfo[2]"+bufferInfo[2]);

		// commands from ADK to this device
		if(Constants.TARGET_BRAIN == bufferInfo[1])
		{
			handleBrainCommands(bufferInfo, bufferMessage);
		}
		// commands from remote to remote. Never used?
		else if(Constants.TARGET_REMOTE == bufferInfo[1])
		{
			sendCommand(bufferInfo[0], bufferInfo[1], bufferMessage);
		}
		// commands from remote to ADK.
		else if(Constants.TARGET_ADK == bufferInfo[1])
		{
			broadcastBufferToUSBService(bufferInfo, bufferMessage);
		}
	}
	
	private void broadcastBufferToUSBService(byte[] bufferInfo, byte[] bufferMessage)
	{
		Log.d(TAG,"BtService: broadcast command to USB Service");
		Intent intent = new Intent("handleBTCommands");
		intent.putExtra("bufferInfo", bufferInfo);
		intent.putExtra("bufferMessage", bufferMessage);
		sendBroadcast(intent);
	}

	int  test = 0;
	private void handleBrainCommands(byte[] bufferInfo, byte[] bufferMessage)
	{
		Log.d(TAG,"BtService: handleBrainCommand");
		switch (bufferInfo[0])
		{
			case Constants.MOTOR_SIGNAL_COMMAND:
				
				tempTestSend(bufferInfo[0]);
								
				break;
				
			default:
			Log.d(TAG, "unknown command: " + bufferInfo[0]);
			break;
		}	
	}

	void tempTestSend(byte bufferInfo)
	{
		String received = "$recevied: " + String.valueOf(bufferInfo) + "\n" 
							+ String.valueOf(test) + " times$";
		sendData(received.getBytes());
		test++;
	}
	
	
	private void sendData(byte[] data)
	{	
		try 
		{
			mmOutStream = mmSocket.getOutputStream();
		} 
		catch (IOException e) 
		{
			btConnectionLost("Lost connection...");
		}

		try 
		{
			mmOutStream.write(data);
		} 
		catch (IOException e) 
		{
			btConnectionLost("Lost connection...");
		}
	}

	private void sendCommand(byte command, byte target, byte[] message)
	{
		Log.d(TAG,"BtService: SendCommand:" + (int) command);
		int byteLength = message.length;
		byte[] buffer = new byte[3+byteLength];

		buffer[0] = command; // command
		buffer[1] = target; // target
		buffer[2] = (byte) byteLength; // length

		for (int x = 0; x < byteLength; x++) 
		{
			buffer[3 + x] = message[x]; // message
		}

		Log.d(TAG,"byteLength:"+byteLength);
		
		try 
		{
			mmOutStream = mmSocket.getOutputStream();
		} 
		catch (IOException e) 
		{
			btConnectionLost("Lost connection...");
		}		
		
		if (mmOutStream != null)
		{
			
			try 
			{
				mmOutStream.write(buffer);
			} 
			catch (IOException e) 
			{
				btConnectionLost("Lost connection...");
				Log.e(TAG, "write failed", e);
			}
		}
	}	

	private void broadcastMessage(String message)
	{
		Intent i = new Intent("printMessage");
		i.putExtra("message", message);
		sendBroadcast(i);
	}
	
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
