package remote.control.motorsignals;
/**
 * \brief MotorSignalsInterface defines , as the name states,the interface of a 
 * motor signals.
 * \author Daniel Josefsson
 */
public interface MotorSignalsInterface
{
	/**
	 * \brief Sets the pitch algorithm.
	 * \author Daniel Josefsson
	 * @param si
	 */
	public void setPitchAlgorithm(AbstractSignalAlgorithm si);

	/**
	 * \brief Sets the roll algorithm.
	 * \author Daniel Josefsson
	 * @param si
	 */
	public void setRollAlgorithm(AbstractSignalAlgorithm si);

	/**
	 * \brief Converts pitch and roll to motor signals. Pitch and roll shall be
	 * normalized values. The return array is filled with left motor power
	 * followed by direction and then the same for the right motor. Motor power
	 * are values between 0 and 2^exp - 1
	 * 
	 * @param float pitch
	 * @param float roll
	 * @param int exp
	 * @return int[] {lMPower, lMDirection, rMPower, rMDirection}
	 */
	public int[] convert(float pitch, float roll, int exp);
}
