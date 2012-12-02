package on.hovercraft.android;

final public class Constants //final to prevent instantiation
{
    //private constructor to prevent instantiation/inheritance
    private Constants()
    {
    }	
	
	public static enum ConnectionState 
	{
	    CONNECTED, WAITING, DISCONNECTED;
	}
	
	// Targets
	public static final byte TARGET_ADK = (byte) 1;
    public static final byte TARGET_BRAIN = (byte) 2;
    public static final byte TARGET_REMOTE = (byte) 3;
    
    /*
     * 
     * 
     * ADK Commands (1-70)
     * 
     *
     */
	/*
	 * Desc: Start Blinky on PIN 13
	 * Args: none
	 */    
	public static final byte BLINKY_ON_COMMAND = (byte) 1;
	/*
	 * Desc: Stop Blinky on PIN 13
	 * Args: none
	 */    
	public static final byte BLINKY_OFF_COMMAND = (byte) 2;
	
	/*
	 * Desc: Sends drive signals.
	 * Args: Protocol Buffer engine object
	 */    
	public static final byte MOTOR_CONTROL_COMMAND = (byte) 3;
	
	/*
	 * Desc: ADK prints message to the serial port
	 * Args: message as Protocol Buffer object or not
	 */
	public static final byte PRINT_MESSAGE_COMMAND = (byte) 4;
		
	/*
	 * Desc: Sensor request for I2C sensors.
	 * Args: Put receiver target in message byte.
	 */
	public static final byte I2C_SENSOR_REQ_COMMAND = (byte) 5; 
	
	/*
	 * Desc: Sensor request for ultrasonic sensor.
	 * Args: Put receiver target in message byte.
	 */
	public static final byte US_SENSOR_REQ_COMMAND = (byte) 6; 
	
	
    /*
     * 
     *  
     * Brain Commands (71-141)
     *  
     *  
     */

	
	
	/*
	 * 
	 * 
	 * Remote Commands (142-211)
	 * 
	 * 
	 */
	/*
	 * Desc: Remote will log US sensor data when this command is received
	 * Args: Protocol Buffer USSensorData
	 */
	public static final byte LOG_US_SENSOR_COMMAND = (byte) 142; 

	
	/*
	 * 
	 * 
	 * Shared Commands (212-255)
	 * 
	 * 
	 */
	/*
	 * Desc: 
	 * Args: 
	 */
	public static final byte I2C_SENSOR_COMMAND = (byte) 212;	
	/*
	 * Desc: 
	 * Args: 
	 */
	public static final byte US_SENSOR_COMMAND = (byte) 213;	
}