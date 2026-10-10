package hai913i.tp1;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;

import hai913i.tp1.parse.JdtParser;
import hai913i.tp1.parse.JdtParser.ParsedFile;
import hai913i.tp1.parse.ProjectSources;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.report.TextReport;
import hai913i.tp1.metrics.metrics;

import hai913i.tp1.callgraph.CallGraph;
import hai913i.tp1.cli.CommandLine;
import hai913i.tp1.cli.UsageException;
import hai913i.tp1.report.CallGraphReport;
import hai913i.tp1.report.MetricsReport;
import hai913i.tp1.cli.Options;
import hai913i.tp1.extract.FactsExtractor;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        try {
            run(args);
        } catch (Exception e) {
            System.err.println("Erreur interne : " + e.getClass().getSimpleName() + " : " + e.getMessage());
            System.exit(1);
        }
    }

    private static void run(String[] args) throws Exception {

        if (Arrays.asList(args).contains("--help")) {
        System.out.println(CommandLine.USAGE);
        System.exit(0);
        }
        Options options;
        try {
            options = CommandLine.parse(args);
        } catch (UsageException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.err.println(CommandLine.USAGE);
            System.exit(2);
            return;
        }
        Path project = options.project();

        ProjectSources sources;
        try {
            sources = ProjectSources.of(project);
            if(sources.javaFiles().isEmpty()) {
                System.err.println("Erreur : aucun fichier Java trouve dans " + project);
                System.exit(3);
                return;
            }
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

        FactsExtractor.Extraction extraction;
        try {
            extraction = FactsExtractor.extract(files);
        } catch (IOException e) {
            System.err.println("Erreur : " + e.getMessage());
            System.exit(4);
            return;
        }
        ProjectFacts projectFacts = extraction.facts();


        if (extraction.orphanCalls() > 0) {
            System.err.println("Attention : " + extraction.orphanCalls() + " appels sans methode appelante (ignores)");
        }

        System.out.println();

        switch (options.action()) {
            case "structure" -> System.out.print(TextReport.generateStructure(projectFacts));
            case "calls" -> System.out.print(TextReport.generateCalls(projectFacts));
            case "metrics" -> System.out.print(MetricsReport.generateReport(new metrics(projectFacts), projectFacts, options.x()));
            case "graph" -> System.out.print(CallGraphReport.generateReport(new CallGraph(projectFacts)));
            case "query" -> System.out.print(CallGraphReport.generateQueryReport(new CallGraph(projectFacts), options.method()));
            case "all" -> {
                System.out.print(TextReport.generateReport(projectFacts));
                System.out.println();
                System.out.print(MetricsReport.generateReport(new metrics(projectFacts), projectFacts, options.x()));
                System.out.println();
                System.out.print(CallGraphReport.generateReport(new CallGraph(projectFacts)));
            }
        }


    }

}
