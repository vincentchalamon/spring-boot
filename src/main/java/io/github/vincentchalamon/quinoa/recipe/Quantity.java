package io.github.vincentchalamon.quinoa.recipe;

import java.math.BigDecimal;
import java.util.Objects;

record Quantity(BigDecimal value, Unit unit) {
    Quantity {
        Objects.requireNonNull(value, "Value cannot be null.");
        Objects.requireNonNull(unit, "Unit cannot be null.");

        if (value.signum() <= 0) {
            throw new IllegalArgumentException("Quantity must be a positive number.");
        }

        value = value.stripTrailingZeros();
    }

    static Quantity parse(String input) {
        String[] parts = input.split(" ");
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Argument is not correctly formatted, expecting: \"<quantity> <unit>\".");
        }

        return new Quantity(new BigDecimal(parts[0]), Unit.fromSymbol(parts[1]));
    }

    @Override
    public String toString() {
        return String.format("%s %s", value.toPlainString(), unit.symbol());
    }
}
