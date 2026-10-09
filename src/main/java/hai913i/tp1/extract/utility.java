package hai913i.tp1.extract;

import java.util.StringJoiner;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class utility {
    public static String methodIdentifier(IMethodBinding methodBinding) {
        if (methodBinding == null) {
            return "<non resolue>";
        }
        IMethodBinding methodDeclaration = methodBinding.getMethodDeclaration();
        ITypeBinding declaringClass = methodDeclaration != null ? methodDeclaration.getDeclaringClass() : null;
        if (declaringClass == null) {
            return "<non resolue>";
        }
        StringJoiner parameters = new StringJoiner(", ", "(", ")");
        for (ITypeBinding parameterType : methodDeclaration.getParameterTypes()) {
            parameters.add(parameterType.getErasure().getQualifiedName());
        }
        return declaringClass.getQualifiedName() + "#" + methodDeclaration.getName() + parameters;
    }
}
