# Étape 1.3 - Ingrédients, collections et streams

[Chapitre 1](README.md) > Étape 1.3

## En bref

- **Objectif** : manipuler des listes de valeurs à la manière de Java : collections, génériques, streams, valeurs optionnelles.
- **Apport au projet** : le record `Ingredient` (nom, précision optionnelle, quantité optionnelle) et la règle "pas d'ingrédient en double", écrite avec les streams. La recette s'en servira au chapitre 4.
- **Durée indicative** : une demi-journée (environ 3 h), en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch01/03-ingredients`.

Avancement :

- [ ] A1. Créer la branche
- [ ] A2. Prédire dans jshell
- [ ] A3. Écrire le record `Ingredient`
- [ ] A4. Tester `Ingredient`
- [ ] B1. Écrire `duplicateNames` avec les streams
- [ ] B2. Tester la règle (avec une erreur volontaire)
- [ ] B3. Committer et ouvrir la PR
- [ ] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

Mémos : [Java pour PHP](../memo-java-pour-php.md), [Maven](../memo-maven.md), [tests](../memo-tests.md).

---

## Cycle A - Collections et valeurs optionnelles

### Théorie

#### Ce que vous savez déjà, ce qui change

En PHP, le tableau fait tout : liste, dictionnaire, ensemble. Java sépare ces rôles (vu en session à l'étape 1.0) :

- **`List`** : ordonnée, doublons permis. Comme `['pain', 'lait']`.
- **`Map`** : clé et valeur. Comme `['pain' => 3]`.
- **`Set`** : sans doublons, sans ordre garanti. Comme le résultat d'`array_unique`.

Ce qui change :

- ce sont des **interfaces**. On choisit une implémentation à la création (`ArrayList`, `HashMap`, `HashSet`), mais on déclare la variable avec l'interface : `List<String> names = new ArrayList<>();` ;
- certaines collections sont **non modifiables** : un `add` lève une exception à l'exécution ;
- elles sont **typées** grâce aux génériques.

#### Les génériques

`List<String>` se lit "liste de `String`". Le type entre chevrons est un **paramètre de type**.

- Le compilateur refuse d'y mettre autre chose qu'une `String`.
- À la lecture, pas de conversion : `names.get(0)` est déjà une `String`.

En PHP, l'équivalent n'existe que dans les annotations de PHPStan : `@var list<string>`, `@template T`. En Java, c'est vérifié par le compilateur.

Le **diamant** `<>` laisse le compilateur déduire le type : `new ArrayList<>()` dans `List<String> names = new ArrayList<>();`.

Les génériques ne prennent que des objets : `List<Integer>`, jamais `List<int>`. La conversion entre `int` et `Integer` est automatique (autoboxing, vu à l'étape 1.0).

#### `var`

`var` laisse le compilateur déduire le type d'une variable **locale** :

```java
var names = new ArrayList<String>();     // ArrayList<String>
```

- Le type est fixé à la compilation : ce n'est pas du typage dynamique.
- Seulement pour les variables locales : pas pour un champ, un paramètre ou un type de retour.
- À utiliser quand le type se lit déjà à droite (`new ...`). Si le type n'est pas évident, écrivez-le.

#### Exemple 1 : quatre façons de créer une liste

Avant de lire les résultats, **prédisez** chaque ligne.

```console
jshell> List<String> fixed = List.of("pain", "lait")
fixed ==> [pain, lait]

jshell> fixed.add("café")
|  Exception java.lang.UnsupportedOperationException

jshell> List<String> view = Arrays.asList("pain", "lait")
view ==> [pain, lait]

jshell> view.set(0, "brioche")
$4 ==> "pain"

jshell> view
view ==> [brioche, lait]

jshell> view.add("café")
|  Exception java.lang.UnsupportedOperationException

jshell> List<String> free = new ArrayList<>(List.of("pain", "lait"))
free ==> [pain, lait]

jshell> free.add("café")
$8 ==> true

jshell> free
free ==> [pain, lait, café]

jshell> List<String> copy = List.copyOf(free)
copy ==> [pain, lait, café]

jshell> free.add("thé")
$11 ==> true

jshell> copy
copy ==> [pain, lait, café]

jshell> List.of("pain", null)
|  Exception java.lang.NullPointerException
```

À retenir :

- `List.of(...)` : **non modifiable**. Ni ajout, ni modification, ni `null`. Le choix par défaut.
- `Arrays.asList(...)` : taille **fixe**, mais éléments modifiables (`set` marche, `add` échoue). Une vue sur un tableau, héritée des anciennes versions de Java. À éviter.
- `new ArrayList<>(...)` : **modifiable**. Quand on a vraiment besoin d'ajouter ou de retirer.
- `List.copyOf(...)` : une copie **non modifiable**. Ajouter à l'original ne change pas la copie : c'est la **copie défensive**, à utiliser quand un record reçoit une liste de l'extérieur.

L'erreur arrive à l'exécution, pas à la compilation : `List.of` et `ArrayList` ont le même type `List`.

#### Exemple 2 : `Set`, `Map` et `var`

```console
jshell> Set<String> tags = new HashSet<>(List.of("bio", "local", "bio"))
tags ==> [bio, local]

jshell> tags.size()
$2 ==> 2

jshell> tags.contains("bio")
$3 ==> true

jshell> Map<String, Integer> stock = new HashMap<>()
stock ==> {}

jshell> stock.put("pain", 3)
$5 ==> null

jshell> stock.put("lait", 0)
$6 ==> null

jshell> stock.get("pain")
$7 ==> 3

jshell> stock.get("café")
$8 ==> null

jshell> stock.getOrDefault("café", 0)
$9 ==> 0

jshell> var names = new ArrayList<String>()
names ==> []

jshell> names.add(42)
|  Error:
|  incompatible types: int cannot be converted to java.lang.String
|  names.add(42)
|            ^^
```

À retenir :

- le doublon `"bio"` disparaît dans le `Set` (grâce à `equals` et `hashCode`, étape 1.0) ;
- `put` renvoie l'ancienne valeur, `null` s'il n'y en avait pas ;
- `get` sur une clé absente renvoie `null`, sans erreur. En PHP, `$stock['café']` donnerait un warning ;
- `var` ne change rien au typage : `names` reste une `ArrayList<String>`.

#### Valeurs optionnelles : `@Nullable` ou `Optional` ?

Un ingrédient peut ne pas avoir de quantité : "sel, poivre". Java offre deux outils.

**`@Nullable`** (JSpecify, étape 1.2) : la valeur peut être `null`, et le type le dit.

**`Optional<T>`** : une boîte qui contient une valeur, ou rien. Vous en verrez avec les streams (cycle B) : `findFirst()` renvoie un `Optional`.

La règle, donnée par la Javadoc d'`Optional` elle-même (section "API Note" : "primarily intended for use as a method return type") :

- `Optional` sert de **type de retour** d'une méthode qui peut ne rien trouver ;
- **pas** pour un champ, un composant de record, un paramètre ou une collection. Il alourdit le code, et Jackson ou JPA (chapitres 4 et 5) le gèrent mal.

Pour un composant de record optionnel : `@Nullable`.

#### Exemple 3 : un record avec un composant optionnel

Projet `shop` de l'étape 1.2. Fichier `src/main/java/com/example/shop/price/Product.java` (première partie) :

```java
package com.example.shop.price;

import java.util.Objects;
import org.jspecify.annotations.Nullable;

public record Product(String name, @Nullable String brand, Price price) {

    public Product {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(price, "price");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Nom vide");
        }
        if (brand != null && brand.isBlank()) {
            throw new IllegalArgumentException("Marque vide : utiliser null");
        }

        name = name.strip();
        brand = brand == null ? null : brand.strip();
    }

    @Override
    public String toString() {
        if (brand == null) {
            return name + " " + price;
        }
        return name + " (" + brand + ") " + price;
    }
}
```

Lecture ligne par ligne :

1. `import org.jspecify.annotations.Nullable;`

   L'annotation, dans le même paquet JSpecify que `@NullMarked`.

2. `public record Product(String name, @Nullable String brand, Price price) {`

   Le package est `@NullMarked` : `name` et `price` sont non nuls. `brand` est explicitement nullable.

   En PHP : `public function __construct(public string $name, public ?string $brand, public Price $price)`.

3. `Objects.requireNonNull(name, "name");`

   Pas de `requireNonNull` pour `brand` : `null` y est permis.

4. `if (brand != null && brand.isBlank()) {`

   Une marque vide (`" "`) n'a pas de sens : on refuse, et on impose `null` pour "pas de marque". Une seule façon de dire "absent".

   `&&` court-circuite : `isBlank()` n'est jamais appelé sur `null`.

5. `name = name.strip();`

   Normalise : `"  Pain "` devient `"Pain"`. `strip()` retire les espaces Unicode, contrairement à `trim()` (étape 1.0).

6. `brand = brand == null ? null : brand.strip();`

   Même normalisation, seulement si la marque existe. Le ternaire, comme en PHP.

7. `toString()`

   Deux formats selon la présence de la marque : `Pain 1.20 EUR` ou `Café (Arabica & Co) 4.50 EUR`.

Tests (extrait) :

```java
    @Test
    void brandIsOptional() {
        Product bread = new Product("Pain", null, Price.parse("1.20 EUR"));

        assertThat(bread.brand()).isNull();
        assertThat(bread).hasToString("Pain 1.20 EUR");
    }

    @Test
    void stripsName() {
        assertThat(new Product("  Pain ", null, Price.parse("1.20 EUR")).name()).isEqualTo("Pain");
    }

    @Test
    void rejectsBlankBrand() {
        assertThatThrownBy(() -> new Product("Pain", " ", Price.parse("1.20 EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }
```

### Pièges

- **`add` sur `List.of`** : `UnsupportedOperationException` à l'exécution, pas à la compilation.
- **`Arrays.asList`** : on croit avoir une liste modifiable, elle a une taille fixe.
- **`Optional` en composant de record** : à éviter. `@Nullable`, et le contrôle à l'exécution là où c'est nécessaire.
- **Deux façons de dire "absent"** : `null` et `""`. Choisissez-en une, et refusez l'autre.
- **`get` sur une `Map`** : une clé absente donne `null`, et un `int count = map.get(...)` lève alors une `NullPointerException` (déballage d'un `Integer` nul).

### Vérifiez votre compréhension

1. Quelle différence entre `List.of`, `Arrays.asList` et `new ArrayList<>()` ?
2. Pourquoi `List.copyOf(free)` n'a-t-il pas changé quand on a ajouté `"thé"` à `free` ?
3. Pourquoi `@Nullable` plutôt qu'`Optional` pour la marque d'un produit ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git switch main
git pull
git switch -c ch01/03-ingredients
```

**Résultat attendu** : `git status` affiche `Sur la branche ch01/03-ingredients`.

Votre dépôt est encore sur `ch01/02-enums-records`, déjà mergée : `git switch main` est indispensable.

#### A2. Prédire dans jshell

Durée : 15 min. Modèle : exemples 1 et 2.

Pour chaque ligne, notez votre prédiction, puis vérifiez :

```text
var stock = new HashMap<String, Integer>()
stock.put("oeufs", 6)
stock.put("oeufs", 4)
stock.get("oeufs")
int flour = stock.get("farine")
List.copyOf(List.of("a", "b")).add("c")
new HashSet<>(List.of("A", "a")).size()
```

La ligne `int flour = ...` illustre le piège du `get` sur une `Map`. La dernière prépare B1 : la casse compte dans un `Set`.

#### A3. Écrire le record `Ingredient`

Durée : 30 min. Modèle : exemple 3.

Dans le package `recipe`, un record `Ingredient` à trois composants :

1. `name` (`String`) : obligatoire, non vide, normalisé par `strip()` ;
2. `precision` (`String`, optionnelle) : "bien mûres", "pour le décor". `null` si absente ; vide refusée ; normalisée par `strip()` ;
3. `quantity` (`Quantity`, optionnelle) : absente pour "sel, poivre".

`toString()` affiche, dans cet ordre, le nom, la quantité si elle existe, la précision entre parenthèses si elle existe :

- `farine 250 g (tamisée)` ;
- `sel`.

Ce qui change par rapport au modèle : deux composants optionnels au lieu d'un, donc quatre cas pour `toString`.

**Résultat attendu** : `./mvnw -ntp compile` affiche `BUILD SUCCESS`.

#### A4. Tester `Ingredient`

Durée : 30 min. Modèle : tests de l'exemple 3.

Un test par règle :

- nom `null` (`NullPointerException`) ou vide (`IllegalArgumentException`) refusé ;
- nom normalisé ;
- précision vide refusée, précision normalisée ;
- précision et quantité absentes acceptées ;
- `toString` dans les quatre cas (avec ou sans quantité, avec ou sans précision). Un `@CsvSource` peut les regrouper : souvenez-vous qu'une valeur vide y vaut `null`.

```bash
./mvnw -ntp test
```

**Résultat attendu** : `BUILD SUCCESS`.

Committez :

```bash
git add src
git commit -m "feat: add Ingredient record"
```

> **Point d'arrêt** : `Ingredient` existe et est testé. Le cycle B ajoute la règle des doublons.

---

## Cycle B - Les streams

### Théorie

#### Ce que vous savez déjà, ce qui change

Vous enchaînez `array_filter` et `array_map` en PHP, ou les méthodes de `Doctrine\Common\Collections\ArrayCollection` (`filter`, `map`). Les **streams** jouent ce rôle en Java, sous forme de chaîne :

```java
List<String> result = words.stream()      // 1. ouvrir le flux
        .filter(w -> w.startsWith("p"))     // 2. opérations intermédiaires
        .map(String::toUpperCase)
        .toList();                          // 3. opération terminale
```

Ce qui change :

- **un stream n'est pas une collection** : c'est un traitement. Rien ne se passe tant qu'on n'appelle pas une opération **terminale** (`toList`, `collect`, `findFirst`, `count`) ;
- un stream ne sert **qu'une fois** ;
- la collection d'origine n'est jamais modifiée.

#### Lambdas et références de méthode

Une **lambda** est une fonction passée en argument : `w -> w.startsWith("p")`.

En PHP : `fn($w) => str_starts_with($w, 'p')`.

Une **référence de méthode** est un raccourci quand la lambda ne fait qu'appeler une méthode :

- `String::toUpperCase` équivaut à `w -> w.toUpperCase()` ;
- `Product::name` équivaut à `p -> p.name()`.

En PHP, l'équivalent le plus proche est la syntaxe callable de PHP 8.1 : `strtoupper(...)`.

#### Exemple 4 : streams dans jshell

```console
jshell> var words = List.of("pain", "Lait", "café", "pain", "PAIN")
words ==> [pain, Lait, café, pain, PAIN]

jshell> words.stream().map(String::toUpperCase).toList()
$2 ==> [PAIN, LAIT, CAFÉ, PAIN, PAIN]

jshell> words.stream().filter(w -> w.startsWith("p")).toList()
$3 ==> [pain, pain]

jshell> var counts = words.stream().collect(Collectors.groupingBy(w -> w.toLowerCase(Locale.ROOT), Collectors.counting()))
counts ==> {lait=1, pain=3, café=1}

jshell> counts.entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet())
$5 ==> [pain]

jshell> words.stream().filter(w -> w.startsWith("c")).findFirst()
$6 ==> Optional[café]

jshell> words.stream().filter(w -> w.startsWith("z")).findFirst()
$7 ==> Optional.empty

jshell> words.stream().filter(w -> w.startsWith("z")).findFirst().orElse("aucun")
$8 ==> "aucun"

jshell> words.stream().map(String::length)
$9 ==> java.util.stream.ReferencePipeline$3@30946e09
```

Lecture :

1. `map(String::toUpperCase)` : transforme chaque élément. `array_map` en PHP.
2. `filter(w -> w.startsWith("p"))` : garde les éléments qui vérifient la condition. `array_filter`. `"PAIN"` n'est pas gardé : la casse compte.
3. `.toList()` : opération terminale, renvoie une liste **non modifiable**.
4. `Collectors.groupingBy(clé, Collectors.counting())` : regroupe par clé et compte. Le résultat est une `Map<String, Long>` : chaque nom en minuscules, avec son nombre d'occurrences. Pas d'équivalent direct en PHP : `array_count_values` après un `array_map` sur `strtolower`.
5. `counts.entrySet().stream()` : on peut streamer une `Map` à travers ses **entrées** (`Map.Entry`, un couple clé-valeur). `e.getValue()` donne le compte, `Map.Entry::getKey` la clé.
6. `Collectors.toSet()` : opération terminale qui renvoie un `Set`.
7. `findFirst()` : renvoie un `Optional`, plein ou vide. `orElse("aucun")` donne une valeur de repli, comme `?? 'aucun'` en PHP.
8. Dernière ligne : sans opération terminale, on obtient l'objet stream lui-même, et rien n'a été calculé.

#### `toLowerCase` et la langue

```console
jshell> "TITLE".toLowerCase(Locale.forLanguageTag("tr"))
$1 ==> "tıtle"

jshell> "TITLE".toLowerCase(Locale.ROOT)
$2 ==> "title"
```

En turc, le `I` majuscule devient un `ı` sans point. Sans argument, `toLowerCase()` utilise la langue de la machine : le même code donnerait un autre résultat sur un serveur réglé en turc.

Pour comparer des textes destinés à la machine (codes, noms à dédoublonner), toujours `Locale.ROOT`. C'est le même piège que `"%.2f".formatted(...)` à l'étape 1.0.

#### Exemple 5 : streams dans le code

Suite de `Product.java` :

```java
    public static List<String> namesCheaperThan(List<Product> products, Price limit) {
        return products.stream()
                .filter(product -> product.price().currency() == limit.currency())
                .filter(product -> product.price().amount().compareTo(limit.amount()) < 0)
                .map(Product::name)
                .toList();
    }

    public static Map<Currency, Long> countByCurrency(List<Product> products) {
        return products.stream()
                .collect(Collectors.groupingBy(product -> product.price().currency(), Collectors.counting()));
    }
```

Imports à ajouter : `java.util.List`, `java.util.Map`, `java.util.stream.Collectors`.

Lecture :

1. `public static List<String> namesCheaperThan(List<Product> products, Price limit)`

   Une méthode statique : elle porte sur une liste de produits, pas sur un produit.

2. Deux `filter` à la suite : même devise, puis montant inférieur. On peut enchaîner autant d'opérations qu'il faut ; une condition par `filter` se lit mieux.

3. `compareTo(...) < 0` : "strictement inférieur" pour des `BigDecimal` (étape 1.2).

4. `.map(Product::name)` : on garde le nom seul.

5. `Map<Currency, Long>` : `groupingBy` sur la devise, `counting` donne un `Long` (objet), pas un `long`.

Tests (extrait) :

```java
    private static final List<Product> CATALOG = List.of(
            new Product("Pain", null, Price.parse("1.20 EUR")),
            new Product("Café", "Arabica & Co", Price.parse("4.50 EUR")),
            new Product("Chocolat", "Cailler", Price.parse("3.90 CHF")),
            new Product("Lait", null, Price.parse("0.95 EUR")));

    @Test
    void listsNamesCheaperThanLimit() {
        assertThat(Product.namesCheaperThan(CATALOG, Price.parse("2 EUR"))).containsExactly("Pain", "Lait");
    }

    @Test
    void countsProductsByCurrency() {
        assertThat(Product.countByCurrency(CATALOG))
                .isEqualTo(Map.of(Currency.EURO, 3L, Currency.SWISS_FRANC, 1L));
    }

    @Test
    void returnsEmptyListWhenNothingMatches() {
        assertThat(Product.namesCheaperThan(CATALOG, Price.parse("0.10 EUR"))).isEmpty();
    }
```

Ce qui est nouveau :

- `private static final List<Product> CATALOG` : une **constante** partagée par les tests. `static final` et nom en majuscules : l'équivalent d'une `const` de classe PHP. `List.of` la rend non modifiable : aucun test ne peut la modifier par erreur ;
- `containsExactly(...)` : contenu **et ordre** exacts ;
- `3L` : un littéral `long`. `Map.of(Currency.EURO, 3)` contiendrait un `Integer`, différent d'un `Long` pour `equals`.

Les 6 tests de `ProductTest` passent.

#### Text blocks

Pour un texte sur plusieurs lignes, Java a le **text block**, l'équivalent du heredoc PHP :

```java
String menu = """
    Entrée
      Plat
    Dessert
    """;
```

Dans jshell, la variable vaut :

```console
menu ==> "Entrée\n  Plat\nDessert\n"
```

- Il commence par `"""` suivi d'un retour à la ligne.
- L'indentation commune est retirée, l'indentation relative (`  Plat`) est gardée.

Utile pour les tests qui comparent un texte long, ou pour du JSON dans les tests (chapitre 5).

### Pièges

- **Stream sans opération terminale** : rien n'est calculé (dernière ligne de l'exemple 4).
- **`toLowerCase()` sans `Locale`** : le résultat dépend de la machine. `Locale.ROOT`.
- **`containsExactly` sur un `Set`** : l'ordre d'un `HashSet` n'est pas garanti. Avec plusieurs éléments, utilisez `containsExactlyInAnyOrder`.
- **`3` au lieu de `3L`** : `Integer` et `Long` ne sont jamais égaux selon `equals`, même pour la même valeur.
- **Réutiliser un stream** : un second appel terminal lève `IllegalStateException`. Recréez-le depuis la collection.

### Vérifiez votre compréhension

1. Que renvoie `words.stream().filter(...)` sans `.toList()` ?
2. Pourquoi `Locale.ROOT` dans `toLowerCase` ?
3. Que contient la `Map` produite par `groupingBy(..., counting())` ?

---

### Pratique

#### B1. Écrire `duplicateNames` avec les streams

Durée : 30 min. Modèle : exemple 4 (lignes 4 et 5), exemple 5.

Dans `Ingredient`, une méthode statique :

```java
static Set<String> duplicateNames(List<Ingredient> ingredients)
```

Elle renvoie les noms présents **plus d'une fois** dans la liste, en minuscules :

- `Farine`, `sel`, `farine` donne `{farine}` ;
- une liste sans doublon donne un ensemble vide.

La comparaison ignore la casse. Les noms sont déjà normalisés (`strip`) par le constructeur.

Ce qui change par rapport aux modèles : vous composez vous-même les deux étapes de l'exemple 4 (`groupingBy` puis le stream sur les entrées) dans une méthode.

La recette (chapitre 4) appellera cette méthode pour refuser une liste d'ingrédients avec doublons.

**Résultat attendu** : `./mvnw -ntp compile` affiche `BUILD SUCCESS`.

#### B2. Tester la règle (avec une erreur volontaire)

Durée : 30 min. Modèle : tests de l'exemple 5.

Cas à couvrir :

1. aucun doublon ;
2. un doublon de même casse ;
3. un doublon de casse différente (`Farine` et `farine`) ;
4. plusieurs doublons (attention à l'ordre d'un `Set`) ;
5. une liste vide.

Erreur volontaire :

1. dans `duplicateNames`, remplacez temporairement la clé de regroupement par `Ingredient::name`, sans mise en minuscules ;
2. lancez `./mvnw -ntp -q test` ;
3. lisez l'échec du cas 3, puis remettez la mise en minuscules.

**Résultat attendu** à l'étape 2, un échec de la forme :

```console
Expecting actual:
  []
to contain exactly (and in same order):
  ["farine"]
but could not find the following elements:
  ["farine"]
```

Puis `BUILD SUCCESS`.

> **Point d'arrêt** : la règle est écrite et testée. Committez.

#### B3. Committer et ouvrir la PR

Durée : 15 min.

```bash
./mvnw -ntp clean verify
git push -u origin ch01/03-ingredients
gh pr create
```

**Résultat attendu** : `BUILD SUCCESS`, puis la PR ouverte.

Pour la description, les points "à consolider" :

- "Pourquoi" donne la vraie raison : le besoin du cahier des charges (quantité optionnelle pour "sel, poivre", pas de doublon dans une recette) ;
- un titre court.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit la PR ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [ ] `./mvnw clean verify` passe depuis un clone propre.
- [ ] `Ingredient` accepte une précision et une quantité absentes, refuse un nom ou une précision vides.
- [ ] `toString` couvre les quatre cas.
- [ ] `duplicateNames` est écrite avec les streams et ignore la casse avec `Locale.ROOT`.
- [ ] Les tests des doublons ne dépendent pas de l'ordre d'un `Set`.

Questions "expliquez" :

1. Quelle différence entre `List.of`, `Arrays.asList` et `new ArrayList<>()` ? Laquelle choisir par défaut ?
2. Pourquoi `@Nullable` et pas `Optional<Quantity>` pour la quantité d'un ingrédient ?
3. Que fait `groupingBy(..., counting())`, et pourquoi faut-il ensuite streamer `entrySet()` ?

---

## Documentation

- Collections (dev.java) : https://dev.java/learn/api/collections-framework/
- Collections non modifiables : https://docs.oracle.com/en/java/javase/25/core/creating-immutable-lists-sets-and-maps.html
- Génériques (dev.java) : https://dev.java/learn/generics/
- Lambdas (dev.java) : https://dev.java/learn/lambdas/
- Streams (dev.java) : https://dev.java/learn/api/streams/
- `Collectors` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/stream/Collectors.html
- `Optional` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/Optional.html
- `var` (guide de style officiel) : https://openjdk.org/projects/amber/guides/lvti-style-guide
- Text blocks : https://docs.oracle.com/en/java/javase/25/text-blocks/index.html

## Pour aller plus loin (facultatif)

- Effective Java, items 42 à 48 : lambdas et streams, dont "use streams judiciously" (une boucle `for` reste parfois plus lisible).
- `Collectors.toMap`, `partitioningBy`, `joining` : d'autres collecteurs courants.
