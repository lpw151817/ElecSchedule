package nercms.schedule.utils;

import java.io.Serializable;

public class MyGPS implements Serializable{
	private String time;
	private double longitude;
	private double latitude;
	private float radius;
	private double altitude;
	private float speed;
	private String coorType;

	public MyGPS(String time, double longitude, double latitude,
			float radius, double altitude, float speed, String coorType) {
		super();
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
		this.radius = radius;
		this.altitude = altitude;
		this.speed = speed;
		this.coorType = coorType;
	}

	public MyGPS(){
		
	}
	
	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public double getAltitude() {
		return altitude;
	}

	public void setAltitude(double altitude) {
		this.altitude = altitude;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}

	public String getCoorType() {
		return coorType;
	}

	public void setCoorType(String coorType) {
		this.coorType = coorType;
	}

	@Override
	public String toString() {
		return "MyGPS [time=" + time + ", longitude=" + longitude
				+ ", latitude=" + latitude + ", radius=" + radius
				+ ", altitude=" + altitude + ", speed=" + speed + ", coorType="
				+ coorType + "]";
	}
	
	

}
