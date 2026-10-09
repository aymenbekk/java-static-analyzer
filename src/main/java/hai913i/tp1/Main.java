package hai913i.tp1;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.extract.CallVisitor;
import hai913i.tp1.extract.FactsBuilder;
import hai913i.tp1.extract.StructureVisitor;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.report.TextReport;
import hai913i.tp1.metrics.metrics;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage : java -jar target/hai913i-tp1-analyzer.jar DOSSIER_DU_PROJET");
            System.exit(2);
        }
        Path project = Path.of(args[0]);
        ProjectSources sources;
        try {
            sources = ProjectSources.of(project);
        } catch (IllegalArgumentException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.exit(3);
            return;
        }

        List<ParsedFile> files = JdtParser.parse(sources, List.of());
        int errors = 0;
        for (ParsedFile file : files) {
            for (IProblem problem : file.unit().getProblems()) {
                if (problem.isError()) {
                    errors++;
                    System.err.println(file.path().getFileName() + ":" + problem.getSourceLineNumber() + " "
                            + problem.getMessage());
                }
            }
        }
        System.out.println("Racine des sources     : " + sources.sourceRoot());
        System.out.println("Unites de compilation  : " + files.size());
        System.out.println("Erreurs de compilation : " + errors);

        FactsBuilder facts = new FactsBuilder();
        for (ParsedFile file : files) {
            try {
                facts.addSourceFile(file.path(), Files.readAllLines(file.path()).size());
            } catch (IOException e) {
                System.err.println("Erreur : lecture impossible de " + file.path() + " (" + e.getMessage() + ")");
                System.exit(4);
            }
        }

        // A2 : structure de chaque type (un seul visiteur pour tous les fichiers).
        StructureVisitor structure = new StructureVisitor(facts);
        for (ParsedFile file : files) {
            file.unit().accept(structure);
        }

        // A3 : appels de methodes (un seul visiteur pour tous les fichiers).
        CallVisitor calls = new CallVisitor(facts);
        for (ParsedFile file : files) {
            file.unit().accept(calls);
        }

        ProjectFacts projectFacts = facts.build();

        // Affichage : ne lit que le modele de faits (aucune classe de JDT).
        System.out.println();
        TextReport.print(projectFacts);
        System.out.println("Appels orphelins   : " + facts.orphanCalls());

        metrics metrics = new metrics(projectFacts);
        System.out.println("Nombre de types : " + metrics.getNumberOftypes());
        System.out.println("Somme des lignes : " + metrics.getSumOfLines());
        System.out.println("Nombre de methodes : " + metrics.numberOfMethods());
        System.out.println("Nombre de packages distincts : " + metrics.numberOfDistinctPackages());
        System.out.println("Moyenne de methodes par type : " + metrics.avgNumberOfMethodsPerType());
        System.out.println("Moyenne de lignes par methode : " + metrics.avgNumberOfLinesPerMethod());
        System.out.println("Moyenne de champs par type : " + metrics.avgNumberOfFieldsPerType());
        metrics.MaxParameters maxParameters = metrics.getMaxParameters();
        System.out.println("Nombre maximum de parametres : " + maxParameters.count());
        System.out.println("Methodes avec le maximum de parametres :");
        for (var method : maxParameters.methods()) {
            System.out.println("  " + method.id() + " (" + method.parameterTypes().size() + " parametres)");
        }
        System.out.println("Top 10% des types par nombre de methodes :");
        for (var rankedType : metrics.topTenPercentTypesByMethodCount()) {
            System.out.println("  " + rankedType.item().qualifiedName() + " (" + rankedType.value() + " methodes)");
        }

        System.out.println("Top 10% des types par nombre d'attributs :");
        for (var rankedType : metrics.topTenPercentTypesByFieldCount()) {
            System.out.println("  " + rankedType.item().qualifiedName() + " (" + rankedType.value() + " attributs)");
        }

        System.out.println(" les classes qui font partie à la fois des deux categories precedentes");
         for (var rankedType : metrics.typesInBothTopTenPercent()) {
            System.out.println("  " + rankedType.qualifiedName() );
        }

        int x = 2; 
        System.out.println("Les classes qui ont plus de " + x + " methodes :");
        for (var type : metrics.TypesWithMoreThanXMethods(x)) {
            System.out.println("  " + type.qualifiedName() + " (" + type.methods().size() + " methodes)");
        }

        System.out.println("Top 10% des methodes pour chaque type par nombre de lignes de code :");
        for (var type : projectFacts.types()) {
            System.out.println("Type : " + type.qualifiedName());
            for (var rankedMethod : metrics.topTenPercentMethodsByLineCountForType(type)) {
                System.out.println("  " + rankedMethod.item().id() + " (" + rankedMethod.value() + " lignes)");
            }
        }
    }

}
