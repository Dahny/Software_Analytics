package DynamicDuo.Study;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;

import DynamicDuo.Visitors.MetricsVisitor;
import DynamicDuo.Visitors.RefactoringsVisitor;


public class RefactorStudy implements Study 
{
	
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
						.hostedOn(StudyConstants.Repo_Url)
						.inTempDir(StudyConstants.Repo_Path_Relative)
						.buildAsSCMRepository()
						)
				.through(Commits.monthly(1))
				//get refactors
				//.process(new RefactoringsVisitor(), new CSVFile(String.format("../%s-%s.csv", StudyConstants.Repo_Name, "refactors"))) 
				//get ck metrics
				.process(new MetricsVisitor(), new CSVFile(String.format("../%s-%s.csv", StudyConstants.Repo_Name, "metrics")))		
				.mine();
			}
		catch(Exception e) {
			//this really shouldnt happen but well, compile errors on the sdf parse without try/catch..
		}
	}
}
