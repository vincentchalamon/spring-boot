# Étape 2.1 - Formatage et analyse statique

[Chapitre 2](README.md) > Étape 2.1

## En bref

- **Objectif** : faire porter le style et les erreurs courantes par l'outillage, pas par la relecture.
- **Apport au projet** : `./mvnw verify` vérifie la version de Java et de Maven, le formatage du code, et refuse à la compilation les erreurs détectées par Error Prone et NullAway.
- **Durée indicative** : une demi-journée (environ 3 h 30), en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch02/01-quality`.

Avancement :

- [x] A1. Créer la branche
- [x] A2. Maven Enforcer (avec une erreur volontaire)
- [x] A3. Spotless : premier `check`
- [x] A4. VSCode et formatage du projet
- [x] B1. Error Prone
- [x] B2. Une erreur volontaire sur `Quantity`
- [x] B3. NullAway
- [x] B4. Les avertissements deviennent des erreurs
- [x] B5. Committer et ouvrir la PR
- [x] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

Mémos : [Maven](../memo-maven.md) (nouvelle section "Plugins et phases"), [tests](../memo-tests.md).

---

## Cycle A - Plugins Maven : versions et formatage

### Théorie

#### Ce que vous savez déjà, ce qui change

En PHP, chaque outil qualité est un binaire à part :

- `vendor/bin/php-cs-fixer fix` pour le formatage ;
- `vendor/bin/phpstan analyse` pour l'analyse statique ;
- `"require": {"php": ">=8.3"}` dans `composer.json` pour la version de PHP.

On les enchaîne dans un `Makefile` ou un script Composer.

Ce qui change :

- en Java, ces outils sont des **plugins Maven**, déclarés dans le `pom.xml` ;
- ils se **branchent sur le cycle de vie** : `./mvnw verify` les lance tous, dans l'ordre, sans `Makefile` ;
- on peut aussi lancer un plugin seul, comme un binaire PHP : `./mvnw spotless:apply`.

#### Plugins, goals et phases

Vous connaissez les phases (étape 1.1) : `validate -> compile -> test -> package -> verify`.

Une phase ne fait rien par elle-même. Le travail est fait par des **goals** de plugins **attachés** à la phase.

- **Plugin** : un outil. Exemple : `maven-compiler-plugin`.
- **Goal** : une commande du plugin. Exemple : `compile`, noté `compiler:compile`.
- **Execution** : dans le `pom.xml`, l'attachement d'un goal à une phase.

Certains goals sont attachés d'office : `compiler:compile` à `compile`, `surefire:test` à `test`. C'est pourquoi `./mvnw test` compile puis teste sans rien déclarer.

Les plugins qualité, eux, ne sont attachés à rien : il faut une `<execution>`. Chaque goal a une **phase par défaut**, utilisée si l'execution n'en précise pas :

- `enforcer:enforce` : `validate`, tout au début ;
- `spotless:check` : `verify`, tout à la fin.

En Symfony, l'analogie la plus proche : un listener (le goal) abonné à un événement du kernel (la phase).

#### `<plugins>` ou `<pluginManagement>` ?

Rappel de l'étape 1.1 :

- `<pluginManagement>` **fixe** la version et la configuration d'un plugin, sans l'activer ;
- `<plugins>` **active** un plugin dans le build.

Le compilateur et Surefire sont actifs d'office : les régler dans `<pluginManagement>` suffit. Enforcer et Spotless ne le sont pas : ils vont dans `<plugins>`.

#### Exemple 1 : Maven Enforcer

Projet `shop` de l'étape 1.3. Dans `<build>`, après `</pluginManagement>` :

```xml
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-enforcer-plugin</artifactId>
                <version>3.7.0</version>
                <executions>
                    <execution>
                        <id>enforce-versions</id>
                        <goals>
                            <goal>enforce</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <requireJavaVersion>
                                    <version>[26,)</version>
                                </requireJavaVersion>
                                <requireMavenVersion>
                                    <version>[3.9,4)</version>
                                </requireMavenVersion>
                            </rules>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
```

Lecture ligne par ligne :

1. `<plugins>`

   Active les plugins listés. Un seul bloc `<plugins>` dans `<build>` : Spotless viendra s'y ajouter.

2. `<execution>` et `<id>enforce-versions</id>`

   L'attachement. L'`id` est libre : il apparaît dans les logs.

3. `<goal>enforce</goal>`

   Le goal lancé. Pas de `<phase>` : il prend sa phase par défaut, `validate`.

4. `<configuration>` dans l'`<execution>`

   La configuration de cette execution seulement.

5. `<version>[26,)</version>`

   Un **intervalle** de versions, en notation mathématique : `[` inclus, `)` exclu, rien après la virgule = sans limite. Ici "26 ou plus".

   En Composer : `>=26`. Et `[3.9,4)` s'écrirait `>=3.9 <4`.

Le JDK de la machine est en version 25 : la règle est volontairement fausse.

```console
$ ./mvnw -ntp verify
[INFO] --- enforcer:3.7.0:enforce (enforce-versions) @ shop ---
[INFO] Rule 1: org.apache.maven.enforcer.rules.version.RequireMavenVersion passed
[INFO] BUILD FAILURE
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-enforcer-plugin:3.7.0:enforce (enforce-versions) on project shop:
[ERROR] Rule 0: org.apache.maven.enforcer.rules.version.RequireJavaVersion failed with message:
[ERROR] Detected JDK /home/vincent/.sdkman/candidates/java/25.0.5.0+8-tem is version 25.0.5 which is not in the allowed range [26,).
```

À retenir :

- le build s'arrête **avant** de compiler : un message clair, au lieu d'une erreur de compilation obscure plus loin ;
- avec `[25,)`, les deux règles passent (`Rule 0 ... passed`).

Pourquoi vérifier Maven alors que le Wrapper fixe sa version ? Parce que rien n'empêche quelqu'un de lancer `mvn` au lieu de `./mvnw`.

#### Formater du Java : quel outil ?

L'équivalent de PHP-CS-Fixer s'appelle **Spotless**. C'est un plugin qui pilote un **formateur**, au choix :

- **google-java-format** : le style Google, indentation de 2 espaces ;
- **palantir-java-format** : un dérivé, indentation de 4 espaces, lignes de 120 caractères, pensé pour les chaînes de streams et les lambdas ;
- le formateur d'Eclipse, celui de VSCode, configurable par un fichier XML de plusieurs centaines de réglages.

Les deux premiers ne se configurent **pas** : un seul style possible, comme PSR-12 sans options. Il n'y a plus rien à discuter en revue.

Choix du cours : **palantir-java-format**. Le projet est déjà en 4 espaces (`.editorconfig`), et vous écrirez beaucoup de streams.

#### Exemple 2 : Spotless avec palantir-java-format

À ajouter dans le même bloc `<plugins>`, après Enforcer :

```xml
            <plugin>
                <groupId>com.diffplug.spotless</groupId>
                <artifactId>spotless-maven-plugin</artifactId>
                <version>3.12.0</version>
                <configuration>
                    <java>
                        <palantirJavaFormat>
                            <version>2.103.0</version>
                        </palantirJavaFormat>
                    </java>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>check</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

Lecture ligne par ligne :

1. `<configuration>` au niveau du plugin

   Hors de l'`<execution>` : elle vaut pour **tous** les goals du plugin, `check` comme `apply`. Indispensable ici, puisque vous lancerez `apply` à la main.

2. `<palantirJavaFormat>` et sa `<version>`

   Le formateur choisi, avec sa version exacte. Spotless le télécharge comme une dépendance.

3. `<goal>check</goal>`

   Vérifie sans rien modifier, et fait échouer le build en cas d'écart. Phase par défaut : `verify`.

   L'autre goal, `apply`, reformate les fichiers. On le lance à la main, jamais dans le build.

En PHP : `check` est `php-cs-fixer fix --dry-run --diff`, `apply` est `php-cs-fixer fix`.

Premier lancement sur `shop` :

```console
$ ./mvnw -ntp verify
...
[INFO] --- spotless:3.12.0:check (default) @ shop ---
[INFO] Spotless.Java is keeping 5 files clean - 1 needs changes to be clean, 4 were already clean, 0 were skipped because caching determined they were already clean
[INFO] BUILD FAILURE
[ERROR] Failed to execute goal com.diffplug.spotless:spotless-maven-plugin:3.12.0:check (default) on project shop: The following files had format violations:
[ERROR]     src/test/java/com/example/shop/price/CurrencyTest.java
[ERROR]         @@ -11,11 +11,7 @@
[ERROR]          class·CurrencyTest·{
[ERROR]
[ERROR]          ····@ParameterizedTest
[ERROR]         -····@CsvSource({
[ERROR]         -········"EUR,·EURO",
[ERROR]         -········"USD,·US_DOLLAR",
[ERROR]         -········"CHF,·SWISS_FRANC"
[ERROR]         -····})
[ERROR]         +····@CsvSource({"EUR,·EURO",·"USD,·US_DOLLAR",·"CHF,·SWISS_FRANC"})
[ERROR]          ····void·findsCurrencyByCode(String·code,·Currency·expected)·{
[ERROR]          ········assertThat(Currency.fromCode(code)).isEqualTo(expected);
[ERROR]          ····}
[ERROR] Run 'mvn spotless:apply' to fix these violations.
```

Lecture :

1. un diff unifié : `-` la ligne actuelle, `+` la ligne attendue ;
2. `·` matérialise chaque espace ;
3. palantir regroupe le `@CsvSource` sur une ligne, puisqu'il tient en 120 caractères.

Puis :

```console
$ ./mvnw -ntp -q spotless:apply
$ git diff --stat
 .../java/com/example/shop/price/CurrencyTest.java  |  6 +--
```

Le fichier est reformaté, `./mvnw verify` passe. palantir trie aussi les imports et retire ceux qui ne servent pas.

Ordre des plugins dans un `verify` complet :

```console
[INFO] --- clean:3.2.0:clean (default-clean) @ shop ---
[INFO] --- enforcer:3.7.0:enforce (enforce-versions) @ shop ---
[INFO] --- resources:3.4.0:resources (default-resources) @ shop ---
[INFO] --- compiler:3.16.0:compile (default-compile) @ shop ---
[INFO] --- resources:3.4.0:testResources (default-testResources) @ shop ---
[INFO] --- compiler:3.16.0:testCompile (default-testCompile) @ shop ---
[INFO] --- surefire:3.6.1:test (default-test) @ shop ---
[INFO] --- jar:3.5.0:jar (default-jar) @ shop ---
[INFO] --- spotless:3.12.0:check (default) @ shop ---
```

Chaque ligne est un goal, attaché à sa phase. `./mvnw test` s'arrête après Surefire : il ne vérifie **pas** le formatage.

#### L'IDE formate, le build vérifie

Question restée floue à l'étape 0.2 : qui garantit le formatage ? Réponse : **le build**, donc la CI à l'étape 2.3.

Problème : VSCode formate à l'enregistrement avec le formateur d'Eclipse, et range les imports en groupes séparés par une ligne vide. palantir fait autrement. Les deux se contrediraient à chaque enregistrement.

La solution du cours :

1. désactiver le formatage à l'enregistrement **pour Java seulement** ;
2. lancer `./mvnw spotless:apply` avant chaque commit.

Les fichiers JSON, YAML et Markdown restent formatés par VSCode.

### Pièges

- **`No plugin found for prefix 'spotless'`** : `./mvnw spotless:apply` ne marche que si le plugin est déclaré dans le `pom.xml` du répertoire courant.
- **`WARNING: A terminally deprecated method in sun.misc.Unsafe has been called`** : affiché par Spotless sur Java 25. Sans conséquence, à ignorer.
- **Commentaire de fin de ligne** : s'il dépasse 120 caractères, palantir le coupe en deux lignes, mal placées. Relisez le diff après `apply`, et mettez ces commentaires au-dessus de la ligne.
- **`spotless:apply` modifie vos fichiers** : committez votre travail avant, pour pouvoir relire le diff du formatage seul.
- **Indentation en 2 espaces partout** : c'est google-java-format, pas palantir. Vérifiez la balise.
- **`Shift+Alt+F` sur un fichier Java** : "Format Document" utilise toujours le formateur d'Eclipse. Pour Java, seul `./mvnw spotless:apply` formate.

### Vérifiez votre compréhension

1. Pourquoi `./mvnw test` ne détecte-t-il pas un fichier mal formaté ?
2. Que signifie `[3.9,4)` ?
3. Pourquoi la `<configuration>` de Spotless est-elle au niveau du plugin et non dans l'`<execution>` ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git switch main
git pull
git switch -c ch02/01-quality
```

**Résultat attendu** : `git status` affiche `Sur la branche ch02/01-quality`.

#### A2. Maven Enforcer (avec une erreur volontaire)

Durée : 15 min. Modèle : exemple 1.

Dans le `pom.xml` du projet, ajoutez Enforcer avec deux règles : Java 25 ou plus, Maven 3.9 ou plus mais pas 4.

Erreur volontaire :

1. mettez d'abord `[26,)` pour Java ;
2. lancez `./mvnw -ntp verify` ;
3. lisez le message, puis corrigez en `[25,)`.

**Résultat attendu** : d'abord `BUILD FAILURE` avec `is not in the allowed range [26,)`, puis `BUILD SUCCESS` avec deux `passed`.

Committez :

```bash
git add pom.xml
git commit -m "build: enforce Java and Maven versions"
```

#### A3. Spotless : premier `check`

Durée : 20 min. Modèle : exemple 2.

Ajoutez Spotless dans le même bloc `<plugins>`, puis :

```bash
./mvnw -ntp verify
```

**Résultat attendu** : `BUILD FAILURE`. Votre code a été formaté par VSCode, pas par palantir : plusieurs fichiers sont signalés.

Lisez les diffs avant d'appliquer quoi que ce soit. Repérez au moins :

- les imports ;
- les `@CsvSource` et `@ValueSource` ;
- la chaîne de streams de `duplicateNames` ;
- un commentaire de fin de ligne dans `IngredientTest`.

Committez la configuration seule, avant tout reformatage :

```bash
git add pom.xml
git commit -m "build: add Spotless with palantir-java-format"
```

> **Point d'arrêt** : le build échoue sur le formatage, c'est l'état attendu à ce stade.

#### A4. VSCode et formatage du projet

Durée : 20 min. Modèle : section "L'IDE formate, le build vérifie".

1. Dans `.vscode/settings.json`, le bloc `"[java]"` contient déjà l'organisation des imports à l'enregistrement. Remplacez son contenu :
   - plus d'organisation des imports (palantir s'en charge) ;
   - le réglage `editor.formatOnSave`, avec la valeur inverse du réglage global. Un bloc `"[java]": {}` vide ne change rien : le réglage global s'appliquerait encore.

   Le `"editor.formatOnSave": true` global reste, pour JSON, YAML et Markdown.
2. Lancez :

   ```bash
   ./mvnw -ntp -q spotless:apply
   git diff
   ```

3. Relisez le diff. Corrigez à la main le commentaire de fin de ligne coupé, puis relancez `spotless:apply`.
4. Vérifiez :

   ```bash
   ./mvnw -ntp verify
   ```

**Résultat attendu** : `BUILD SUCCESS`, et l'enregistrement d'un fichier Java dans VSCode ne change plus sa mise en forme.

Deux commits séparés, pour que la revue distingue le réglage du reformatage :

```bash
git add .vscode/settings.json
git commit -m "chore: let Spotless format Java files"
git add src
git commit -m "style: apply palantir-java-format"
```

`style:` est le type Conventional Commits d'un changement de mise en forme seule, sans effet sur le comportement.

> **Point d'arrêt** : le formatage est porté par le build. Le cycle B ajoute l'analyse statique.

---

## Cycle B - Analyse statique : Error Prone et NullAway

### Théorie

#### Ce que vous savez déjà, ce qui change

PHPStan lit votre code **à côté** de PHP, et signale ce qui plantera à l'exécution.

En Java, le compilateur fait déjà une bonne partie de ce travail : types, méthodes inexistantes, `switch` non exhaustif. Il reste des erreurs que `javac` laisse passer.

**Error Prone**, de Google, comble ce manque :

- c'est un **plugin du compilateur** : il tourne pendant `javac`, pas à côté ;
- ses erreurs sont des **erreurs de compilation** : le build s'arrête, comme pour une faute de syntaxe ;
- chaque règle est un **bug pattern** documenté, avec une page web et souvent un correctif proposé.

**NullAway**, d'Uber, est un module d'Error Prone. Il vérifie le contrat JSpecify posé à l'étape 1.2 : promesse tenue, `@NullMarked` devient vérifié à la compilation.

Différence avec PHPStan : pas de niveaux 0 à 9. Chaque règle a une **sévérité**, `ERROR` ou `WARNING`.

Piège de l'analogie : ces vérifications tournent dans `./mvnw compile`, **pas dans VSCode**. VSCode compile avec son propre compilateur (Eclipse, étape 0.2), sans Error Prone.

#### Pourquoi un fichier `.mvn/jvm.config`

Error Prone utilise des classes internes de `javac`. Depuis Java 9, le JDK est découpé en **modules**, qui cachent leurs classes internes.

Le fichier `.mvn/jvm.config` passe des options à la JVM qui exécute Maven. Les options `--add-exports` et `--add-opens` y rouvrent l'accès à ces classes.

C'est une incantation documentée par Error Prone, à recopier telle quelle. Le système de modules est hors du périmètre du cours.

#### Exemple 3 : Error Prone

Fichier `.mvn/jvm.config` du projet `shop`, recopié de la [page d'installation](https://errorprone.info/docs/installation) :

```text
--add-exports jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.model=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED
--add-exports jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED
--add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED
--add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED
```

Le compilateur est actif d'office : sa configuration va dans `<pluginManagement>`, à côté de sa version.

```xml
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>3.16.0</version>
                    <configuration>
                        <compilerArgs>
                            <arg>-XDcompilePolicy=simple</arg>
                            <arg>--should-stop=ifError=FLOW</arg>
                            <arg>-Xplugin:ErrorProne</arg>
                        </compilerArgs>
                        <annotationProcessorPaths>
                            <path>
                                <groupId>com.google.errorprone</groupId>
                                <artifactId>error_prone_core</artifactId>
                                <version>2.53.0</version>
                            </path>
                        </annotationProcessorPaths>
                    </configuration>
                </plugin>
```

Lecture ligne par ligne :

1. `<compilerArgs>`

   Des options passées à `javac`, une par `<arg>`.

2. `-XDcompilePolicy=simple` et `--should-stop=ifError=FLOW`

   Deux options internes de `javac` exigées par Error Prone, pour qu'il voie tout le code, même en présence d'erreurs. À recopier.

3. `-Xplugin:ErrorProne`

   Active le plugin. Les options d'Error Prone se placeront **dans le même `<arg>`**, après un espace.

4. `<annotationProcessorPaths>`

   Où `javac` trouve Error Prone. Ce n'est **pas** une dépendance du projet : rien n'arrive dans le `.jar`, rien n'est visible depuis votre code.

   En Composer, l'équivalent serait un outil installé à part, hors de `require` et de `require-dev`.

Compilation de `shop` :

```console
$ ./mvnw -ntp verify
[WARNING] /tmp/claude-2.1/shop/src/main/java/com/example/shop/price/Price.java:[24,36] [StringSplitter] String.split(String) has surprising behavior
...
[INFO] BUILD SUCCESS
```

Un avertissement, et le build passe : une règle de sévérité `WARNING` ne bloque rien. On y revient plus bas.

Retirons maintenant `amount =` devant `amount.setScale(2);` dans le constructeur de `Price` :

```console
$ ./mvnw -ntp -q compile
[ERROR] COMPILATION ERROR :
[ERROR] /tmp/claude-2.1/shop/src/main/java/com/example/shop/price/Price.java:[20,24] [ReturnValueIgnored] Return value of 'setScale' must be used
    (see https://errorprone.info/bugpattern/ReturnValueIgnored)
  Did you mean 'amount = amount.setScale(2);' or to remove this line?
```

Lecture :

1. `[20,24]` : ligne 20, colonne 24 ;
2. `[ReturnValueIgnored]` : le nom du bug pattern ;
3. le lien explique la règle ;
4. `Did you mean` : le correctif proposé.

`BigDecimal` est immuable (étape 1.2) : `setScale` renvoie un **nouveau** `BigDecimal`. Sans affectation, la ligne ne fait rien. `javac` l'accepte, Error Prone non.

#### Exemple 4 : le comportement surprenant de `split`

Avant de lire les résultats, **prédisez** chaque ligne.

```console
jshell> "12.50 EUR ".split(" ")
$1 ==> String[2] { "12.50", "EUR" }

jshell> "12.50 EUR ".split(" ", -1)
$2 ==> String[3] { "12.50", "EUR", "" }

jshell> " ".split(" ")
$3 ==> String[0] {  }

jshell> " ".split(" ", -1)
$4 ==> String[2] { "", "" }

jshell> "".split(" ")
$5 ==> String[1] { "" }
```

À retenir :

- `split(" ")` **supprime les chaînes vides en fin de tableau**. `"12.50 EUR "`, avec un espace final, donne deux parties : `Price.parse` l'accepte sans le dire ;
- avec une limite négative, `split(" ", -1)` garde tout : trois parties, le format est refusé ;
- `explode(' ', ...)` en PHP garde toujours les parties vides : c'est le comportement de `-1`.

C'est ce que signale `StringSplitter`. Son correctif proposé utilise la bibliothèque Guava, absente du projet ; la page du bug pattern recommande aussi une limite explicite, `-1`.

Changer la limite change le comportement pour certaines entrées (`" "` ci-dessus). Les tests disent si ce changement est acceptable.

#### Exemple 5 : NullAway

Deux ajouts à la configuration du compilateur de l'exemple 3 :

```xml
                            <arg>-Xplugin:ErrorProne -Xep:NullAway:ERROR -XepOpt:NullAway:OnlyNullMarked=true</arg>
```

```xml
                            <path>
                                <groupId>com.uber.nullaway</groupId>
                                <artifactId>nullaway</artifactId>
                                <version>0.14.4</version>
                            </path>
```

Lecture :

1. tout reste dans **un seul** `<arg>`, séparé par des espaces : ce sont des options du plugin, pas de `javac` ;
2. `-Xep:NullAway:ERROR` : la sévérité de NullAway passe de `WARNING` (par défaut) à `ERROR` ;
3. `-XepOpt:NullAway:OnlyNullMarked=true` : NullAway ne vérifie que les packages `@NullMarked`. C'est notre cas : `package-info.java` de l'étape 1.2 ;
4. le second `<path>` ajoute NullAway à côté d'Error Prone.

Erreur volontaire 1, dans `Currency.fromCode` : `return null;` au lieu du `throw`.

```console
$ ./mvnw -ntp -q compile
[ERROR] /tmp/claude-2.1/shop/src/main/java/com/example/shop/price/Currency.java:[24,9] [NullAway] returning @Nullable expression from method with @NonNull return type
    (see http://t.uber.com/nullaway )
```

Le type de retour `Currency` est non nul par défaut (`@NullMarked`) : renvoyer `null` viole le contrat.

Erreur volontaire 2, un record avec un composant `@Nullable` utilisé sans contrôle :

```java
public record Label(String name, @Nullable String brand) {

    public String shout() {
        return brand.toUpperCase();
    }
}
```

```console
[ERROR] /tmp/claude-2.1/shop/src/main/java/com/example/shop/price/Label.java:[8,21] [NullAway] dereferenced expression 'brand' is @Nullable
```

"Dereferenced" : on appelle une méthode sur `brand`, qui peut être `null`. C'est une `NullPointerException` trouvée à la compilation. Un `if (brand == null)` avant l'appel suffit à NullAway.

Une fois le `if` ajouté, Error Prone signale autre chose sur la même ligne :

```console
[WARNING] /tmp/claude-2.1/shop/src/main/java/com/example/shop/price/Label.java:[11,33] [StringCaseLocaleUsage] Specify a `Locale` when calling `String#to{Lower,Upper}Case`. (Note: there are multiple suggested fixes; the third may be most appropriate if you're dealing with ASCII Strings.)
    (see https://errorprone.info/bugpattern/StringCaseLocaleUsage)
  Did you mean 'return brand.toUpperCase(Locale.ROOT);' or 'return brand.toUpperCase(Locale.getDefault());' or 'return Ascii.toUpperCase(brand);'?
```

Le piège de `Locale.ROOT` de l'étape 1.3, détecté sans relecture.

Les tests aussi sont vérifiés : `PriceTest` est dans le package `com.example.shop.price`, déclaré `@NullMarked`.

```console
[ERROR] /tmp/claude-2.1/shop/src/test/java/com/example/shop/price/PriceTest.java:[29,44] [NullAway] passing @Nullable parameter 'null' where @NonNull is required
```

C'est `rejectsNullAmount` (étape 1.2), qui passe `null` **exprès**, pour vérifier le `requireNonNull` du constructeur. Ici, NullAway a tort : on le dit, sur ce test seulement.

```java
    @Test
    @SuppressWarnings("NullAway")
    void rejectsNullAmount() {
        assertThatThrownBy(() -> new Price(null, Currency.EURO))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("amount");
    }
```

- `@SuppressWarnings("NullAway")` : désactive NullAway pour cette méthode seule ;
- l'équivalent PHPStan : `@phpstan-ignore argument.type` sur la ligne ;
- jamais sur toute une classe : les autres tests resteraient sans contrôle.

#### Les avertissements deviennent des erreurs

Un avertissement qui ne bloque rien s'accumule : on finit par ne plus les lire. Comme une baseline PHPStan qui grossit.

Le compilateur a une option pour les traiter comme des erreurs (`-Werror` de `javac`). Dans `maven-compiler-plugin`, à côté de `<compilerArgs>` :

```xml
                        <failOnWarning>true</failOnWarning>
```

Sur `shop`, avant d'avoir traité `StringSplitter` :

```console
$ ./mvnw -ntp verify
[WARNING] COMPILATION WARNING :
[WARNING] /tmp/claude-2.1/shop/src/main/java/com/example/shop/price/Price.java:[24,36] [StringSplitter] String.split(String) has surprising behavior
[ERROR] COMPILATION ERROR :
[ERROR] warnings found and -Werror specified
[INFO] BUILD FAILURE
```

Après passage à `split(" ", -1)` et ajout du `@SuppressWarnings` : `BUILD SUCCESS`, 10 tests.

### Pièges

- **`-Xplugin:ErrorProne` en double** (un `<arg>` avec options, un autre sans) : `plug-in not found: ErrorProne`. Le plugin est bien trouvé, c'est le doublon qui fait échouer `javac`. Un seul `-Xplugin:ErrorProne` dans tout le bloc.
- **Options d'Error Prone dans des `<arg>` séparés** : `-Xep:...` seul dans un `<arg>` est passé à `javac`, qui répond `error: invalid flag: -Xep:NullAway:ERROR`. Tout dans l'`<arg>` de `-Xplugin:ErrorProne`.
- **`.mvn/jvm.config` oublié** : `An exception has occurred in the compiler`, puis `IllegalAccessError: ... module jdk.compiler does not export com.sun.tools.javac.api`. Ce n'est pas un bug de `javac`, malgré le message.
- **"Ça compile dans VSCode"** : VSCode n'exécute ni Error Prone ni NullAway. Seul `./mvnw compile` fait foi.
- **Commande lancée avant l'enregistrement** : Maven lit le disque, pas l'éditeur. Un point blanc sur l'onglet signale un fichier non enregistré.
- **`@SuppressWarnings` trop large** : sur une classe ou sans nom de règle, il masque aussi les vraies erreurs.
- **Correctif proposé appliqué sans réfléchir** : `Did you mean` est une piste. Pour `StringSplitter`, il ajoute une dépendance et change le comportement.

### Vérifiez votre compréhension

1. Pourquoi Error Prone ne figure-t-il pas dans `<dependencies>` ?
2. Que vérifie NullAway que `javac` ne vérifie pas, et dans quels packages ?
3. Pourquoi `"12.50 EUR ".split(" ")` donne-t-il deux parties ?

---

### Pratique

#### B1. Error Prone

Durée : 20 min. Modèle : exemple 3.

1. Créez `.mvn/jvm.config` (le dossier `.mvn/` existe déjà, pour le Wrapper).
2. Configurez `maven-compiler-plugin` dans `<pluginManagement>`.
3. Lancez :

   ```bash
   ./mvnw -ntp clean verify
   ```

**Résultat attendu** : `BUILD SUCCESS`, avec un avertissement `[StringSplitter]` sur `Quantity.java`. Gardez-le pour B4.

#### B2. Une erreur volontaire sur `Quantity`

Durée : 10 min. Modèle : exemple 3, seconde sortie.

1. Dans le constructeur compact de `Quantity`, retirez l'affectation devant `stripTrailingZeros()`.
2. Prédisez le message, puis lancez `./mvnw -ntp -q compile`.
3. Remettez l'affectation.

**Résultat attendu** : une erreur `[ReturnValueIgnored]`, puis `BUILD SUCCESS` une fois l'affectation remise.

Lien avec l'étape 1.2 : sans cette ligne, la normalisation n'a pas lieu, et `1.0` n'est plus égal à `1.00`. Le test le détectait à l'exécution ; Error Prone le détecte à la compilation.

Committez :

```bash
git add .mvn/jvm.config pom.xml
git commit -m "build: add Error Prone"
```

> **Point d'arrêt** : Error Prone tourne. NullAway reste à brancher.

#### B3. NullAway

Durée : 25 min. Modèle : exemple 5.

1. Ajoutez NullAway : options dans l'`<arg>`, second `<path>`.
2. Lancez `./mvnw -ntp clean verify`.

**Résultat attendu** : `BUILD FAILURE` sur des tests qui passent `null` exprès.

Pour chaque erreur :

1. vérifiez que le test passe bien `null` exprès, pour tester un `requireNonNull` ;
2. si oui, ajoutez `@SuppressWarnings("NullAway")` sur cette méthode de test seulement ;
3. si non, c'est une vraie erreur : corrigez le code.

**Résultat attendu** : `BUILD SUCCESS`.

Puis une erreur volontaire, dans le code de production : dans `Ingredient.toString`, appelez une méthode sur `quantity` sans tester qu'elle existe. Lisez l'erreur, puis annulez.

Committez :

```bash
git add pom.xml src
git commit -m "build: add NullAway"
```

#### B4. Les avertissements deviennent des erreurs

Durée : 30 min. Modèle : section "Les avertissements deviennent des erreurs", exemple 4.

1. Ajoutez `<failOnWarning>true</failOnWarning>` au compilateur.
2. Lancez `./mvnw -ntp verify`.

**Résultat attendu** : `BUILD FAILURE`, `warnings found and -Werror specified`, à cause de `StringSplitter` dans `Quantity.parse`.

Traitez l'avertissement sans ajouter de dépendance :

1. passez `split` avec une limite `-1` ;
2. lancez les tests et lisez ce qui casse ;
3. adaptez `parse` pour que **tous les tests existants** passent, messages compris ;
4. décidez si `"250 g "` (espace final) doit être accepté ou refusé, et ajoutez le cas au test des formats invalides ou valides.

Ce qui change par rapport au modèle : `Price` n'avait pas de test sur une entrée vide ; `Quantity` en a, et c'est lui qui guide la correction.

**Résultat attendu** : `BUILD SUCCESS` avec `./mvnw -ntp clean verify`, sans aucun `[WARNING]` de compilation.

> **Point d'arrêt** : la chaîne qualité est complète. Il reste la PR.

#### B5. Committer et ouvrir la PR

Durée : 15 min.

```bash
./mvnw -ntp spotless:apply
./mvnw -ntp clean verify
git add pom.xml src
git commit -m "<type>: <message>"
git push -u origin ch02/01-quality
gh pr create
```

Le type du dernier commit dépend de votre décision de B4 :

- le comportement de `parse` change (une entrée acceptée est désormais refusée) : `fix:` ;
- il ne change pas : `refactor:`.

**Résultat attendu** : `BUILD SUCCESS`, puis la PR ouverte.

Pour la description, les points "à consolider" :

- relisez `git log origin/main..HEAD` : le titre et le "Quoi" couvrent tous les commits ;
- "Comment c'est testé" donne les erreurs volontaires et leur résultat attendu ;
- un titre court.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit la PR ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [x] `./mvnw clean verify` passe depuis un clone propre, sans avertissement de compilation.
- [x] Un fichier mal indenté fait échouer `./mvnw verify` ; `./mvnw spotless:apply` le corrige.
- [x] Un `return null;` dans une méthode d'un package `@NullMarked` fait échouer la compilation.
- [x] Chaque `@SuppressWarnings("NullAway")` porte sur un seul test, qui passe `null` exprès.
- [x] Le comportement de `Quantity.parse` sur une entrée vide est inchangé, et l'espace final est testé.

Questions "expliquez" :

1. Comment `./mvnw verify` sait-il qu'il doit lancer Enforcer et Spotless, et à quel moment ?
2. Pourquoi désactiver le formatage à l'enregistrement de VSCode pour Java ?
3. Pourquoi une erreur Error Prone est-elle plus utile qu'un test qui échoue, et pourquoi ne remplace-t-elle pas le test ?

---

## Documentation

- Cycle de vie et plugins (Maven) : https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html
- Maven Enforcer, règles intégrées : https://maven.apache.org/enforcer/enforcer-rules/index.html
- Intervalles de versions (Maven) : https://maven.apache.org/enforcer/enforcer-rules/versionRanges.html
- Spotless pour Maven : https://github.com/diffplug/spotless/tree/main/plugin-maven
- palantir-java-format : https://github.com/palantir/palantir-java-format
- Error Prone, installation : https://errorprone.info/docs/installation
- Error Prone, liste des bug patterns : https://errorprone.info/bugpatterns
- `StringSplitter` : https://errorprone.info/bugpattern/StringSplitter
- NullAway, configuration : https://github.com/uber/NullAway/wiki/Configuration
- `String.split(String, int)` : https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/lang/String.html#split(java.lang.String,int)

## Pour aller plus loin (facultatif)

- `-XepOpt:NullAway:JSpecifyMode=true` : vérifications plus complètes des génériques (`List<@Nullable String>`).
- Spotless peut aussi formater le `pom.xml` (`<pom><sortPom/></pom>`) et le Markdown.
- Les modules Java (`module-info.java`), qui expliquent `.mvn/jvm.config` : https://dev.java/learn/modules/
