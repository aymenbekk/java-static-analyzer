package hai913i.tp1.extract;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;

import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.TypeFact;


public class StructureVisitor extends ASTVisitor {

    private final FactsBuilder facts;

    public StructureVisitor(FactsBuilder facts) {
        this.facts = facts;
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }
    @Override
    public boolean visit(TypeDeclaration node) {
        buildType(node);
        return true;
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        buildType(node);
        return true;
    }

    @Override
    public boolean visit(RecordDeclaration node) {
        buildType(node);
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        return false;
    }

    private void buildType(AbstractTypeDeclaration node) {
        ITypeBinding type = node.resolveBinding();

        List<String> superclasses = new ArrayList<>();
        for (ITypeBinding s = type.getSuperclass(); s != null && s.getSuperclass() != null; s = s.getSuperclass()) {
            superclasses.add(s.getErasure().getQualifiedName());
        }
        List<String> interfaces = new ArrayList<>();
        for (ITypeBinding i : type.getInterfaces()) {
            interfaces.add(i.getQualifiedName());
        }

        List<FieldFact> fields = new ArrayList<>();
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof FieldDeclaration field) {
                for (Object f : field.fragments()) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) f;
                    fields.add(new FieldFact(fragment.getName().getIdentifier(), field.getType().toString(),
                            visibility(field.getModifiers(), type)));
                }
            }
        }

        List<MethodFact> methods = new ArrayList<>();
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof MethodDeclaration method) {
                IMethodBinding binding = method.resolveBinding();
                List<String> parameterTypes = new ArrayList<>();
                if (binding != null) {
                    for (ITypeBinding p : binding.getParameterTypes()) {
                        parameterTypes.add(p.getErasure().getQualifiedName());
                    }
                }
                methods.add(new MethodFact(utility.methodIdentifier(binding), method.getName().getIdentifier(),
                        List.copyOf(parameterTypes), method.isConstructor(), bodyLoc(method), List.of()));
            }
        }

        facts.addType(new TypeFact(type.getQualifiedName(), type.getPackage().getName(), kind(type),
                List.copyOf(superclasses), List.copyOf(interfaces), List.copyOf(fields), List.copyOf(methods)));
    }

    private static int bodyLoc(MethodDeclaration method) {
        Block body = method.getBody();
        if (body == null || !(method.getRoot() instanceof CompilationUnit cu)) {
            return 0;
        }
        int start = cu.getLineNumber(body.getStartPosition());
        int end = cu.getLineNumber(body.getStartPosition() + body.getLength() - 1);
        return end - start + 1;
    }

    private static String kind(ITypeBinding type) {
        if (type.isInterface()) {
            return "interface";
        }
        if (type.isEnum()) {
            return "enumeration";
        }
        if (type.isRecord()) {
            return "record";
        }
        return "classe";
    }

    private static String visibility(int modifiers, ITypeBinding owner) {
        if (Modifier.isPublic(modifiers) || owner.isInterface()) {
            return "public";
        }
        if (Modifier.isProtected(modifiers)) {
            return "protected";
        }
        if (Modifier.isPrivate(modifiers)) {
            return "private";
        }
        return "package";
    }
}
