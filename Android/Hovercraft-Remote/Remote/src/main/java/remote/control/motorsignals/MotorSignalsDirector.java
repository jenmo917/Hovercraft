package remote.control.motorsignals;

import se.liu.ed.Constants;
/**
 * Director part of builder pattern.
 * \brief
 * \author
 *
 */
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

	/**
	 * \brief Basic constructor.
	 * \author Daniel Josefsson
	 * @param builder
	 */
	public MotorSignalsDirector(MotorSignalsBuilder builder)
	{
		this.builder = builder;
	}

	/**
	 * \brief Returns a system with linear signal algorithms.
	 * \author Daniel Josefsson
	 * @return
	 */
	public MotorSignals buildLinearSystem()
	{
		this.buildPitchLin();
		this.buildRollLin();
		return this.builder.getResult();
	}

	/**
	 * \brief Returns a system with logarithmic signal algorithms.
	 * \author Daniel Josefsson
	 * @return
	 */
	public MotorSignals buildLogSystem()
	{
		this.buildPitchLog();
		this.buildRollLog();
		return this.builder.getResult();
	}

	/**
	 * \brief Returns a system with exponential pitch algorithms.
	 * \author Daniel Josefsson
	 * @return
	 */
	public MotorSignals buildExpSystem()
	{
		this.buildPitchExp();
		this.buildRollExp();
		return this.builder.getResult();
	}

	/**
	 * \brief Builds a exponential pitch algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildPitchExp()
	{
		this.builder.buildPitchExp(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
			CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_EXP_DEAD_ZONE,
			CONTROLLER_PITCH_EXP_SCALE);
		return this.builder;
	}

	/**
	 * \brief Builds a linear pitch algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildPitchLin()
	{
		this.builder.buildPitchLin(CONTROLLER_PITCH_MIN,
			CONTROLLER_PITCH_MAX, CONTROLLER_PITCH_MEAN,
			CONTROLLER_PITCH_LIN_DEAD_ZONE, CONTROLLER_PITCH_LIN_SCALE);
		return this.builder;
	}

	/**
	 * \brief Builds a logarithmic pitch algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildPitchLog()
	{
		this.builder.buildPitchLog(CONTROLLER_PITCH_MIN, CONTROLLER_PITCH_MAX,
			CONTROLLER_PITCH_MEAN, CONTROLLER_PITCH_LOG_DEAD_ZONE,
			CONTROLLER_PITCH_LOG_SCALE);
		return this.builder;
	}

	/**
	 * \brief Builds a linear pitch algorithm that can reverse.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildPitchReverseLin()
	{
		this.builder.buildPitchReverseLin(CONTROLLER_PITCH_MIN,
			CONTROLLER_PITCH_MAX, CONTROLLER_PITCH_REVERSE_MEAN,
			CONTROLLER_PITCH_LIN_DEAD_ZONE, CONTROLLER_PITCH_LIN_SCALE);
		return this.builder;
	}

	/**
	 * \brief Builds a exponential roll algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildRollExp()
	{
		this.builder.buildRollExp(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
			CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_EXP_DEAD_ZONE,
			CONTROLLER_ROLL_EXP_SCALE);
		return this.builder;
	}

	/**
	 * \brief Builds a linear roll algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildRollLin()
	{
		this.builder.buildRollLin(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
			CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_LIN_DEAD_ZONE,
			CONTROLLER_ROLL_LIN_SCALE);
		return this.builder;
	}

	/**
	 * \brief Builds a logarithmic roll algorithm.
	 * \author Daniel Josefsson
	 * @return
	 */
	private MotorSignalsBuilder buildRollLog()
	{
		this.builder.buildRollLog(CONTROLLER_ROLL_MIN, CONTROLLER_ROLL_MAX,
			CONTROLLER_ROLL_MEAN, CONTROLLER_ROLL_LOG_DEAD_ZONE,
			CONTROLLER_ROLL_LOG_SCALE);
		return this.builder;
	}

	/**
	 * \brief Change an algorithm.
	 * \author Daniel Josefsson
	 * @param ms The MotorSignal object.
	 * @param alg The algorithm.
	 * @param type Type of algorithm.
	 * @return
	 */
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

	/**
	 * \brief Change the pitch algorithm.
	 * \author Daniel Josefsson
	 * @param ms
	 * @param type
	 */
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

	/**
	 * \brief Change the roll algorithm.
	 * \author Daniel Josefsson
	 * @param ms
	 * @param type
	 */
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
