package remote.control.motorsignals;
/**
 * \brief AbstractSignalAlgorithm is an implementation of the
 * SignalAlgorithmInterface.
 * 
 * \author Daniel Josefsson
 *
 */
public abstract class AbstractSignalAlgorithm implements
	SignalAlgorithmInterface
{
	protected float		maxValue;
	protected float		minValue;
	protected float		deadZone;
	protected float		mean;
	protected float scale;
	protected boolean	inverted;
	protected String type;

	/**
	 * \brief Basic constructor.
	 * 
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone [mean - deadZone, mean + deadZone] = 0
	 * @param scale Scale factor that can be used of an implementation.
	 */
	AbstractSignalAlgorithm(float min, float max, float mean, float deadZone, float scale)
	{
		this.minValue = min;
		this.maxValue = max;
		this.mean = mean;
		this.deadZone = deadZone;
		this.scale = scale;
	}

	/**
	 * \brief Basic constructor.
	 * 
	 * @param type Name of the algorithm
	 * @param min
	 * @param max
	 * @param mean
	 * @param deadZone [mean - deadZone, mean + deadZone] = 0
	 * @param scale Scale factor that can be used of an implementation.
	 */
	AbstractSignalAlgorithm(String type, float min, float max, float mean,
		float deadZone, float scale)
	{
		this.minValue = min;
		this.maxValue = max;
		this.mean = mean;
		this.deadZone = deadZone;
		this.scale = scale;
		this.type = type;
	}

	/**
	 * \brief The implementation of this SignalAlgorithmInterface function is
	 * redirected to the implementation of this class.
	 * @param value
	 * @return float[]
	 * \author Daniel Josefsson
	 */
	abstract public float[] convert(float value);

	/**
	 * \brief Constraints to [min, max] and returns it times the scale. It also
	 * inverts the value if that flag is set.
	 * 
	 * \author Daniel Josefsson
	 * @param value
	 * @return float
	 */
	protected float cut(float value)
	{
		float returnValue;
		if (this.inverted)
		{
			returnValue = -value;
		}
		else
		{
			returnValue = value;
		}
		if (this.minValue > returnValue)
		{
			returnValue = this.minValue;
		}
		else if (this.maxValue < returnValue)
		{
			returnValue = this.maxValue;
		}
		return returnValue * this.scale;
	}

	/**
	 * \brief Flag that decides if the input data should be inverted.
	 * \author Daniel Josefsson
	 * @param inv
	 */
	protected void invert(boolean inv)
	{
		this.inverted = inv;
	}

	/**
	 * \brief Returns the type/name of the algorithm. Impleme
	 */
	public String getType()
	{
		return this.type;
	}
}
