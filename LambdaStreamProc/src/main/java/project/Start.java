package project;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class Start {

	private final String FILE_NAME = "Activities.txt";
	private final String WRITER_FILE_NAME = "Results.txt";
	private List<MonitoredData> data;

	public Start() {
		data = new ArrayList<MonitoredData>();
	}

	private void streamReader() {
		try {
			data = Files.lines(Paths.get(FILE_NAME)).map(line -> line.split("\t\t")).map(line -> {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
				while (line[2].contains("\t")) {
					line[2] = line[2].substring(0, line[2].indexOf("\t"));
				}
				MonitoredData md = new MonitoredData(LocalDateTime.parse(line[0], formatter),
						LocalDateTime.parse(line[1], formatter), line[2]);
				return md;
			}).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printData() {
		data.stream().map(MonitoredData::toString).forEachOrdered(System.out::println);
	}

	private long countDaysOfMonitoredDataLog() {
		return data.stream().map(day -> day.getDate()).distinct().count();
	}

	private ConcurrentMap<String, Integer> countActivityOnEntireMonitoringPeriod() {
		return data.parallelStream().map(act -> act.getActivity())
				.collect(Collectors.toConcurrentMap(act -> act, act -> 1, Integer::sum));
	}

	private Map<String, Map<String, Integer>> countEachActivityForEachDay() {
		return data.stream().collect(Collectors.groupingBy(MonitoredData::getDate,
				Collectors.groupingBy(MonitoredData::getActivity, Collectors.reducing(0, e -> 1, Integer::sum))));
	}

	private List<String> getActivityDuration() {
		return data.stream().map(act -> act.toString() + "\t\tDuration: " + act.computeDurationInSeconds() + " seconds")
				.collect(Collectors.toList());
	}

	private Map<String, Integer> getActivityTotalDuration() {
		return data.stream().collect(Collectors.groupingBy(MonitoredData::getActivity,
				Collectors.summingInt(MonitoredData::computeDurationInSeconds)));
	}

	private List<String> filterActivities() {
		return data.stream().collect(Collectors.groupingBy(MonitoredData::getActivity, Collectors.toSet())).entrySet()
				.stream().map(md -> {
					int count = 0;
					for (MonitoredData x : md.getValue()) {
						if (x.computeDurationInSeconds() < 5 * 60) {
							count++;
						}
					}
					if (count >= (float) 90 / 100 * md.getValue().size()) {
						return md.getKey();
					}
					return null;
				}).filter(md -> md != null).collect(Collectors.toList());
	}

	private void writeResultsToFile() {
		try {

			PrintWriter pw = new PrintWriter(WRITER_FILE_NAME);

			pw.println("1. Days of monitored data in the log: " + countDaysOfMonitoredDataLog());
			pw.println();
			pw.println();

			pw.println("2. Total count of each activity:");
			pw.println();
			ConcurrentMap<String, Integer> activities = countActivityOnEntireMonitoringPeriod();
			for (Entry<String, Integer> entry : activities.entrySet()) {
				pw.println(entry.getKey() + ": " + entry.getValue().toString());
			}
			pw.println();
			pw.println();

			pw.println("3. Apparition of activities each day:");
			pw.println();
			Map<String, Map<String, Integer>> act_per_day = countEachActivityForEachDay();
			for (Entry<String, Map<String, Integer>> day : act_per_day.entrySet()) {
				for (Entry<String, Integer> act : day.getValue().entrySet()) {
					pw.println(day.getKey() + "\t" + act.getKey() + "\t" + act.getValue());
				}
			}
			pw.println();
			pw.println();

			pw.println("4. Activity duration:");
			pw.println();
			List<String> durations = getActivityDuration();
			for (String st : durations) {
				pw.println(st);
			}
			pw.println();
			pw.println();

			pw.println("5. Total activities durations:");
			pw.println();
			Map<String, Integer> tdurations = getActivityTotalDuration();
			for (Entry<String, Integer> entry : tdurations.entrySet()) {
				pw.println(entry.getKey() + ": " + entry.getValue() + " seconds");
			}
			pw.println();
			pw.println();

			pw.println("6. Filtered activities:");
			pw.println();
			List<String> lst = filterActivities();
			for (String st : lst) {
				pw.println(st);
			}

			pw.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		Start s = new Start();
		s.streamReader();
		s.printData();
		s.writeResultsToFile();

	}

}
