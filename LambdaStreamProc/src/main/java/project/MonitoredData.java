package project;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class MonitoredData {

	private final LocalDateTime start_time;
	private final LocalDateTime end_time;
	private final String activity;

	private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

	public MonitoredData(LocalDateTime s, LocalDateTime e, String a) {
		this.start_time = s;
		this.end_time = e;
		this.activity = a;
	}

	public LocalDateTime getStart_time() {
		return start_time;
	}

	public LocalDateTime getEnd_time() {
		return end_time;
	}

	public String getActivity() {
		return activity;
	}

	public String getDate() {
		return start_time.format(formatter).substring(0, 10);
	}

	public Integer computeDurationInSeconds() {
		Integer seconds = Integer.valueOf(String.valueOf(this.start_time.until(this.end_time, ChronoUnit.SECONDS)));
		return seconds;
	}

	public String toString() {
		return this.start_time.format(formatter) + "\t\t" + this.end_time.format(formatter) + "\t\t" + this.activity;
	}

}
