package on.hovercraft.android;

import common.files.android.Constants;
import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
/**
 * \brief This is an placeholder for an control system. At it current state it just 
 * passes motor signals down to the ADK.
 * \author Daniel Josefsson
 */
public class ControlSystemService extends IntentService
{
	/**
	 * This is true if the service is up and running
	 */
	public static boolean isActive = false;
	
	private static String TAG = "JM";
	
	private final BroadcastReceiver messageReceiver =
			new MotorSignalsBroadcastReceiver();


	public ControlSystemService()
	{
		super("ControlSystem");
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		Log.d(TAG, "ControlSystem started");
		isActive = true;
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		isActive = false;
		unregisterReceiver(messageReceiver);
		Log.d(TAG, "ControlSystem destroyed");
	}

	@Override
	protected void onHandleIntent(Intent arg0)
	{
		Log.w(TAG, "ControlSystem onHandleIntent entered");

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
		filter.addAction(Constants.Broadcast.MotorSignals.REMOTE);
		registerReceiver(messageReceiver, filter);
	}

	/**
	 * \brief Passes motor signals to the ADK (USB service).
	 * This is where one should send motor signals to the control system.
	 * \author Daniel Josefsson
	 *
	 */
	private class MotorSignalsBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (action.equalsIgnoreCase(
					Constants.Broadcast.MotorSignals.REMOTE))
			{
				// Only loop through to ADK, but change action
				intent.setAction(
						Constants.Broadcast.MotorSignals.CONTROL_SYSTEM);
				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.COMMAND, Constants.MOTOR_CONTROL_COMMAND);
				intent.putExtra(Constants.Broadcast.BluetoothService.Actions.SendCommand.Intent.TARGET, Constants.TARGET_ADK);
				sendBroadcast(intent);
			}
		}
	}
}
