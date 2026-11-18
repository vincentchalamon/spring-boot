package io.github.vincentchalamon.quinoa.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IngredientTest {
    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> new Ingredient(null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Ingredient name cannot be null.");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "' '"
    })
    void rejectsBlankName(String name) {
        assertThatThrownBy(() -> new Ingredient(name, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient name cannot be blank.");
    }

    @ParameterizedTest
    @CsvSource(value = {
            "''",
            "' '"
    })
    void rejectsBlankPrecision(String precision) {
        assertThatThrownBy(() -> new Ingredient("farine", precision, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient precision cannot be blank.");
    }

    @Test
    void precisionAndQuantityCanBeNull() {
        Ingredient ingredient = new Ingredient("farine", null, null);

        assertThat(ingredient.name()).isEqualTo("farine");
        assertThat(ingredient.precision()).isNull();
        assertThat(ingredient.quantity()).isNull();
    }

    @CsvSource(value = {
            "farine, null, null, farine",
            "'farine ', null, null, farine", // Test that name is stripped
            "farine, null, 250 g, farine 250 g",
            "farine, bien mûres, null, farine (bien mûres)",
            "farine, 'bien mûres ', null, farine (bien mûres)", // Test that precision is stripped
            "farine, bien mûres, 250 g, farine 250 g (bien mûres)",
            "'farine ', 'bien mûres ', 250 g, farine 250 g (bien mûres)" // Test that name and precision are stripped
    }, nullValues = "null")
    @ParameterizedTest
    void toStringReturnsExpectedStrippedString(String name, String precision, String quantityString, String expected) {
        Quantity quantity = quantityString != null ? Quantity.parse(quantityString) : null;
        Ingredient ingredient = new Ingredient(name, precision, quantity);

        assertThat(ingredient.toString()).isEqualTo(expected);
    }
}
