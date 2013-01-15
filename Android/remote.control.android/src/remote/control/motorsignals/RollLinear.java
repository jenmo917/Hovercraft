package remote.control.motorsignals;

import common.files.android.Constants;
/**
 * \brief Linear roll algorithm.
 * \author Daniel Josefsson
 */
public class RollLinear extends AbstractSignalAlgorithm
{
	/**
	 * \brief Basic constructor, utilizes abstract class basic constructor.
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 */
	public RollLinear(float min, float max, float mean, float deadZone, float scale)
	{
		super(min, max, mean, deadZone, scale);
		this.type = Constants.Broadcast.MotorSignals.Algorithms.Roll.LIN;
	}

	/**
	 * \brief Implementation of the linear roll algorithm.
	 * \author Daniel Josefsson
	 */
	@Override
	public float[] convert(float roll)
	{
		float deadZone = this.deadZone * scale;
		float minValue = this.minValue * scale;
		float maxValue = this.maxValue * scale;
		float mean = this.mean * scale;

		float leftMotor = 0f;
		float rightMotor = 0f;

		float cutRoll = this.cut(roll) - mean;
		if (minValue <= cutRoll && cutRoll < -deadZone)
		{
			leftMotor = -cutRoll / (maxValue - mean);
		}
		if (deadZone < cutRoll && cutRoll <= maxValue)
		{
			rightMotor = cutRoll / (maxValue - mean);
		}
		return new float[] { leftMotor, rightMotor };
	}

}
