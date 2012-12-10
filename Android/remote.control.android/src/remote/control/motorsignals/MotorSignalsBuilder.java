package remote.control.motorsignals;

public class MotorSignalsBuilder
{
	private MotorSignals assembly;

	public MotorSignalsBuilder()
	{
		assembly = new MotorSignals();
	}

	public MotorSignalsBuilder buildPitchLin(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setPitchAlgorithm(new PitchLinear(min, max, mean, deadZone,
			scale));
		return this;
	}

	public MotorSignalsBuilder buildPitchLog(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setPitchAlgorithm(new PitchLog(min, max, mean, deadZone,
			scale));
		return this;
	}

	public MotorSignalsBuilder buildPitchExp(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setPitchAlgorithm(new PitchExp(min, max, mean, deadZone,
			scale));
		return this;
	}

	public MotorSignalsBuilder buildRollLin(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setRollAlgorithm(new RollLinear(min, max, mean, deadZone,
			scale));
		return this;
	}

	public MotorSignalsBuilder buildRollLog(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setRollAlgorithm(new RollLog(min, max, mean, deadZone,
			scale));
		return this;
	}

	public MotorSignalsBuilder buildRollExp(float min, float max, float mean,
		float deadZone, float scale)
	{
		assembly.setRollAlgorithm(new RollExp(min, max, mean, deadZone,
			scale));
		return this;
	}

	public MotorSignals getResult()
	{
		return this.assembly;
	}
}
