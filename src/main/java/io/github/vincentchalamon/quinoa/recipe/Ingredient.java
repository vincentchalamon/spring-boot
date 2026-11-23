package io.github.vincentchalamon.quinoa.recipe;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

record Ingredient(String name, @Nullable String precision, @Nullable Quantity quantity) {
    Ingredient {
        Objects.requireNonNull(name, "Ingredient name cannot be null.");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Ingredient name cannot be blank.");
        }
        if (precision != null && precision.isBlank()) {
            throw new IllegalArgumentException("Ingredient precision cannot be blank.");
        }
        name = name.strip();
        precision = precision != null ? precision.strip() : null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (quantity != null) {
            sb.append(" ").append(quantity);
        }
        if (precision != null) {
            sb.append(" (").append(precision).append(")");
        }
        return sb.toString();
    }

    static Set<String> duplicateNames(List<Ingredient> ingredients) {
        return ingredients.stream()
                .map(ingredient -> ingredient.name().toLowerCase(Locale.ROOT))
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .filter(entry -> entry.getValue() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }
}
