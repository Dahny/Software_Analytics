package DynamicDuo.Models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.github.mauricioaniche.ck.CKNumber;

import DynamicDuo.RefactoringUtils.HalsteadExtractor;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;

public class FileMetricsModel {

	private CKNumber metrics;
	private double halsteadVolume;
	private int maintainabilityIndex;
	
	public FileMetricsModel(String filePath) throws IOException {
		String sourceCode = new String(Files.readAllBytes(Paths.get(Character.toUpperCase(filePath.charAt(0)) + filePath.substring(1))));
		initialize(filePath, sourceCode);
	}
	
	public FileMetricsModel(String filePath, String sourceCode) {
		initialize(filePath, sourceCode);
	}
	
	public String toCsvString() {
		char comma = StudyConstants.CSV_Delimiter;
		return maintainabilityIndex + comma + (halsteadVolume+"") + comma + metrics.getCbo() + comma + metrics.getDit() 
			+ comma + metrics.getNoc() + comma + metrics.getNof()+ comma + metrics.getNopf()+ comma + metrics.getNosf()+ comma 
			+ metrics.getNom() + comma + metrics.getNopm() + comma + metrics.getNosm() + comma + metrics.getNosi() + comma 
			+ metrics.getRfc()+ comma +metrics.getWmc() + comma + metrics.getLoc() + comma + metrics.getLcom();
	}
	
	public CKNumber getMetrics() {
		return metrics;
	}
	
	public double getHalsteadVolume() {
		return halsteadVolume;
	}
	
	public int getMaintainabilityIndex() {
		return maintainabilityIndex;
	}
	
	private void initialize(String filePath, String sourceCode) {
		halsteadVolume = HalsteadExtractor.calculateHalsteadVolume(sourceCode);
		metrics = StudyUtils.getMetricsForFile(filePath.replace('\\', '/'));
		maintainabilityIndex = StudyUtils.getMaintainabilityIndex(metrics.getLoc(), metrics.getWmc(), halsteadVolume);
	}
	
}
