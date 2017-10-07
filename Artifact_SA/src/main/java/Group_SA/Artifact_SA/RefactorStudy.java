package Group_SA.Artifact_SA;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;
import org.repodriller.scm.GitRepository;


public class RefactorStudy implements Study 
{
    public static void main(String[] args) {
		new RepoDriller().start(new RefactorStudy());
	}
	
	@Override
	public void execute() {
		new RepositoryMining()
			.in(
					//GitRepository.singleProject("/Users/mauricioaniche/Desktop/tutorial/jfreechart-fse")
					GitRemoteRepository
					.hostedOn("https://github.com/SonarSource/sonarqube.git")
					.inTempDir("/temp")
					.asBareRepos()
					.buildAsSCMRepository()
					)
			.through(Commits.all())
			.process(new DevelopersVisitor(), new CSVFile("/Users/Dplen/Documents/Software_Analytics/Artifact_SA/data.data.csv"))
			.mine();
	}
}
