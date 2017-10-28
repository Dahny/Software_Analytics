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

public class MICorrelationVisualiser {

	private String classPairsFilePath;
	private String monthlyMetricsFilePath;
	
	private HashMap<String, List<ClassPairModel>> commitHashToClassPairs;
	private HashMap<String, Integer> hashClassToMaintainabilityIndex;
	
	private int maxMi = -1;
	
	public MICorrelationVisualiser() {
		classPairsFilePath = StudyConstants.CSV_Class_Pairs;
		monthlyMetricsFilePath = StudyConstants.CSV_Monthly_Metrics;
		commitHashToClassPairs = new HashMap<String, List<ClassPairModel>>();
		hashClassToMaintainabilityIndex = new HashMap<String, Integer>();
		
		try {
			System.out.println("loading data");
			loadClassPairs();
			loadMIData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void plot() {
		XYDataset inputData = createDataset();
		JFreeChart chart = ChartFactory.createScatterPlot(
	            "Maintainability Indexes", // chart title
	            "test", // x axis label
	            "production", // y axis label
	            inputData, // data  ***-----PROBLEM------***
	            PlotOrientation.HORIZONTAL,
	            true, // include legend
	            true, // tooltips
	            false // urls
	            );
		ChartFrame frame = new ChartFrame("First", chart);
		
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
	
	private void loadMIData() throws IOException {
		
		IOHandler.readLines(monthlyMetricsFilePath, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
				String key = cols[0]+cols[3];
				key = key.toLowerCase();
				
				Integer maintainabilityIndex;
				if(hashClassToMaintainabilityIndex.containsKey(key)) {
					//wtf this shouldnt happen!
					System.err.println("THIS SHOULD NOT HAPPEN");
					maintainabilityIndex = -1;
				} else {
					maintainabilityIndex = Integer.parseInt(cols[4]);
				}
				if(maintainabilityIndex > maxMi) {
					maxMi = maintainabilityIndex;
				}
				hashClassToMaintainabilityIndex.put(key, maintainabilityIndex);
			}
			
		});
		
		System.out.println("MaxMi found: " +maxMi);
	}
	
	
	private XYDataset createDataset() {
		int successCount = 0;
		int failureCount = 0;
	    XYSeriesCollection result = new XYSeriesCollection();
	   	Iterator<String> hashes = commitHashToClassPairs.keySet().iterator();
	    
	    while(hashes.hasNext()) {
	    	String hash = hashes.next();
	    	if(hashes.hasNext()) continue;
	    	
	    	XYSeries series = new XYSeries("MI - "+hash.substring(0, 6));
	    	List<ClassPairModel> pairs = commitHashToClassPairs.get(hash);
	    	for(ClassPairModel pair : pairs) {
	    		Integer prodMi = hashClassToMaintainabilityIndex.get((hash+pair.getProductionClass()).toLowerCase());
	    		Integer testMi = hashClassToMaintainabilityIndex.get((hash+pair.getTestClass()).toLowerCase());
	    		if(testMi == null || prodMi == null) {
	    			failureCount++;
	    			continue;
	    		}
	    		successCount++;
	    		series.add(prodMi, testMi);
	    	}
	    	result.addSeries(series);
	    }
	    System.out.println("Done creating dataset: " + successCount + " - " + failureCount);
	    
	    return result;
	}
	
	private void drawRegressionLine(JFreeChart chart, XYSeriesCollection inputData) {
		// Get the parameters 'a' and 'b' for an equation y = a + b * x,
		// fitted to the inputData using ordinary least squares regression.
		// a - regressionParameters[0], b - regressionParameters[1]
		double regressionParameters[] = Regression.getOLSRegression(inputData,
				0);

		// Prepare a line function using the found parameters
		LineFunction2D linefunction2d = new LineFunction2D(
				regressionParameters[0], regressionParameters[1]);

		// Creates a dataset by taking sample values from the line function
		XYDataset dataset = DatasetUtilities.sampleFunction2D(linefunction2d,
				0D, 300, 100, "Fitted Regression Line");

		// Draw the line dataset
		XYPlot xyplot = chart.getXYPlot();
		xyplot.setDataset(1, dataset);
		XYLineAndShapeRenderer xylineandshaperenderer = new XYLineAndShapeRenderer(
				true, false);
		xylineandshaperenderer.setSeriesPaint(0, Color.YELLOW);
		xyplot.setRenderer(1, xylineandshaperenderer);
	}
	
}
