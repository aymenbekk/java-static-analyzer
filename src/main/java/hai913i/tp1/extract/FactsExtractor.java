package hai913i.tp1.extract;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.parse.JdtParser.ParsedFile;

public final class FactsExtractor {

    public record Extraction(ProjectFacts facts, int orphanCalls) {
    }

    private FactsExtractor() {
    }

    public static Extraction extract(List<ParsedFile> files) throws IOException {
        FactsBuilder builder = new FactsBuilder();
        for (ParsedFile file : files) {
            builder.addSourceFile(file.path(), countLines(file.path()));
        }

        StructureVisitor structure = new StructureVisitor(builder);
        for (ParsedFile file : files) {
            file.unit().accept(structure);
        }

        CallVisitor calls = new CallVisitor(builder);
        for (ParsedFile file : files) {
            file.unit().accept(calls);
        }

        ProjectFacts facts = builder.build();
        return new Extraction(facts, builder.orphanCalls());
    }

    private static int countLines(Path path) throws IOException {
        try {
            return Files.readAllLines(path).size();
        } catch (IOException e) {
            throw new IOException("lecture impossible de " + path + " (" + e.getMessage() + ")", e);
        }
    }
}
