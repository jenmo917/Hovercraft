package remote.control.motorsignals;

import se.liu.ed.Constants;
/**
 * \brief Exponential pitch algorithm.
 * \author Daniel Josefsson
 */
public class PitchLinear extends AbstractSignalAlgorithm
{
	/**
	 * \brief Basic constructor, utilizes abstract class basic constructor.
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 */
	public PitchLinear(float min, float max, float mean, float deadZone,
		float scale)
	{
		super(Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN, min, max,
			mean, deadZone, scale);
	}

	public PitchLinear(String type, float min, float max, float mean,
		float deadZone, float scale)
	{
		super(type, min, max, mean, deadZone, scale);
	}

	/**
	 * \brief Implementation of the linear pitch algorithm.
	 * \author Daniel Josefsson
	 */
	@Override
	public float[] convert(float pitch)
	{
		float deadZone = this.deadZone * scale;
		float minValue = this.minValue * scale;
		float maxValue = this.maxValue * scale;
		float mean = this.mean * scale;

		float cutPitch = cut(pitch) - mean;
		if (-deadZone < cutPitch && cutPitch < deadZone)
		{
			cutPitch = 0f;
		}
		float power;
		if (0f <= cutPitch)
		{
			power = cutPitch / (maxValue - mean);
		}
		else
		{
			power = cutPitch / (mean - minValue);
		}
		return new float[] { power, power };
	}
}
