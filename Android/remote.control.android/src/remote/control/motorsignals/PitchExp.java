package remote.control.motorsignals;

public class PitchExp extends AbstractSignalAlgorithm
{

	public PitchExp(float min, float max, float mean, float deadZone,
		float scale)
	{
		super(min, max, mean, deadZone, scale);
	}

	@Override
	public float[] convert(float pitch)
	{
		float deadZone = this.deadZone * scale;
		float minValue = this.minValue * scale;
		float maxValue = this.maxValue * scale;
		float mean = this.mean * scale;

		float cutPitch = cut(pitch) - mean;
		float power = 0;
		if (minValue <= cutPitch && cutPitch < -deadZone)
		{
			power = (float) (-Math.exp(-cutPitch) /
				Math.exp(minValue - mean));
		}
		else if (deadZone < cutPitch && cutPitch <= maxValue)
		{
			power = (float) (Math.exp(cutPitch) /
				Math.exp(maxValue - mean));
		}
		return new float[] { power, power };
	}
}
