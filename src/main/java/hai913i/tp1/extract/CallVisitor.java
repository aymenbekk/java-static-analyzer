package hai913i.tp1.extract;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;

import hai913i.tp1.model.CallFact;

public class CallVisitor extends ASTVisitor {

    private final FactsBuilder facts;

    public CallVisitor(FactsBuilder facts) {
        this.facts = facts;
    }

    private final Deque<ITypeBinding> typeStack = new LinkedList<>();
    private final Deque<IMethodBinding> methodStack = new LinkedList<>();

    private int anonymousDepth = 0;

    private ITypeBinding currentType() {
        return typeStack.peek(); 
    }

    private IMethodBinding currentMethod() {
        return methodStack.peek(); 
    }

    private static String typeName(ITypeBinding type) {
        return type != null ? type.getErasure().getQualifiedName() : "<non resolu>";
    }

    private static int lineOf(ASTNode node) {
        ASTNode root = node.getRoot();
        return root instanceof CompilationUnit cu ? cu.getLineNumber(node.getStartPosition()) : -1;
    }

    private void record(IMethodBinding methodBinding, String receiverType, int line) {
        IMethodBinding caller = currentMethod();
        if (caller == null) {
            return; 
        }
        IMethodBinding methodDeclaration = methodBinding != null ? methodBinding.getMethodDeclaration() : null;
        ITypeBinding declaringClass = methodDeclaration != null ? methodDeclaration.getDeclaringClass() : null;
        boolean resolved = declaringClass != null;
        boolean internal = resolved && declaringClass.isFromSource();

        Optional<String> targetId = resolved ? Optional.of(utility.methodIdentifier(methodBinding)) : Optional.empty();

        facts.addCall(new CallFact(utility.methodIdentifier(caller), targetId, receiverType, line, resolved, internal));
    }

    @Override
    public boolean visit(MethodInvocation node) {
        IMethodBinding binding = node.resolveMethodBinding();
        Expression expr = node.getExpression();
        ITypeBinding receiver;
        if (expr != null) {
            receiver = expr.resolveTypeBinding();
        } else if (binding != null && Modifier.isStatic(binding.getModifiers())) {
            receiver = binding.getDeclaringClass();
        } else {
            receiver = currentType();
        }
        record(binding, typeName(receiver), lineOf(node));
        return true; // Continue visiting childs
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        ITypeBinding currentType = currentType();
        String receiverType = typeName(currentType != null ? currentType.getSuperclass() : null);
        record(node.resolveMethodBinding(), receiverType, lineOf(node));
        return true; // Continue visiting childs
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        typeStack.push(node.resolveBinding());
        if (node.isLocalTypeDeclaration()) {
            anonymousDepth++;
        }
        return true; // Continue visiting childs
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        typeStack.pop();
        if (node.isLocalTypeDeclaration()) {
            anonymousDepth--;
        }
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        typeStack.push(node.resolveBinding());
        return true; // Continue visiting childs
    }

    @Override
    public void endVisit(EnumDeclaration node) {
        typeStack.pop();
    }

    @Override
    public boolean visit(RecordDeclaration node) {
        typeStack.push(node.resolveBinding());
        return true; // Continue visiting childs
    }

    @Override
    public void endVisit(RecordDeclaration node) {
        typeStack.pop();
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        anonymousDepth++;
        return true; // Continue visiting childs
    }

    @Override
    public void endVisit(AnonymousClassDeclaration node) {
        anonymousDepth--;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        if (anonymousDepth == 0) {
            methodStack.push(node.resolveBinding());
        }
        return true; // Continue visiting childs
    }

    @Override
    public void endVisit(MethodDeclaration node) {
        
        if (anonymousDepth == 0) {
            methodStack.pop();
        }
    }
}
