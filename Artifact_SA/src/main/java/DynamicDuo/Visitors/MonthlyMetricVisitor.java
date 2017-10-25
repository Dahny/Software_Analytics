package DynamicDuo.Visitors;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;
import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKNumber;
import com.github.mauricioaniche.ck.CKReport;

import DynamicDuo.IO.IOHandler;
import DynamicDuo.RefactoringUtils.HalsteadExtractor;
import DynamicDuo.Study.StudyConstants;
import DynamicDuo.Study.StudyUtils;

public class MonthlyMetricVisitor implements CommitVisitor {

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		try {
			CK ck = new CK();
			StringBuilder path = new StringBuilder(StudyConstants.Repo_Path_Absolute);
			path.append(StudyConstants.Repo_Name);

			repo.getScm().checkout(commit.getHash());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String commitDate = sdf.format(commit.getDate().getTime());
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
						IOHandler.writeLineToCsv(StudyConstants.CSV_Class_Pairs, commit.getHash()
								+ StudyConstants.CSV_Delimiter + ckn.getFile() + StudyConstants.CSV_Delimiter + pair);
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

	@Override
	public String name() {
		return "MonthlyMetricsVisitor";
	}

}
