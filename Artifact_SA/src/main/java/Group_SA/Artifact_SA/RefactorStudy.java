package Group_SA.Artifact_SA;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;


public class RefactorStudy implements Study 
{
	public static final String repository = "https://github.com/SonarSource/sonarqube.git"; //"https://github.com/tuplejump/MapDB.git"
	public static final String repo_name = "sonarqube";
	
    public static void main(String[] args) {
		new RepoDriller().start(new RefactorStudy());
	}
	
	@Override
	public void execute() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy:mm:dd");
			Calendar from = Calendar.getInstance();
			from.setTime(sdf.parse("2017:05:01"));
			Calendar to = Calendar.getInstance();
			to.setTime(sdf.parse("2018:06:01"));
			new RepositoryMining()
				.in(
						GitRemoteRepository
						.hostedOn(repository)
						.inTempDir("/temp")
						.buildAsSCMRepository()
						)
				.through(Commits.betweenDates(from, to))
				.process(new DevelopersVisitor(), new CSVFile("../dataMAPDB.csv"))
				.mine();
			}
		catch(Exception e) {
			//this really shouldnt happen but well, compile errors on the sdf parse without try/catch..
		}
	}
}
