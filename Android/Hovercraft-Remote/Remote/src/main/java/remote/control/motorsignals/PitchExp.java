package remote.control.motorsignals;

import se.liu.ed.Constants;
/**
 * \brief Exponential pitch algorithm.
 * \author Daniel Josefsson
 */
public class PitchExp extends AbstractSignalAlgorithm
{
	/**
	 * \brief Basic constructor, utilizes abstract class basic constructor.
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 */
	public PitchExp(float min, float max, float mean, float deadZone,
		float scale)
	{
		super(min, max, mean, deadZone, scale);
		this.type = Constants.Broadcast.MotorSignals.Algorithms.Pitch.EXP;
	}

	/**
	 * \brief Implementation of the exponential pitch algorithm.
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
		float power = 0;
		if (minValue <= cutPitch && cutPitch < -deadZone)
		{
			power = (float) (-Math.exp(-cutPitch) /
				Math.exp(minValue - mean));
		}
		else if (deadZone < cutPitch && cutPitch <= maxValue)
		{
			power = (float) (Math.exp(cutPitch) /
				Math.exp(maxValue - mean));
		}
		return new float[] { power, power };
	}
}
