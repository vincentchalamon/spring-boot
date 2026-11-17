# Mémo : JUnit et AssertJ pour un développeur PHPUnit

Aide-mémoire, complété au fil des chapitres. Détails et exemples : [étape 1.1](chapitre-01/etape-1.1.md).

Références officielles : https://docs.junit.org/current/user-guide/ et https://assertj.github.io/doc/

---

## Structure d'un test

```java
package com.example.shop.price;                      // même package que la classe testée

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class PriceTest {                                    // pas de public, pas d'extends ; nom en ...Test

    @Test
    void createsValidPrice() {                       // nom = comportement attendu
        Price price = new Price(12.5, "EUR");

        assertThat(price.getAmount()).isEqualTo(12.5);
    }
}
```

## Correspondances

| PHPUnit | JUnit + AssertJ |
|---|---|
| `extends TestCase` | rien |
| `#[Test]` ou `testXxx()` | `@Test` |
| `assertSame($expected, $actual)` | `assertThat(actual).isEqualTo(expected)` |
| `assertEquals` sur objets | `isEqualTo` (appelle `equals`) |
| `assertNotEquals` | `isNotEqualTo` |
| `assertTrue($x)` | `assertThat(x).isTrue()` |
| `assertStringContainsString` | `assertThat(s).contains("...")` |
| `expectException(Foo::class)` | `assertThatThrownBy(() -> ...).isInstanceOf(Foo.class)` |
| `expectExceptionMessage` | `.hasMessage("...")` ou `.hasMessageContaining("...")` |
| `phpunit.xml` | rien : Surefire trouve les classes `*Test` |

## Exceptions

```java
assertThatThrownBy(() -> new Price(-1, "EUR"))      // lambda : exécutée par AssertJ
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Montant négatif");
```

Sans `() ->`, l'exception part avant l'assertion.

## Tests paramétrés

```java
@ParameterizedTest
@ValueSource(strings = {"-1", "-0.01"})              // un argument par cas
void rejectsNegative(String amount) { ... }

@ParameterizedTest
@CsvSource({"EUR, EURO", "USD, US_DOLLAR"})           // plusieurs arguments ; vide = null, '' = chaîne vide
void findsByCode(String code, Currency expected) { ... }

@ParameterizedTest
@EnumSource(Currency.class)                           // une fois par constante
void roundTrips(Currency currency) { ... }

@ParameterizedTest
@MethodSource("invalidTexts")                         // équivalent du dataProvider
void rejectsInvalid(String text) { ... }

static Stream<Arguments> invalidTexts() {
    return Stream.of(Arguments.of("12.50"), Arguments.of("abc EUR"));
}
```

Imports : `org.junit.jupiter.params.ParameterizedTest`, `org.junit.jupiter.params.provider.*Source`.

## Assertions utiles

| Besoin | AssertJ |
|---|---|
| même `hashCode` | `assertThat(a).hasSameHashCodeAs(b)` |
| `toString` | `assertThat(a).hasToString("...")` |
| type de la cause | `.hasCauseInstanceOf(Foo.class)` |
| `BigDecimal` par valeur | `assertThat(a).isEqualByComparingTo("12.50")` |

## Lancer les tests

- tous : `./mvnw test` ;
- un seul : `./mvnw test -Dtest='PriceTest#parsesText'` (comme `--filter`) ;
- VSCode : triangle vert dans la marge, ou vue "Testing" (icône de fiole).
