package DynamicDuo.Visitors;

import org.repodriller.domain.Commit;
import org.repodriller.domain.Modification;
import org.repodriller.domain.ModificationType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;
import com.github.mauricioaniche.ck.CK;
import com.github.mauricioaniche.ck.CKReport;

import DynamicDuo.Study.StudyConstants;

public class MetricsVisitor implements CommitVisitor {
	/**
	 * 
	 * 
	 * 
	 * 
	 * WE PROBABLY DONT EVEN NEED THIS CLASS
	 * 
	 * 
	 * 
	 * 
	 */
	
	
	@Override
	public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {		
		try {
			CK ck = new CK();
//			if(IdentifiedRefactorCommitsHolder.getInstance().isIdentifiedRefactorCommit(commit.getHash())) {
//				//we are dealing with a commit that contained refactors
//			} 
			
			repo.getScm().checkout(commit.getHash());
			for(Modification mod : commit.getModifications()) {
		      if(mod.getType().equals(ModificationType.MODIFY) && mod.getNewPath().endsWith(".java")) {
		    	  
		    	StringBuilder path = new StringBuilder(StudyConstants.Repo_Path_Absolute + StudyConstants.Repo_Name +"\\"); 
		    	String[] parts = mod.getNewPath().split("/", -1);
		    	String fileName = parts[parts.length-1];
		    	parts[parts.length-1] = "";
		    	for(String part : parts) {
		    		path.append(part).append("\\");
		    	}
		    	path.delete(path.length() - 2, path.length() - 1);
		    	System.out.println("PATH: " + path);
		    	CKReport report = ck.calculate(path.toString());
		    	System.err.println(report.get(path.toString()+fileName));
		      }
		    }
			
		} 
		catch(Exception e) {
			e.printStackTrace();
		}
		finally {
			repo.getScm().reset();
		}

//		writer.write(	
//			commit.getAuthor().getName(),
//			commit.getAuthor().getEmail()
//		);

	}

	@Override
	public String name() {
		return "MetricsVisitor";
	}

}
