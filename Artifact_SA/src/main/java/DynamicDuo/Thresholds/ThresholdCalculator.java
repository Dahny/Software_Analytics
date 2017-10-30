package DynamicDuo.Thresholds;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.IO.LineHandler;

public class ThresholdCalculator {

	private List<Integer> metrics;
	
	final private double _80th = 0.80d;
	final private double _90th = 0.90d;
	final private double _95th = 0.95d;
	
	public ThresholdCalculator() {
		metrics = new ArrayList<Integer>();
	}
	
	public void calculateThresholds(String colName, int colId) throws IOException {
		load(String.format("../%s-%s.csv", "elasticsearch", "monthly-metrics"), colId);
		load(String.format("../%s-%s.csv", "hadoop", "monthly-metrics"), colId);
		load(String.format("../%s-%s.csv", "sonarqube", "monthly-metrics"), colId);		
		
		Collections.sort(metrics);
		//calculate 80th, 90th and 95th quantile
		int x80th = (int) Math.ceil(_80th * metrics.size()) - 1;
		System.out.println("80th quantile for "+colName+" = " + metrics.get(x80th) );
		int x90th = (int) Math.ceil(_90th * metrics.size()) - 1;
		System.out.println("90th quantile for "+colName+" = " + metrics.get(x90th) );
		int x95th = (int) Math.ceil(_95th * metrics.size()) - 1;
		System.out.println("95th quantile for "+colName+" = " + metrics.get(x95th) );
		
	}
	
	private void load(String filepath, int colId) throws IOException {
		IOHandler.readLines(filepath, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
				Integer metric = Integer.parseInt(cols[colId]);
				metrics.add(metric);
			}
			
		});
	}
	
	public static void main(String[] args) {
		try {
			new ThresholdCalculator().calculateThresholds("WMC", 17);
			System.out.println("---");
			new ThresholdCalculator().calculateThresholds("NOM", 12);
			System.out.println("---");
			new ThresholdCalculator().calculateThresholds("NOF", 9);
			System.out.println("---");
			new ThresholdCalculator().calculateThresholds("LOC", 18);
			System.out.println("---");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
