package DynamicDuo.RefactoringUtils;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import DynamicDuo.Study.StudyConstants;

public class RefactoringMinerRepository {
	
	private static RefactoringMinerRepository instance;
	private Repository repository;
	private GitHistoryRefactoringMiner miner;
	
	private RefactoringMinerRepository() throws Exception {
		GitService gitService = new GitServiceImpl();
		repository = gitService.cloneIfNotExists(StudyConstants.Repo_Path_Absolute+StudyConstants.Repo_Name+"_Ref",StudyConstants.Repo_Url);
		miner = new GitHistoryRefactoringMinerImpl();
	}
	
	public static synchronized RefactoringMinerRepository getInstance() {
		if(instance == null) {
			try {
				instance = new RefactoringMinerRepository();
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(1); //stop the program because we cannot mine without this instance
			}
		}
		return instance;
	}
	
	public Repository getRepository() {
		return repository;
	}
	
	public GitHistoryRefactoringMiner getMiner() {
		return miner;
	}
}
