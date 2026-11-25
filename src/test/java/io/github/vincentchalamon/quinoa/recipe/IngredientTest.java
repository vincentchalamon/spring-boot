package io.github.vincentchalamon.quinoa.recipe;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class IngredientTest {
    @SuppressWarnings("NullAway")
    @Test
    void rejectsNullName() {
        assertThatThrownBy(() -> new Ingredient(null, null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Ingredient name cannot be null.");
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "' '"})
    void rejectsBlankName(String name) {
        assertThatThrownBy(() -> new Ingredient(name, null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Ingredient name cannot be blank.");
    }

    @ParameterizedTest
    @CsvSource(value = {"''", "' '"})
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

    @CsvSource(
            value = {
                "farine, null, null, farine",
                // Test that name is stripped
                "'farine ', null, null, farine",
                "farine, null, 250 g, farine 250 g",
                "farine, bien mûres, null, farine (bien mûres)",
                // Test that precision is stripped
                "farine, 'bien mûres ', null, farine (bien mûres)",
                "farine, bien mûres, 250 g, farine 250 g (bien mûres)",
                // Test that name and precision are stripped
                "'farine ', 'bien mûres ', 250 g, farine 250 g (bien mûres)"
            },
            nullValues = "null")
    @ParameterizedTest
    void toStringReturnsExpectedStrippedString(String name, String precision, String quantityString, String expected) {
        Quantity quantity = quantityString != null ? Quantity.parse(quantityString) : null;
        Ingredient ingredient = new Ingredient(name, precision, quantity);

        assertThat(ingredient.toString()).isEqualTo(expected);
    }

    @MethodSource("duplicateNamesCases")
    @ParameterizedTest
    void duplicateNamesReturnsExpectedSet(List<Ingredient> ingredients, Set<String> expected) {
        assertThat(Ingredient.duplicateNames(ingredients)).isEqualTo(expected);
    }

    static Stream<Arguments> duplicateNamesCases() {
        return Stream.of(
                // No duplicate
                Arguments.of(
                        List.of(
                                new Ingredient("farine", null, null),
                                new Ingredient("Sucre", null, null),
                                new Ingredient("beurre", null, null)),
                        Set.of()),

                // Same case duplicates
                Arguments.of(
                        List.of(
                                new Ingredient("sucre", null, null),
                                new Ingredient("Farine", null, null),
                                new Ingredient("beurre", null, null),
                                new Ingredient("sucre", null, null),
                                new Ingredient("Farine", null, null)),
                        Set.of("farine", "sucre")),

                // Different case duplicates
                Arguments.of(
                        List.of(
                                new Ingredient("farine", null, null),
                                new Ingredient("sucre", null, null),
                                new Ingredient("Farine", null, null),
                                new Ingredient("beurre", null, null),
                                new Ingredient("Sucre", null, null)),
                        Set.of("farine", "sucre")),

                // Multiple duplicates
                Arguments.of(
                        List.of(
                                new Ingredient("Farine", null, null),
                                new Ingredient("Sucre", null, null),
                                new Ingredient("Farine", null, null),
                                new Ingredient("beurre", null, null),
                                new Ingredient("Sucre", null, null),
                                new Ingredient("Farine", null, null)),
                        Set.of("farine", "sucre")),

                // Empty list
                Arguments.of(List.of(), Set.of()));
    }
}
