package Group_SA.Artifact_SA;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;


public class RefactorStudy implements Study 
{
    public static void main(String[] args) {
		new RepoDriller().start(new RefactorStudy());
	}
	
	@Override
	public void execute() {
		new RepositoryMining()
			.in(GitRepository.singleProject("/Users/mauricioaniche/Desktop/tutorial/jfreechart-fse"))
			.through(Commits.all())
			.process(new DevelopersVisitor(), new CSVFile("/Users/mauricioaniche/Desktop/tutorial/q1.csv"))
			.mine();
	}
}
