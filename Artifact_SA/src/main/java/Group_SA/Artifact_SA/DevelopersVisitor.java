package Group_SA.Artifact_SA;

import java.util.ArrayList;
import java.util.List;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.domain.ModificationType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import com.github.mauricioaniche.ck.CK;

public class DevelopersVisitor implements CommitVisitor {

	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {
		
//	    System.out.println("A WOLLAH");
//	    for(Modification mod : commit.getModifications()) {
//	      if(mod.getType().equals(ModificationType.MODIFY)) {
//	        System.out.println(ck.calculate(mod.getNewPath()));
//	      }
//	    }
		
		List<String> sourceC0de = new ArrayList<String>();
		for(Modification mod: commit.getModifications()){
			if(mod.getType().equals(ModificationType.MODIFY)){
				sourceC0de.add(mod.getSourceCode());
			}
		}
		
		System.out.println(repo.getScm().files().size());
		writer.write(	
			commit.getAuthor().getName(),
			commit.getAuthor().getEmail(),
			sourceC0de
		);

	}

	@Override
	public String name() {
		return "sourceC0de";
	}

}
