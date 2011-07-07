package net.projektfriedhof.geocaching;

public class GCData {

	private String gcCode;
	private String name;
	private String coord;
	
	public GCData(String gcCode) {
		this.gcCode = gcCode;
	}

	public String getGcCode() {
		return gcCode;
	}

	public String getName() {
		return name;
	}

	public String getCoord() {
		return coord;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCoord(String coord) {
		this.coord = coord;
	}

}
