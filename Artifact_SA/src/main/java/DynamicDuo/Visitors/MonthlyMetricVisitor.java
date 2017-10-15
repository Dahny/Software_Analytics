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
import DynamicDuo.Study.RefactorStudy;
import DynamicDuo.Study.StudyConstants;

public class MonthlyMetricVisitor implements CommitVisitor {
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {		
		try {
			CK ck = new CK();
			StringBuilder path = new StringBuilder(StudyConstants.Repo_Path_Absolute); 
			path.append(StudyConstants.Repo_Name);
			
			repo.getScm().checkout(commit.getHash());
			CKReport report = ck.calculate(path.toString());
			//TODO: 
			//parse report
			//CALCULATE MAINAINABILITY
			//find test-prod pairs
			//write pairs to file
			//write pairs+metrics to file
//			writer.write();
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			repo.getScm().reset();
		}



	}

	@Override
	public String name() {
		return "MonthlyMetricsVisitor";
	}

}
