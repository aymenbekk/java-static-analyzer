package hai913i.tp1;

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
            facts.addSourceFile(file.path());
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
    }

}
