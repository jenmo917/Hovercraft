package remote.control.motorsignals;

import common.files.android.Constants;

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

	public MotorSignalsBuilder buildPitchReverseLin(float min, float max,
		float mean, float deadZone, float scale)
	{
		String type = Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN_REV;
		assembly.setPitchAlgorithm(new PitchLinear(type, min, max, mean, deadZone,
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
