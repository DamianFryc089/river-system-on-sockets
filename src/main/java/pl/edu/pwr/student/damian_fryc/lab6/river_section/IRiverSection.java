package pl.edu.pwr.student.damian_fryc.lab6.river_section;

public interface IRiverSection {
	void setRealDischarge(int realDischarge);
	void setRainfall(int rainfall);
	void assignRetentionBasin(int port, String host);
}
