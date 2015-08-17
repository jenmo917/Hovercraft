package remote.control.motorsignals;
/**
 * \brief Class implementing the motor signals interface.
 * \author Daniel Josefsson
 *
 */
public class MotorSignals implements MotorSignalsInterface
{
	private AbstractSignalAlgorithm	pitchAlgorithm;
	private AbstractSignalAlgorithm	rollAlgorithm;

	/**
	 * \brief Implements corresponding function in MotorSignalsInterface.
	 * \author Daniel Josefsson
	 */
	@Override
	public void setPitchAlgorithm(AbstractSignalAlgorithm si)
	{
		pitchAlgorithm = si;
	}

	/**
	 * Returns current pitch algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	public AbstractSignalAlgorithm getPitchAlgorithm()
	{
		return this.pitchAlgorithm;
	}

	/**
	 * Returns current roll algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	public AbstractSignalAlgorithm getRollAlgorithm()
	{
		return this.rollAlgorithm;
	}

	/**
	 * \brief Implements corresponding function in MotorSignalsInterface.
	 * \author Daniel Josefsson
	 */
	@Override
	public void setRollAlgorithm(AbstractSignalAlgorithm si)
	{
		rollAlgorithm = si;
	}

	/**
	 * \brief Implements corresponding function in MotorSignalsInterface.
	 * \author Daniel Josefsson
	 * @param pitch
	 * @param roll
	 * @param exp Maximum resolution of output [0, 2^exp - 1].
	 */
	@Override
	public int[] convert(float pitch, float roll, int exp)
	{
		int max = (int) Math.pow(2, exp);
		float[] cPitch = pitchAlgorithm.convert(pitch);
		float[] cRoll = rollAlgorithm.convert(roll);
		int lMDirection = 1;
		int rMDirection = 1;
		float lmRaw = cPitch[0] * (1 - cRoll[0]);
		float rmRaw = cPitch[1] * (1 - cRoll[1]);
		if (0f > lmRaw)
		{
			lMDirection = -1;
			lmRaw = -1 * lmRaw;
		}
		if (0f > rmRaw)
		{
			rMDirection = -1;
			rmRaw = -1 * rmRaw;
		}
		int lMotor = (int) Math
			.floor(Math.scalb(lmRaw, exp));
		int rMotor = (int) Math
			.floor(Math.scalb(rmRaw, exp));
		if (max == lMotor)
		{
			lMotor = lMotor - 1;
		}
		if (max == rMotor)
		{
			rMotor = rMotor - 1;
		}
		return new int[] { lMotor, lMDirection, rMotor, rMDirection };
	}
}
