package DynamicDuo.Models;

import org.refactoringminer.api.Refactoring;

import com.github.mauricioaniche.ck.CKNumber;

import DynamicDuo.Study.StudyConstants;


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
	
	public boolean maintainabilityImproved() {
		return p_halsteadVolume > halsteadVolume;
	}
	
	public String toCSVString() {
		final char comma = StudyConstants.CSV_Delimiter;
		return commitHash + comma + filePath + comma + fileName //commit and file details
				+ comma + halsteadVolume + comma + metrics.getLoc() + comma + metrics.getWmc() + comma + maintainabilityIndex //current commit metrics
				+ comma + p_halsteadVolume + comma + p_metrics.getLoc() + comma + p_metrics.getWmc() + comma + p_maintainabilityIndex //parent commit metrics
				+ comma + refactoring.getRefactoringType().getDisplayName() + comma + refactoring.toString(); //finally some info on the refactor
	}
}
