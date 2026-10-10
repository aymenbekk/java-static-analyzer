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

    public static String generateReport(ProjectFacts facts) {
        StringBuilder report = new StringBuilder();
        appendStructure(report, facts);
        appendCalls(report, facts);
        appendSummary(report, facts);
        return report.toString();
    }

    private static void appendStructure(StringBuilder report, ProjectFacts facts) {
        for (TypeFact type : facts.types()) {
            report.append("Type : ").append(type.qualifiedName()).append("\n");
            report.append("  genre      : ").append(type.kind()).append("\n");
            report.append("  paquetage  : ").append(type.packageName()).append("\n");
            report.append("  superclasses : ").append(String.join(" ", type.superclasses())).append("\n");
            report.append("  interfaces   : ").append(String.join(" ", type.interfaces())).append("\n");
            report.append("  attributs :\n");
            for (FieldFact field : type.fields()) {
                report.append("    - ").append(field.name()).append(" : ").append(field.declaredType())
                        .append(" (").append(field.visibility()).append(")\n");
            }
            report.append("  methodes :\n");
            for (MethodFact method : type.methods()) {
                report.append("    - ").append(method.name())
                        .append(" paramsCount=").append(method.parameterTypes().size())
                        .append(" constructeur=").append(method.constructor() ? "oui" : "non").append("\n");
            }
            report.append("\n");
        }
    }

    private static void appendCalls(StringBuilder report, ProjectFacts facts) {
        for (TypeFact type : facts.types()) {
            for (MethodFact method : type.methods()) {
                if (method.calls().isEmpty()) {
                    continue;
                }
                report.append(method.id()).append("\n");
                for (CallFact call : method.calls()) {
                    report.append("    ligne ").append(call.sourceLine())
                            .append(" : receveur ").append(call.staticReceiverType())
                            .append(", cible ").append(call.targetId().orElse("<non resolue>")).append("\n");
                }
            }
        }
    }

    private static void appendSummary(StringBuilder report, ProjectFacts facts) {
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
        report.append("\n");
        report.append("Types              : ").append(facts.types().size()).append("\n");
        report.append("Methodes           : ").append(methods).append("\n");
        report.append("Attributs          : ").append(fields).append("\n");
        report.append("Appels de methodes : ").append(calls).append("\n");
        report.append("Appels internes    : ").append(internal).append("\n");
        report.append("Appels externes    : ").append(external).append("\n");
        report.append("Appels non resolus : ").append(unresolved).append("\n");
    }
}
