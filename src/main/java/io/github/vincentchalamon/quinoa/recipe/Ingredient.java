package io.github.vincentchalamon.quinoa.recipe;

import java.util.Objects;

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
}
