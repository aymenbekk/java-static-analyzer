package hai913i.tp1.extract;
import java.util.Deque;
import java.util.LinkedList;

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

public class CallVisitor extends ASTVisitor {

    public int methodCalls = 0;
    public int externalMethodCalls = 0;
    public int internalMethodCalls = 0;
    public int unresolvedMethodCalls = 0;

    private final Deque<ITypeBinding> typeStack = new LinkedList<>();
    private final Deque<MethodDeclaration> methodStack = new LinkedList<>();

    private int anonymousDepth = 0;

    private ITypeBinding currentType() {
        return typeStack.peek(); 
    }

    private MethodDeclaration currentMethod() {
        return methodStack.peek(); 
    }

    private static String typeName(ITypeBinding type) {
        return type != null ? type.getErasure().getQualifiedName() : "<non resolu>";
    }

    private static int lineOf(ASTNode node) {
        ASTNode root = node.getRoot();
        return root instanceof CompilationUnit cu ? cu.getLineNumber(node.getStartPosition()) : -1;
    }

    private void record(IMethodBinding methodBinding, String receiverType, boolean isSuperCall, int line, String callText) {
        MethodDeclaration currentMethod = currentMethod();
        IMethodBinding b = currentMethod != null ? currentMethod.resolveBinding() : null;
        String caller = b != null ? b.getDeclaringClass().getName() + "." + b.getName() + ", " : "";
        String cible;
        if (methodBinding != null) {
            IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
            ITypeBinding declaringClass = methodDeclaration.getDeclaringClass();
            if (declaringClass != null) {
                if(declaringClass.isFromSource()) {
                    internalMethodCalls++;
                } else {
                    externalMethodCalls++;
                }
                cible = declaringClass.getQualifiedName() + "." + methodDeclaration.getName();
            }
            else{
                unresolvedMethodCalls++;
                cible = "non résolue";
            }
        }
        else{
            unresolvedMethodCalls++;
            cible = "non résolue";
        }
        System.out.println(caller + "Ligne" + line + ", " + callText + "(), " + (isSuperCall ? "[SUPER] " : "") + "Receveur: " + receiverType + ", Cible: " + cible);
    }
    @Override
    public boolean visit(MethodInvocation node) {
        methodCalls++;
        IMethodBinding binding = node.resolveMethodBinding();
        Expression expr = node.getExpression();
        ITypeBinding receiver;
        String callText;
        if (expr != null) {
            receiver = expr.resolveTypeBinding();
            callText = expr + "." + node.getName();
        } else {
            if (binding != null && Modifier.isStatic(binding.getModifiers())) {
                receiver = binding.getDeclaringClass();
            } else {
                receiver = currentType();
            }
            callText = node.getName().toString();
        }
        record(binding, typeName(receiver), false, lineOf(node), callText);
        return true; // Continue visiting childs
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        methodCalls++;
        ITypeBinding currentType = currentType();
        String receiverType = typeName(currentType != null ? currentType.getSuperclass() : null);
        record(node.resolveMethodBinding(), receiverType, true, lineOf(node), "super." + node.getName());
        return true; // Continue visiting childs
    }

    @Override
    public boolean visit(TypeDeclaration node) {
        typeStack.push(node.resolveBinding());
        return true; // Continue visiting childs
    }

    @Override
    public void endVisit(TypeDeclaration node) {
        typeStack.pop();
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
            methodStack.push(node);
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
