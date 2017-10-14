package DynamicDuo.RefactoringUtils;

import java.util.HashSet;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import DynamicDuo.Study.StudyConstants;

public class IdentifiedRefactorCommitsHolder {
	
	private static IdentifiedRefactorCommitsHolder instance;
	
	//hashset for 0(1) add and contains
	private HashSet<String> identifiedRefactorCommits;
	
	private IdentifiedRefactorCommitsHolder() {
		identifiedRefactorCommits = new HashSet<String>();
	}
	
	public static synchronized IdentifiedRefactorCommitsHolder getInstance() {
		if(instance == null) {
			instance = new IdentifiedRefactorCommitsHolder();
		}
		return instance;
	}
	
	public void addRefactorCommit(String commitHash) {
		identifiedRefactorCommits.add(commitHash);
	}
	
	public boolean isIdentifiedRefactorCommit(String commitHash) {
		return identifiedRefactorCommits.contains(commitHash);
	}

}
