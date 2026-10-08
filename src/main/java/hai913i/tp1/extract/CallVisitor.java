package hai913i.tp1.extract;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

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

    private static String argumentsText(List<?> arguments) {
        StringJoiner text = new StringJoiner(", ", "(", ")");
        for (Object argument : arguments) {
            text.add(argument.toString());
        }
        return text.toString();
    }

    private void record(IMethodBinding methodBinding, String receiverType, boolean isSuperCall, int line, String callText) {
        IMethodBinding b = currentMethod();
        String caller = b != null ? b.getDeclaringClass().getName() + "." + b.getName() + ", " : "";
        IMethodBinding methodDeclaration = methodBinding != null ? methodBinding.getMethodDeclaration() : null;
        ITypeBinding declaringClass = methodDeclaration != null ? methodDeclaration.getDeclaringClass() : null;
        boolean isResolved = declaringClass != null;

        if (isResolved) {
            if (declaringClass.isFromSource()) {
                internalMethodCalls++;
            } else {
                externalMethodCalls++;
            }
        } else {
            unresolvedMethodCalls++;
        }
        String cible = utility.methodIdentifier(methodBinding);
        System.out.println(caller + "Ligne" + line + ", " + callText + ", " + (isSuperCall ? "[SUPER] " : "") + "Receveur: " + receiverType + ", Cible: " + cible);
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
            callText = expr + "." + node.getName() + argumentsText(node.arguments());
        } else {
            if (binding != null && Modifier.isStatic(binding.getModifiers())) {
                receiver = binding.getDeclaringClass();
            } else {
                receiver = currentType();
            }
            callText = node.getName() + argumentsText(node.arguments());
        }
        record(binding, typeName(receiver), false, lineOf(node), callText);
        return true; // Continue visiting childs
    }

    @Override
    public boolean visit(SuperMethodInvocation node) {
        methodCalls++;
        ITypeBinding currentType = currentType();
        String receiverType = typeName(currentType != null ? currentType.getSuperclass() : null);
        record(node.resolveMethodBinding(), receiverType, true, lineOf(node), "super." + node.getName() + argumentsText(node.arguments()));
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
