package hai913i.tp1.report;

import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.FieldFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

// Affichage texte du modele de faits : aucune dependance a JDT.
public final class TextReport {

    private TextReport() {
    }

    public static void print(ProjectFacts facts) {
        printStructure(facts);
        printCalls(facts);
        printSummary(facts);
    }

    // A2 : structure de chaque type.
    private static void printStructure(ProjectFacts facts) {
        for (TypeFact type : facts.types()) {
            System.out.println("Type : " + type.qualifiedName());
            System.out.println("  genre      : " + type.kind());
            System.out.println("  paquetage  : " + type.packageName());
            System.out.println("  superclasses : " + String.join(" ", type.superclasses()));
            System.out.println("  interfaces   : " + String.join(" ", type.interfaces()));
            System.out.println("  attributs :");
            for (FieldFact field : type.fields()) {
                System.out.println("    - " + field.name() + " : " + field.declaredType()
                        + " (" + field.visibility() + ")");
            }
            System.out.println("  methodes :");
            for (MethodFact method : type.methods()) {
                System.out.println("    - " + method.name()
                        + " paramsCount=" + method.parameterTypes().size()
                        + " constructeur=" + (method.constructor() ? "oui" : "non"));
            }
            System.out.println();
        }
    }

    // A3 : pour chaque methode, ses appels.
    private static void printCalls(ProjectFacts facts) {
        for (TypeFact type : facts.types()) {
            for (MethodFact method : type.methods()) {
                if (method.calls().isEmpty()) {
                    continue;
                }
                System.out.println(method.id());
                for (CallFact call : method.calls()) {
                    System.out.println("    ligne " + call.sourceLine() + " : receveur " + call.staticReceiverType()
                            + ", cible " + call.targetId().orElse("<non resolue>"));
                }
            }
        }
    }

    private static void printSummary(ProjectFacts facts) {
        int methods = 0, fields = 0;
        int calls = 0, internal = 0, external = 0, unresolved = 0;
        for (TypeFact type : facts.types()) {
            methods += type.methods().size();
            fields += type.fields().size();
            for (MethodFact method : type.methods()) {
                for (CallFact call : method.calls()) {
                    calls++;
                    if (!call.resolved()) {
                        unresolved++;
                    } else if (call.internal()) {
                        internal++;
                    } else {
                        external++;
                    }
                }
            }
        }
        System.out.println();
        System.out.println("Types              : " + facts.types().size());
        System.out.println("Methodes           : " + methods);
        System.out.println("Attributs          : " + fields);
        System.out.println("Appels de methodes : " + calls);
        System.out.println("Appels internes    : " + internal);
        System.out.println("Appels externes    : " + external);
        System.out.println("Appels non resolus : " + unresolved);
    }
}
