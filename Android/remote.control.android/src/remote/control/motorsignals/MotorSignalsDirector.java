package remote.control.motorsignals;

import common.files.android.Constants;

public class MotorSignalsDirector
{
	private MotorSignalsBuilder builder;

	final float CONTROLLER_PITCH_MIN = 0f;
	final float CONTROLLER_PITCH_MAX = 1f;
	final float CONTROLLER_PITCH_MEAN = 0f;
	final float CONTROLLER_PITCH_REVERSE_MEAN = 0.6f;

	final float CONTROLLER_PITCH_LIN_DEAD_ZONE = 0.05f;
	final float CONTROLLER_PITCH_LIN_SCALE = 1f;

	final float CONTROLLER_PITCH_LOG_DEAD_ZONE = 0.1f;
	final float CONTROLLER_PITCH_LOG_SCALE = 10f;

	final float CONTROLLER_PITCH_EXP_DEAD_ZONE = 0.05f;
	final float CONTROLLER_PITCH_EXP_SCALE = 5f;

	final float CONTROLLER_ROLL_MIN = -0.65f;
	final float CONTROLLER_ROLL_MAX = 0.65f;
	final float CONTROLLER_ROLL_MEAN = 0;

	final float CONTROLLER_ROLL_LIN_DEAD_ZONE = 0.05f;
	final float CONTROLLER_ROLL_LIN_SCALE = 1f;

	final float CONTROLLER_ROLL_LOG_DEAD_ZONE = 0.1f;
	final float CONTROLLER_ROLL_LOG_SCALE = 10f;

	final float CONTROLLER_ROLL_EXP_DEAD_ZONE = 0.05f;
	final float CONTROLLER_ROLL_EXP_SCALE = 5f;

	public MotorSignalsDirector(MotorSignalsBuilder builder)
	{
		this.builder = builder;
	}

	public MotorSignals buildLinearSystem()
	{
		this.buildPitchLin();
		this.buildRollLin();
		return this.builder.getResult();
	}

	public MotorSignals buildLogSystem()
	{
		this.buildPitchLog();
		this.buildRollLog();
		return this.builder.getResult();
	}

	public MotorSignals buildExpSystem()
	{
		this.buildPitchExp();
		this.buildRollExp();
		return this.builder.getResult();
	}

	private MotorSignalsBuilder buildPitchExp()
	{
		this.builder.buildPitchExp(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
			CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_EXP_DEAD_ZONE,
			CONTROLLER_PITCH_EXP_SCALE);
		return this.builder;
	}

	private MotorSignalsBuilder buildPitchLin()
	{
		this.builder.buildPitchLin(CONTROLLER_PITCH_MIN,
			CONTROLLER_PITCH_MAX, CONTROLLER_PITCH_MEAN,
			CONTROLLER_PITCH_LIN_DEAD_ZONE, CONTROLLER_PITCH_LIN_SCALE);
		return this.builder;
	}

	private MotorSignalsBuilder buildPitchLog()
	{
		this.builder.buildPitchLog(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
			CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_LOG_DEAD_ZONE,
			CONTROLLER_PITCH_LOG_SCALE);
		return this.builder;
	}

	private MotorSignalsBuilder buildPitchReverseLin()
	{
		this.builder.buildPitchReverseLin(CONTROLLER_PITCH_MIN,
			CONTROLLER_PITCH_MAX, CONTROLLER_PITCH_REVERSE_MEAN,
			CONTROLLER_PITCH_LIN_DEAD_ZONE, CONTROLLER_PITCH_LIN_SCALE);
		return this.builder;
	}

	private MotorSignalsBuilder buildRollExp()
	{
		this.builder.buildRollExp(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
			CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_EXP_DEAD_ZONE,
			CONTROLLER_ROLL_EXP_SCALE);
		return this.builder;
	}

	private MotorSignalsBuilder buildRollLin()
	{
		this.builder.buildRollLin(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
			CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_LIN_DEAD_ZONE,
			CONTROLLER_ROLL_LIN_SCALE);
		return this.builder;
	}

	private MotorSignalsBuilder buildRollLog()
	{
		this.builder.buildRollLog(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
			CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_LOG_DEAD_ZONE,
			CONTROLLER_ROLL_LOG_SCALE);
		return this.builder;
	}

	public MotorSignals changeAlgorithm(MotorSignals ms, String alg, String type)
	{
		if (alg.equals(Constants.Broadcast.MotorSignals.Algorithms.PITCH))
		{
			this.changePitchAlgorithm(ms, type);
		}
		else if (alg.equals(Constants.Broadcast.MotorSignals.Algorithms.ROLL))
		{
			this.changeRollAlgorithm(ms, type);
		}
		return this.builder.getResult();
	}

	private void changePitchAlgorithm(MotorSignals ms, String type)
	{
		if (type.equals(Constants.Broadcast.MotorSignals.Algorithms.Pitch.EXP))
		{
			this.buildPitchExp();
		}
		else if (type
			.equals(Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN))
		{
			this.buildPitchLin();
		}
		else if (type
			.equals(Constants.Broadcast.MotorSignals.Algorithms.Pitch.LOG))
		{
			this.buildPitchLog();
		}
		else if (type
			.equals(Constants.Broadcast.MotorSignals.Algorithms.Pitch.LIN_REV))
		{
			this.buildPitchReverseLin();
		}
	}

	private void changeRollAlgorithm(MotorSignals ms, String type)
	{
		if (type.equals(Constants.Broadcast.MotorSignals.Algorithms.Roll.EXP))
		{
			this.buildRollExp();
		}
		else if (type
			.equals(Constants.Broadcast.MotorSignals.Algorithms.Roll.LIN))
		{
			this.buildRollLin();
		}
		else if (type
			.equals(Constants.Broadcast.MotorSignals.Algorithms.Roll.LOG))
		{
			this.buildRollLog();
		}
	}
}
