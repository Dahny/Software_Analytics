package DynamicDuo.Visitors;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.rm2.model.refactoring.SDRefactoring;
import org.refactoringminer.util.GitServiceImpl;
import org.refactoringminer.utils.RefactoringRelationship;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.domain.ModificationType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;
import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;

import DynamicDuo.Models.CommitAndParentModel;
import DynamicDuo.RefactoringUtils.HalsteadExtractor;
import DynamicDuo.RefactoringUtils.IdentifiedRefactorCommitsHolder;
import DynamicDuo.RefactoringUtils.RefactorHandler;
import DynamicDuo.RefactoringUtils.RefactoringMinerRepository;
import DynamicDuo.Study.RefactorStudy;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;

public class RefactoringsVisitor implements CommitVisitor {
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		RefactoringMinerRepository refMinerInstance = RefactoringMinerRepository.getInstance();
		try {			
			
			refMinerInstance.getMiner().detectAtCommit(refMinerInstance.getRepository(), null, commit.getHash(), new RefactoringHandler() {
				@Override
				public void handle(String commitId, List<Refactoring> refactorings) {
					if(refactorings.isEmpty()) {return;}//instantly skip if no refactorings found
					
					//call simple function to handle datacollection for RQ1
					RefactorHandler.handleTestRefactors(commit.getHash(), refactorings);
					
					//proceed with more complex procedure for RQ2
					List<CommitAndParentModel> commitAndParentModels = new ArrayList<CommitAndParentModel>();
					Set<String> affectedFiles = new HashSet<String>();
					for(Refactoring ref : refactorings) {
						String classPath = StudyUtils.getClassPathFromRefactoringDescription(ref.toString());
						Modification mod = getMatchingModification(classPath,commit);
						
						//skip if we dont need to process this file
						if(shouldSkipClass(mod, affectedFiles)) { continue; }
						affectedFiles.add(mod.getFileName());
						
						String filePath = mod.getNewPath();
						
						//1. current commit metrics of file..
						String sourceCode = mod.getSourceCode();
						double hsv = HalsteadExtractor.calculateHalsteadVolume(sourceCode);
						CKNumber metrics = StudyUtils.getMetricsForFile(filePath);
						int mi = StudyUtils.getMaintainabilityIndex(metrics.getLoc(), metrics.getWmc(), hsv);
						
						CommitAndParentModel dataModel = new CommitAndParentModel(commit.getHash(), ref);
						dataModel.setFileInfo(filePath, StudyUtils.getFileNameFromPath(filePath));
						dataModel.setCommitData(hsv, metrics, mi);
						commitAndParentModels.add(dataModel);
					}
					
					//2. go to parent version
					repo.getScm().checkout(commit.getParent());
					
					for(CommitAndParentModel dataModel : commitAndParentModels) {
						try {	
							//3. parent commit metrics of file
							String sourceCode = new String(Files.readAllBytes(Paths.get(StudyUtils.getAbsolutePathToFile(dataModel.getFilePath()))));
							double hsv = HalsteadExtractor.calculateHalsteadVolume(sourceCode);
							CKNumber metrics = StudyUtils.getMetricsForFile(dataModel.getFilePath());
							int mi = StudyUtils.getMaintainabilityIndex(metrics.getLoc(), metrics.getWmc(), hsv);
							dataModel.setParentCommitData(hsv, metrics, mi);
							
							//4. write metrics and parent metrics to file..
							writer.write(dataModel.toCSVString());
							
							//proceed to track matching prod file!! (or do mi/parentmi diff?)
							if(dataModel.maintainabilityImproved()) { //we dont care to track files that actually got worse
								IdentifiedRefactorCommitsHolder.getInstance().addRefactorCommit(commit.getHash());
							}
							//...
						} catch (IOException e) {
							//Most likely failed to get the file from parent commit.. skip and continue
							continue;
						}
						
					}
					
					//check maintainability
					//check maintainability of parent
					//if difference > TBD {
					//	fetch prod pair from list (if not present, break;)
					//	add commit to commitsholder (and reference prod file && current commithash)
					//	track prod code maintainability
					//	
					//}
				}
			});
		} catch(NullPointerException e) {
			//commit-related data was not found on disk :/
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			repo.getScm().reset();
		}
	}

	private boolean shouldSkipClass(Modification mod, Set<String> affectedFiles) {
		return mod == null
				|| !mod.getFileName().toLowerCase().endsWith("test.java") //its not a testfile, skip it.
				|| mod.getType().equals(ModificationType.DELETE) //its a deletion, skip it
				|| !mod.getFileName().endsWith(".java") //its not a javafile, skip it
				|| affectedFiles.contains(mod.getFileName()); //we already processed it, skip it
	}
	
	private Modification getMatchingModification(String classPath, Commit commit) {
		if(classPath == null || commit == null) { 
			return null; 
		}
		
		for(Modification m : commit.getModifications()) {
			if(m.getFileName().contains(classPath)) {
				return m;
			}
		}
		return null;
	}
	
	@Override
	public String name() {
		return "RefactoringsVisitor";
	}
	
	/**
	 * Gives a suitable string representation to the refactorings
	 * string is aimed for csv storage usage
	 * @param refactorings
	 * @return
	 */
	private String getRefactorsString(List<Refactoring> refactorings) {
		if(refactorings.isEmpty()) {
			return null;
		}
		StringBuilder refactorsString = new StringBuilder();
	    boolean anyRefs = false;
	    for (Refactoring ref : refactorings) {
	    	refactorsString.append(StudyConstants.CSV_Delimiter)
	    		.append(ref.getRefactoringType().toString())
	    		.append(StudyConstants.CSV_Delimiter)
	    		.append(ref.getName()); //TODO welke exacte data van de refactors willen we..?
	    	anyRefs = true;
	    }
	    if(anyRefs) {
	    	refactorsString.delete(0, 1); //remove first comma
	    } 
	    return refactorsString.toString();
	}
	
}
