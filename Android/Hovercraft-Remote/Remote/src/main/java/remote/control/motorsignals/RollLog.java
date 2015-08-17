package remote.control.motorsignals;

import se.liu.ed.Constants;
/**
 * \brief Logarithmic roll algorithm.
 * \author Daniel Josefsson
 */
public class RollLog extends AbstractSignalAlgorithm
{
	/**
	 * \brief Basic constructor, utilizes abstract class basic constructor.
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 */
	public RollLog(float min, float max, float mean, float deadZone, float scale)
	{
		super(min, max, mean, deadZone, scale);
		this.type = Constants.Broadcast.MotorSignals.Algorithms.Roll.LOG;
	}

	/**
	 * \brief Implementation of the logarithmic roll algorithm.
	 * \author Daniel Josefsson
	 */
	@Override
	public float[] convert(float roll)
	{
		float deadZone = this.deadZone*scale;
		float minValue = this.minValue*scale;
		float maxValue = this.maxValue*scale;
		float mean = this.mean*scale;
		
		float leftMotor = 0f;
		float rightMotor = 0f;
		
		float cutRoll = this.cut(roll) - mean;
		if (minValue <= cutRoll && cutRoll < -deadZone)
		{
			leftMotor = (float) (Math.log10(-cutRoll) /
				Math.log10(mean - minValue));
		}
		if (deadZone < cutRoll && cutRoll <= maxValue)
		{
			rightMotor = (float) (Math.log10(cutRoll) /
				Math.log10(maxValue - mean));
		}
		return new float[] { leftMotor, rightMotor };
	}
}
