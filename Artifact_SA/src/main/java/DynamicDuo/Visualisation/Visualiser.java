package DynamicDuo.Visualisation;

public class Visualiser {
	
	public static void main(String[] args) {
		new MetricCorrelationVisualiser("NOM",12).plot();
		new MetricCorrelationVisualiser("NOF",9).plot();
		new MetricCorrelationVisualiser("LOC",18).plot();
		//new MetricCorrelationVisualiser("MI",4).plot();
		new MetricCorrelationVisualiser("WMC",17).plot();
	}
	
	
}
