package DynamicDuo.RefactoringUtils;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class HalsteadExtractor {

	
	public static void traverseAST(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	    parser.setSource(source.toCharArray());
	    //parser.setSource("/*abc*/".toCharArray());
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    //ASTNode node = parser.createAST(null);
	 
	 
	    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	    HalsteadVisitor visitor = new HalsteadVisitor();
	    cu.accept(visitor);
	    visitor.printResults();
	}
}
