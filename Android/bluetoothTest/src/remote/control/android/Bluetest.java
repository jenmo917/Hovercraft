package remote.control.android;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import protocolbufferjava.Test.Protocol;

public class Bluetest extends Activity implements SensorEventListener
{

	protected static final int REQUEST_ENABLE_BT = 1;

	String Dev;
	String adress = null;
	
	Button buttonToggleBT;
	
	Button buttonFind;
	Button buttonPair;
	Button buttonChoose;
	Button buttonTest;
	Button buttonUp;
	Button buttonDown;
	Button buttonTest1;
	Button buttonTest2;
	
	boolean sendCoordinates = false;
	Button buttonSendCoor;
	
	TextView xCoordinate;
	TextView yCoordinate;
	TextView zCoordinate;
	TextView infoText;
	
	int length = 0;
	int i = 0;
	

	public List<String> devicesFound = new ArrayList<String>();

	private SensorManager sensorManager;
	
	//**Necesse est
	public void onAccuracyChanged(Sensor sensor,int accuracy)
	{

	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_bluetest);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		
		buttonToggleBT = (Button) findViewById(R.id.btn_toggleBT);
		buttonFind = (Button) findViewById(R.id.btn_find);
		buttonPair = (Button) findViewById(R.id.btn_pair);
		buttonChoose = (Button) findViewById(R.id.btn_choose);
		
		buttonUp = (Button) findViewById(R.id.btn_up);
		buttonDown = (Button) findViewById(R.id.btn_down);
		buttonTest1 = (Button) findViewById(R.id.btn_test1);
		buttonTest2 = (Button) findViewById(R.id.btn_test2);
		
		buttonSendCoor = ( Button) findViewById(R.id.btn_sendCoor);
		
		xCoordinate = ( TextView ) findViewById(R.id.textX);
		yCoordinate = ( TextView ) findViewById(R.id.textY);
		zCoordinate = ( TextView ) findViewById(R.id.textZ);
	
		
		sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
		// add listener. The listener will be HelloAndroid (this) class
		sensorManager.registerListener(	this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
										SensorManager.SENSOR_DELAY_NORMAL);
		
		
		infoText = (TextView) findViewById(R.id.text_View);
		
		//Click connect button
		buttonToggleBT.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				if( bluetooth != null && bluetooth.getState() != BluetoothAdapter.STATE_ON)
				{
				    //bluetooth.enable();
					Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				    buttonToggleBT.setText(R.string.btON);	
				}
				
				else if (bluetooth != null && bluetooth.getState() != BluetoothAdapter.STATE_OFF)
				{
					bluetooth.disable();
					buttonToggleBT.setText(R.string.btOFF);	
				}
			}
		});


		//Search button
		buttonFind.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				//Clear list of devices
				devicesFound.clear();
				
				if(bluetooth.startDiscovery())	
				{
					infoText.setText("Searching for devices...");
				}
				else
				{
					infoText.setText("Failed to search...");
				}	
			}
		});

		buttonPair.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v)
			{
				try 
				{
					connectDevice();
				} 
				catch (IOException e) 
				{
					
				}
			}
		});
		
		buttonSendCoor.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				if(!sendCoordinates && mmSocketUp)
				{
					sendCoordinates = true;
					buttonSendCoor.setText(R.string.btnSendCoor);	
				}
				else
				{
					sendCoordinates = false;
					buttonSendCoor.setText(R.string.btnSendCoorNot);
					infoText.setText("Socket not up...");
				}
			}
		});
		

		buttonChoose.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				length = devicesFound.size(); 
				
				if( length > 0 )
				{
					
					if( i < ( length - 2 ) )
					{
						//save adress
						adress = devicesFound.get( 1 + i );
						
						infoText.setText("Selected device:" + "\n\n" + devicesFound.get(0 +  i ) 
									     + "\n" + devicesFound.get(1 + i ));
						
						i += 2;
					}
					else if (i == (length - 2 ) )
					{
						//save adress
						adress = devicesFound.get( 1 + i );
						
						infoText.setText("Selected device:" + "\n\n" + devicesFound.get(0  + i ) 
								 		 + "\n" + devicesFound.get(1 + i ));
						
						i = 0;
					}
					else
					{
						i = 0;
					}
				}
				else
					infoText.setText("No devices found");
			}
		});
		
		
		buttonUp.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				String up = "$up$";
				sendData(up.getBytes());
			}
		});
		
		buttonDown.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				String down = "$down$";			
				sendData(down.getBytes());
			}
		});
		

		buttonTest1.setOnClickListener(new OnClickListener() 
		{
			
			@Override
			public void onClick(View v) 
			{
				byte[] test = new byte[1024];
				test = coords.toByteArray();
				String coordsString = new String(test);
				coordsString = coordsString + "$";
				
				try
				{
					Protocol testProt = protocolbufferjava.Test.Protocol.parseFrom(test);
					infoText.setText("Parse from get x: " + testProt.getXCoor() + "\n" + ">" + coordsString + "<");
				}
				catch (IOException e) 
				{
					
				}
				if(sendCoordinates)
					sendData(coordsString.getBytes());
			}
		});
		
		buttonTest2.setOnClickListener(new OnClickListener() 
		{
			@Override
			public void onClick(View v) 
			{
				startReceiveing();
			}
		});
		
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

		// Register for broadcasts when discovery has finished
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);  
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) 
	{
		getMenuInflater().inflate(R.menu.activity_bluetest, menu);
		return true;
	}
	
	
	void printDevicesFound()
	{
		//bluetooth.cancelDiscovery();
		
		infoText.setText("Devices found:" + "\n\n");
		
		Iterator<String> it = devicesFound.iterator();
		
        while(it.hasNext())
        {
        	infoText.append((String)it.next() + "\n");
        }
	}
	
	
	//*********************************ACC**********************************************************
	
	
	Protocol coords = createProtocol("x", "y", "z");
	
	public float threashold = (float)0.5;
	public boolean first = true;
	public float magnitude; 
	
	float x = 0;
	float y = 0;
	float z = 0;
	
	String coordinates; 
	
	public void onSensorChanged(SensorEvent event)
	{
		// check sensor type
		if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
		
			float newX, newY, newZ;
			float oldX = 0, oldY = 0, oldZ = 0;
			
			//magnitude = (float)Math.sqrt(event.values[0]*event.values[0]+event.values[1]*event.values[1]+event.values[2]*event.values[2]);
			//magnitude = Math.abs(magnitude - SensorManager.GRAVITY_EARTH);	

			if(first)
			{
				oldX = (float)(Math.round(event.values[0]));
				oldY = (float)(Math.round(event.values[1]));
				oldZ = (float)(Math.round(event.values[2]));
				first = false;
			}
			
			newX  = (float)(Math.round(event.values[0]));
			newY = (float)(Math.round(event.values[1]));
			newZ = (float)(Math.round(event.values[2]));
			

			if( ( Math.abs( Math.abs( newX ) - Math.abs( oldX ))) > threashold) 
			{
				x = newX;
				oldX = newX;
			}
			if( ( Math.abs( Math.abs( newY ) - Math.abs( oldY ))) > threashold) 
			{
				y = newY;
				oldY = newY;
			}
			if( ( Math.abs( Math.abs( newZ ) - Math.abs( oldZ ) ) ) > threashold ) 
			{
				z = newZ;
				oldZ = newZ;
			}
			
			//create protocol
			coords = createProtocol(String.valueOf(x), String.valueOf(y), String.valueOf(z));
			
			xCoordinate.setText(String.valueOf(x));
			yCoordinate.setText(String.valueOf(y));
			zCoordinate.setText(String.valueOf(z));
		}
	}
	
	void startReceiveing()
	{
		if(mmSocketUp)
		{
			if(!thread.isAlive())
			{
				thread.start();
			}
			buttonTest2.setText(R.string.btnReceive);
		}
		else
			infoText.setText("Socket not up...");

		
	}
	
	void sendProtocol()
	{
		byte[] test = new byte[1024];
		test = coords.toByteArray();
		
		String coordsString = new String(test);
		coordsString = coordsString + "$";
		
		if(sendCoordinates)
			sendData(coordsString.getBytes());
	}
	
	
	//*********************************ACC**********************************************************
	
	BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
    private static final UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private BluetoothSocket mmSocket;
	private OutputStream mmOutStream;
	
	private void sendData(byte[] data)
	{			
		if(mmSocketUp)
		{
			try 
		    {
				mmOutStream = mmSocket.getOutputStream();
		    } 
		    catch (IOException e) 
		    {
		    	infoText.setText("Failed to open stream...");
		    }

		    try 
		    {
		    	mmOutStream.write(data);
		    } 
		    catch (IOException e) 
		    {
		    	infoText.setText("Failed to send data...");
		    }
		}
		else
			infoText.setText("Socket not up...");
	}
	
    byte[] buffer = new byte[1024];  // buffer store for the stream
    int bytes; // bytes returned from read()
    
    
    /* Call this from the main activity to shutdown the connection */
    public void cancel() 
    {
        try 
        {
            mmSocket.close();
        } 
        
        catch (IOException e) { }
    }
	
	boolean mmSocketUp = false;
    
	private void connectDevice() throws IOException 
	{
		//API14
		//if(!mmSocket.isConnected())
		//{
			//Get the BluetoothDevice object
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
					mmSocketUp = true;

				} 
				catch (IOException e) 
				{ 
					mmSocket = null;
					mmSocketUp = false;
					infoText.setText("Failed to create socket...");
				}

				if( mmSocketUp )
				{
					try 
					{
						// Connect the device through the socket. This will block until it succeeds or throws an exception
						mmSocket.connect(); 
						infoText.setText("Connected...");
					} 
					catch (IOException connectException) 
					{
						// Unable to connect; close the socket and get out
						try 
						{
							mmSocket.close();
							infoText.setText("Connection failed...");
						} 
						catch (IOException closeException) 
						{ 

						}
					}
				}
			}
			else
			{
				infoText.setText("No selected device");
			}	
		//}
	}
	
	// The BroadcastReceiver that listens for discovered devices and
	// changes the title when discovery is finished
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() 
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
					
					infoText.setText(device.getName() + "\n" + device.getAddress());
				}
			} 

			//When discovery is finished, change the Activity title
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) 
			{
				printDevicesFound();
			}
		}
	};

	
	public Protocol createProtocol(String x, String y, String z) 
	{
		Protocol.Builder coords = Protocol.newBuilder();
		coords.setXCoor(x);
		coords.setYCoor(y);
		coords.setZCoor(z);
		return coords.build();
	}
	
	//***********************************************
	
	//TA EMOT STERÄNGJÄVEL!!
	
	String recivedString = null;
	byte [] recivedData = new byte[1024];
	
	private final Handler handler = new Handler();
	final Runnable r = new Runnable()
	{
		public void run() 
		{
			infoText.setText(recivedString);
		}
	};

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
				
			    
				readData();
				handler.post(r);
			}
		}
	};
	
	private InputStream mmInStream;
	void readData()
	{
		try
	    {
	    	mmInStream = mmSocket.getInputStream();
	    }
	    catch (IOException e)
	    {
	    }
	    
	    try
	    {
	    	bytes = mmInStream.read(recivedData);
	    	recivedString = new String(recivedData);
	    }
	    catch (IOException e)
	    {
	    }
	    
	}
	
	//***********************************************
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}