package hai913i.tp1.extract;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.SourceFileFact;
import hai913i.tp1.model.TypeFact;

public class FactsBuilder {
    private final List<TypeFact> types = new ArrayList<>();
    private final List<CallFact> calls = new ArrayList<>();
    private final List<SourceFileFact> sourceFiles = new ArrayList<>();
    private int orphanCalls = 0;

    public void addType(TypeFact type) {
        types.add(type);
    }

    public void addCall(CallFact call) {
        calls.add(call);
    }

    public void addSourceFile(Path path, int lines) {
        sourceFiles.add(new SourceFileFact(path, lines));
    }

    public int orphanCalls() {
        return orphanCalls;
    }

    public ProjectFacts build() {
        // Chaque methode retire ses appels de la table ; ce qui reste n'a pas d'appelant : orphelins.
        Map<String, List<CallFact>> callsByCaller = calls.stream()
                .collect(Collectors.groupingBy(CallFact::callerId));

        List<TypeFact> builtTypes = new ArrayList<>();
        for (TypeFact type : types) {
            List<MethodFact> methods = new ArrayList<>();
            for (MethodFact method : type.methods()) {
                methods.add(method.attachCalls(callsByCaller.getOrDefault(method.id(), List.of())));
                callsByCaller.remove(method.id());
            }
            builtTypes.add(type.replaceMethods(methods));
        }
        builtTypes.sort(Comparator.comparing(TypeFact::qualifiedName));

        orphanCalls = callsByCaller.values().stream().mapToInt(List::size).sum();

        return new ProjectFacts(List.copyOf(builtTypes), List.copyOf(sourceFiles));
    }
}
