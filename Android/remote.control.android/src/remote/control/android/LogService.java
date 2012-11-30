package remote.control.android;

import java.io.File;
import java.io.FileInputStream;
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

import remote.control.android.Command.*;

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
import android.widget.Toast;

public class LogService extends IntentService implements SensorEventListener  
{

	private static final String TAG = "REMOTE";
	private static final String ADK_TARGET = null;
	public static Boolean logStarted = false;
	public static boolean accSensor = false;
	public static boolean proxSensor = false;
	public static boolean usBrainSensor = false;
	private int logDelay = 1;
	Sensor accelerometer;
	Sensor proximity;
	SensorManager sm;
	List<Float> sensorDataRemote = new ArrayList<Float>();
	List<Float> usBrain = new ArrayList<Float>();
	private File accfile = new File(Environment.getExternalStorageDirectory()+File.separator + "remote_acc_data.txt");

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
		intentFilter.addAction("UsOnBrainAction");		
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
    		else if(action.equalsIgnoreCase("UsOnBrainAction"))
    		{
    			Log.d(TAG, "UsOnBrainAction received");
    			usBrainSensor = !usBrainSensor;
    			Log.d(TAG, Boolean.toString(usBrainSensor));
       		} 
    		else if(action.equalsIgnoreCase("logUs"))
    		{
    			Log.d(TAG, "usLog received from Brain");
        		Bundle bundle = intent.getExtras();
        		USSensorData uUSensorData = (USSensorData) bundle.get("USSensorData");
        		
        		String desc = uUSensorData.getDescription();
        		int value = uUSensorData.getValue();
        		
        		// desc: frontRight, frontLeft, backRight, backLeft
        		// value: >200 = "-", 120, 55, 10, 0
        		
        		// Olle får göra ett PB-objekt som innehåller alla USSensorer. Helst dynamiskt så att man kan ha hur många som helst.
        		
        		// spara till fil. Gör en ny funktion för det.
        		
        		// saveData(all sensordata i en list eller array eller whaaatevah);
        		
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
				try
				{
					accfile.createNewFile();															
				}
				catch(IOException e)
				{
		    		e.printStackTrace();
		    	}				
				// Titta vilka sensorer vi ska logga
    			if(accSensor == true)
    			{
    				Log.d(TAG, "Logging acc");    				    				
    				try 
    				{
        				Log.d(TAG, "Logging header to acc");    				    				
    					accHeaderToSd();
					} 
    				catch (IOException e) 
					{
						e.printStackTrace();
					}
       				accRemoteToSd();   				
    			}
    			if(proxSensor == true)
    			{
    				Log.d(TAG, "Logging prox");
    				// Hämta data från proxsensor
    				// Sparar proxdata till minneskortet   				
    			}
    			if(usBrainSensor == true)
    			{
    				Intent logUsIntent = new Intent("LogUs");	
    		    	logUsIntent.putExtra("Target", ADK_TARGET);
    		    	sendBroadcast(logUsIntent);
    		    	
    		    	Context context2 = getApplicationContext();
    	    		Toast.makeText(context2, "Log Started", Toast.LENGTH_SHORT).show();
    	    		
    	    		Log.d(TAG, "Logging usBrain");    				    				
    				Log.d(TAG, "Logging header to usBrain");    				    				
					usHeaderToSd();
       				usBrainToSd();   	
    	    		

        			// tillgång till en intent (en väska/behållare) med sensorvärden
        			// gör en funktion som sparar dessa till minneskortet
        			// kalla på funktionen här
        			// onHoverAccToSd(value1, value2, value3);
    				
    				// kör funktion som skickar logg-kommando till svävarens telefon eller till adk
        			// sendCommand(command, target, messageLength, message)
        			// sendCommand(logOnHoverAcc,0x2,0,0)
    			}  		
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
			sensorDataRemote.clear();
			sensorDataRemote.add(event.values[0]);
		    sensorDataRemote.add(event.values[1]);
		    sensorDataRemote.add(event.values[2]);		    
        }
		else if (sensor.getType() == Sensor.TYPE_PROXIMITY) 
        {

        }
	}
	
	public void accHeaderToSd() throws IOException
	{		    
		{
		    FileInputStream fis = new FileInputStream(accfile);  
		    int b = fis.read();  
		    if (b == -1)  
		    {  
		    	FileOutputStream fo = new FileOutputStream(accfile, true);
				PrintWriter oWriter = new PrintWriter(fo);
				oWriter.println("X,Y,Z,date,time");	
				fis.close();
				oWriter.close();
		    }   
		}	
	}
	public void accRemoteToSd()
	{
		try
		{
			FileOutputStream fo = new FileOutputStream(accfile, true);
			PrintWriter oWriter = new PrintWriter(fo);				
			DecimalFormat form = new DecimalFormat("#.##");					
			DecimalFormatSymbols format = new DecimalFormatSymbols();
			format.setDecimalSeparator('.');
			form.setDecimalFormatSymbols(format);
			ListIterator<Float> lI = sensorDataRemote.listIterator();				
			while(lI.hasNext())
			{
				oWriter.append(form.format(lI.next()));
				if(lI.hasNext())
				{
					oWriter.append(',');
				}
			}			
			SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yyyy,kk:mm:ss:SSS"); 
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
	public void usHeaderToSd()
	{
		
	}
	public void usBrainToSd()
	{
		
	}
}

