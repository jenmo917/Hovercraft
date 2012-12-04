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
	private static final UUID  MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static final String NAME = "Bluetooth SPP";	
	
	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; // BT connection state	
	
	private boolean listenOnBtInputstream = false;	
	private BluetoothSocket bluetoothSocket;
	private BluetoothServerSocket bluetoothServerSocket;
	private BluetoothAdapter mBluetoothAdapter;
	private InputStream btInputStream;
	private OutputStream btOutStream;
	private int bluetoothConnectionTimeout = 5000;
	private boolean bluetoothServerUp = false;
	private boolean bluetoothSocketUp = false;	
	
	private SensorManager sensorManager;
	private double accX;
	private double accY;
	private double accZ;
	
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
	
	private void updateBtConnectionState(ConnectionState state)
	{
		if(connectionState != state)
		{
			connectionState = state;
			Intent i = new Intent(Constants.Broadcast.BluetoothService.UPDATE_CONNECTION_STATE);
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
			if( listenOnBtInputstream )
			{
				checkInput();
			}
		}
	}

	private void btConnectionLost(String message)
	{
		listenOnBtInputstream = false;
		closeServerSocket();

		if(btInputStream != null)
		{
			try 
			{
				btInputStream.close();
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
		
		broadcastMessage(message);
		updateBtConnectionState(ConnectionState.DISCONNECTED);
	}
	
	private void closeServerSocket()
	{
		if( bluetoothServerUp )
		{
			try 
			{
				bluetoothServerSocket.close();
				broadcastMessage("Server down...");
				bluetoothServerUp = false;
				bluetoothSocketUp = false;
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
			bluetoothServerUp = true;
			updateBtConnectionState(ConnectionState.WAITING);
		} 
		catch (IOException e) 
		{ 
			btConnectionLost("Failed to create setup server...");
			bluetoothServerUp = false;
		}

		bluetoothServerSocket = tmp;
		return bluetoothServerUp;
	}

	private boolean waitToConnect()
	{
		if(bluetoothServerUp)
		{
			bluetoothSocket = null;
			try 
			{
				bluetoothSocket = bluetoothServerSocket.accept(bluetoothConnectionTimeout);
				bluetoothSocketUp = true;
				broadcastMessage("Socket is up..");
				updateBtConnectionState(ConnectionState.CONNECTED);
				
				listen();
			} 
			catch (IOException e) 
			{
				btConnectionLost("Failed to connect...");
			}
		}
		else
		{
			bluetoothSocketUp = false;
		}

		return bluetoothSocketUp;      
	}

	@SuppressLint("HandlerLeak")

	private void listen()
	{
		if( bluetoothSocketUp )
		{
			try
			{
				btInputStream = bluetoothSocket.getInputStream();
				broadcastMessage("Input stream open...");
				listenOnBtInputstream = true;
			}
			catch (IOException e)
			{
				btConnectionLost("Failed to open input stream...");
				listenOnBtInputstream = false;
				return;
			}
		}
	}

	void checkInput()
	{
		byte[] bufferInfo = new byte[3];		
		try
		{
			bufferInfo[0] = (byte) btInputStream.read(); // Command
			bufferInfo[1] = (byte) btInputStream.read(); // Target
			bufferInfo[2] = (byte) btInputStream.read(); // Message length
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
			btInputStream.read(bufferMessage, 0, (int) bufferInfo[2]);
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

	private void handleBrainCommands(byte[] bufferInfo, byte[] bufferMessage)
	{
		Log.d(TAG,"BtService: handleBrainCommand");
		switch (bufferInfo[0])
		{
			case Constants.MOTOR_SIGNAL_COMMAND:
				
				Intent intent = new Intent(Constants.Broadcast.MotorSignals.REMOTE);
				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND, bufferInfo[0]);
				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES, bufferMessage);
				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET, bufferInfo[1]);
				sendBroadcast(intent);
				break;
				
			default:
			Log.d(TAG, "unknown command: " + bufferInfo[0]);
			break;
		}	
	}

	private void sendData(byte[] data)
	{	
		try 
		{
			btOutStream = bluetoothSocket.getOutputStream();
		} 
		catch (IOException e) 
		{
			btConnectionLost("Lost connection...");
		}

		try 
		{
			btOutStream.write(data);
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
			btOutStream = bluetoothSocket.getOutputStream();
		} 
		catch (IOException e) 
		{
			btConnectionLost("Lost connection...");
		}		
		
		if (btOutStream != null)
		{

			try 
			{
				btOutStream.write(buffer);
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
					if( !bluetoothServerUp )
						setupServer();
				}	

				if(intent.hasExtra("waitToConnect"))
				{
					if( !bluetoothSocketUp )
						waitToConnect();
				}		
			}
		}
	};
}
