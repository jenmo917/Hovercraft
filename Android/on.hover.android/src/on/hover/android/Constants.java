package on.hover.android;

final public class Constants //final to prevent instantiation
{
    public static final byte TARGET_ADK = 0x1;
    public static final byte TARGET_BRAIN = 0x2;
    public static final byte TARGET_REMOTE = 0x3;
    
	public static final byte MOTOR_CONTROL_COMMAND = 0x3;

    //private constructor to prevent instantiation/inheritance
    private Constants()
    {
    }
}