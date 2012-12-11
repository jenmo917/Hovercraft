package on.hovercraft.android;

import java.util.ArrayList;
import java.util.List;

import common.files.android.Constants;
import common.files.android.Constants.Broadcast.BluetoothService.Actions.SendCommand;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class LogServiceBrain extends IntentService implements SensorEventListener 
{
	private static final String TAG = "JM";
	Sensor accelerometerBrain;
	SensorManager smBrain;
	List<Float> accDataBrain = new ArrayList<Float>();
	boolean sendAccBrainData = false;
	private int logDelay = 5000;
	
	public LogServiceBrain() 
	{
		super("LogService");
	}
	@Override
	public void onCreate() 
	{
		super.onCreate();
		Log.d(TAG, "LogServiceBrain onCreate");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("accBrainReq");
		registerReceiver(broadcastReceiver, intentFilter);
		initSensors();
		
	}
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "IntentService onDestroy");
		unregisterReceiver(broadcastReceiver);
	}
	private void initSensors()
	{
		smBrain = (SensorManager)getSystemService(SENSOR_SERVICE);
		smBrain.registerListener((SensorEventListener) this, smBrain.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
	}
	private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if(action.equalsIgnoreCase("accBrainReq"))
			{
				sendAccBrainData = intent.getBooleanExtra("send", false);
			}
		}
	};
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{
		// TODO Auto-generated method stub
	}
	@Override
	public void onSensorChanged(SensorEvent event) 
	{
		Sensor sensor = event.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER)
		{
			accDataBrain.clear();
			accDataBrain.add(event.values[0]);
			accDataBrain.add(event.values[1]);
			accDataBrain.add(event.values[2]);
		}
		
	}
	@Override
	protected void onHandleIntent(Intent intent) 
	{
		while(true)
		{
			// read accdata
			
			if(sendAccBrainData)
			{
				// send accDataBrain
				String x = accDataBrain.get(0).toString();
				String y = accDataBrain.get(1).toString();
				String z = accDataBrain.get(2).toString();
				
				String data = x + ":" + y + ":" + z;
				byte[] message = data.getBytes();
				
				Intent sendDataIntent = new Intent("callFunction");
				sendDataIntent.putExtra("sendToRemote", "sendToRemote");
				sendDataIntent.putExtra("onlyMessage", message);
				sendBroadcast(sendDataIntent);	
				
			}
			try 
			{
				Thread.sleep(logDelay);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		
	}
	
}
