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
		if (this.deadZone < cutPitch && cutPitch < +this.deadZone)
		{
			cutPitch = 0f;
		}
		float forward = cutPitch / (this.maxValue - this.mean);
		return new float[] { forward, forward };
	}

}
