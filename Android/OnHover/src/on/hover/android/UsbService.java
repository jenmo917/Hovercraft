package on.hover.android;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

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
	public static boolean isActive = false; // this is true if the service is up and running
	private boolean accessoryDetached = false; // TODO: true if android accessory is detached
	
	public static enum ConnectionState 
	{
	    CONNECTED, WAITING, DISCONNECTED;
	}
	
	public static ConnectionState connectionState = ConnectionState.DISCONNECTED; // USB connection state

    private static String TAG = "JMMainActivity";
    private static UsbService singleton;
	private static final byte TOGGLE_LED_COMMAND = 15;
	
	private UsbManager mUsbManager = UsbManager.getInstance(this);
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mFileDescriptor;
	private FileOutputStream mOutputStream;
	private BroadcastReceiver mReceiver;		
	
	public UsbService() 
	{
		super("IntentTestService");
	}

    public static UsbService getInstance() 
    {
        return singleton;
    }
    
    public void sendString()
    {    	   
    	sendCommand(TOGGLE_LED_COMMAND);
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
        Log.d(TAG, "FirstService destroyed");
    }

	private void setupDetachingAccessoryHandler() 
	{
		mReceiver = new BroadcastReceiver() 
		{
			@Override
			public void onReceive(Context context, Intent intent) 
			{
				String action = intent.getAction();
				if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action))
				{
					closeAccessory();
				}
			}
		};
		IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mReceiver, filter);
	}

	private void reOpenAccessoryIfNecessary(Intent intent)
	{
		connectionState = ConnectionState.WAITING;
		if (mOutputStream != null)
		{
			connectionState = ConnectionState.CONNECTED;
			return;
		}
		
		String action = intent.getAction();
		if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) 
		{
				mAccessory = UsbManager.getAccessory(intent);
				openAccessory();
		}
		// Update connection state in our view	
    	Intent i = new Intent("updateUSBConnectionState");
    	sendBroadcast(i);
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
				if(connectionState != ConnectionState.CONNECTED)
				{
					connectionState = ConnectionState.CONNECTED;
		    		Intent i = new Intent("updateUSBConnectionState");
		    		sendBroadcast(i);
				}
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
		
		// Update connection state in our view
		if(connectionState != ConnectionState.DISCONNECTED)
		{
			connectionState = ConnectionState.DISCONNECTED;
	    	Intent i = new Intent("updateUSBConnectionState");
	    	sendBroadcast(i);			
		}
	}

	private void sendCommand(byte command)
	{
		if (mOutputStream != null)
		{
			try
			{
				mOutputStream.write(command);
			}
			catch (IOException e) 
			{
				// Do nothing
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
        
    	setupDetachingAccessoryHandler();
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
        	
			// try to send data to arduino
        	sendCommand(TOGGLE_LED_COMMAND);

        	try
        	{
				Thread.sleep(2000);
			} 
        	catch (InterruptedException e)
			{
				e.printStackTrace();
			}  
        }
        
		closeAccessory();
		unregisterReceiver(mReceiver);
        
	}
}