package remote.control.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

public class settingActivity extends Activity implements OnClickListener 
{
	private static final String TAG = "REMOTE";
	
	CheckBox acc;
	CheckBox prox;
	CheckBox brainAcc;
	
	Button btnBack;
	
	public void onCreate(Bundle savedInstanceState) 
	{         
		Log.d(TAG,"onCreate Settings");
		super.onCreate(savedInstanceState);    
        setContentView(R.layout.settings); 
        initButtons();
        initOnClickListners();        
	}
	
	private void initButtons()
	{
        acc = (CheckBox) findViewById(R.id.checkbox_accelerometer);
        acc.setChecked(LogService.accSensor);
        prox = (CheckBox) findViewById(R.id.checkbox_proximity);
        prox.setChecked(LogService.proxSensor);
        brainAcc = (CheckBox) findViewById(R.id.checkBox_BrainAccelerometer);
        btnBack = (Button) findViewById(R.id.backButton);
	}
	
	private void initOnClickListners()
	{
        acc.setOnClickListener(this);
        prox.setOnClickListener(this);
        brainAcc.setOnClickListener(this);
        btnBack.setOnClickListener(this);	
	}	
	
	public void onClick(View src) 
    {
    	switch (src.getId()) 
    	{
	    	case R.id.checkbox_accelerometer:
	    		Log.d(TAG,"checkbox acc checked");
		    	Intent sensorAccActionIntent = new Intent("AccSensorAction");	
		    	sendBroadcast(sensorAccActionIntent);	    		
	    	break;
	    	case R.id.checkbox_proximity:
	    		Log.d(TAG,"checkbox prox checked");
		    	Intent sensorProxActionIntent = new Intent("ProxSensorAction");	
		    	sendBroadcast(sensorProxActionIntent);	    		
	    	break;
	    	case R.id.backButton:
	    		Log.d(TAG,"backButton pushed");
	   		 	finish();
	    	break;
    	}    	   	
    }
	
	 @Override
	 public void onPause() 
	 {		 
		 Log.d(TAG,"onPause settings");
		 super.onPause();
		 finish();
	 }

	 @Override
	 public void onResume()
	 {		 
		 Log.d(TAG,"onResume settings");
		 super.onResume();
	 }
}	
