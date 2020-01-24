package ab.demo.other;

public class CompetitionParameters {
	
	private byte roundType = 1;
	private byte timeLimitMinutes = 10;
	private byte numLeves = 21;
	
	public CompetitionParameters() {};
	
	public CompetitionParameters(byte roundType, byte timeLimitMinutes, byte numLevels) {
		this.roundType = roundType;
		this.timeLimitMinutes = timeLimitMinutes;
		this.numLeves = numLevels;
	}

	public byte getRoundType() {
		return roundType;
	}

	public void setRoundType(byte roundType) {
		this.roundType = roundType;
	}

	public byte getTimeLimitMinutes() {
		return timeLimitMinutes;
	}

	public void setTimeLimitMinutes(byte timeLimitMinutes) {
		this.timeLimitMinutes = timeLimitMinutes;
	}

	public byte getNumLeves() {
		return numLeves;
	}

	public void setNumLeves(byte numLeves) {
		this.numLeves = numLeves;
	}
}
