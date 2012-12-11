package common.files.android;

final public class Constants // final to prevent instantiation
{
	// private constructor to prevent instantiation/inheritance
	private Constants()
	{
	}

	public static enum ConnectionState
	{
		CONNECTED, WAITING, DISCONNECTED;
	}

	// Targets
	public static final byte	TARGET_ADK				= (byte) 1;
	public static final byte	TARGET_BRAIN			= (byte) 2;
	public static final byte	TARGET_REMOTE			= (byte) 3;

	/*
	 * ADK Commands (1-70)
	 */
	/*
	 * Desc: Start Blinky on PIN 13 Args: none
	 */
	public static final byte	BLINKY_ON_COMMAND		= (byte) 1;
	/*
	 * Desc: Stop Blinky on PIN 13 Args: none
	 */
	public static final byte	BLINKY_OFF_COMMAND		= (byte) 2;

	/*
	 * Desc: Sends drive signals. Args: Protocol Buffer engine object
	 */
	public static final byte	MOTOR_CONTROL_COMMAND	= (byte) 3;

	/*
	 * Desc: ADK prints message to the serial port Args: message as Protocol
	 * Buffer object or not
	 */
	public static final byte	PRINT_MESSAGE_COMMAND	= (byte) 4;

	/*
	 * Desc: Sensor request for I2C sensors. Args: Put receiver target in
	 * message byte.
	 */
	public static final byte	I2C_SENSOR_REQ_COMMAND	= (byte) 5;

	/*
	 * Desc: Sensor request for ultrasonic/acc sensor. Args: Put receiver target in
	 * message byte.
	 */
	public static final byte	US_SENSOR_REQ_COMMAND			= (byte) 6;
	/**
	 * Message: 1 for lift fans on and 0 for lift fans off.
	 */
	public static final byte LIFT_FAN_COMMAND = (byte) 7;
	public static final byte	ACC_BRAIN_SENSOR_REQ_COMMAND	= (byte) 8;
	public static final byte	ACC_BRAIN_SENSOR_STOP_REQ_COMMAND	= (byte) 9;


	/*
	 * Brain Commands (71-141)
	 */
	public static final byte MOTOR_SIGNAL_COMMAND = (byte) 71;

	/*
	 * Remote Commands (142-211)
	 */
	/*
	 * Desc: Remote will log US sensor data when this command is received Args:
	 * Protocol Buffer USSensorData
	 */
	public static final byte	LOG_US_SENSOR_COMMAND			= (byte) 142;
	public static final byte	LOG_ACC_BRAIN_SENSOR_COMMAND 	= (byte) 143;

	/*
	 * Shared Commands (212-255)
	 */
	/*
	 * Desc: Args:
	 */
	public static final byte	I2C_SENSOR_COMMAND		= (byte) 212;
	/*
	 * Desc: Args:
	 */
	public static final byte	US_SENSOR_COMMAND		= (byte) 213;

	/**
	 * Broadcast actions
	 */
	public static class Broadcast
	{
		public static class System
		{
			public static String	POWER_CONNECTED	= "android.intent.action.ACTION_POWER_CONNECTED";
		}

		public static class UsbService
		{
			public static String	UPDATE_CONNECTION_STATE	= "USB.updateConnectionState";
		}

		public static class ControlSystem
		{
			public static class Status
			{
				public static String	TRANSMISSION	= "controlSystemStatusTransmission";
				
				public static class Query
				{
					public static String ACTION = "controlSystemStatusQueryAction";
					public static String TYPE = "controlSystemStatusQueryType";
				}
				public static class Response
				{
					public static String ACTION = "controlSystemStatusResponseAction";
					public static String TYPE = "controlSystemStatusResponseType";
					public static String STATUS = "controlSystemStatusResponseStatus";
				}
				
			}
		}

		public static class BluetoothService
		{
			public static String	UPDATE_CONNECTION_STATE	= "updateBTConnectionState";

			public static class Actions
			{
				public static class SendCommand
				{
					public static String	ACTION	= "btActionSendCommand";
					public static String	REQUEST_US_DATA = "btActionRequestUsData";
					public static String	REQUEST_ACC_BRAIN_DATA = "btActionRequestAccBrainData";
					public static String	REQUEST_STOP_ACC_BRAIN_DATA = "btActionRequestStopAccBrainData";


					public static class Intent
					{
						public static String	COMMAND	= "btActionSendCommandItentCommand";
						public static String	TARGET	= "btActionSendCommandItentTarget";
						public static String	BYTES	= "btActionSendCommandItentBytes";
					}
				}
			}
		}

		// TODO: Make functions that prepends all class names. This to make it
		// less error prone.
		public static class MotorSignals
		{
			public static String	CONTROL_SYSTEM	= "motorSignalsFromControlSystem";
			public static String	REMOTE			= "motorSignalsFromRemote";

			public static class Remote
			{
				public static String	ENABLE_TRANSMISSION		= "enableMotorSignalsTransmission";
				public static String	DISABLE_TRANSMISSION	= "disableMotorSignalsTransmission";
			}

			public static class Algorithms
			{
				public final static String AVAILABLE_QUERY = "motorSignalsAlgorithmAvaliableQuery";
				public final static String AVAILABLE_RESPONSE = "motorSignalsAlgorithmAvaliableResponse";
				public final static String TYPE_QUERY = "motorSignalsAlgorithmTypeQuery";
				public final static String TYPE_RESPONSE = "motorSignalsAlgorithmTypeResponse";
				public final static String ALGORITHM = "motorSignalsAlgorithm";
				public final static String PITCH = "motorSignalsAlgorithmPitch";
				public final static String ROLL = "motorSignalsAlgorithmRoll";
				public final static String CHANGE = "motorSignalsAlgorithmChange";
				public final static String TYPE = "motorSignalsAlgorithmType";

				public static class Pitch
				{
					public final static String LIN = "Linear";
					public final static String LOG = "Logarithmic";
					public final static String EXP = "Exponential";
				}

				public static class Roll
				{
					public final static String LIN = "Linear";
					public final static String LOG = "Logarithmic";
					public final static String EXP = "Exponential";
				}
			}
		}

		public static class LogService
		{
			public static class Actions
			{
				public static String	ADK_US_RESPONSE	= "logServiceActionAdkUsResponse";
				public static class Intent
				{
					public static String	COMMAND	= "logActionSendCommandItentCommand";
					public static String	TARGET	= "logActionSendCommandItentTarget";
					public static String	BYTES	= "logActionSendCommandItentBytes";
				}
			}
		}

		public static class ConnectionStates
		{
			public static String	CONNECTION_STATE	= "connectionState";
		}
	}

	public static class SharedPrefs
	{
		public static class SettingsAct
		{
			public static class Algorithms
			{
				public static class Pitch
				{
					public final static String CHOSEN = "sPSAApitchChosen";
				}

				public static class Roll
				{
					public final static String CHOSEN = "sPSAArollChosen";
				}

				public final static int CHOSEN_DEF = -1;
			}
		}
	}

	// private final String MOTOR_SIGNALS_ADK = "motorSignalsFromADK";
}