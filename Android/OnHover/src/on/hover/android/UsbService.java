package on.hover.android;

import java.util.Random;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

import on.hover.android.Command.DriveSignals;
import on.hover.android.Command.Engines;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class UsbService extends IntentService
{
	Random generator = new Random();
	
	public static boolean isActive = false; // this is true if the service is up and running
	private boolean accessoryDetached = false; // TODO: true if android accessory is detached
	
	public static enum ConnectionState 
	{
	    CONNECTED, WAITING, DISCONNECTED;
	}
	
	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; // USB connection state

    private static String TAG = "JMMainActivity";
    private static UsbService singleton;
	private static final byte MOTOR_CONTROL_COMMAND = 0x3;
	private static final byte TARGET_ADK = 0x1;

	private final BroadcastReceiver messageReceiver = new newMessage();	
	
	private UsbManager mUsbManager = UsbManager.getInstance(this);
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileOutputStream mOutputStream;
	
	public UsbService() 
	{
		super("UsbService");
	}

    public static UsbService getInstance() 
    {
        return singleton;
    }
    
    public void sendString()
    {    	   
		// try to send data to arduino
    	DriveSignals driveSignalLeft = createDriveSignalProtocol(true,true,20);
    	DriveSignals driveSignalRight = createDriveSignalProtocol(true,true,190);
		Engines engine = createEngineProtocol(driveSignalLeft,driveSignalRight); 
		byte[] message = engine.toByteArray();
		int byteLength = message.length;
		for (int x = 0; x < byteLength; x++) 
		{
			Log.d(TAG,""+message[x]);
		}
    	sendCommand(MOTOR_CONTROL_COMMAND,TARGET_ADK, message);
    	Log.d(TAG,"Send engine command");
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
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "FirstService started");
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
		
        Log.d(TAG, "FirstService destroyed");
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
		int byteLength = message.length;
		byte[] buffer = new byte[3+byteLength];
		
		buffer[0] = command; // command
		buffer[1] = target; // target
		buffer[2] = (byte) byteLength; // length

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
        	// TODO: Not implemented
        	if (accessoryDetached) 
        	{
        		break;
        	}
        	
        	try
        	{
				Thread.sleep(2000);
			} 
        	catch (InterruptedException e)
			{
				e.printStackTrace();
			}  
        }
        onDestroy();
	}
	
	private void setupBroadcastFilters() 
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		filter.addAction("lala");
		filter.addAction("sendString");		
		registerReceiver(messageReceiver, filter);
	}
	
	public class newMessage extends BroadcastReceiver 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{    
			String action = intent.getAction();
			if(action.equalsIgnoreCase("lala"))
			{
				Log.d(TAG,"lalalalal");
			}
			else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
			{
				closeAccessory();
			}
			else if (action.equalsIgnoreCase("sendString"))
			{
				sendString();
			}			
		}
	}	
}