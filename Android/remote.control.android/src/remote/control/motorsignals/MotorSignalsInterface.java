package remote.control.motorsignals;

public interface MotorSignalsInterface
{
	public void setPitchAlgorithm(AbstractSignalAlgorithm si);

	public void setRollAlgorithm(AbstractSignalAlgorithm si);

	public int[] convert(float pitch, float roll);
}
