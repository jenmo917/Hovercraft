package on.hovercraft.android;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.google.protobuf.InvalidProtocolBufferException;

import on.hovercraft.android.Command.DriveSignals;
import on.hovercraft.android.Command.Engines;
import on.hovercraft.android.Command.SensorData;
import common.files.android.Constants;
import common.files.android.Constants.ConnectionState;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class UsbService extends IntentService
{	
	public static boolean isActive = false; // this is true if the service is up and running
	private boolean accessoryDetached = false; // TODO: true if android accessory is detached

	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; // USB connection state

	private static String TAG = "JM";
	private static UsbService singleton;

	private final BroadcastReceiver messageReceiver = new myBroadcastReceiver();	

    private UsbManager mUsbManager = UsbManager.getInstance(this);
    private UsbAccessory mAccessory;
    
	private ParcelFileDescriptor mFileDescriptor;
	private FileOutputStream mOutputStream;
	private FileInputStream mInputStream;

	public UsbService()
	{
		super("UsbService");
	}

	public static UsbService getInstance() 
	{
		return singleton;
	}

	public void sendADKTestCommand()
	{
		Random generator = new Random();
		int power1 = generator.nextInt(255);
		int power2 = generator.nextInt(255);

		// try to send data to arduino
		DriveSignals driveSignalLeft = createDriveSignalProtocol(true,true,power1);
		DriveSignals driveSignalRight = createDriveSignalProtocol(true,true,power2);
		Engines engine = createEngineProtocol(driveSignalLeft,driveSignalRight); 
		byte[] message = engine.toByteArray();
		int byteLength = message.length;
		for (int x = 0; x < byteLength; x++) 
		{
			Log.d(TAG,""+message[x]);
		}
		sendCommand(Constants.MOTOR_CONTROL_COMMAND,Constants.TARGET_ADK, message);
		Log.d(TAG,"Send engine command");

		//    	byte[] message = new byte[1];
		//    	message[0] = Constants.TARGET_BRAIN;
		//    	sendCommand(Constants.I2C_SENSOR_REQ_COMMAND,Constants.TARGET_ADK, message);
	}



	static Engines createEngineProtocol(DriveSignals driveSignalRight, DriveSignals driveSignalLeft) 
	{
		Engines.Builder engines = Engines.newBuilder();
		engines.setRight(driveSignalRight);
		engines.setLeft(driveSignalLeft);		
		return engines.build();
	}

	static DriveSignals createDriveSignalProtocol(boolean forward, boolean enable, int power)
	{
		DriveSignals.Builder driveSignal = DriveSignals.newBuilder();		
		driveSignal.setForward(forward);
		driveSignal.setEnable(enable);
		driveSignal.setPower(power);
		return driveSignal.build();
	}

	static SensorData createSensorDataProtocol(String type, String desc, int address, int value)
	{
		SensorData.Builder sensorData = SensorData.newBuilder();
		sensorData.setType(type);
		sensorData.setDescription(desc);
		sensorData.setAddress(address);
		sensorData.setValue(value);
		return sensorData.build();
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "UsbService started");
		singleton = this;
		isActive = true;
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		isActive = false;

		closeAccessory();
		unregisterReceiver(messageReceiver);

		Log.d(TAG, "UsbService destroyed");
	}

	private void reOpenAccessoryIfNecessary(Intent intent)
	{
		updateConnectionState(ConnectionState.WAITING);
		if (mOutputStream != null)
		{
			updateConnectionState(ConnectionState.CONNECTED);
			return;
		}

		String action = intent.getAction();
		if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) 
		{
			mAccessory = UsbManager.getAccessory(intent);
			openAccessory();
			return;
		}
		updateConnectionState(ConnectionState.DISCONNECTED);
	}

	private void openAccessory()
	{
		try
		{
			mFileDescriptor = mUsbManager.openAccessory(mAccessory);
			if (mFileDescriptor != null) 
			{
				FileDescriptor fd = mFileDescriptor.getFileDescriptor();
				mOutputStream = new FileOutputStream(fd);
				mInputStream = new FileInputStream(fd);
				Log.d(TAG, "mFileDesc != null");

				// Update connection state in our view
				updateConnectionState(ConnectionState.CONNECTED);
			}
			else
			{
				updateConnectionState(ConnectionState.DISCONNECTED);
			}
		}
		catch (IllegalArgumentException ex) 
		{
			// Accessory detached while activity was inactive
			closeAccessory();
		}
	}

	private void closeAccessory() 
	{
		try 
		{
			if (mOutputStream != null) 
			{
				mInputStream.close();
				mOutputStream.close();
			}
			if (mFileDescriptor != null)
			{
				mFileDescriptor.close();
			}
			updateConnectionState(ConnectionState.DISCONNECTED);
		} 
		catch (IOException e) 
		{
			
		} 
		finally 
		{
			mInputStream = null;
			mOutputStream = null;
			mFileDescriptor = null;
			mAccessory = null;
		}
		updateConnectionState(ConnectionState.DISCONNECTED);
	}

	private void updateConnectionState(ConnectionState state)
	{
		if(connectionState != state)
		{
			connectionState = state;
			Intent i = new Intent("updateUSBConnectionState");
			i.putExtra("connectionState", state);
			sendBroadcast(i);
		}
	}

	private void sendCommand(byte command, byte target, byte[] message)
	{
		Log.d(TAG,"SendCommand:" + (int) command);
		byte byteLength = (byte) message.length;
		byte[] buffer = new byte[3+byteLength];

		buffer[0] = command; // command
		buffer[1] = target; // target
		buffer[2] = byteLength; // length

		for (int x = 0; x < byteLength; x++) 
		{
			buffer[3 + x] = message[x]; // message
			//Log.d(TAG,""+message[x]);
		}

		Log.d(TAG,"byteLength:"+byteLength);

		if (mOutputStream != null)
		{
			try
			{
				mOutputStream.write(buffer);
			}
			catch (IOException e) 
			{
				Log.e(TAG, "write failed", e);
			}
		} 
		else
		{
			closeAccessory();
		}
	}

	private void sendByteArray(byte[] byteArray)
	{
		Log.d(TAG,"SendByteArray");
		if (mOutputStream != null)
		{
			try
			{
				mOutputStream.write(byteArray);
				Log.d(TAG, "write byteArray to outPutStream");
			}
			catch (IOException e) 
			{
				Log.e(TAG, "SendByteArray failed", e);
			}
		} 
		else
		{
			closeAccessory();
		}
	}	

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		Log.w(TAG,"onHandleIntent entered");
		UsbAccessory accessory = UsbManager.getAccessory(intent);

		setupBroadcastFilters();
		reOpenAccessoryIfNecessary(intent);

		if (accessory != null)
		{
			Log.d(TAG, "Got accessory: " + accessory.getModel());
		}

		while(true) 
		{
			if (accessoryDetached) 
			{
				break;
			}
			checkInput();
			try 
			{
				Thread.sleep(10);
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
	}

	public void checkInput() 
	{
		if(mInputStream != null) 
		{
			int ret = 0;
			byte[] buffer = new byte[255];

			while (ret >= 0) 
			{
				try 
				{
					ret = (mInputStream.read(buffer));
				} 
				catch (IOException e) 
				{
					break;
				}

				byte[] bufferInfo = new byte[3];
				int i = 0;
				while(i < 3)
				{
					bufferInfo[i] = buffer[i];
					i++;
				}		

				byte[] bufferPB = new byte[buffer[2]];
				i = 0;
				while(i < buffer[2])
				{
					bufferPB[i] = buffer[i+3];
					i++;
				}				

				byte[] combinedInfoAndPB = new byte[bufferInfo.length + bufferPB.length];
				i = 0;
				while(i < combinedInfoAndPB.length)
				{
					combinedInfoAndPB[i] = i < bufferInfo.length ? bufferInfo[i] : bufferPB[i - bufferInfo.length];
					i++;
				}

				Log.d(TAG, "bufferInfo[0]"+bufferInfo[0]);
				Log.d(TAG, "bufferInfo[1]"+bufferInfo[1]);
				Log.d(TAG, "bufferInfo[2]"+bufferInfo[2]);

				// commands from ADK to this device
				if(Constants.TARGET_BRAIN == bufferInfo[1])
				{
					handleBrainCommands(bufferInfo, bufferPB, combinedInfoAndPB);
				}
				// commands from ADK to remote
				else if(Constants.TARGET_REMOTE == bufferInfo[1])
				{
					broadcastBufferToBTService(combinedInfoAndPB);
				}
				// commands from ADK back to ADK. Never used?
				else if(Constants.TARGET_ADK == bufferInfo[1])
				{
					sendByteArray(combinedInfoAndPB);
				}
			}
		}
	}	

	private void handleBrainCommands(byte[] bufferInfo, byte[] bufferPB, byte[] combinedInfoAndPB)
	{
		switch (bufferInfo[0])
		{
		case Constants.I2C_SENSOR_COMMAND:
			Log.d(TAG,"I2C_SENSOR_COMMAND RECEIVED!");
			break;
		case Constants.US_SENSOR_COMMAND:
			Log.d(TAG,"US_SENSOR_COMMAND RECEIVED!");
			break;
		case 5:
			break;
		case 6:
			Log.d(TAG, "brain command received: " + bufferInfo[0]);
			try
			{
				SensorData sensorData = SensorData.parseFrom(bufferPB);
				Log.d(TAG,"PB parse success");
				Log.d(TAG,"sensorData desc: "+sensorData.getDescription());
			}
			catch (InvalidProtocolBufferException e) 
			{
				e.printStackTrace();
				Log.d(TAG,"PB parse failed");
				break;
			}
			break;
		default:
			Log.d(TAG, "unknown command: " + bufferInfo[0]);
			break;
		}		
	}

	private void broadcastBufferToBTService(byte[] combinedInfoAndPB)
	{
		Intent intent = new Intent("callFunction");
		intent.putExtra("combinedInfoAndPB", combinedInfoAndPB);
		sendBroadcast(intent);		
	}

	private void setupBroadcastFilters() 
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction("sendADKTestCommand");	
		filter.addAction("sendBlinkyOnCommand");
		filter.addAction("sendBlinkyOffCommand");
		registerReceiver(messageReceiver, filter);
	}

	public class myBroadcastReceiver extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{    
			String action = intent.getAction();
			if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
			{
				accessoryDetached = true;
			}
			else if (action.equalsIgnoreCase("sendADKTestCommand"))
			{
				sendADKTestCommand();
			}
			else if (action.equalsIgnoreCase("sendBlinkyOnCommand"))
			{

				String test = "message";
				sendCommand(Constants.BLINKY_ON_COMMAND, Constants.TARGET_ADK, test.getBytes());
			}
			else if (action.equalsIgnoreCase("sendBlinkyOffCommand"))
			{
				String test = "message";
				sendCommand(Constants.BLINKY_OFF_COMMAND, Constants.TARGET_ADK, test.getBytes());
			}
		}
	}
}