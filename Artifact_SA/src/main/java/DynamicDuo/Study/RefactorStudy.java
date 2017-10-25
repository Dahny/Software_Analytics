package DynamicDuo.Study;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.eclipse.jgit.revwalk.filter.CommitterRevFilter;
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.CommitFilter;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRemoteRepository;

import DynamicDuo.RefactoringUtils.ProductionClassPairRepository;
import DynamicDuo.Visitors.MetricsVisitor;
import DynamicDuo.Visitors.MonthlyMetricVisitor;
import DynamicDuo.Visitors.RefactoringsVisitor;


public class RefactorStudy implements Study 
{
	
    public static void main(String[] args) {
    	//initialize the instance to ensure correct loading of the classpairs.
    	ProductionClassPairRepository.getInstance();
    	
		new RepoDriller().start(new RefactorStudy());
	}
	
	@Override
	public void execute() {
		//processMonthlyMetricsAndClassPairs();	
		processRefactorCommitsAndTheirMetrics();
	}
	
	private void processMonthlyMetricsAndClassPairs() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy:mm:dd");
		Calendar from = Calendar.getInstance();
		from.setTime(sdf.parse("2013:01:01"));
		Calendar to = Calendar.getInstance();
		to.setTime(sdf.parse("2018:12:01"));
		new RepositoryMining()
		.in(
			GitRemoteRepository
			.hostedOn(StudyConstants.Repo_Url)
			.inTempDir(StudyConstants.Repo_Path_Relative)
			.buildAsSCMRepository()
			)
		.through(Commits.monthly(1)).reverseOrder()
		.process(new MonthlyMetricVisitor(), new CSVFile(StudyConstants.CSV_Monthly_Metrics)) 
		.mine();
		} catch(Exception e) {
			//this really shouldnt happen but well, compile errors on the sdf parse without try/catch..
		}
		
	}
	
	private void processRefactorCommitsAndTheirMetrics() {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy:mm:dd");
			Calendar from = Calendar.getInstance();
			from.setTime(sdf.parse("2016:05:01"));
			Calendar to = Calendar.getInstance();
			to.setTime(sdf.parse("2018:06:01"));
			
			new RepositoryMining()
				.in(
					GitRemoteRepository
					.hostedOn(StudyConstants.Repo_Url)
					.inTempDir(StudyConstants.Repo_Path_Relative)
					.buildAsSCMRepository()
					)
				.through(Commits.betweenDates(from, to))
				.process(new RefactoringsVisitor(), new CSVFile(StudyConstants.CSV_Refactors))
				.withThreads(1)
				.mine();
			}
		catch(Exception e) {
			//this really shouldnt happen but well, compile errors on the sdf parse without try/catch..
		}
	}
}
