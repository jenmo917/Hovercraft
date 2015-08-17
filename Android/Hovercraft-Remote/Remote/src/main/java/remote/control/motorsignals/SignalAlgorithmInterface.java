package remote.control.motorsignals;
/**
 * \brief SignalAlgorithmInterface defines , as the name states,the interface of a 
 * signal algorithm.
 * \author Daniel Josefsson
 */
public interface SignalAlgorithmInterface
{
	/**
	 * Converts an raw signal to an desired output.
	 * @param value
	 * @return
	 */
	public float[] convert(float value);

	/**
	 * Returns the type/name of the signal algorithm.
	 * @return
	 */
	public String getType();
}
