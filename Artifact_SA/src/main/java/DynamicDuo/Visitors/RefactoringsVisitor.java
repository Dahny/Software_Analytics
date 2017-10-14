package DynamicDuo.Visitors;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.domain.ModificationType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;
import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKReport;

import DynamicDuo.RefactoringUtils.IdentifiedRefactorCommitsHolder;
import DynamicDuo.RefactoringUtils.RefactoringMinerRepository;
import DynamicDuo.Study.RefactorStudy;
import DynamicDuo.Study.StudyConstants;

public class RefactoringsVisitor implements CommitVisitor {
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		RefactoringMinerRepository refMinerInstance = RefactoringMinerRepository.getInstance();
		try {
			//repo.getScm().getCommit(commit.getHash()).getParent()
			
			refMinerInstance.getMiner().detectAtCommit(refMinerInstance.getRepository(), null, commit.getHash(), new RefactoringHandler() {
				@Override
				public void handle(String commitId, List<Refactoring> refactorings) {
					String rfString = getRefactorsString(refactorings);
					if(rfString != null) {
						//add commit to commits-holder for later reference
						IdentifiedRefactorCommitsHolder.getInstance().addRefactorCommit(commitId);
						writer.write(commitId, rfString);
					}
				}
			});
		} catch(NullPointerException e) {
			//commit-related data was not found on disk :/
			e.printStackTrace();
		} catch(Exception e) {
			e.printStackTrace();
		}
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
