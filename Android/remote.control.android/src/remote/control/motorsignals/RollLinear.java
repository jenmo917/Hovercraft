package remote.control.motorsignals;

public class RollLinear extends AbstractSignalAlgorithm
{

	public RollLinear(float min, float max, float mean, float deadZone)
	{
		super(min, max, mean, deadZone);
	}

	@Override
	public float[] convert(float roll)
	{
		float leftMotor = 0f;
		float rightMotor = 0f;

		float cutRoll = this.cut(roll) - mean;
		if (cutRoll < -this.deadZone)
		{
			leftMotor = -cutRoll / (this.maxValue - mean);
		}
		if (this.deadZone < cutRoll)
		{

			rightMotor = cutRoll / (this.maxValue - mean);
		}
		return new float[] { leftMotor, rightMotor };
	}

}
