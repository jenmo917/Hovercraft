package remote.control.android;

import se.liu.ed.Command.DriveSignals;
import se.liu.ed.Command.Engines;

import remote.control.motorsignals.MotorSignals;
import remote.control.motorsignals.MotorSignalsBuilder;
import remote.control.motorsignals.MotorSignalsDirector;
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

import se.liu.ed.Constants;
/**
 * \brief MotorSignalsService takes care of the creation of a MotorSignal object and 
 * commands passed to it. It also transfers the MotorSignals commands to the
 * Bluetooth service for transfer to the ADK.
 * 
 * \author Daniel Josefsson
 *
 */
public class MotorSignalsService extends IntentService implements
		SensorEventListener
{
	/**
	 * This is true if the service is up and running
	 */
	public static boolean isActive = false;

	private static String TAG = "MotorSignals";

	private final BroadcastReceiver messageReceiver =
		new MotorSignalsBroadcastReceiver();

	private SensorManager mgr;
	private Sensor accel;
	private boolean enabled = false;

	private float[] accVals = new float[3];
	float lpf = 0.8f;
	private MotorSignals motorSignals;
	private MotorSignalsDirector director;

	/**
	 * \brief Basic constructor, utilizes the base class constructor.
	 * \author Daniel Josefsson
	 */
	public MotorSignalsService()
	{
		super("MotorSignals");

	}

	/**
	 * \brief Register for Accelerometer data. Builds a Log MotorSystem.
	 * \author Daniel Josefsson
	 */
	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "MotorSignals started");
		isActive = true;

		mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		accel = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		director = new MotorSignalsDirector(new MotorSignalsBuilder());
		motorSignals = director.buildLogSystem();
	}

	/**
	 * \brief Destroys the message receiver.
	 * \author Daniel Josefsson
	 */
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		isActive = false;
		// TODO: Send command to Brain
		unregisterReceiver(messageReceiver);
		Log.d(TAG, "MotorSignals destroyed");
	}

	/**
	 * \brief Sets the broadcast filters.
	 * \author Daniel Josefsson
	 * @param intent
	 */
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

	/**
	 * \brief Sets the broadcast filters.
	 * \author Daniel Josefsson
	 */
	private void setupBroadcastFilters()
	{
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.Broadcast.MotorSignals.Remote.DISABLE_TRANSMISSION);
		filter.addAction(Constants.Broadcast.MotorSignals.Remote.ENABLE_TRANSMISSION);
		filter.addAction(Constants.Broadcast.ControlSystem.Status.Query.ACTION);
		filter
			.addAction(Constants.Broadcast.MotorSignals.Algorithms.TYPE_QUERY);
		filter
			.addAction(Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_QUERY);
		filter
			.addAction(Constants.Broadcast.MotorSignals.Algorithms.CHANGE);
		registerReceiver(messageReceiver, filter);
	}

	/**
	 * Handles MotorSignals Intents.
	 * \brief
	 * \author
	 *
	 */
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
			else if (action
				.equals(Constants.Broadcast.MotorSignals.Algorithms.TYPE_QUERY))
			{
				broadcastAlgorithms();
			}
			else if (action
				.equals(Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_QUERY))
			{
				String[] pitches = new String[] {
					Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN,
					Constants.Broadcast.MotorSignals.Algorithms.Pitch.LOG,
					Constants.Broadcast.MotorSignals.Algorithms.Pitch.EXP,
					Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN_REV };
				String[] rolls = new String[] {
					Constants.Broadcast.MotorSignals.Algorithms.Roll.LIN,
					Constants.Broadcast.MotorSignals.Algorithms.Roll.LOG,
					Constants.Broadcast.MotorSignals.Algorithms.Roll.EXP };
				Intent response = new Intent(
					Constants.Broadcast.MotorSignals.Algorithms.AVAILABLE_RESPONSE);
				response
					.putExtra(
						Constants.Broadcast.MotorSignals.Algorithms.PITCH,
						pitches);
				response
					.putExtra(
					Constants.Broadcast.MotorSignals.Algorithms.ROLL,
					rolls);
				sendBroadcast(response);
			}
			else if (action
				.equals(Constants.Broadcast.MotorSignals.Algorithms.CHANGE))
			{
				changeAlgorithms(intent);
				broadcastAlgorithms();
			}
		}

		private void changeAlgorithms(Intent intent)
		{
			String algorithm = intent
				.getStringExtra(Constants.Broadcast.MotorSignals.Algorithms.ALGORITHM);
			String type = intent
				.getStringExtra(Constants.Broadcast.MotorSignals.Algorithms.TYPE);
			MotorSignalsService.this.director.changeAlgorithm(
				MotorSignalsService.this.motorSignals, algorithm, type);
		}

		private void broadcastAlgorithms()
		{
			Intent response = new Intent(
				Constants.Broadcast.MotorSignals.Algorithms.TYPE_RESPONSE);
			response.putExtra(
				Constants.Broadcast.MotorSignals.Algorithms.PITCH,
				MotorSignalsService.this.motorSignals.getPitchAlgorithm()
					.getType());
			response.putExtra(
				Constants.Broadcast.MotorSignals.Algorithms.ROLL,
				MotorSignalsService.this.motorSignals.getRollAlgorithm()
					.getType());
			sendBroadcast(response);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1)
	{
		// Do nothing right now. Nothing is implemented.
	}

	/**
	 * \brief Calculates the MotorSignals according to accelerometer data.
	 * The accelerometer data is low pass filtered.
	 * \author Daniel Josefsson
	 * @param event
	 */
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

	/**
	 * \brief Normalize float array.
	 * \author Daniel Josefsson
	 * @param arr
	 * @return
	 */
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

	/**
	 * \brief Create Protocol buffer EnginesProtocol.
	 * \author Daniel Josefsson
	 * @param driveSignalRight
	 * @param driveSignalLeft
	 * @return
	 */
	static Engines createEngineProtocol(DriveSignals driveSignalRight,
			DriveSignals driveSignalLeft)
	{
		Engines.Builder engines = Engines.newBuilder();
		engines.setRight(driveSignalRight);
		engines.setLeft(driveSignalLeft);
		return engines.build();
	}

	/**
	 * \brief Create Protocol buffer DriveSignalProtocol.
	 * \author Daniel Josefsson
	 * @param driveSignalRight
	 * @param driveSignalLeft
	 * @return
	 */
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
