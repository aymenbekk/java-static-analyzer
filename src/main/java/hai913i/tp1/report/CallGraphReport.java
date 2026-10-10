package hai913i.tp1.report;
import java.util.List;

import hai913i.tp1.callgraph.Arc;
import hai913i.tp1.callgraph.CallGraph;

public class CallGraphReport {

    public static String generateReport(CallGraph callGraph) {



        StringBuilder report = new StringBuilder();
        

        report.append("Call Graph Report\n");
        report.append("=================\n");
        for (Arc arc : callGraph.arcs()) {
            report.append(arc.from()).append(" -> ").append(arc.to())
                    .append(" (").append(arc.weight()).append(")\n");
        }
        report.append("\n");
        report.append("Number of nodes: ").append(callGraph.nodeCount()).append("\n");
        report.append("Number of arcs: ").append(callGraph.arcCount()).append("\n");
        report.append("internal sites: ").append(callGraph.internalSites()).append("\n");
        report.append("Number of external calls: ").append(callGraph.externalCalls()).append("\n");
        report.append("Number of unresolved calls: ").append(callGraph.unresolvedCalls()).append("\n");
        return report.toString();
    }

    public static String generateQueryReport(CallGraph graph, String query){
        StringBuilder report = new StringBuilder();
        List<String> matches = graph.findNodes(query);
        if (matches.isEmpty()) {
            report.append("Methode inconnue : ").append(query).append("\n");
            return report.toString();
        }
        for (String id : matches) {
            appendMethodQuery(report, graph, id);
        }
        return report.toString();
    }

    private static void appendMethodQuery(StringBuilder report, CallGraph graph, String id) {
        report.append("Methode : ").append(id).append("\n");
        report.append("  Appelees :\n");
        List<Arc> callees = graph.callees(id);
        if (callees.isEmpty()) {
            report.append("    (aucune)\n");
        }
        for (Arc arc : callees) {
            report.append("    ").append(arc.to()).append(" (").append(arc.weight()).append(")\n");
        }

        report.append("  Appelantes :\n");
        List<Arc> callers = graph.callers(id);
        if (callers.isEmpty()) {
            report.append("    (aucune)\n");
        }
        for (Arc arc : callers) {
            report.append("    ").append(arc.from()).append(" (").append(arc.weight()).append(")\n");
        }
    }
}
