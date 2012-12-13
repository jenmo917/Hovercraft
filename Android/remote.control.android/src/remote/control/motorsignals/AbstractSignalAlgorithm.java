package remote.control.motorsignals;

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

	AbstractSignalAlgorithm(float min, float max, float mean, float deadZone, float scale)
	{
		this.minValue = min;
		this.maxValue = max;
		this.mean = mean;
		this.deadZone = deadZone;
		this.scale = scale;
	}

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

	abstract public float[] convert(float value);

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

	protected void invert(boolean inv)
	{
		this.inverted = inv;
	}

	public String getType()
	{
		return this.type;
	}
}
