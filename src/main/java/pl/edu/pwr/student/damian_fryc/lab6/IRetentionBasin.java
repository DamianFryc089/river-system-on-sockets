package pl.edu.pwr.student.damian_fryc.lab6;

public interface IRetentionBasin {
	int getWaterDischarge();
	long getFillingPercentage();
	void setWaterDischarge(int waterDischarge);
	void setWaterInflow(int waterInflow, int port);
	void assignRiverSection(int port, String host);
}
