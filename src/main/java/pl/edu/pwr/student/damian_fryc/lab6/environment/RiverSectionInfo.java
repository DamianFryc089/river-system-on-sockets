package pl.edu.pwr.student.damian_fryc.lab6.environment;

public class RiverSectionInfo {
	private final int port;
	private final String host;
	public int rainAmount = 0;

	RiverSectionInfo(int port, String host){
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
				'}';
	}
}