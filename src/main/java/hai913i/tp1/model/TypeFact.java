package hai913i.tp1.model;

import java.util.List;

public record TypeFact(String qualifiedName, String packageName, String kind,
         List<String> superclasses, List<String> interfaces,
         List<FieldFact> fields, List<MethodFact> methods) {

    public TypeFact replaceMethods(List<MethodFact> newMethods) {
        return new TypeFact(qualifiedName, packageName, kind, superclasses, interfaces, fields,
                List.copyOf(newMethods));
    }
}
