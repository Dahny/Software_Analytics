package DynamicDuo.Visitors;

import java.util.Calendar;
import java.util.Date;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;
import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;

import DynamicDuo.RefactoringUtils.HalsteadExtractor;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;
import DynamicDuo.Utils.CsvFileWriter;

public class MonthlyMetricVisitor implements CommitVisitor {

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		try {
			CK ck = new CK();
			StringBuilder path = new StringBuilder(StudyConstants.Repo_Path_Absolute);
			path.append(StudyConstants.Repo_Name);

			repo.getScm().checkout(commit.getHash());
			String commitDate = parseDate(commit.getDate().getTime());
			CKReport report = ck.calculate(path.toString());

			// parse report
			for (CKNumber ckn : report.all()) {
				int loc = ckn.getLoc();
				int wmc = ckn.getWmc();
				double hsv = HalsteadExtractor.calculateHalsteadVolume(StudyUtils.readFile(ckn.getFile()));

				// CALCULATE MAINAINABILITY
				int mtnIndex = StudyUtils.getMaintainabilityIndex(loc, wmc, hsv);

				// find test-prod pairs
				String fileType = "TestFile";
				if (!StudyUtils.isTestClass(ckn.getClassName())) {
					fileType = "ProductionFile";
					String pair = StudyUtils.findTestPair(ckn.getFile());

					if (!pair.equals("no testfile found")) {
						// write pairs to file
						CsvFileWriter.writeLineToCsv(
								"C:\\Users\\Dplen\\Documents\\Software_Analytics\\Artifact_SA\\data\\PairDataSonarQube.csv",
								ckn.getFile() + ", " + pair);
					}
				}

				// Write -> fileType, absolute file path, MaintainabilityIndex,
				// [ALL CK METRICS].
				writer.write(commit.getHash(), commitDate, fileType, ckn.getFile(), mtnIndex, hsv, ckn.getCbo(),
						ckn.getDit(), ckn.getNoc(), ckn.getNof(), ckn.getNopf(), ckn.getNosf(), ckn.getNom(),
						ckn.getNopm(), ckn.getNosm(), ckn.getNosi(), ckn.getRfc(), ckn.getWmc(), ckn.getLoc(),
						ckn.getLcom());

			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			repo.getScm().reset();
		}

	}

	public String parseDate(Date date) {
		String parsedDate = "";
		parsedDate += date.getDay() + "-";
		parsedDate += date.getMonth() + "-";
		parsedDate += date.getYear();
		return parsedDate;
	}

	@Override
	public String name() {
		return "MonthlyMetricsVisitor";
	}

}
