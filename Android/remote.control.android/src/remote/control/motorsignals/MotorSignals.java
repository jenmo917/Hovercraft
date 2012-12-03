package remote.control.motorsignals;

public class MotorSignals implements MotorSignalsInterface
{
	private AbstractSignalAlgorithm	pitchAlgorithm;
	private AbstractSignalAlgorithm	rollAlgorithm;

	@Override
	public void setPitchAlgorithm(AbstractSignalAlgorithm si)
	{
		pitchAlgorithm = si;
	}

	@Override
	public void setRollAlgorithm(AbstractSignalAlgorithm si)
	{
		rollAlgorithm = si;
	}

	@Override
	public int[] convert(float pitch, float roll)
	{
		float[] cPitch = pitchAlgorithm.convert(pitch);
		float[] cRoll = rollAlgorithm.convert(roll);
		int leftMotor = Math.round(Math.scalb(cPitch[0] * (1 - cRoll[0]), 8));
		int rightMotor = Math.round(Math.scalb(cPitch[1] * (1 - cRoll[1]), 8));
		return new int[] { leftMotor, rightMotor };
	}

}
