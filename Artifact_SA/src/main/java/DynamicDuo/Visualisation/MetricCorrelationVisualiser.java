package DynamicDuo.Visualisation;

import java.awt.Color;
import java.awt.Shape;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.LineFunction2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.statistics.Regression;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.util.ShapeUtilities;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.IO.LineHandler;
import DynamicDuo.Models.ClassPairModel;
import DynamicDuo.Study.StudyConstants;

public class MetricCorrelationVisualiser {

	private String classPairsFilePath;
	private String monthlyMetricsFilePath;
	
	private HashMap<String, List<ClassPairModel>> commitHashToClassPairs;
	private HashMap<String, Integer> hashClassToMetric;
	
	private int maxM = -1;
	private String metricName;
	private int colId;
	
	
	public MetricCorrelationVisualiser(String metricName, int colId) {
		classPairsFilePath = StudyConstants.CSV_Class_Pairs;
		monthlyMetricsFilePath = StudyConstants.CSV_Monthly_Metrics;
		commitHashToClassPairs = new HashMap<String, List<ClassPairModel>>();
		hashClassToMetric = new HashMap<String, Integer>();
		
		this.metricName = metricName;
		this.colId = colId;
		
		try {
			System.out.println("loading data");
			loadClassPairs();
			loadMetricData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void plot() {
		XYDataset inputData = createDataset();
		String plotName = StudyConstants.Repo_Name + " - " + metricName;
		JFreeChart chart = ChartFactory.createScatterPlot(
				plotName, // chart title
	            "test", // x axis label
	            "production", // y axis label
	            inputData, // data
	            PlotOrientation.HORIZONTAL,
	            true, // include legend
	            true, // tooltips
	            false // urls
	            );
		ChartFrame frame = new ChartFrame(plotName, chart);
		
		Shape diamond = ShapeUtilities.createDiamond(2);
		XYPlot plot = (XYPlot) chart.getPlot();
		XYItemRenderer renderer = plot.getRenderer();
		renderer.setSeriesShape(0, diamond);
		
		drawRegressionLine(chart,(XYSeriesCollection)inputData);
		
        frame.pack();
        frame.setVisible(true);
	}
	
	private void loadClassPairs() throws IOException {
		IOHandler.readLines(classPairsFilePath, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
				List<ClassPairModel> pairs;
				String key = cols[0].toLowerCase();
				if(commitHashToClassPairs.containsKey(key)) {
					pairs = commitHashToClassPairs.get(key);
				} else {
					pairs = new ArrayList<ClassPairModel>();
				}
				pairs.add(new ClassPairModel(cols[2],cols[1]));
				commitHashToClassPairs.put(key, pairs);
			}
			
		});
	}
	
	private void loadMetricData() throws IOException {
		
		IOHandler.readLines(monthlyMetricsFilePath, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
				String key = cols[0]+cols[3];
				key = key.toLowerCase();
				
				Integer metric;
				if(hashClassToMetric.containsKey(key)) {
					System.err.println("THIS SHOULD NOT HAPPEN");
					metric = -1;
				} else {
					metric = Integer.parseInt(cols[colId]);
				}
				hashClassToMetric.put(key, metric);
			}
			
		});
	}
	
	
	private XYDataset createDataset() {
		int successCount = 0;
		int failureCount = 0;
		
		maxM = 0;
		
	    XYSeriesCollection result = new XYSeriesCollection();
	   	Iterator<String> hashes = commitHashToClassPairs.keySet().iterator();
	   	XYSeries series = new XYSeries(metricName + " - " + StudyConstants.Repo_Name);
	    while(hashes.hasNext()) {
	    	String hash = hashes.next();
	    	if(hashes.hasNext()) continue;
	    	
	    	List<ClassPairModel> pairs = commitHashToClassPairs.get(hash);
	    	for(ClassPairModel pair : pairs) {
	    		Integer prodMetric = hashClassToMetric.get((hash+pair.getProductionClass()).toLowerCase());
	    		Integer testMetric = hashClassToMetric.get((hash+pair.getTestClass()).toLowerCase());
	    		if(testMetric == null || prodMetric == null) {
	    			failureCount++;
	    			continue;
	    		}
	    		if(testMetric > maxM || prodMetric > maxM) {
	    			maxM = testMetric > prodMetric ? testMetric : prodMetric;
	    		}
	    		successCount++;
	    		series.add(prodMetric, testMetric);
	    	}
	    	
	    }
	    result.addSeries(series);
		System.out.println("Max " + metricName + " found: " + maxM);

	    System.out.println("Done creating dataset: " + successCount + " - " + failureCount);
	    
	    return result;
	}
	
	private void drawRegressionLine(JFreeChart chart, XYSeriesCollection inputData) {
		double regressionParameters[] = Regression.getOLSRegression(inputData,0);
		LineFunction2D linefunction2d = new LineFunction2D(regressionParameters[0], regressionParameters[1]);

		XYDataset dataset = DatasetUtilities.sampleFunction2D(linefunction2d, 0D, maxM, 1000, "Fitted Regression Line");

		// Draw the line dataset
		XYPlot xyplot = chart.getXYPlot();
		xyplot.setDataset(1, dataset);
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(true, false);
		xylineandshaperenderer.setSeriesPaint(0, Color.YELLOW);
		xyplot.setRenderer(1, xylineandshaperenderer);
	}
	
}
