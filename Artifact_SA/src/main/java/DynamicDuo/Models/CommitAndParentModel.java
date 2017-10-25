package DynamicDuo.Models;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.refactoringminer.api.Refactoring;

import com.github.mauricioaniche.ck.CKNumber;

import DynamicDuo.RefactoringUtils.HalsteadExtractor;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;


public class CommitAndParentModel {
	private String commitHash;
	
	private String filePath;
	private String fileName;
	
	private Refactoring refactoring;
	
	private double halsteadVolume;
	private CKNumber metrics;
	private int maintainabilityIndex;
	
	private double p_halsteadVolume;
	private CKNumber p_metrics;
	private int p_maintainabilityIndex;
	
	public CommitAndParentModel(String _commitHash, Refactoring _refactoring) {
		commitHash = _commitHash;
		refactoring = _refactoring;
	}
	
	
	
	public void setFileInfo(String _filePath, String _fileName) {
		filePath = _filePath;
		fileName = _fileName;
	}
	
	public void setCommitData(double _halsteadVolume, CKNumber _metrics, int _maintainabilityIndex) {
		halsteadVolume = _halsteadVolume;
		metrics = _metrics;
		maintainabilityIndex = _maintainabilityIndex;
	}
	
	public void setParentCommitData(double _halsteadVolume, CKNumber _metrics, int _maintainabilityIndex) {
		p_halsteadVolume = _halsteadVolume;
		p_metrics = _metrics;
		p_maintainabilityIndex = _maintainabilityIndex;
	}
	
	public String getFilePath() {
		return filePath;
	}
	
	public String toCSVString() {
		final char comma = StudyConstants.CSV_Delimiter;
		return commitHash + comma + filePath + comma + fileName //commit and file details
				//current commit metrics
				+ comma + maintainabilityIndex + comma + halsteadVolume + comma + metrics.getCbo() + comma + metrics.getDit() 
				+ comma + metrics.getNoc() + comma + metrics.getNof()+ comma + metrics.getNopf()+ comma + metrics.getNosf()+ comma 
				+ metrics.getNom() + comma + metrics.getNopm() + comma + metrics.getNosm() + comma + metrics.getNosi() + comma 
				+ metrics.getRfc()+ comma +metrics.getWmc() + comma + metrics.getLoc() + comma + metrics.getLcom()  
				//parent commit metrics
				+ comma + p_maintainabilityIndex + comma + p_halsteadVolume + comma + p_metrics.getCbo() + comma + p_metrics.getDit() 
				+ comma + p_metrics.getNoc() + comma + p_metrics.getNof()+ comma + p_metrics.getNopf()+ comma + p_metrics.getNosf()+ comma 
				+ p_metrics.getNom() + comma + p_metrics.getNopm() + comma + p_metrics.getNosm() + comma + p_metrics.getNosi() + comma 
				+ p_metrics.getRfc()+ comma +p_metrics.getWmc() + comma + p_metrics.getLoc() + comma + p_metrics.getLcom() //parent commit metrics
				+ comma + refactoring.getRefactoringType().getDisplayName() + comma + refactoring.toString(); //finally some info on the refactor
	}
	
}
