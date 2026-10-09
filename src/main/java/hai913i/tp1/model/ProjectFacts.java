package hai913i.tp1.model;

import java.nio.file.Path;
import java.util.List;

public record ProjectFacts(List<TypeFact> types, List<Path> sourceFiles) {
    
}
