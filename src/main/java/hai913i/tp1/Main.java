package hai913i.tp1;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.extract.CallVisitor;
import hai913i.tp1.extract.StructureVisitor;

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

        // A2 : structure de chaque type (un seul visiteur pour tous les fichiers).
        StructureVisitor structure = new StructureVisitor();
        for (ParsedFile file : files) {
            file.unit().accept(structure);
        }
        // System.out.println();
        // structure.lignes().forEach(System.out::println);
        // System.out.println(structure.report());

        // Appels de methodes (un seul visiteur pour tous les fichiers).
        CallVisitor calls = new CallVisitor();
        System.out.println();
        for (ParsedFile file : files) {
            file.unit().accept(calls);
        }
        System.out.println("Appels de methodes     : " + calls.methodCalls);
        System.out.println("Appels internes         : " + calls.internalMethodCalls);
        System.out.println("Appels externes         : " + calls.externalMethodCalls);
        System.out.println("Appels non resolus      : " + calls.unresolvedMethodCalls);
    }

}
