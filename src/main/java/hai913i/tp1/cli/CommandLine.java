package hai913i.tp1.cli;

import java.nio.file.Path;
import java.util.Set;

public final class CommandLine {

    private static final Set<String> ACTIONS = Set.of("structure", "calls", "metrics", "graph", "query", "all");

    public static final String USAGE = String.join("\n",
            "Usage : java -jar target/hai913i-tp1-analyzer.jar <projet> --action <action> [--x <N>] [--method \"<id>\"]",
            "Actions :",
            "  structure   types, attributs et methodes (A2)",
            "  calls       appels de chaque methode (A3)",
            "  metrics     les 13 metriques (B2)              necessite --x",
            "  graph       graphe d'appel (B3)",
            "  query       appelees et appelantes (B3)        necessite --method",
            "  all         tout ce qui precede                necessite --x",
            "Exemples :",
            "  ... ../validation --action all --x 5",
            "  ... ../validation --action query --method \"Catalog#add(Item)\"");

    private CommandLine() {
    }

    public static Options parse(String[] args) throws UsageException {
        if (args.length == 0 || args[0].startsWith("--")) {
            throw new UsageException("chemin du projet manquant");
        }
        Path project = Path.of(args[0]);
        String action = null;
        Integer x = null;
        String method = null;

        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--action" -> action = next(args, ++i, "--action");
                case "--x" -> x = parseX(next(args, ++i, "--x"));
                case "--method" -> method = next(args, ++i, "--method");
                default -> throw new UsageException("option inconnue : " + args[i]);
            }
        }

        if (action == null) {
            throw new UsageException("option --action manquante");
        }
        if (!ACTIONS.contains(action)) {
            throw new UsageException("action inconnue '" + action + "'");
        }

        boolean needsX = action.equals("metrics") || action.equals("all");
        if (needsX && x == null) {
            throw new UsageException("l'action " + action + " necessite --x <N>");
        }
        if (!needsX && x != null) {
            throw new UsageException("--x ne s'utilise qu'avec les actions metrics ou all");
        }

        boolean needsMethod = action.equals("query");
        if (needsMethod && method == null) {
            throw new UsageException("l'action query necessite --method \"<id>\"");
        }
        if (!needsMethod && method != null) {
            throw new UsageException("--method ne s'utilise qu'avec l'action query");
        }

        return new Options(project, action, x, method);
    }

    private static String next(String[] args, int i, String option) throws UsageException {
        if (i >= args.length || args[i].startsWith("--")) {
            throw new UsageException("valeur manquante apres " + option);
        }
        return args[i];
    }

    private static int parseX(String text) throws UsageException {
        int x;
        try {
            x = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new UsageException("X doit etre un entier positif ou nul (recu : '" + text + "')");
        }
        if (x < 0) {
            throw new UsageException("X doit etre un entier positif ou nul (recu : " + x + ")");
        }
        return x;
    }
}
