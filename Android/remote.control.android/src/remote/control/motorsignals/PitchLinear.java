package remote.control.motorsignals;

public class PitchLinear extends AbstractSignalAlgorithm
{
	public PitchLinear(float min, float max, float mean, float deadZone)
	{
		super(min, max, mean, deadZone);
	}

	@Override
	public float[] convert(float pitch)
	{
		float cutPitch = this.cut(pitch) - this.mean;
		if (this.deadZone < cutPitch && cutPitch < this.deadZone)
		{
			cutPitch = 0f;
		}
		float power;
		if (0f <= cutPitch)
		{
			power = cutPitch / (this.maxValue - this.mean);
		}
		else
		{
			power = cutPitch / (this.mean - this.minValue);
		}
		return new float[] { power, power };
	}

}
