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
import DynamicDuo.Models.FileMetricsModel;
import DynamicDuo.RefactoringUtils.DelayedFutureFileVersionChecker;
import DynamicDuo.RefactoringUtils.HalsteadExtractor;
import DynamicDuo.RefactoringUtils.ProductionClassPairRepository;
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
					//first make delayedchecker check files that need to be tracked..
					DelayedFutureFileVersionChecker.getInstance().doCheck();
					
					if(refactorings.isEmpty()) {return;}//instantly skip if no refactorings found
					
					//call simple function to handle datacollection for RQ1
					RefactorHandler.handleTestRefactors(commitId, refactorings);
					
					//proceed with more complex procedure for RQ2
					List<CommitAndParentModel> commitAndParentModels = new ArrayList<CommitAndParentModel>();
					Set<String> affectedFiles = new HashSet<String>();
					for(Refactoring ref : refactorings) {
						String classPath = StudyUtils.getClassPathFromRefactoringDescription(ref.toString());
						Modification mod = getMatchingModification(classPath,commit);
						
						//skip if we dont need to process this file
						if(shouldSkipClass(mod, affectedFiles)) { continue; }
						affectedFiles.add(mod.getFileName());
						
						String filePath = mod.getNewPath() != null || mod.getNewPath() != "" ? mod.getNewPath() : mod.getOldPath();
						
						//1. current commit metrics of file..
						FileMetricsModel mm;
						try{
							mm = new FileMetricsModel(filePath, mod.getSourceCode());
						} catch(Exception e) {
							//okay so the file was not found so this object failed to instantiate..
							//skip and continue
							System.err.println("FAILED TO INSTANTIATE MM IN STEP 1, SKIPPING");
							continue;
						}
						
						CommitAndParentModel dataModel = new CommitAndParentModel(commitId, ref);
						dataModel.setFileInfo(filePath, StudyUtils.getFileNameFromPath(filePath));
						dataModel.setCommitData(mm.getHalsteadVolume(), mm.getMetrics(), mm.getMaintainabilityIndex());
						commitAndParentModels.add(dataModel);
					}
					if(commitAndParentModels.isEmpty()) { return; } //stop here if nothing worth looking at came up.
					
					//2. go to parent version
					while(true) {
						try {
							repo.getScm().reset();
							repo.getScm().checkout(commit.getParent());
							break;
						} catch(Exception e) {
							//probably a lock failure..
							try {
								System.err.println("Failed to checkout --> " + commit.getParent());
								Thread.sleep(1000);
								continue;
							} catch (InterruptedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}
					}
					for(CommitAndParentModel dataModel : commitAndParentModels) {
						String testClass = dataModel.getFilePath().toLowerCase().startsWith("c:") ? dataModel.getFilePath() : StudyConstants.Repo_Path_Absolute + StudyConstants.Repo_Name + "\\" + dataModel.getFilePath().replace('/', '\\');
						String productionClass = ProductionClassPairRepository.getInstance().getPairedProductionClass(testClass);
						//first check if theres a prod pair found, if not we can discard this data..
						if(productionClass == null) {
							System.out.println("MISSING PROD FILE!!");
							System.out.println(testClass);
						}
						if(productionClass != null) {
							System.out.println("PERFORMING PARENT CHECK FOR: test = " + testClass + " AND production= "+productionClass);
							
							//3. parent commit metrics of file
							FileMetricsModel mm;
							try{
								mm = new FileMetricsModel(testClass);
							} catch(Exception e) {
								//okay so the file was not found so this object failed to instantiate..
								//skip and continue
								System.err.println("FAILED TO INSTANTIATE MM IN STEP 3, SKIPPING");
								continue;
							}								
							
							dataModel.setParentCommitData(mm.getHalsteadVolume(), mm.getMetrics(), mm.getMaintainabilityIndex());
							
							//4. write metrics and parent metrics to file..
							writer.write(dataModel.toCSVString());
							
							//proceed to track matching prod file!! (or do mi/parentmi diff?)
							System.out.println("SETTING UP DELAYED CHECKER: commitId = "+commitId + " AND prodClass = "+productionClass);
							DelayedFutureFileVersionChecker.getInstance().trackFile(productionClass, commitId);
						}
					}
					
					
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
				|| !StudyUtils.isPathToTestClass(mod.getFileName().replace(".java", "")) //its not a testfile, skip it.
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
