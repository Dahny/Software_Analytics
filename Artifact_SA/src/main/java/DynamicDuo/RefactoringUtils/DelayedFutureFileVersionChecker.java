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

import DynamicDuo.Study.StudyConstants;

public class DelayedFutureFileVersionChecker {
	
	private static DelayedFutureFileVersionChecker instance;
	
	private int commitsSkipped;
	private int commitsToSkip;
	private Map<String,Integer> trackingFiles; //note: key=path to file, value=amount of versions left to track
	
	private DelayedFutureFileVersionChecker() {
		commitsSkipped = 0;
		commitsToSkip = StudyConstants.Commits_To_Skip_In_Delayed_Checker;
		trackingFiles = new HashMap<String,Integer>();
	}
		
	public static synchronized DelayedFutureFileVersionChecker getInstance() {
		if(instance == null) {
			instance = new DelayedFutureFileVersionChecker();
		}
		return instance;
	}
	
	public void doCheck() {
		if(instance.canDoCheck()) {
			//one in x amount of commits will be checked
			for(String filePath : trackingFiles.keySet()) {
				//calculate metrics and write to file..
				
				//CsvFileWriter.writeLineToCsv(pathToCsv, String content);
				
				//decrement versions that are left to check
				Integer versionsLeftToCheck = trackingFiles.get(filePath);
				versionsLeftToCheck -= 1;
				trackingFiles.put(filePath, versionsLeftToCheck);
			}
		}
		
	}
	
	public void trackFile(String path) {
		trackingFiles.put(path, StudyConstants.Versions_To_Check_In_Delayed_Checker);
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
