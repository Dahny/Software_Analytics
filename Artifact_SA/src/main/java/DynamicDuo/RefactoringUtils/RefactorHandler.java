package DynamicDuo.RefactoringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.utils.RefactoringRelationship;
import org.repodriller.persistence.PersistenceMechanism;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;

public class RefactorHandler {
	
	//probably trash this..
	public static void handleMaintainabilityImprovingRefactors(String commitHash, List<Refactoring> refactorings, PersistenceMechanism writer) {
	}
	
	public static void handleTestRefactors(String commitHash, List<Refactoring> refactorings) {
		Set<String> affectedEntities = new HashSet<String>();
		for(Refactoring ref : refactorings) {
			Collection<RefactoringRelationship> refactoringRelationships = new ArrayList<RefactoringRelationship>();
			try{
				RefactoringType.parse(ref.toString().replace("\t"," "),refactoringRelationships);
			} catch(Exception e) {
				//failed to parse, log it and continue..
				e.printStackTrace();
			}
			for(RefactoringRelationship refrel : refactoringRelationships) {
				String mainEntityName = refrel.getMainEntity();		
				if(StudyUtils.isTestClass(mainEntityName) && !affectedEntities.contains(mainEntityName)) {
					affectedEntities.add(mainEntityName);
					//conditions statisfied for RQ1
					writeRQ1Output(buildRQ1Output(commitHash, mainEntityName, ref));
				}
			}
		}
	}
	
	private static void writeRQ1Output(String csvLine) {
		try {
			IOHandler.writeLineToCsv(StudyConstants.CSV_ALL_REFACTORS_Path, csvLine);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static String buildRQ1Output(String commitHash, String mainEntityName, Refactoring refactoring) {
		StringBuilder csvLine = new StringBuilder();
		
		csvLine.append(commitHash)
		.append(StudyConstants.CSV_Delimiter)
		.append(mainEntityName)
		.append(StudyConstants.CSV_Delimiter)
		.append(StudyUtils.isMethod(mainEntityName) ? "Method" : "Class") //append type of entity
		.append(StudyConstants.CSV_Delimiter)
		.append(refactoring.getRefactoringType().getDisplayName())
		.append(StudyConstants.CSV_Delimiter)
		.append(refactoring.getRefactoringType().toString());
		
		return csvLine.toString();
	}
	
}
