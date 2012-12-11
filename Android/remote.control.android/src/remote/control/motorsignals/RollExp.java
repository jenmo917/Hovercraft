package remote.control.motorsignals;

import common.files.android.Constants;

public class RollExp extends AbstractSignalAlgorithm
{
	public RollExp(float min, float max, float mean, float deadZone, float scale)
	{
		super(min, max, mean, deadZone, scale);
		this.type = Constants.Broadcast.MotorSignals.Algorithms.Roll.EXP;
	}

	@Override
	public float[] convert(float roll)
	{
		float deadZone = this.deadZone * scale;
		float minValue = this.minValue * scale;
		float maxValue = this.maxValue * scale;
		float mean = this.mean * scale;

		float leftMotor = 0f;
		float rightMotor = 0f;

		float cutRoll = this.cut(roll) - mean;
		if (minValue <= cutRoll && cutRoll < -deadZone)
		{
			leftMotor = (float) (Math.exp(-cutRoll) /
				Math.exp(mean - minValue));
		}
		if (deadZone < cutRoll && cutRoll <= maxValue)
		{
			rightMotor = (float) (Math.exp(cutRoll) /
				Math.exp(maxValue - mean));
		}
		return new float[] { leftMotor, rightMotor };
	}
}
