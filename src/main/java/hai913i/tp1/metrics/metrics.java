package hai913i.tp1.metrics;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.ToIntFunction;

import hai913i.tp1.model.MethodFact;
import hai913i.tp1.model.ProjectFacts;
import hai913i.tp1.model.TypeFact;

public class metrics {
    
    public ProjectFacts facts;

    public record MaxParameters(int count, List<MethodFact> methods) { }

    public record Ranked<T>(T item, int value) { }


    public metrics(ProjectFacts facts) {
       this.facts = facts;
    }

    public int getNumberOftypes() {
        return facts.types().size();
    }

    public int getSumOfLines() {
        return facts.sourceFiles().stream().mapToInt(f -> f.lines()).sum();
    }

    public int numberOfMethods() {
        return facts.types().stream().mapToInt(t -> t.methods().size()).sum();
    }

    public int numberOfDistinctPackages() {
        return (int) facts.types().stream().map(t -> t.packageName()).distinct().count();
    }

    public BigDecimal avgNumberOfMethodsPerType() {
        return roundTwoDecimals((double) numberOfMethods() / getNumberOftypes());
    }

    public BigDecimal avgNumberOfLinesPerMethod(){
        int sumOfLines = facts.types().stream().mapToInt(t -> t.methods().stream().mapToInt(m -> m.bodyLoc()).sum()).sum();
        long methodsWithBody = facts.types().stream().mapToLong(t -> t.methods().stream().filter(m -> m.bodyLoc() > 0).count()).sum();
        return roundTwoDecimals((double) sumOfLines / methodsWithBody);
    }

    public BigDecimal avgNumberOfFieldsPerType() {
        return roundTwoDecimals((double) facts.types().stream().mapToInt(t -> t.fields().size()).sum() / getNumberOftypes());
    }

    public MaxParameters getMaxParameters() {
        int maxCount = facts.types().stream()
                .flatMap(t -> t.methods().stream())
                .mapToInt(m -> m.parameterTypes().size())
                .max()
                .orElse(0);

        List<MethodFact> methodsWithMaxParameters = facts.types().stream()
                .flatMap(t -> t.methods().stream())
                .filter(m -> m.parameterTypes().size() == maxCount)
                .toList();

        return new MaxParameters(maxCount, methodsWithMaxParameters);
    }

    public List<Ranked<TypeFact>> topTenPercentTypesByMethodCount() {
        return topTenPercent(facts.types(), t -> t.methods().size());
    }

    public List<Ranked<TypeFact>> topTenPercentTypesByFieldCount() {
        return topTenPercent(facts.types(), t -> t.fields().size());
    }

    public List<Ranked<MethodFact>> topTenPercentMethodsByLineCountForType(TypeFact type) {
        return topTenPercent(type.methods(), m -> m.bodyLoc());
    }
    
    public List<TypeFact> typesInBothTopTenPercent() {
        List<TypeFact> topByFields = new ArrayList<>();
        for (Ranked<TypeFact> r : topTenPercentTypesByFieldCount()) {
            topByFields.add(r.item());
        }

        List<TypeFact> result = new ArrayList<>();
        for (Ranked<TypeFact> r : topTenPercentTypesByMethodCount()) {
            if (topByFields.contains(r.item())) {
                result.add(r.item());
            }
        }
        return result;
    }

    public List<TypeFact> TypesWithMoreThanXMethods(int x) {
        List<TypeFact> result = new ArrayList<>();
        for (TypeFact t : facts.types()) {
            if (t.methods().size() > x) {
                result.add(t);
            }
        }
        return result;
    }

    private static BigDecimal roundTwoDecimals(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    //generic method to get the top 10% of a list based on a value function
    private static <T> List<Ranked<T>> topTenPercent(List<T> items, ToIntFunction<T> valueFunction) {
        int k = (items.size() + 9) / 10; 

        List<T> sorted = new ArrayList<>();
        for (T c : items) {
            if (valueFunction.applyAsInt(c) > 0) {
                sorted.add(c);
            }
        }
        sorted.sort(Comparator.comparingInt(valueFunction).reversed());

        if (sorted.isEmpty()) {
            return List.of();
        }

        int threshold = valueFunction.applyAsInt(sorted.get(Math.min(k, sorted.size()) - 1));  // step 5

        List<Ranked<T>> result = new ArrayList<>();
        for (T c : sorted) {
            if (valueFunction.applyAsInt(c) >= threshold) {
                result.add(new Ranked<>(c, valueFunction.applyAsInt(c)));
            }
        }
        return result;
    }

}
