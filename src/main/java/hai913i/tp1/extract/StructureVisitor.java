package hai913i.tp1.extract;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;


public class StructureVisitor extends ASTVisitor {


    private int classes, interfaces, enums, records;
    private int methods, constructors, fields;

    private final List<String> lignes = new ArrayList<>();

    public List<String> lignes() {
        return lignes;
    }

    public String report() {
        return (classes + interfaces + enums + records) + " types : "
                + classes + " classes, " + interfaces + " interfaces, " + enums + " énumérations, "
                + records + " records ; " + methods + " méthodes, dont " + constructors
                + " constructeurs ; " + fields + " attributs";
    }

    @Override
    public boolean visit(AnonymousClassDeclaration node) {
        return false;
    }
    @Override
    public boolean visit(TypeDeclaration node) {
        printType(node);
        return true; 
    }

    @Override
    public boolean visit(EnumDeclaration node) {
        printType(node);
        return true;
    }

    @Override
    public boolean visit(RecordDeclaration node) {
        printType(node);
        return true;
    }

    @Override
    public boolean visit(MethodDeclaration node) {
        return false; 
    }

    private void printType(AbstractTypeDeclaration node) {
        ITypeBinding type = node.resolveBinding();

        lignes.add("Type : " + type.getQualifiedName());
        lignes.add("  genre      : " + Type(type));
        if (type.isInterface()) {
            interfaces++;
        } else if (type.isEnum()) {
            enums++;
        } else if (type.isRecord()) {
            records++;
        } else {
            classes++;
        }
        lignes.add("  paquetage  : " + type.getPackage().getName());

        String superClasses = "";
        for (ITypeBinding s = type.getSuperclass(); s != null && s.getSuperclass() != null; s = s.getSuperclass()) {
            superClasses += s.getErasure().getQualifiedName() + " ";
        }
        String interfaces = "";
        for (ITypeBinding i : type.getInterfaces()) {
            interfaces += i.getQualifiedName() + " ";
        }
        lignes.add("  superclasses : " + superClasses);
        lignes.add("  interfaces   : " + interfaces);


        lignes.add("  attributs :");
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof FieldDeclaration field) {
                for (Object f : field.fragments()) {
                    VariableDeclarationFragment fragment = (VariableDeclarationFragment) f;
                    fields++;
                    lignes.add("    - " + fragment.getName() + " : " + field.getType()
                            + " (" + visibility(field.getModifiers(), type) + ")");
                }
            }
        }


        lignes.add("  méthodes :");
        for (Object o : node.bodyDeclarations()) {
            if (o instanceof MethodDeclaration method) {
                methods++;
                if (method.isConstructor()) {
                    constructors++;
                }
                lignes.add("    - " + method.getName()
                        + " paramsCount=" + method.parameters().size()
                        + " constructeur=" + (method.isConstructor() ? "oui" : "non"));
            }
        }
        lignes.add("");
    }

    private static String Type(ITypeBinding type) {
        if (type.isInterface()) {
            return "interface";
        }
        if (type.isEnum()) {
            return "énumération";
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
