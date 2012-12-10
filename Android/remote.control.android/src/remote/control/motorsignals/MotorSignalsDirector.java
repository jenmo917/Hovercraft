package remote.control.motorsignals;

public class MotorSignalsDirector
{
	private MotorSignalsBuilder builder;

	final float CONTROLLER_PITCH_MIN = 0f;
	final float CONTROLLER_PITCH_MAX = 1f;
	final float CONTROLLER_PITCH_MEAN = 0f;

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
		return this.builder
			.buildPitchLin(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
				CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_LIN_DEAD_ZONE,
				CONTROLLER_PITCH_LIN_SCALE)
			.buildRollLin(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
				CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_LIN_DEAD_ZONE,
				CONTROLLER_ROLL_LIN_SCALE)
							.getResult();
	}

	public MotorSignals buildLogSystem()
	{
		return this.builder
			.buildPitchLog(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
				CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_LOG_DEAD_ZONE,
				CONTROLLER_PITCH_LOG_SCALE)
			.buildRollLog(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
				CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_LOG_DEAD_ZONE,
				CONTROLLER_ROLL_LOG_SCALE)
			.getResult();
	}

	public MotorSignals buildExpSystem()
	{
		return this.builder
			.buildPitchExp(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
				CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_EXP_DEAD_ZONE,
				CONTROLLER_PITCH_EXP_SCALE)
			.buildRollExp(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
				CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_EXP_DEAD_ZONE,
				CONTROLLER_ROLL_EXP_SCALE)
			.getResult();
	}
}
