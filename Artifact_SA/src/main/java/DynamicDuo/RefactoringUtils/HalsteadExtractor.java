package DynamicDuo.RefactoringUtils;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class HalsteadExtractor {

	
	public static double calculateHalsteadVolume(String source) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
	    parser.setSource(source.toCharArray());
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	 
	    final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	    HalsteadVisitor visitor = new HalsteadVisitor();
	    cu.accept(visitor);
	    return visitor.getHalsteadVolume();
	}
}
