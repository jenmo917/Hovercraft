package remote.control.android;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;

import common.files.android.Constants;

public class settingActivity extends Activity implements OnClickListener,
	OnItemSelectedListener
{
	private static final String TAG = "REMOTE";
	public static final String ALGORITHM_SETTINGS = TAG + "algorithmSettings";
	
	CheckBox acc;
	CheckBox accBrain;
	CheckBox adkUs;

	private Spinner pitchSpinner;
	private Spinner rollSpinner;

	Button btnBack;
	
	SettingsActivityBroadcastReceiver messageReceiver = new SettingsActivityBroadcastReceiver();

	SharedPreferences algorithmSettings;

	public void onCreate(Bundle savedInstanceState) 
	{         
		Log.d(TAG,"onCreate Settings");
		super.onCreate(savedInstanceState);    
        
		setContentView(R.layout.settings); 
        initButtons();
		initSpinners();
        initOnClickListners();        
		initOnItemSelectedListener();
		algorithmSettings = getSharedPreferences(ALGORITHM_SETTINGS,
			MODE_PRIVATE);
		Intent intent = new Intent(
			Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_QUERY);
		sendBroadcast(intent);
	}

	void getCurrentAlgorithm()
	{
		Intent intent = new Intent(
			Constants.Broadcast.MotorSignals.Algorithms.TYPE_QUERY);
		sendBroadcast(intent);
	}

	private void initButtons()
	{
        acc = (CheckBox) findViewById(R.id.checkbox_accelerometer);
        acc.setChecked(LogService.accSensor);
        accBrain = (CheckBox) findViewById(R.id.checkBox_BrainAccelerometer);
        accBrain.setChecked(LogService.accBrainSensor);
        adkUs = (CheckBox) findViewById(R.id.checkBox_AdkUltrasound);
        adkUs.setChecked(LogService.usAdkSensor);
        btnBack = (Button) findViewById(R.id.backButton);
	}
	
	private void initOnClickListners()
	{
        acc.setOnClickListener(this);
        accBrain.setOnClickListener(this);
        adkUs.setOnClickListener(this);
        btnBack.setOnClickListener(this);
	}

	private void initOnItemSelectedListener()
	{
		pitchSpinner.setOnItemSelectedListener(this);
		rollSpinner.setOnItemSelectedListener(this);
	}

	public void onClick(View src) 
    {
    	switch (src.getId()) 
    	{
	    	case R.id.checkbox_accelerometer:
	    		Log.d(TAG,"checkbox acc remote checked");
		    	Intent sensorAccActionIntent = new Intent("CheckboxAccRemoteAction");	
		    	sendBroadcast(sensorAccActionIntent);	    		
	    	break;
	    	case R.id.checkBox_BrainAccelerometer:
	    		Log.d(TAG,"checkbox acc brain checked");
		    	Intent sensorAccBrainActionIntent = new Intent("CheckboxAccBrainAction");	
		    	sendBroadcast(sensorAccBrainActionIntent);	    		
	    	break;
	    	case R.id.checkBox_AdkUltrasound:
	    		Log.d(TAG,"checkbox US adk checked");
		    	Intent sensorUsBrainActionIntent = new Intent("CheckboxUsOnAdkAction");	
		    	sendBroadcast(sensorUsBrainActionIntent);	    		
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
		unregisterReceiver(messageReceiver);
		 finish();
	 }

	 @Override
	 public void onResume()
	 {		 
		 Log.d(TAG,"onResume settings");
		 super.onResume();
		setupBroadcastFilters();
	 }
	 
	private class SettingsActivityBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action
				.equals(Constants.Broadcast.MotorSignals.Algorithms.TYPE_RESPONSE))
			{
				this.typeResponse(intent);
			}
			else if (action
				.equals(Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_RESPONSE))
			{
				this.availableResponse(intent);
			}
		}

		public void typeResponse(Intent intent)
		{
			String pitch = intent
				.getStringExtra(Constants.Broadcast.MotorSignals.Algorithms.PITCH);
			String roll = intent
				.getStringExtra(Constants.Broadcast.MotorSignals.Algorithms.ROLL);
			int[] positions = settingActivity.this
				.chooseSpinnerItemFromStrings(pitch, roll);
			SharedPreferences.Editor editor = algorithmSettings.edit();
			editor.putInt(
				Constants.SharedPrefs.SettingsAct.Algorithms.Pitch.CHOSEN,
				positions[0]);
			editor.putInt(
				Constants.SharedPrefs.SettingsAct.Algorithms.Roll.CHOSEN,
				positions[1]);
			editor.commit();
		}

		public void availableResponse(Intent intent)
		{
			String[] pitches = intent
				.getStringArrayExtra(Constants.Broadcast.MotorSignals.Algorithms.PITCH);
			String[] rolls = intent
				.getStringArrayExtra(Constants.Broadcast.MotorSignals.Algorithms.ROLL);
			settingActivity.this.populateSpinners(pitches, rolls);
			// Get current algorithms
			int def = Constants.SharedPrefs.SettingsAct.Algorithms.CHOSEN_DEF;
			String pitchChosen = Constants.SharedPrefs.SettingsAct.Algorithms.Pitch.CHOSEN;
			int pitchChosenValue = algorithmSettings.getInt(pitchChosen, def);
			String rollChosen = Constants.SharedPrefs.SettingsAct.Algorithms.Roll.CHOSEN;
			int rollChosenValue = algorithmSettings.getInt(rollChosen, def);
			// Request from motor signals service if not set.
			if (def == pitchChosenValue || def == rollChosenValue)
			{
				Intent query = new Intent(
					Constants.Broadcast.MotorSignals.Algorithms.TYPE_QUERY);
				sendBroadcast(query);
			}
			else
			{
				setSpinnerItems(pitchChosenValue, rollChosenValue);
			}
		}
	}

	private void setupBroadcastFilters()
	{
		IntentFilter filter = new IntentFilter();
		filter
			.addAction(Constants.Broadcast.MotorSignals.Algorithms.TYPE_RESPONSE);
		filter
			.addAction(Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_RESPONSE);
		registerReceiver(messageReceiver, filter);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
		long id)
	{
		if (parent == pitchSpinner)
		{
			String chosen = Constants.SharedPrefs.SettingsAct.Algorithms.Pitch.CHOSEN;
			int chosenDef = Constants.SharedPrefs.SettingsAct.Algorithms.CHOSEN_DEF;
			int chosenValue = algorithmSettings.getInt(chosen, chosenDef);
			if (chosenDef != chosenValue && chosenValue != pos)
			{
				Intent intent = new Intent(
					Constants.Broadcast.MotorSignals.Algorithms.CHANGE);
				intent.putExtra(
					Constants.Broadcast.MotorSignals.Algorithms.ALGORITHM,
					Constants.Broadcast.MotorSignals.Algorithms.PITCH);
				intent.putExtra(
					Constants.Broadcast.MotorSignals.Algorithms.TYPE,
					(String) parent.getItemAtPosition(pos));
				sendBroadcast(intent);
			}
		}
		else if (parent == rollSpinner)
		{
			String chosen = Constants.SharedPrefs.SettingsAct.Algorithms.Roll.CHOSEN;
			int chosenDef = Constants.SharedPrefs.SettingsAct.Algorithms.CHOSEN_DEF;
			int chosenValue = algorithmSettings.getInt(chosen, chosenDef);
			if (chosenDef != chosenValue && chosenValue != pos)
			{
				Intent intent = new Intent(
					Constants.Broadcast.MotorSignals.Algorithms.CHANGE);
				intent.putExtra(
					Constants.Broadcast.MotorSignals.Algorithms.ALGORITHM,
					Constants.Broadcast.MotorSignals.Algorithms.ROLL);
				intent.putExtra(
					Constants.Broadcast.MotorSignals.Algorithms.TYPE,
					(String) parent.getItemAtPosition(pos));
				sendBroadcast(intent);
			}
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0)
	{
	}

	private void initSpinners()
	{
		pitchSpinner = (Spinner) findViewById(R.id.pitchAlgorithm);
		rollSpinner = (Spinner) findViewById(R.id.rollAlgorithm);
		// Ask for available algorithms.
		Intent intent = new Intent(
			Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_QUERY);
		sendBroadcast(intent);
	}

	private void populateSpinners(String[] pitch, String[] roll)
	{
		ArrayAdapter<String> pitchAdapter = new ArrayAdapter<String>(
			this, android.R.layout.simple_spinner_item, pitch);
		pitchAdapter
			.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		pitchSpinner.setAdapter(pitchAdapter);
		ArrayAdapter<String> rollAdapter = new ArrayAdapter<String>(
			this, android.R.layout.simple_spinner_item, roll);
		rollAdapter
			.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		rollSpinner.setAdapter(pitchAdapter);
	}

	private int[] chooseSpinnerItemFromStrings(String pitch, String roll)
	{
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> pitchAdapter =
			(ArrayAdapter<String>) pitchSpinner.getAdapter();
		int pitchPosition = pitchAdapter.getPosition(pitch);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> rollAdapter =
			(ArrayAdapter<String>) rollSpinner.getAdapter();
		int rollPosition = rollAdapter.getPosition(roll);
		this.setSpinnerItems(pitchPosition, rollPosition);
		return new int[] { pitchPosition, rollPosition };
	}

	private void setSpinnerItems(int pitchPosition, int rollPosition)
	{
		pitchSpinner.setSelection(pitchPosition, false);
		rollSpinner.setSelection(rollPosition, false);
	}
}	
