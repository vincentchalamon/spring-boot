package io.github.vincentchalamon.quinoa.recipe;

import java.util.Objects;

public final class Quantity {
    private final double value;
    private final String unit;

    public Quantity(double value, String unit) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit must not be empty.");
        }

        this.value = value;
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    public static Quantity parse(String input) {
        String[] parts = input.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Argument is not correctly formatted, expecting: \"<quantity> <unit>\".");
        }

        double value;
        try {
            value = Double.parseDouble(parts[0]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Quantity is not a valid number, expecting something like \"0.5\", got \"%s\"."
                            .formatted(parts[0]),
                    e);
        }

        String unit = parts[1];

        return new Quantity(value, unit);
    }

    @Override
    public String toString() {
        return String.format("%s %s", value, unit);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Quantity quantity)) {
            return false;
        }

        return Double.compare(quantity.value, value) == 0 && unit.equals(quantity.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
}
