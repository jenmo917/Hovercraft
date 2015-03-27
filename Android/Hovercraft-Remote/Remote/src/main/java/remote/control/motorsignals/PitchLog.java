package remote.control.motorsignals;

import se.liu.ed.Constants;
/**
 * \brief Logarithmic pitch algorithm.
 * \author Daniel Josefsson
 *
 */
public class PitchLog extends AbstractSignalAlgorithm
{
	/**
	 * \brief Basic constructor, utilizes abstract class basic constructor.
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 */
	public PitchLog(float min, float max, float mean, float deadZone,
		float scale)
	{
		super(min, max, mean, deadZone, scale);
		this.type = Constants.Broadcast.MotorSignals.Algorithms.Pitch.LOG;
	}

	@Override
	public float[] convert(float pitch)
	{
		float deadZone = this.deadZone * scale;
		float minValue = this.minValue * scale;
		float maxValue = this.maxValue * scale;
		float mean = this.mean * scale;

		/**
		 * \brief Implementation of the logarithmic pitch algorithm.
		 * \author Daniel Josefsson
		 */
		float cutPitch = this.cut(pitch) - mean;
		float power = 0f;
		if (minValue <= cutPitch && cutPitch < -deadZone)
		{
			power = (float) (-Math.log10(-cutPitch) /
				Math.log10(minValue - mean));
		}
		else if (deadZone < cutPitch && cutPitch <= maxValue)
		{
			power = (float) (Math.log10(cutPitch) /
				Math.log10(maxValue - mean));
		}
		return new float[] { power, power };
	}
}
