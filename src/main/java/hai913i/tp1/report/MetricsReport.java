package hai913i.tp1.report;

import java.util.List;

import hai913i.tp1.metrics.metrics;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

public final class MetricsReport {

    private MetricsReport() {
    }

    public static String generateReport(metrics metrics, ProjectFacts facts, int x) {
        StringBuilder report = new StringBuilder();
        report.append("Q1  Nombre de types : ").append(metrics.getNumberOftypes()).append("\n");
        report.append("Q2  Somme des lignes : ").append(metrics.getSumOfLines()).append("\n");
        report.append("Q3  Nombre de methodes : ").append(metrics.numberOfMethods()).append("\n");
        report.append("Q4  Nombre de packages distincts : ").append(metrics.numberOfDistinctPackages()).append("\n");
        report.append("Q5  Moyenne de methodes par type : ").append(metrics.avgNumberOfMethodsPerType()).append("\n");
        report.append("Q6  Moyenne de lignes par methode : ").append(metrics.avgNumberOfLinesPerMethod()).append("\n");
        report.append("Q7  Moyenne de champs par type : ").append(metrics.avgNumberOfFieldsPerType()).append("\n");

        report.append("Q8  Top 10% des types par nombre de methodes :\n");
        List<metrics.Ranked<TypeFact>> topByMethods = metrics.topTenPercentTypesByMethodCount();
        appendIfEmpty(report, topByMethods);
        for (metrics.Ranked<TypeFact> rankedType : topByMethods) {
            report.append("  ").append(rankedType.item().qualifiedName())
                    .append(" (").append(rankedType.value()).append(" methodes)\n");
        }

        report.append("Q9  Top 10% des types par nombre d'attributs :\n");
        List<metrics.Ranked<TypeFact>> topByFields = metrics.topTenPercentTypesByFieldCount();
        appendIfEmpty(report, topByFields);
        for (metrics.Ranked<TypeFact> rankedType : topByFields) {
            report.append("  ").append(rankedType.item().qualifiedName())
                    .append(" (").append(rankedType.value()).append(" attributs)\n");
        }

        report.append("Q10 Classes qui font partie a la fois des deux categories precedentes :\n");
        List<TypeFact> inBoth = metrics.typesInBothTopTenPercent();
        appendIfEmpty(report, inBoth);
        for (TypeFact type : inBoth) {
            report.append("  ").append(type.qualifiedName()).append("\n");
        }

        report.append("Q11 Classes qui ont plus de ").append(x).append(" methodes :\n");
        List<TypeFact> moreThanX = metrics.TypesWithMoreThanXMethods(x);
        appendIfEmpty(report, moreThanX);
        for (TypeFact type : moreThanX) {
            report.append("  ").append(type.qualifiedName())
                    .append(" (").append(type.methods().size()).append(" methodes)\n");
        }

        report.append("Q12 Top 10% des methodes de chaque type par nombre de lignes de code :\n");
        for (TypeFact type : facts.types()) {
            report.append("Type : ").append(type.qualifiedName()).append("\n");
            List<metrics.Ranked<MethodFact>> topMethods = metrics.topTenPercentMethodsByLineCountForType(type);
            appendIfEmpty(report, topMethods);
            for (metrics.Ranked<MethodFact> rankedMethod : topMethods) {
                report.append("  ").append(rankedMethod.item().id())
                        .append(" (").append(rankedMethod.value()).append(" lignes)\n");
            }
        }

        metrics.MaxParameters maxParameters = metrics.getMaxParameters();
        report.append("Q13 Nombre maximum de parametres : ").append(maxParameters.count()).append("\n");
        report.append("    Methodes avec le maximum de parametres :\n");
        appendIfEmpty(report, maxParameters.methods());
        for (MethodFact method : maxParameters.methods()) {
            report.append("  ").append(method.id())
                    .append(" (").append(method.parameterTypes().size()).append(" parametres)\n");
        }
        return report.toString();
    }

    private static void appendIfEmpty(StringBuilder report, List<?> list) {
        if (list.isEmpty()) {
            report.append("  (aucune)\n");
        }
    }
}
