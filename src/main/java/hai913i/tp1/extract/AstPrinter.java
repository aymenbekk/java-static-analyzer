package hai913i.tp1.extract;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

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
