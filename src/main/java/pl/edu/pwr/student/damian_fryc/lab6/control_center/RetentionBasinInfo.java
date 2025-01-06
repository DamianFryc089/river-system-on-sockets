package pl.edu.pwr.student.damian_fryc.lab6.control_center;

public class RetentionBasinInfo{
	private final int port;
	private final String host;
	public int waterDischarge = 0;
	public long fillingPercentage = 0;

	RetentionBasinInfo(int port, String host){
		this.port = port;
		this.host = host;
	}

	public int getPort() {
		return port;
	}
	public String getHost() {
		return host;
	}

	@Override
	public String toString() {
		return "RetentionBasinInfo{" +
				"port=" + port +
				", host='" + host + '\'' +
				", waterDischarge=" + waterDischarge +
				", fillingPercentage=" + fillingPercentage +
				'}';
	}
}