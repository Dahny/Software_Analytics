package DynamicDuo.Visualisation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.IO.LineHandler;
import DynamicDuo.Study.StudyConstants;

public class ImprovedMaintainabilityChecker {

	
	private HashMap<String,String> classPairs;
	private HashMap<String,List<HashMap<String,Integer>>> prodMetrics; 
	
	public int total = 0;
	public int improved = 0;
	public int worsened = 0;
	public int unaffected = 0;
	public int big_improvement = 0;
	
	public int prodAffected = 0;
	public int prodUnaffected = 0;
	public int prodMissing = 0;
	
	public static void main(String[] args) throws IOException {
		ImprovedMaintainabilityChecker checker = new ImprovedMaintainabilityChecker();
		System.out.println("total refactors on test classes: " + checker.total);
		System.out.println("improved: " + checker.improved);
		System.out.println("worsened: " + checker.worsened);
		System.out.println("unaffected: " + checker.unaffected);
		System.out.println("major improvement: " + checker.big_improvement);
		System.out.println("------");
		System.out.println("out of " + checker.big_improvement + " major improvements:");
		System.out.println(checker.prodAffected + " prod files were affected in the next 50 commits");
		System.out.println(checker.prodUnaffected + " prod files were NOT affected in the next 50 commits");
		System.out.println(checker.prodMissing + " prod files were unable to be tracked");
	}
	
	public ImprovedMaintainabilityChecker() {
		classPairs = new HashMap<String,String>();
		prodMetrics = new HashMap<String,List<HashMap<String,Integer>>>();
		try {
			loadClassPairs();
			loadTrackedProductionFiles();
			loadMetricData();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private void loadClassPairs() throws IOException {
		IOHandler.readLines(StudyConstants.CSV_Class_Pairs, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
				String key = cols[2].toLowerCase();
				String value = cols[1].toLowerCase();
				if(!classPairs.containsKey(key)) {
					classPairs.put(key, value);
				}
			}
			
		});
	}
	
	private void loadMetricData() throws IOException {
		
		IOHandler.readLines(StudyConstants.CSV_Refactors, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
//				String key = cols[0]+cols[3];
//				key = key.toLowerCase();
				
				//original
				//nof = 8 //nom = 11 //wmc = 16 // loc = 17
				Category orig_nof = CategoryAssigner.getCategory("NOF", Integer.parseInt(cols[8]));
				Category orig_nom = CategoryAssigner.getCategory("NOM", Integer.parseInt(cols[11]));
				Category orig_wmc = CategoryAssigner.getCategory("WMC", Integer.parseInt(cols[16]));
				Category orig_loc = CategoryAssigner.getCategory("LOC", Integer.parseInt(cols[17]));
				//previous
				//nof = 24 //nom = 27 //wmc = 32 //loc = 33
				Category prev_nof = CategoryAssigner.getCategory("NOF", Integer.parseInt(cols[24]));
				Category prev_nom = CategoryAssigner.getCategory("NOM", Integer.parseInt(cols[27]));
				Category prev_wmc = CategoryAssigner.getCategory("WMC", Integer.parseInt(cols[32]));
				Category prev_loc = CategoryAssigner.getCategory("LOC", Integer.parseInt(cols[33]));
				
				int totalDiff = prev_nof.compareTo(orig_nof) 
						+ prev_nom.compareTo(orig_nom) 
						+ prev_wmc.compareTo(orig_wmc)
						+ prev_loc.compareTo(orig_loc);
				
				if(totalDiff > 0) worsened++;
				if(totalDiff < 0) improved++;
				if(totalDiff == 0) unaffected++;
				if(totalDiff < -4) { //this threshold defines what we see as a major improvement
					big_improvement++;
//					System.out.println(cols[cols.length -1]);
//					System.out.println(cols[8] + " (" + orig_nof.toString() + ")" + " - " + cols[24] + " (" + prev_nof.toString() + ")");
//					System.out.println(cols[11] + " (" + orig_nom.toString() + ")" + " - " + cols[27] + " (" + prev_nom.toString() + ")");
//					System.out.println(cols[16] + " (" + orig_wmc.toString() + ")" + " - " + cols[32] + " (" + prev_wmc.toString() + ")");
//					System.out.println(cols[17] + " (" + orig_loc.toString() + ")" + " - " + cols[33] + " (" + prev_loc.toString() + ")");
//					System.out.println("-------------------");
					
					String absFilePath = StudyConstants.Repo_Path_Absolute + StudyConstants.Repo_Name + "\\" + cols[1];
					String normalisedTestFilePath = absFilePath.toLowerCase().replace("/", "\\");//normalise the path
					String commitHash = cols[0].substring(1);//somehow the hash starts with a " symbol, just split it off, no time for a proper solution
					String key = commitHash + classPairs.get(normalisedTestFilePath);
					checkTrackedMetrics(prodMetrics.get(key));
					
				}
				total++;
			}
			
		});
	}
	
	private void checkTrackedMetrics(List<HashMap<String,Integer>> metricList) {
		if(metricList == null) {
			prodMissing++;
			return;
		}
		HashMap<String,Integer> firstVersion = metricList.get(0);
		Category prev_nof = CategoryAssigner.getCategory("NOF", firstVersion.get("NOF"));
		Category prev_nom = CategoryAssigner.getCategory("NOM", firstVersion.get("NOM"));
		Category prev_wmc = CategoryAssigner.getCategory("WMC", firstVersion.get("WMC"));
		Category prev_loc = CategoryAssigner.getCategory("LOC", firstVersion.get("LOC"));
		
		
		
		HashMap<String,Integer> lastVersion = metricList.get(metricList.size()-1);
		Category orig_nof = CategoryAssigner.getCategory("NOF", lastVersion.get("NOF"));
		Category orig_nom = CategoryAssigner.getCategory("NOM", lastVersion.get("NOM"));
		Category orig_wmc = CategoryAssigner.getCategory("WMC", lastVersion.get("WMC"));
		Category orig_loc = CategoryAssigner.getCategory("LOC", lastVersion.get("LOC"));
		
		int totalDiff = prev_nof.compareTo(orig_nof) 
				+ prev_nom.compareTo(orig_nom) 
				+ prev_wmc.compareTo(orig_wmc)
				+ prev_loc.compareTo(orig_loc);
		if(totalDiff != 0) {
			prodAffected++;
		} else {
			prodUnaffected++;
		}
	}
	
	private void loadTrackedProductionFiles() throws IOException {
		IOHandler.readLines(StudyConstants.CSV_Refactors_Tracking, new LineHandler() {

			@Override
			public void handle(String line) {
				String[] cols = line.split(",");
				HashMap<String, Integer> metrics = new HashMap<String, Integer>();
				//nof=7 //nom=10 //wmc=15 //loc=16
				metrics.put("LOC", Integer.parseInt(cols[16]));
				metrics.put("WMC", Integer.parseInt(cols[15]));
				metrics.put("NOF", Integer.parseInt(cols[7]));
				metrics.put("NOM", Integer.parseInt(cols[10]));
				
				String key = cols[0]+cols[1];//commithash+filepath as key
				List<HashMap<String,Integer>> metricList;
				if(prodMetrics.containsKey(key)) {
					metricList = prodMetrics.get(key);
				} 
				else {
					metricList = new ArrayList<HashMap<String,Integer>>();
				}
				metricList.add(metrics);
				prodMetrics.put(key, metricList);
			}
			
		});
	}

}
