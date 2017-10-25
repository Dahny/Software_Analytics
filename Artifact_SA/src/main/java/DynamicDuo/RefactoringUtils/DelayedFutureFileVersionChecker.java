package DynamicDuo.RefactoringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import com.github.mauricioaniche.ck.CKNumber;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.Models.FileMetricsModel;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;

public class DelayedFutureFileVersionChecker {
	
	private static DelayedFutureFileVersionChecker instance;
	
	private int commitsSkipped;
	private int commitsToSkip;
	private Map<String,Integer> trackingFiles; //note: key=path to file, value=amount of versions left to track
	private Map<String,String> filesToCommitHash; //note: key=path to file, value=amount of versions left to track
	
	private DelayedFutureFileVersionChecker() {
		commitsSkipped = 0;
		commitsToSkip = StudyConstants.Commits_To_Skip_In_Delayed_Checker;
		trackingFiles = new HashMap<String,Integer>();
		filesToCommitHash = new HashMap<String,String>();
	}
		
	public static synchronized DelayedFutureFileVersionChecker getInstance() {
		if(instance == null) {
			instance = new DelayedFutureFileVersionChecker();
		}
		return instance;
	}
	
	public void doCheck(){
		if(instance.canDoCheck()) {
			//one in x amount of commits will be checked
			for(String filePath : trackingFiles.keySet()) {
				try {
					//calculate metrics and write to file..
					FileMetricsModel metricsModel;
					try {
						metricsModel = new FileMetricsModel(filePath);
					} catch(Exception e) {
						//again file probably went missing, continue and hope it returns
						System.err.println("FAILED TO INSTANTIATE MM IN DELAYED CHECKER, SKIPPING");
						continue;
					}
					String csvLine = filesToCommitHash.get(filePath) + StudyConstants.CSV_Delimiter + filePath + StudyConstants.CSV_Delimiter + metricsModel.toCsvString();
					IOHandler.writeLineToCsv(StudyConstants.CSV_Refactors_Tracking, csvLine);
					//decrement versions that are left to check
					CheckCompleteFor(filePath);
					System.out.println("SUCCESFULLY DELAY-CHECKED " + filePath + ", versions left: " + trackingFiles.get(filePath));
				} catch (IOException e) {
					e.printStackTrace();
					//okay we might assume the file has been deleted/moved in a future version
					//so we stop tracking it
					trackingFiles.remove(filePath);
					filesToCommitHash.remove(filePath);
				}
				
			}
		}
		
	}

	public void trackFile(String path, String commitHash) {
		trackingFiles.put(path, StudyConstants.Versions_To_Check_In_Delayed_Checker);
		filesToCommitHash.put(path, commitHash);
	}
	
	private void CheckCompleteFor(String filePath) {
		Integer versionsLeftToCheck = trackingFiles.get(filePath);
		versionsLeftToCheck -= 1;
		if(versionsLeftToCheck == 0) {
			trackingFiles.remove(filePath);
		} else {
			trackingFiles.put(filePath, versionsLeftToCheck);
		}
	}
	
	private boolean canDoCheck() {
		if(commitsSkipped >= commitsToSkip) {
			commitsSkipped = 0;
			return true;
		} else {
			commitsSkipped++;
			return false;
		}
	}
}
