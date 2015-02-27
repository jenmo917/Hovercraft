package remote.control.motorsignals;

import common.files.android.Constants;
/**
 * \brief Implements the builder in a builder pattern.
 * \author Daniel Josefsson
 */
public class MotorSignalsBuilder
{
	private MotorSignals assembly;

	/**
	 * \brief Basic constructor.
	 * \author Daniel Josefsson
	 */
	public MotorSignalsBuilder()
	{
		assembly = new MotorSignals();
	}

	/**
	 * \brief Builds a linear pitch algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildPitchLin(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setPitchAlgorithm(new PitchLinear(min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Builds a reverse linear pitch algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildPitchReverseLin(float min, float max,
		float mean, float deadZone, float scale)
	{
		String type = Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN_REV;
		assembly.setPitchAlgorithm(new PitchLinear(type, min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Builds a logarithmic pitch algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildPitchLog(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setPitchAlgorithm(new PitchLog(min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Builds an exponential pitch algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildPitchExp(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setPitchAlgorithm(new PitchExp(min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Builds a linear roll algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildRollLin(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setRollAlgorithm(new RollLinear(min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Builds a logarithmic roll algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildRollLog(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setRollAlgorithm(new RollLog(min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Builds an exponential roll algorithm.
	 * \author Daniel Josefsson
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone
	 * @param scale
	 * @return
	 */
	public MotorSignalsBuilder buildRollExp(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setRollAlgorithm(new RollExp(min, max, mean, deadZone,
			scale));
		return this;
	}

	/**
	 * \brief Return the result.
	 * \author Daniel Josefsson
	 * @return
	 */
	public MotorSignals getResult()
	{
		return this.assembly;
	}
}
