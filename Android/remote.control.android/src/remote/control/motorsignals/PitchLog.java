package remote.control.motorsignals;

public class PitchLog extends AbstractSignalAlgorithm
{
	public PitchLog(float min, float max, float mean, float deadZone,
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

		float cutPitch = this.cut(pitch) - mean;
		float power = 0f;
		if (minValue <= cutPitch && cutPitch < -deadZone)
		{
			power = (float) (-Math.log10(-cutPitch) /
				Math.log10(minValue - mean));
		}
		else if (deadZone < cutPitch && cutPitch <= maxValue)
		{
			power = (float) (Math.log10(cutPitch) /
				Math.log10(maxValue - mean));
		}
		return new float[] { power, power };
	}
}
