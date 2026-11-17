# Étape 1.2 - Enums et records : type, régime, unité, quantité

[Chapitre 1](README.md) > Étape 1.2

## En bref

- **Objectif** : modéliser des valeurs immuables et sûres avec les outils modernes de Java : `enum`, `record`, `BigDecimal`.
- **Apport au projet** : les enums `RecipeType`, `Diet` et `Unit` ; `Quantity` réécrite en record (`BigDecimal` + `Unit`) ; le package déclaré non nul par défaut (JSpecify).
- **Durée indicative** : une journée, en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch01/02-enums-records`.

Avancement :

- [ ] A1. Créer la branche
- [ ] A2. Écrire `RecipeType` et `Diet`
- [ ] A3. Écrire `Unit` avec son symbole
- [ ] A4. Tester `Unit`
- [ ] B1. Ajouter JSpecify et déclarer le package non nul
- [ ] B2. Réécrire `Quantity` en record
- [ ] B3. Adapter les tests (avec une erreur volontaire)
- [ ] B4. Inverser le test des `double`
- [ ] B5. Comparer avec la classe de l'étape 1.1
- [ ] B6. Committer et ouvrir la PR
- [ ] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

Mémos : [Java pour PHP](../memo-java-pour-php.md), [Maven](../memo-maven.md), [tests](../memo-tests.md).

---

## Cycle A - Les enums

### Théorie

#### Ce que vous savez déjà, ce qui change

Vous connaissez les enums de PHP 8.1. Ceux de Java existent depuis 2004 et vont plus loin.

Correspondances :

- `enum Suit { case Hearts; }` : `enum Suit { HEARTS }`. Les constantes sont en majuscules, séparées par des virgules ;
- backed enum `enum Suit: string { case Hearts = 'H'; }` : un **champ** déclaré dans l'enum, rempli par un constructeur (exemple 2) ;
- `Suit::cases()` : `Suit.values()` ;
- `Suit::from('H')` : rien d'automatique pour un champ. On écrit une méthode statique (exemple 2) ;
- `Suit::Hearts->name` : `Suit.HEARTS.name()`.

Ce qui change :

- un enum Java est une **classe** : il peut avoir des champs, un constructeur et des méthodes ;
- `valueOf("HEARTS")` existe d'office, mais il cherche par **nom de constante**, pas par valeur de champ ;
- `==` est le bon moyen de comparer deux constantes : chacune existe en un seul exemplaire.

#### Exemple 1 : un enum simple, dans jshell

```console
jshell> enum Size { SMALL, MEDIUM, LARGE }
|  created enum Size

jshell> Size.valueOf("MEDIUM")
$2 ==> MEDIUM

jshell> Size.valueOf("medium")
|  Exception java.lang.IllegalArgumentException: No enum constant REPL.$JShell$2.Size.medium

jshell> Size.values()
$4 ==> Size[3] { SMALL, MEDIUM, LARGE }

jshell> Size.LARGE.ordinal()
$5 ==> 2

jshell> Size.SMALL == Size.valueOf("SMALL")
$6 ==> true
```

À retenir :

- `valueOf` respecte la casse, et lance `IllegalArgumentException` si le nom n'existe pas ;
- `ordinal()` donne la position (à partir de 0). Ne vous en servez jamais comme donnée : elle change si l'on insère une constante ;
- `==` compare deux constantes sans risque.

Une **expression** `switch` (qui renvoie une valeur) sur un enum est vérifiée par le compilateur :

```console
jshell> String label(Size s) { return switch (s) { case SMALL -> "S"; case MEDIUM -> "M"; case LARGE -> "L"; }; }
|  created method label(Size)

jshell> label(Size.LARGE)
$8 ==> "L"

jshell> String bad(Size s) { return switch (s) { case SMALL -> "S"; case MEDIUM -> "M"; }; }
|  Error:
|  the switch expression does not cover all possible input values
|  String bad(Size s) { return switch (s) { case SMALL -> "S"; case MEDIUM -> "M"; }; }
|                              ^---------------------------------------------------^
```

Oublier une constante est une erreur de **compilation**. En PHP, un `match` incomplet n'échoue qu'à l'exécution, avec `UnhandledMatchError`.

Seule l'expression est vérifiée. Une **instruction** `switch` (qui ne renvoie rien) sur un enum peut omettre des constantes sans erreur.

#### Exemple 2 : un enum avec un champ

Projet `shop` de l'étape 1.1. Fichier `src/main/java/com/example/shop/price/Currency.java` :

```java
package com.example.shop.price;

public enum Currency {
    EURO("EUR"),
    US_DOLLAR("USD"),
    SWISS_FRANC("CHF");

    private final String code;

    Currency(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static Currency fromCode(String code) {
        for (Currency currency : values()) {
            if (currency.code.equals(code)) {
                return currency;
            }
        }
        throw new IllegalArgumentException("Devise inconnue : " + code);
    }
}
```

Lecture ligne par ligne :

1. `EURO("EUR"),`

   Une constante, construite avec l'argument `"EUR"`. Les constantes sont séparées par des virgules.

   En PHP : `case Euro = 'EUR';`

2. `SWISS_FRANC("CHF");`

   La dernière constante se termine par un **point-virgule** : il sépare les constantes du reste de l'enum (champs, méthodes).

3. `private final String code;`

   Le champ de chaque constante. `final` : il ne change jamais.

4. `Currency(String code) {`

   Le constructeur. Pas de `public` : il est privé d'office. Personne ne peut créer de nouvelle devise, seules les constantes existent.

5. `public String code() {`

   L'accesseur. Nommé `code()` et non `getCode()` : c'est le style des records (cycle B), adopté aussi pour les enums.

6. `public static Currency fromCode(String code) {`

   L'équivalent de `Currency::from('EUR')` en PHP, écrit à la main.

7. `for (Currency currency : values()) {`

   Parcourt toutes les constantes, dans l'ordre de déclaration.

8. `if (currency.code.equals(code)) {`

   Compare le **champ**, avec `equals` : c'est une `String`.

9. `throw new IllegalArgumentException(...)`

   Comme `valueOf`, un code inconnu est refusé.

#### Exemple 3 : tester un enum

Fichier `src/test/java/com/example/shop/price/CurrencyTest.java` :

```java
package com.example.shop.price;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

class CurrencyTest {

    @ParameterizedTest
    @CsvSource({
        "EUR, EURO",
        "USD, US_DOLLAR",
        "CHF, SWISS_FRANC"
    })
    void findsCurrencyByCode(String code, Currency expected) {
        assertThat(Currency.fromCode(code)).isEqualTo(expected);
    }

    @ParameterizedTest
    @EnumSource(Currency.class)
    void codeRoundTrips(Currency currency) {
        assertThat(Currency.fromCode(currency.code())).isEqualTo(currency);
    }

    @Test
    void rejectsUnknownCode() {
        assertThatThrownBy(() -> Currency.fromCode("XYZ"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("XYZ");
    }
}
```

Ce qui est nouveau :

1. `void findsCurrencyByCode(String code, Currency expected)`

   Dans `@CsvSource`, `EURO` est du texte. JUnit le convertit en constante avec `valueOf`, parce que le paramètre est de type `Currency`.

2. `@EnumSource(Currency.class)`

   Lance le test une fois **par constante**. Si vous ajoutez une devise, elle est testée sans toucher au test.

3. `codeRoundTrips`

   Un test "aller-retour" : constante, puis code, puis constante. Il garantit que chaque code est unique et retrouvable.

Exécution :

```console
$ ./mvnw -ntp test
...
[INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.040 s -- in com.example.shop.price.CurrencyTest
```

7 tests : 3 cas de `@CsvSource`, 3 constantes pour `@EnumSource`, 1 test simple.

### Pièges

- **`valueOf` au lieu de `fromCode`** : `Currency.valueOf("EUR")` cherche une constante nommée `EUR` et échoue. `valueOf` travaille sur les noms, votre méthode sur les champs.
- **Point-virgule oublié** après la dernière constante : erreur de compilation dès qu'il y a un champ.
- **`ordinal()` comme donnée** : si l'on insère une constante, toutes les positions suivantes changent. Au chapitre 4, on stockera le **nom** en base, jamais la position.

### Vérifiez votre compréhension

1. Que renvoie `Currency.valueOf("EUR")` ?
2. Pourquoi le constructeur d'un enum n'a-t-il pas besoin d'être `private` ?
3. Que se passe-t-il pour `codeRoundTrips` si l'on ajoute une devise ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git switch main
git pull
git switch -c ch01/02-enums-records
```

**Résultat attendu** : `git status` affiche `Sur la branche ch01/02-enums-records`.

#### A2. Écrire `RecipeType` et `Diet`

Durée : 15 min. Modèle : exemple 1.

Deux enums simples, sans champ, dans le package `recipe`. Le cahier des charges donne "dessert, plat, accompagnement, etc." et "vegan, végétarien, etc.". Proposition à valider ou ajuster :

- `RecipeType` : `STARTER`, `MAIN_COURSE`, `SIDE_DISH`, `DESSERT`, `BREAKFAST`, `SNACK`, `DRINK`, `SAUCE` ;
- `Diet` : `VEGAN`, `VEGETARIAN`, `GLUTEN_FREE`, `LACTOSE_FREE`.

Les noms des constantes deviendront les codes de l'API (chapitre 5) : en anglais, en majuscules, stables.

Pas de test : un enum sans comportement n'a rien à tester.

**Résultat attendu** : `./mvnw -ntp compile` affiche `BUILD SUCCESS`.

#### A3. Écrire `Unit` avec son symbole

Durée : 20 min. Modèle : exemple 2.

Les unités du cahier des charges, chacune avec son **symbole**, celui qu'on écrit dans `"250 g"` :

| Constante | Symbole | Sens |
|---|---|---|
| `GRAM` | `g` | gramme |
| `MILLILITER` | `ml` | millilitre |
| `TEASPOON` | `càc` | cuillère à café |
| `TABLESPOON` | `càs` | cuillère à soupe |
| `PIECE` | `pièce` | pièce |
| `PINCH` | `pincée` | pincée |
| `CLOVE` | `gousse` | gousse |
| `SPRIG` | `brin` | brin |
| `BRANCH` | `branche` | branche |
| `LEAF` | `feuille` | feuille |
| `SLICE` | `tranche` | tranche |
| `BUNCH` | `botte` | botte |
| `SACHET` | `sachet` | sachet |
| `JAR` | `pot` | pot |

Ce qui change par rapport au modèle : `symbol` au lieu de `code`, `fromSymbol` au lieu de `fromCode`.

Les symboles sont sans espace : `Quantity.parse` découpe sur l'espace (cycle B).

**Résultat attendu** : `./mvnw -ntp compile` affiche `BUILD SUCCESS`.

#### A4. Tester `Unit`

Durée : 15 min. Modèle : exemple 3.

`UnitTest`, trois tests :

1. quelques symboles retrouvent leur unité (`@CsvSource`) ;
2. chaque unité fait l'aller-retour (`@EnumSource`) ;
3. un symbole inconnu (`"kg"`) est refusé.

```bash
./mvnw -ntp test
```

**Résultat attendu** : `BUILD SUCCESS`. `UnitTest` compte au moins 14 cas pour `@EnumSource`, un par unité.

Committez :

```bash
git add src
git commit -m "feat: add recipe type, diet and unit enums"
```

> **Point d'arrêt** : les enums sont prêts. Le cycle B réécrit `Quantity`.

---

## Cycle B - Records, `BigDecimal` et nullité

### Théorie

#### Les records

Un **record** est une classe de valeur dont Java écrit le code répétitif à votre place.

```console
jshell> record Point(int x, int y) {}
|  created record Point

jshell> var p = new Point(1, 2)
p ==> Point[x=1, y=2]

jshell> p.x()
$3 ==> 1

jshell> p.equals(new Point(1, 2))
$4 ==> true

jshell> p.hashCode() == new Point(1, 2).hashCode()
$5 ==> true

jshell> p.x = 5
|  Error:
|  x has private access in Point
|  p.x = 5
|  ^-^
```

La ligne `record Point(int x, int y) {}` génère :

- deux champs `private final` ;
- un constructeur avec les deux paramètres ;
- deux accesseurs, `x()` et `y()`, **sans** préfixe `get` ;
- `equals`, `hashCode` et `toString`, à partir de tous les champs.

C'est tout ce que vous avez écrit à la main pour `Quantity` à l'étape 1.0.

En PHP, l'équivalent le plus proche : `final readonly class Point { public function __construct(public int $x, public int $y) {} }`. Mais PHP n'écrit ni `equals` ni `hashCode` (PHP n'en a pas besoin), et l'objet n'est pas `final` d'office.

Ce qu'un record ne peut pas faire :

- hériter d'une autre classe (il est `final`, et hérite déjà de `Record`) ;
- avoir d'autres champs d'instance que ses composants.

Il peut avoir des méthodes, des méthodes statiques, et implémenter des interfaces.

#### Le constructeur compact

Pour valider ou normaliser, on écrit un **constructeur compact** : sans parenthèses ni paramètres.

```java
public record Point(int x, int y) {

    public Point {
        if (x < 0) {
            throw new IllegalArgumentException("x négatif : " + x);
        }
    }
}
```

- Il s'exécute **avant** l'affectation des champs, que Java ajoute à la fin, en silence.
- Les paramètres (`x`, `y`) y sont disponibles, et on peut les **réaffecter** : `x = Math.abs(x);` modifie la valeur qui sera stockée.
- On n'écrit jamais `this.x = x;` : c'est Java qui le fait.

#### `BigDecimal` : des décimaux exacts

Un `double` est binaire : `0.1` n'y est qu'approché. `BigDecimal` stocke des décimaux exacts. C'est l'équivalent de l'extension `bcmath` ou de `brick/math` en PHP.

Session jshell (`java.math` est importé d'office) :

```console
jshell> new BigDecimal(0.1)
$1 ==> 0.1000000000000000055511151231257827021181583404541015625

jshell> new BigDecimal("0.1")
$2 ==> 0.1

jshell> new BigDecimal("0.1").add(new BigDecimal("0.2"))
$3 ==> 0.3

jshell> new BigDecimal("1.0").equals(new BigDecimal("1.00"))
$4 ==> false

jshell> new BigDecimal("1.0").compareTo(new BigDecimal("1.00"))
$5 ==> 0

jshell> new BigDecimal("1.50").stripTrailingZeros()
$6 ==> 1.5

jshell> new BigDecimal("100").stripTrailingZeros()
$7 ==> 1E+2

jshell> new BigDecimal("100").stripTrailingZeros().toPlainString()
$8 ==> "100"

jshell> new BigDecimal("-3").signum()
$9 ==> -1
```

Lecture :

1. `new BigDecimal(0.1)` : construit **depuis un `double`**, donc depuis la valeur déjà approchée. Toujours construire depuis une `String`.
2. `add` : les opérateurs `+`, `-`, `*` n'existent pas pour un objet. On écrit `add`, `subtract`, `multiply`.
3. `equals` : compare la valeur **et l'échelle** (le nombre de décimales). `1.0` et `1.00` sont différents. Vous l'avez vu à l'étape 0.1.
4. `compareTo` : compare la valeur seule.
5. `stripTrailingZeros` : retire les zéros inutiles. Deux valeurs égales deviennent alors identiques, échelle comprise.
6. `1E+2` : piège de `stripTrailingZeros`. `100` devient "1 fois 10 puissance 2". La valeur est juste, mais l'affichage surprend. `toPlainString()` l'écrit normalement.
7. `signum()` : le signe, `-1`, `0` ou `1`. Le moyen simple de tester "positif".

Autre différence avec `double` : `BigDecimal` n'a ni `NaN` ni infini. `new BigDecimal("NaN")` lance une `NumberFormatException`.

#### Nullité : JSpecify

En Java, toute variable objet peut être `null`, sans que le type le dise. C'est la cause de la `NullPointerException`, l'erreur la plus fréquente en Java.

**JSpecify** est un ensemble d'annotations standard pour le dire explicitement. Spring 7 l'utilise partout.

- `@NullMarked` sur un package : dans ce package, **rien n'est nul par défaut**. Comme si chaque type était non nullable en PHP (`string` et non `?string`).
- `@Nullable` sur un type : celui-ci peut être `null`. L'équivalent du `?string` de PHP.

On pose `@NullMarked` sur tout le package, dans un fichier spécial `package-info.java` :

```java
@NullMarked
package com.example.shop.price;

import org.jspecify.annotations.NullMarked;
```

Différence majeure avec PHP : ce ne sont que des **annotations**. Le compilateur Java ne les vérifie pas, et `new Price(null, Currency.EURO)` compile toujours.

- Elles documentent le contrat, et l'IDE s'en sert pour avertir.
- Au chapitre 2, NullAway (un module d'Error Prone) fera échouer le build sur un `null` interdit.
- À l'exécution, on vérifie quand même : `Objects.requireNonNull(x, "x")`.

Convention Java : un argument `null` interdit lance `NullPointerException` (c'est ce que fait `requireNonNull`), et un argument non nul mais invalide lance `IllegalArgumentException`.

#### Exemple 4 : `Price` en record

Ajout au `pom.xml` du projet `shop`, dans `<dependencies>` (pas dans `<dependencyManagement>`) :

```xml
        <dependency>
            <groupId>org.jspecify</groupId>
            <artifactId>jspecify</artifactId>
            <version>1.0.1</version>
        </dependency>
```

Pas de `<scope>` : la dépendance sert au code de production.

Fichier `src/main/java/com/example/shop/price/Price.java` :

```java
package com.example.shop.price;

import java.math.BigDecimal;
import java.util.Objects;

public record Price(BigDecimal amount, Currency currency) {

    public Price {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(currency, "currency");

        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Montant négatif : " + amount);
        }

        if (amount.stripTrailingZeros().scale() > 2) {
            throw new IllegalArgumentException("Plus de deux décimales : " + amount);
        }

        amount = amount.setScale(2);
    }

    public static Price parse(String text) {
        String[] parts = text.split(" ");

        if (parts.length != 2) {
            throw new IllegalArgumentException("Format attendu \"<montant> <devise>\" : " + text);
        }

        return new Price(new BigDecimal(parts[0]), Currency.fromCode(parts[1]));
    }

    public Price add(Price other) {
        if (currency != other.currency) {
            throw new IllegalArgumentException("Devises différentes : " + currency + ", " + other.currency);
        }

        return new Price(amount.add(other.amount), currency);
    }

    @Override
    public String toString() {
        return amount.toPlainString() + " " + currency.code();
    }
}
```

Lecture ligne par ligne :

1. `public record Price(BigDecimal amount, Currency currency) {`

   Les deux composants. Java génère les champs, les accesseurs `amount()` et `currency()`, `equals` et `hashCode`.

2. `public Price {`

   Le constructeur compact.

3. `Objects.requireNonNull(amount, "amount");`

   Lance `NullPointerException("amount")` si `amount` est `null`. Le message nomme le paramètre fautif.

4. `if (amount.signum() < 0) {`

   Le test de signe. `amount < 0` ne compile pas : `<` ne s'applique qu'aux primitifs.

5. `if (amount.stripTrailingZeros().scale() > 2) {`

   Refuse plus de deux décimales significatives : `1.005` est refusé, `1.500` est accepté.

6. `amount = amount.setScale(2);`

   **Normalise** : tous les prix sont stockés avec exactement deux décimales. `1.5` et `1.50` deviennent identiques, donc égaux selon le `equals` généré. Le contrôle précédent garantit qu'aucun arrondi n'est nécessaire : sans lui, `setScale(2)` lancerait `ArithmeticException` sur `1.005`.

7. `new BigDecimal(parts[0])`

   Construit depuis le texte : exact. Un texte invalide lance `NumberFormatException`.

8. `Currency.fromCode(parts[1])`

   L'enum de l'exemple 2.

9. `public Price add(Price other) {`

   Une méthode ordinaire. Elle renvoie un **nouveau** `Price` : un record est immuable.

   `currency != other.currency` : `!=` est correct entre deux constantes d'enum.

10. `@Override public String toString()`

    Remplace le `toString` généré (`Price[amount=12.50, currency=EURO]`) par un format lisible. `toPlainString()` évite l'écriture `1E+2`.

#### Exemple 5 : ce que la normalisation apporte

Les tests de `Price` (extrait) :

```java
    @Test
    void pricesWithDifferentScalesAreEqual() {
        Price price = new Price(new BigDecimal("1.5"), Currency.EURO);
        Price same = new Price(new BigDecimal("1.50"), Currency.EURO);

        assertThat(price).isEqualTo(same);
        assertThat(price).hasSameHashCodeAs(same);
    }

    @Test
    void decimalArithmeticIsExact() {
        Price sum = Price.parse("0.10 EUR").add(Price.parse("0.20 EUR"));

        assertThat(sum).isEqualTo(Price.parse("0.30 EUR"));
    }

    @Test
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Price(null, Currency.EURO))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("amount");
    }
```

Les 8 tests de `PriceTest` passent.

Retirons la ligne `amount = amount.setScale(2);` du constructeur :

```console
$ ./mvnw -ntp -q test -Dtest='PriceTest#pricesWithDifferentScalesAreEqual'
[ERROR] com.example.shop.price.PriceTest.pricesWithDifferentScalesAreEqual -- Time elapsed: 0.071 s <<< FAILURE!
org.opentest4j.AssertionFailedError:

expected: 1.50 EUR
 but was: 1.5 EUR
```

Sans normalisation, le `equals` généré compare les `BigDecimal` avec **leur** `equals`, qui tient compte de l'échelle.

`-Dtest='Classe#méthode'` lance un seul test, comme `--filter` en PHPUnit.

Notez aussi `rejectsNullAmount` : passer `null` compile, malgré `@NullMarked`. Seul le contrôle à l'exécution l'arrête.

### Pièges

- **`new BigDecimal(0.1)`** : construit depuis un `double` déjà faux. Toujours depuis une `String`.
- **Record sans normalisation** : le `equals` généré utilise `BigDecimal.equals`. `1.0` et `1.00` sont différents.
- **`stripTrailingZeros` et l'affichage** : `100` s'affiche `1E+2`. Utilisez `toPlainString()` dans `toString`.
- **`getValue()` sur un record** : l'accesseur s'appelle `value()`. Les appels existants sont à renommer.
- **`this.value = value` dans un constructeur compact** : erreur de compilation. Réaffectez le paramètre, Java affecte le champ.
- **`@NullMarked` sans contrôle** : l'annotation ne protège de rien à l'exécution. Gardez `requireNonNull`.

### Vérifiez votre compréhension

1. Pourquoi `new Price(new BigDecimal("1.5"), ...)` égale-t-il `new Price(new BigDecimal("1.50"), ...)`, alors que les deux `BigDecimal` ne sont pas `equals` ?
2. Que contient un record `record Point(int x, int y) {}` que vous n'avez pas écrit ?
3. `new Price(null, Currency.EURO)` compile-t-il dans un package `@NullMarked` ? Pourquoi ?

---

### Pratique

#### B1. Ajouter JSpecify et déclarer le package non nul

Durée : 10 min. Modèle : exemple 4 (`pom.xml`) et section "Nullité".

1. Ajoutez `jspecify` 1.0.1 dans `<dependencies>`.
2. Créez `package-info.java` dans le package `recipe`, avec `@NullMarked`.

**Résultat attendu** : `./mvnw -ntp compile` affiche `BUILD SUCCESS`.

#### B2. Réécrire `Quantity` en record

Durée : 30 min. Modèle : exemple 4.

Avant de modifier, gardez l'ancienne version pour B5 :

```bash
mkdir -p sandbox/1.2
cp src/main/java/io/github/vincentchalamon/quinoa/recipe/Quantity.java sandbox/1.2/QuantityClass.java
```

Règles de la nouvelle `Quantity` :

1. deux composants : `value` (`BigDecimal`) et `unit` (`Unit`) ;
2. `value` et `unit` non nuls : `NullPointerException`, avec le nom du paramètre en message ;
3. `value` strictement positive ;
4. `value` **normalisée** sans zéros inutiles : `1.0`, `1.00` et `1` donnent la même quantité ;
5. `parse("250 g")` : la valeur depuis le texte, l'unité par son symbole ;
6. `toString()` : `"250 g"`, `"0.5 càc"`, `"100 ml"`, jamais `1E+2`.

Ce qui change par rapport au modèle :

- la normalisation : pas d'échelle fixe comme pour un prix. Voir `stripTrailingZeros` dans la section `BigDecimal` ;
- valeur strictement positive (`signum`) ;
- `NaN` et l'infini : plus de règle à écrire. `BigDecimal` ne les connaît pas, et `parse` les refuse déjà.

**Résultat attendu** : `./mvnw -ntp compile` affiche `BUILD SUCCESS`. Les tests ne compilent plus (`getValue()`, constructeur avec `double`) : c'est B3.

#### B3. Adapter les tests (avec une erreur volontaire)

Durée : 40 min. Modèle : exemples 3 et 5.

1. Mettez `doubleArithmeticIsNotExact` en commentaire (`Ctrl+/`) : il est traité en B4.
2. Adaptez le reste de `QuantityTest` : constructeur avec `new BigDecimal("...")` et `Unit`, accesseurs `value()` et `unit()`.
3. Revoyez les données de `rejectsIncorrectValue` : `kg` et `l` n'existent pas dans `Unit` (utilisez `g`, `ml`), et `NaN` / `Infinity` sont maintenant des nombres invalides (déplacez-les dans `rejectsInvalidQuantityNumber`).
4. Supprimez les tests devenus sans objet (unité vide : une `Unit` ne peut pas l'être), ajoutez ceux des nouvelles règles :
   - `null` refusé pour la valeur et pour l'unité, avec `NullPointerException` ;
   - `1.0` g égale `1.00` g, avec le même `hashCode` ;
   - symbole inconnu dans `parse` (`"250 kg"`) refusé ;
   - affichage de `100` sans `1E+2`.

Erreur volontaire :

1. retirez temporairement la normalisation du constructeur compact ;
2. lancez `./mvnw -ntp -q test` ;
3. lisez l'échec du test `1.0` / `1.00`, puis remettez la normalisation.

**Résultat attendu** à l'étape 2, un échec de la forme :

```console
expected: 1.00 g
 but was: 1.0 g
```

Puis `BUILD SUCCESS`.

> **Point d'arrêt** : `Quantity` est un record testé. Committez.

#### B4. Inverser le test des `double`

Durée : 10 min. Modèle : `decimalArithmeticIsExact` (exemple 5).

À l'étape 1.1, `doubleArithmeticIsNotExact` documentait la limite : `0.1 + 0.2` n'égale pas `0.3`.

1. Retirez le commentaire posé en B3.
2. Construisez la somme avec `add`, sur deux `BigDecimal`.
3. La seconde assertion (`0.30000000000000004`) n'a plus de sens : supprimez-la.
4. Lancez le test : que devient l'assertion `isNotEqualTo` avec `BigDecimal` ?
5. Inversez-la, et renommez le test pour décrire le nouveau comportement.

```bash
./mvnw -ntp -q test -Dtest='QuantityTest'
```

**Résultat attendu** : à l'étape 4, un échec sur `isNotEqualTo`. À l'étape 5, `BUILD SUCCESS`, et un test qui affirme que `0.1 + 0.2` g égale `0.3` g.

#### B5. Comparer avec la classe de l'étape 1.1

Durée : 10 min.

```bash
diff sandbox/1.2/QuantityClass.java src/main/java/io/github/vincentchalamon/quinoa/recipe/Quantity.java
wc -l sandbox/1.2/QuantityClass.java src/main/java/io/github/vincentchalamon/quinoa/recipe/Quantity.java
```

**Résultat attendu** : `wc -l` montre un record nettement plus court que la classe.

Notez, pour la session :

- ce qui a disparu, et qui l'écrit désormais ;
- ce qui reste écrit à la main, et pourquoi.

#### B6. Committer et ouvrir la PR

Durée : 15 min.

```bash
./mvnw -ntp clean verify
git push -u origin ch01/02-enums-records
gh pr create
```

**Résultat attendu** : `BUILD SUCCESS`, puis la PR ouverte.

Pour la description, reprenez les deux points "à consolider" de l'étape 1.1 :

- "Quoi" couvre **tout** ce que contient la PR ;
- "Comment c'est testé" donne la commande **et** le résultat attendu.

Pour le titre, un seul type Conventional Commits, celui du changement principal.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit la PR ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [ ] `./mvnw clean verify` passe depuis un clone propre.
- [ ] `Quantity` est un record, sans `equals` ni `hashCode` écrits à la main.
- [ ] `1.0` g égale `1.00` g, avec le même `hashCode`.
- [ ] Valeur nulle ou négative refusée ; `null` refusé avec `NullPointerException`.
- [ ] `toString` de `100` g affiche `100 g`.
- [ ] Chaque unité est testée par `@EnumSource`.
- [ ] Le test des `double` affirme maintenant que `0.1 + 0.2` égale `0.3`.

Questions "expliquez" :

1. Pourquoi faut-il normaliser la valeur dans le constructeur compact ?
2. Pourquoi `Unit.fromSymbol` et pas `Unit.valueOf` ?
3. Qu'apporte `@NullMarked`, et qu'est-ce qu'il n'apporte pas ?

---

## Documentation

- Enums (dev.java) : https://dev.java/learn/classes-objects/enums/
- Records (dev.java) : https://dev.java/learn/records/
- Records (JEP 395) : https://openjdk.org/jeps/395
- `BigDecimal` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/math/BigDecimal.html
- `Objects.requireNonNull` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Objects.html#requireNonNull(T,java.lang.String)
- JSpecify, guide utilisateur : https://jspecify.dev/docs/user-guide/
- Tests paramétrés et `@EnumSource` : https://docs.junit.org/current/writing-tests/parameterized-classes-and-tests.html#sources-EnumSource

## Pour aller plus loin (facultatif)

- `EnumSet` et `EnumMap` : des collections optimisées pour les enums. Utiles à l'étape 1.3, par exemple pour l'ensemble des régimes d'une recette.
- Effective Java, items 34 à 38 : enums. Item 17 : minimiser la mutabilité.
- La Javadoc de `BigDecimal` explique en détail l'échelle, la précision et les modes d'arrondi (`RoundingMode`).
