package Group_SA.Artifact_SA;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;


public class RefactorStudy implements Study 
{
    public static void main(String[] args) {
		new RepoDriller().start(new RefactorStudy());
	}
	
	@Override
	public void execute() {
		new RepositoryMining()
			.in(
					GitRemoteRepository
					.hostedOn("https://github.com/tuplejump/MapDB.git")
					.inTempDir("/temp")
					.buildAsSCMRepository()
					)
			.through(Commits.all())
			.process(new DevelopersVisitor(), new CSVFile("../dataMAPDB.csv"))
			.mine();
	}
}
