package remote.control.android;

import remote.control.android.Command.DriveSignals;
import remote.control.android.Command.Engines;
import remote.control.motorsignals.AbstractSignalAlgorithm;
import remote.control.motorsignals.MotorSignals;
import remote.control.motorsignals.PitchLinear;
import remote.control.motorsignals.RollLinear;
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

import common.files.android.Constants;

public class MotorSignalsService extends IntentService implements
		SensorEventListener
{
	/**
	 * This is true if the service is up and running
	 */
	public static boolean			isActive					= false;

	private static String			TAG							= "MotorSignals";

	private final BroadcastReceiver	messageReceiver				= new MotorSignalsBroadcastReceiver();

	private SensorManager			mgr;
	private Sensor					accel;
	private boolean					enabled						= false;

	private float[]					accVals						= new float[3];
	final float						CONTROLLER_ROLL_MIN			= -45;
	final float						CONTROLLER_ROLL_MAX			= 45;
	final float						CONTROLLER_ROLL_MEAN		= 0;
	final float						CONTROLLER_ROLL_DEAD_ZONE	= 3;
	final float						CONTROLLER_PITCH_MIN		= 0;
	final float						CONTROLLER_PITCH_MAX		= 128;
	final float						CONTROLLER_PITCH_MEAN		= 64;
	final float						CONTROLLER_PITCH_DEAD_ZONE	= 3;
	float							lpf							= 0.8f;
	private MotorSignals			motorSignals;
	private AbstractSignalAlgorithm	pitch;
	private AbstractSignalAlgorithm	roll;

	public MotorSignalsService()
	{
		super("MotorSignals");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "MotorSignals started");
		isActive = true;

		mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		accel = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		pitch = new PitchLinear(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
				CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_DEAD_ZONE);
		roll = new RollLinear(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
				CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_DEAD_ZONE);
		motorSignals = new MotorSignals();
		motorSignals.setPitchAlgorithm(pitch);
		motorSignals.setRollAlgorithm(roll);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		isActive = false;
		// TODO: Send command to Brain
		unregisterReceiver(messageReceiver);
		Log.d(TAG, "MotorSignals destroyed");
	}

	@Override
	protected void onHandleIntent(Intent intent)
	{
		Log.w(TAG, "MotorSignals onHandleIntent entered");

		setupBroadcastFilters();

		while (true)
		{
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

	private void setupBroadcastFilters()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION);
		filter.addAction(Constants.Broadcast.MotorSignals.Remote.ENABLE_TRANSMISSION);
		registerReceiver(messageReceiver, filter);
	}

	private class MotorSignalsBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action
					.equals(Constants.Broadcast.MotorSignals.Remote.ENABLE_TRANSMISSION))
			{
				MotorSignalsService.this.mgr.registerListener(
						MotorSignalsService.this, accel,
						SensorManager.SENSOR_DELAY_GAME);
				MotorSignalsService.this.enabled = true;
			}
			else if (action
					.equals(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION))
			{
				MotorSignalsService.this.mgr.unregisterListener(
						MotorSignalsService.this, accel);
				MotorSignalsService.this.enabled = false;
				// TODO: Send command to Brain
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		float[] tempValues = new float[3];
		switch (event.sensor.getType())
		{
			case Sensor.TYPE_ACCELEROMETER:
				System.arraycopy(event.values, 0, tempValues, 0, 3);
				// Normalize
				tempValues = normalise(tempValues);
				break;
		}

		if (!MotorSignalsService.this.enabled)
		{
			return;
		}
		accVals[0] = lpf * accVals[0] + (1 - lpf) * tempValues[0];
		accVals[1] = lpf * accVals[1] + (1 - lpf) * tempValues[1];
		accVals[2] = lpf * accVals[2] + (1 - lpf) * tempValues[2];
		int[] apr = new int[3];
		apr[0] = Math.round(Math.scalb(accVals[0], 7));
		apr[1] = Math.round(Math.scalb(accVals[1], 7));
		apr[2] = Math.round(Math.scalb(accVals[2], 7));
		int[] motorValues = motorSignals.convert(apr[2], apr[1]);
		Engines engineValues = createEngineProtocol(
				createDriveSignalProtocol(true, true, motorValues[1]),
				createDriveSignalProtocol(true, true, motorValues[0]));
		byte[] message = engineValues.toByteArray();
		Log.d(TAG, message.toString());
		/*
		 * sendCommand(Constants.MOTOR_CONTROL_COMMAND, Constants.TARGET_BRAIN,
		 * message);
		 */
	}

	public float[] normalise(float[] arr)
	{
		float sum = 0f;
		int arrL = arr.length;
		for (int i = 0; i < arrL; i++)
		{
			sum += Math.pow(arr[i], 2);
		}
		float size = (float) Math.sqrt(sum);
		for (int i = 0; i < arrL; i++)
		{
			arr[i] = arr[i] / size;
		}
		return arr;
	}

	static Engines createEngineProtocol(DriveSignals driveSignalRight,
			DriveSignals driveSignalLeft)
	{
		Engines.Builder engines = Engines.newBuilder();
		engines.setRight(driveSignalRight);
		engines.setLeft(driveSignalLeft);
		return engines.build();
	}

	static DriveSignals createDriveSignalProtocol(boolean forward,
			boolean enable, int power)
	{
		DriveSignals.Builder driveSignal = DriveSignals.newBuilder();
		driveSignal.setForward(forward);
		driveSignal.setEnable(enable);
		driveSignal.setPower(power);
		return driveSignal.build();
	}
}
