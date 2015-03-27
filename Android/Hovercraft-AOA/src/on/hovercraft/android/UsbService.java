package on.hovercraft.android;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;
import com.google.protobuf.InvalidProtocolBufferException;

import common.files.android.Command.DriveSignals;
import common.files.android.Command.Engines;

import common.files.android.Constants;
import common.files.android.Constants.ConnectionState;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;
/**
* \brief USB-Service.
* This service controls all USB-communication.
*
* \author Jens Moser
*/
public class UsbService extends IntentService
{	
	public static boolean isActive = false; /**< this is true if the service is up and running */
	private boolean accessoryDetached = false; /**< TODO: true if android accessory is detached */

	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; /**<  USB connection state */

	private static String TAG = "JM"; /**< LogCat TAG */
	private static UsbService singleton; /**< Singleton of this service */

	private final BroadcastReceiver messageReceiver = new myBroadcastReceiver(); /**< BroadcastReceiver */

    private UsbManager mUsbManager = UsbManager.getInstance( this ); /**< UsbManager */
    private UsbAccessory mAccessory; /**< UsbAccessory */
    
	private ParcelFileDescriptor mFileDescriptor; /**< ParcelFileDescriptor */
	private FileOutputStream mOutputStream; /**< FileOutputStream */
	private FileInputStream mInputStream; /**< FileInputStream */
	
	/**
	* \brief Constructor
	*
	* Constructor
	*
	* \author Jens Moser
	*/
	public UsbService()
	{
		super( "UsbService" );
	}

	/**
	* \brief get Service Instance
	*
	* returns Service Instance
	*
	* @return UsbService
	*
	* \author Jens Moser
	*/	
	public static UsbService getInstance() 
	{
		return singleton;
	}
	
	/**
	* \brief Test command
	*
	* Test command. Sends test commands to ADK.
	*
	* \author Jens Moser
	*/
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
	}

	/**
	* \brief Create Protocol buffer engine object
	*
	* Return Engine object
	*
	* @return Engines
	*
	* \author Jens Moser
	*/
	static Engines createEngineProtocol(DriveSignals driveSignalRight, DriveSignals driveSignalLeft) 
	{
		Engines.Builder engines = Engines.newBuilder();
		engines.setRight(driveSignalRight);
		engines.setLeft(driveSignalLeft);		
		return engines.build();
	}
	
	/**
	* \brief Create Protocol buffer DriveSignal object
	*
	* Return DriveSignal object
	*
	* @return DriveSignals
	*
	* \author Jens Moser
	*/
	static DriveSignals createDriveSignalProtocol(boolean forward, boolean enable, int power)
	{
		DriveSignals.Builder driveSignal = DriveSignals.newBuilder();		
		driveSignal.setForward(forward);
		driveSignal.setEnable(enable);
		driveSignal.setPower(power);
		return driveSignal.build();
	}

	/**
	* \brief Called when service is destroyed
	*
	* \author Jens Moser
	*/	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		isActive = false;

		closeAccessory();
		unregisterReceiver(messageReceiver);

		Log.d(TAG, "UsbService destroyed");
	}
	
	/**
	* \brief Tries to reopen accessory if connection is lost.
	*
	* @return NULL
	*
	* \author Jens Moser
	*/
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
	
	/**
	* \brief Open accessory connection.
	*
	* \author Jens Moser
	*/
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
	
	/**
	* \brief Close accessory connection.
	*
	* \author Jens Moser
	*/
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

	/**
	* \brief Update USB connection state.
	*
	* Broadcast the new connection state. Broadcast received by MainActivity.
	*
	* @param state New connection state
	*
	* \author Jens Moser
	*/
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
	
	/**
	* \brief Send USB Commands
	*
	* @param command Command to be sent
	* @param target Target
	* @param message Message to be sent	
	*
	* \author Jens Moser
	*/
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

	/**
	* \brief Send Byte array over USB
	*
	* @param byteArray byteArray to be sent
	*
	* \author Jens Moser
	*/	
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

	/**
	* \brief Service main function
	*
	* @param intent The value passed to startService(Intent).
	*
	* \author Jens Moser
	*/	
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		Log.d(TAG,"UsbService started");
		
		singleton = this;
		isActive = true;
		
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
	
	/**
	* \brief Check USB-input and route it to the appropriate target.
	*
	* \author Jens Moser
	*/	
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

				Log.d(TAG, "USB service msg from ADK");
				byte[] bufferPB = new byte[buffer[2]];
				i = 0;
				while(i < buffer[2])
				{
					bufferPB[i] = buffer[i+3];
					Log.d(TAG, "bufferPB["+i+"]"+bufferPB[i]);
					
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

				if(bufferInfo[0] == Constants.LIFT_FAN_RESPONSE_COMMAND)
				{
					Log.d("LF", "USBS Lift fans response received from ADK");
				}

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
	
	/**
	* \brief This function handle commands sent to this phone (router).
	* Router was called brain earlier.
	*
	* @param bufferInfo Target
	* @param bufferPB PB-bytearray
	* @param combinedInfoAndPB Target and message combined
	*
	* \author Jens Moser
	*/	
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
			Log.d(TAG, "FAIL!!!!, missing shit");
//			try
//			{
//				SensorData sensorData = SensorData.parseFrom(bufferPB);
//				Log.d(TAG,"PB parse success");
//				Log.d(TAG,"sensorData desc: "+sensorData.getDescription());
//			}
//			catch (IOException e) 
//			{
//				Log.d(TAG,"PB parse failed");
//				break;
//			}
			break;
		default:
			Log.d(TAG, "unknown command: " + bufferInfo[0]);
			break;
		}		
	}
	
	/**
	* \brief Broadcast buffer over to BTService.
	* Buffer will be sent to Remote.
	*
	* @param combinedInfoAndPB Byte array Buffer to be sent
	* 
	* \author Jens Moser
	*/
	private void broadcastBufferToBTService(byte[] combinedInfoAndPB)
	{
		Intent intent = new Intent("callFunction");
		intent.putExtra("sendToRemote", "sendToRemote");
		intent.putExtra("combinedInfoAndPB", combinedInfoAndPB);
		sendBroadcast(intent);		
	}
	
	/**
	* \brief Setup broadcast filters.
	* This service will listen on specified filters.
	*
	* \author Jens Moser
	*/
	private void setupBroadcastFilters() 
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction(Constants.Broadcast.MotorSignals.CONTROL_SYSTEM);
		filter.addAction("sendADKTestCommand");	
		filter.addAction("handleBTCommands");	
		registerReceiver(messageReceiver, filter);
	}

	/**
	* \brief Broadcast recevier
	*
	* \author Jens Moser
	*/	
	public class myBroadcastReceiver extends BroadcastReceiver 
	{
		/**
		* \brief onReceive
		* This function will execute when broadcast is received.
		*
		* @param intent Information sent with the broadcast
		*
		* \author Jens Moser
		*/
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
			else if (action.equalsIgnoreCase("handleBTCommands"))
			{
				// Skicka vidare till USB
				byte[] bufferInfo = intent.getByteArrayExtra("bufferInfo");
				byte[] bufferMessage = intent.getByteArrayExtra("bufferMessage");
				if(bufferInfo[0] == Constants.LIFT_FAN_REQUEST_COMMAND)
				{
					Log.d("LF", "USBS Lift fans command received from remote.");
				}
				sendCommand(bufferInfo[0], Constants.TARGET_ADK, bufferMessage);
				
				//String blinky = "blinky";
				//sendCommand(Constants.BLINKY_ON_COMMAND, Constants.TARGET_ADK, blinky.getBytes());
			}
			else if ((Constants.Broadcast.MotorSignals.CONTROL_SYSTEM).equals(action))
			{
				byte bufferCommand = intent.getByteExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND, (byte)0);
				byte[] bufferMessage = intent.getByteArrayExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES);
				byte target = intent.getByteExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET, (byte)0x0);
				sendCommand(bufferCommand, target, bufferMessage);
			}
		}
	}
}