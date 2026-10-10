package hai913i.tp1.report;

import hai913i.tp1.metrics.metrics;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

public final class MetricsReport {

    private MetricsReport() {
    }

    public static String generateReport(metrics metrics, ProjectFacts facts, int x) {
        StringBuilder report = new StringBuilder();
        report.append("Nombre de types : ").append(metrics.getNumberOftypes()).append("\n");
        report.append("Somme des lignes : ").append(metrics.getSumOfLines()).append("\n");
        report.append("Nombre de methodes : ").append(metrics.numberOfMethods()).append("\n");
        report.append("Nombre de packages distincts : ").append(metrics.numberOfDistinctPackages()).append("\n");
        report.append("Moyenne de methodes par type : ").append(metrics.avgNumberOfMethodsPerType()).append("\n");
        report.append("Moyenne de lignes par methode : ").append(metrics.avgNumberOfLinesPerMethod()).append("\n");
        report.append("Moyenne de champs par type : ").append(metrics.avgNumberOfFieldsPerType()).append("\n");

        metrics.MaxParameters maxParameters = metrics.getMaxParameters();
        report.append("Nombre maximum de parametres : ").append(maxParameters.count()).append("\n");
        report.append("Methodes avec le maximum de parametres :\n");
        for (MethodFact method : maxParameters.methods()) {
            report.append("  ").append(method.id())
                    .append(" (").append(method.parameterTypes().size()).append(" parametres)\n");
        }

        report.append("Top 10% des types par nombre de methodes :\n");
        for (metrics.Ranked<TypeFact> rankedType : metrics.topTenPercentTypesByMethodCount()) {
            report.append("  ").append(rankedType.item().qualifiedName())
                    .append(" (").append(rankedType.value()).append(" methodes)\n");
        }

        report.append("Top 10% des types par nombre d'attributs :\n");
        for (metrics.Ranked<TypeFact> rankedType : metrics.topTenPercentTypesByFieldCount()) {
            report.append("  ").append(rankedType.item().qualifiedName())
                    .append(" (").append(rankedType.value()).append(" attributs)\n");
        }

        report.append(" les classes qui font partie a la fois des deux categories precedentes\n");
        for (TypeFact type : metrics.typesInBothTopTenPercent()) {
            report.append("  ").append(type.qualifiedName()).append("\n");
        }

        report.append("Les classes qui ont plus de ").append(x).append(" methodes :\n");
        for (TypeFact type : metrics.TypesWithMoreThanXMethods(x)) {
            report.append("  ").append(type.qualifiedName())
                    .append(" (").append(type.methods().size()).append(" methodes)\n");
        }

        report.append("Top 10% des methodes pour chaque type par nombre de lignes de code :\n");
        for (TypeFact type : facts.types()) {
            report.append("Type : ").append(type.qualifiedName()).append("\n");
            for (metrics.Ranked<MethodFact> rankedMethod : metrics.topTenPercentMethodsByLineCountForType(type)) {
                report.append("  ").append(rankedMethod.item().id())
                        .append(" (").append(rankedMethod.value()).append(" lignes)\n");
            }
        }
        return report.toString();
    }
}
