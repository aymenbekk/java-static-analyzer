package hai913i.tp1.model;

import java.util.List;

public record MethodFact(String id, String name, List<String> parameterTypes,
           boolean constructor, int bodyLoc, List<CallFact> calls) {

    public MethodFact attachCalls(List<CallFact> newCalls) {
        return new MethodFact(id, name, parameterTypes, constructor, bodyLoc, List.copyOf(newCalls));
    }
}
