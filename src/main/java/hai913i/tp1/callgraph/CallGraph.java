package hai913i.tp1.callgraph;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import hai913i.tp1.model.CallFact;
import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

public class CallGraph {
    private final ProjectFacts facts;

    private final Set<String> nodes = new HashSet<>();
    private final List<CallFact> internalCalls = new ArrayList<>();
    private int externalCalls = 0;
    private int unresolvedCalls = 0;
    private record Pair(String from, String to) { }
    private final List<Arc> arcs = new ArrayList<>();


    public CallGraph(ProjectFacts facts) {
        this.facts = facts;

        for (TypeFact type : facts.types()) {
            for (MethodFact method : type.methods()) {
                nodes.add(method.id());
            }
        }

        for (TypeFact type : facts.types()) {
            for (MethodFact method : type.methods()) {
                for (CallFact call : method.calls()) {
                    if (!call.resolved()) {
                        unresolvedCalls++;
                    } else if (!call.internal()) {
                        externalCalls++;
                    } else if (nodes.contains(call.targetId().get())) {
                        internalCalls.add(call);
                    }
                }
            }
        }

        Map<Pair, Long> counts = internalCalls.stream()
        .collect(Collectors.groupingBy(c -> new Pair(c.callerId(), c.targetId().get()), Collectors.counting()));

        for (Map.Entry<Pair, Long> e : counts.entrySet()) {
            arcs.add(new Arc(e.getKey().from(), e.getKey().to(), Math.toIntExact(e.getValue())));
        }
        arcs.sort(Comparator.comparing(Arc::from).thenComparing(Arc::to));

    }

    public int nodeCount() {
        return nodes.size();
    }

    public int internalSites() {
        return internalCalls.size();
    }

    public int externalCalls() {
        return externalCalls;
    }

    public int unresolvedCalls() {
        return unresolvedCalls;
    }

    public List<Arc> arcs() { return List.copyOf(arcs); }
    public int arcCount()   { return arcs.size(); }

    public List<Arc> callers(String id) {
    List<Arc> result = new ArrayList<>();
    for (Arc arc : arcs) {
        if (arc.to().equals(id)) {
            result.add(arc);
        }
    }
    return result;
    }

    public List<Arc> callees(String id) {
        List<Arc> result = new ArrayList<>();
        for (Arc arc : arcs) {
            if (arc.from().equals(id)) {
                result.add(arc);
            }
        }
        return result;
    }

    public boolean isNode(String id) {
        return nodes.contains(id);
    }


}
