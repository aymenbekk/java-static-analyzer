package hai913i.tp1.extract;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class AstPrinter extends ASTVisitor {

    private int classCount = 0;
    private int methodCount = 0;
    private int fieldCount = 0;


    private int depth = 0;

    @Override
    public boolean preVisit2(ASTNode node) {
        System.out.println("  ".repeat(depth) + node.getClass().getSimpleName() );
        depth++;
        return true; 
    }

    @Override
    public void postVisit(ASTNode node) {
        depth--;
    }

    public int classCount() { return classCount; }
    public int methodCount() { return methodCount; }
    public int fieldCount() { return fieldCount; }

}
