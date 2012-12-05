package remote.control.motorsignals;

public interface MotorSignalsInterface
{
	public void setPitchAlgorithm(AbstractSignalAlgorithm si);

	public void setRollAlgorithm(AbstractSignalAlgorithm si);

	/**
	 * Converts pitch and roll to motor signals. Pitch and roll shall be
	 * normalized values. The return array is filled with left motor power
	 * followed by direction and then the same for the right motor. Motor power
	 * are values between 0 and 2^exp - 1
	 * 
	 * @param float pitch
	 * @param float roll
	 * @return int[] {lMPower, lMDirection, rMPower, rMDirection}
	 */
	public int[] convert(float pitch, float roll, int exp);
}
