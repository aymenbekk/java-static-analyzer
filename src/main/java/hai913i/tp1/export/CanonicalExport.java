package hai913i.tp1.export;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import hai913i.tp1.callgraph.Arc;
import hai913i.tp1.callgraph.CallGraph;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

// Ecrit les faits au format canonique du TP 2 : <prefixe>-classes.csv, -methods.csv, -calls.csv.
// Separateur ';', une ligne d'en-tete, UTF-8, fins de ligne CRLF, lignes triees.
public final class CanonicalExport {

    private static final String EOL = "\r\n";

    private CanonicalExport() {
    }

    // facts-<nom du dossier du projet>, comme facts-validation
    public static String prefixFor(Path project) {
        return "facts-" + project.toAbsolutePath().normalize().getFileName();
    }

    public static void write(ProjectFacts facts, CallGraph graph, Path outDir, String prefix) throws IOException {
        Files.createDirectories(outDir);

        List<TypeFact> types = new ArrayList<>(facts.types());
        types.sort(Comparator.comparing(TypeFact::qualifiedName));

        StringBuilder classes = new StringBuilder("class;simpleName;package" + EOL);
        Map<String, String> owners = new TreeMap<>();
        for (TypeFact type : types) {
            String name = type.qualifiedName();
            classes.append(name).append(';')
                    .append(name.substring(name.lastIndexOf('.') + 1)).append(';')
                    .append(type.packageName()).append(EOL);
            for (MethodFact method : type.methods()) {
                owners.put(canonicalId(method.id()), name);
            }
        }

        StringBuilder methods = new StringBuilder("method;owner" + EOL);
        owners.forEach((id, owner) -> methods.append(id).append(';').append(owner).append(EOL));

        StringBuilder calls = new StringBuilder("caller;callerClass;callee;calleeClass;sites" + EOL);
        for (Arc arc : graph.arcs()) {
            String from = canonicalId(arc.from());
            String to = canonicalId(arc.to());
            calls.append(from).append(';').append(owners.get(from)).append(';')
                    .append(to).append(';').append(owners.get(to)).append(';')
                    .append(arc.weight()).append(EOL);
        }

        Files.writeString(outDir.resolve(prefix + "-classes.csv"), classes, StandardCharsets.UTF_8);
        Files.writeString(outDir.resolve(prefix + "-methods.csv"), methods, StandardCharsets.UTF_8);
        Files.writeString(outDir.resolve(prefix + "-calls.csv"), calls, StandardCharsets.UTF_8);
    }

    // Le TP 1 separe les parametres par ", ", le format canonique par ","
    private static String canonicalId(String tp1Id) {
        return tp1Id.replace(", ", ",");
    }
}
