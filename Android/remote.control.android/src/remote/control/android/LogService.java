package remote.control.android;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class LogService extends IntentService implements SensorEventListener  
{

	private static final String TAG = "REMOTE";
	public static Boolean logStarted = false;
	public static boolean accSensor = false;
	public static boolean proxSensor = false;	
	private int logDelay = 1;
	Sensor accelerometer;
	Sensor proximity;
	SensorManager sm;
	List<Float> sensorData = new ArrayList<Float>();
	private File accfile = new File(Environment.getExternalStorageDirectory()+File.separator + "acc_data.txt");

	public LogService() 
	{
		super("LogService");
	}
	
	@Override
	public void onCreate() 
	{
		super.onCreate();
		Log.d(TAG, "IntentService onCreate");
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction("StartLogAction");
		intentFilter.addAction("StopLogAction");
		intentFilter.addAction("AccSensorAction");
		intentFilter.addAction("ProxSensorAction");
		intentFilter.addAction("logOnHoverAction");		
		registerReceiver(broadcastReceiver, intentFilter);
		initSensors();
	}
	
	@Override
	public void onDestroy()
	{
		Log.d(TAG, "IntentService onDestroy");
    	unregisterReceiver(broadcastReceiver); 		
	}
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() 
    {
    	@Override
    	public void onReceive(Context context, Intent intent) 
    	{
    		String action = intent.getAction();
    		if(action.equalsIgnoreCase("StartLogAction"))
      		{
    			Log.d(TAG, "StartLogAction received");
        		Bundle bundle = intent.getExtras();
        		logDelay = bundle.getInt("logDelay");
    			logStarted = true;
   			
    		}
    		else if(action.equalsIgnoreCase("StopLogAction"))
      		{
    			Log.d(TAG, "StopLogAction received");
    			logStarted = false;
    		}
    		else if(action.equalsIgnoreCase("AccSensorAction"))
    		{
    			Log.d(TAG, "AccSensorAction received");
    			accSensor = !accSensor;
    			Log.d(TAG, Boolean.toString(accSensor));  					    	
    		}
    		else if(action.equalsIgnoreCase("ProxSensorAction"))
    		{
    			Log.d(TAG, "ProxSensorAction received");
    			proxSensor = !proxSensor;
    			Log.d(TAG, Boolean.toString(proxSensor));
    		}
    		else if(action.equalsIgnoreCase("logOnHoverAction"))
    		{
    			Log.d(TAG, "logOnHoverAcc received");
    			// tillgång till en intent (en väska/behållare) med sensorvärden
    			// gör en funktion som sparar dessa till minneskortet
    			// kalla på funktionen här
    			// onHoverAccToSd(value1, value2, value3);
    		}    		
    	}
    };
	
    // här någonstans skriver du onHoverAccToSd

	@Override
	protected void onHandleIntent(Intent intent) 
	{
		while(true)
		{
			if(logStarted==true)
			{
				// Titta vilka sensorer vi ska logga
    			if(accSensor == true)
    			{
    				Log.d(TAG, "Logging acc");    				   				 				
    				accToSd();   				
    			}
    			if(proxSensor == true)
    			{
    				Log.d(TAG, "Logging prox");
    				// Hämta data från proxsensor
    				// Sparar proxdata till minneskortet   				
    			}
    			// if sensor == onHoverAcc
    			// kör funktion som skickar logg-kommando till svävarens telefon eller till adk
    			// sendCommand(command, target, messageLength, message)
    			// sendCommand(logOnHoverAcc,0x2,0,0)
			}

			// delay med en viss tid t.ex. 5sek.
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
	
	private void initSensors()
	{
		sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        sm.registerListener((SensorEventListener) this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sm.registerListener((SensorEventListener) this, sm.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_GAME);
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) 
	{		
		
	}

	public void onSensorChanged(SensorEvent event)
	{
		Sensor sensor = event.sensor;
		if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) 
		{
			sensorData.clear();
			sensorData.add(event.values[0]);
		    sensorData.add(event.values[1]);
		    sensorData.add(event.values[2]);		    
        }
		else if (sensor.getType() == Sensor.TYPE_PROXIMITY) 
        {

        }
	}
	
	public void accToSd()
	{
		try
		{
			accfile.createNewFile();
			FileOutputStream fo = new FileOutputStream(accfile, true);
			PrintWriter oWriter = new PrintWriter(fo);
			oWriter.println("X,Y,Z,date,time");							
			DecimalFormat form = new DecimalFormat("#.##");					
			DecimalFormatSymbols format = new DecimalFormatSymbols();
			format.setDecimalSeparator('.');
			form.setDecimalFormatSymbols(format);
			ListIterator<Float> lI = sensorData.listIterator();				
			while(lI.hasNext())
			{
				oWriter.append(form.format(lI.next()));
				if(lI.hasNext())
				{
					oWriter.append(',');
				}
			}			
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy,kk:mm:ss:SSS");//dd/MM/yyyy
			Date now = new Date();
			String strDate = sdfDate.format(now);
			oWriter.append(","+strDate);
			oWriter.append("\r\n");
			oWriter.close();			
		}
		catch(IOException e)
		{
    		e.printStackTrace();
    	}				
	}
	
					
	

}

