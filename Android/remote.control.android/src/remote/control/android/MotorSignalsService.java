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
	final float CONTROLLER_ROLL_MIN = -0.65f;
	final float CONTROLLER_ROLL_MAX = 0.65f;
	final float CONTROLLER_ROLL_MEAN = 0f;
	final float CONTROLLER_ROLL_DEAD_ZONE = 0.02f;
	final float CONTROLLER_PITCH_MIN = 0f;
	final float CONTROLLER_PITCH_MAX = 1;
	final float CONTROLLER_PITCH_MEAN = 0f;
	final float CONTROLLER_PITCH_DEAD_ZONE = 0.03f;
	float lpf = 0.8f;
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
		filter.addAction(Constants.Broadcast.ControlSystem.Status.Query.ACTION);
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
			else if (action.equals(Constants.Broadcast.ControlSystem.Status.Query.ACTION))
			{
				String type = intent.getStringExtra(Constants.Broadcast.ControlSystem.Status.Query.TYPE);
				
				if(type.equals(Constants.Broadcast.ControlSystem.Status.TRANSMISSION))
				{
					Intent responseIntent = new Intent(Constants.Broadcast.ControlSystem.Status.Response.ACTION);
					responseIntent.putExtra(Constants.Broadcast.ControlSystem.Status.Response.TYPE, 
											Constants.Broadcast.ControlSystem.Status.TRANSMISSION);
					responseIntent.putExtra(Constants.Broadcast.ControlSystem.Status.Response.STATUS, enabled);
					sendBroadcast(responseIntent);
				}
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
		float alpha = (float) 0.8;
		accVals[0] = alpha * accVals[0] + (1 - alpha) * tempValues[0];
		accVals[1] = alpha * accVals[1] + (1 - alpha) * tempValues[1];
		accVals[2] = alpha * accVals[2] + (1 - alpha) * tempValues[2];
		int[] motorValues = motorSignals.convert(accVals[2], accVals[1], 8);
		boolean leftMotorForward = false, rightMotorForward = false;
		if (1 == motorValues[1])
		{
			leftMotorForward = true;
		}
		if (1 == motorValues[3])
		{
			rightMotorForward = true;
		}
		Log.d(TAG, "LeftMotor; D: "
			+ String.format("%d", motorValues[0])
			+ " RightMotor: "
			+ String.format("%d", motorValues[2]));
		Engines engineValues = createEngineProtocol(
			createDriveSignalProtocol(rightMotorForward, true, motorValues[2]),
			createDriveSignalProtocol(leftMotorForward, true, motorValues[0]));
		byte[] message = engineValues.toByteArray();
		Intent intent = new Intent(
			Constants.Broadcast.BluetoothService.Actions.SendCommand.ACTION);
		intent
			.putExtra(
				Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND,
				Constants.MOTOR_SIGNAL_COMMAND);
		intent
			.putExtra(
				Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET,
				Constants.TARGET_BRAIN);
		intent
			.putExtra(
				Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.BYTES,
				message);
		sendBroadcast(intent);
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
