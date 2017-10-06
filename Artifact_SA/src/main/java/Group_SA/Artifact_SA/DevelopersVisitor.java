package Group_SA.Artifact_SA;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class DevelopersVisitor implements CommitVisitor {

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		writer.write(
			commit.getHash(),
			commit.getAuthor().getName(),
			commit.getAuthor().getEmail()
		);

	}

	@Override
	public String name() {
		return "developers";
	}

}
