# Étape 2.2 - Couverture et tests de mutation

[Chapitre 2](README.md) > Étape 2.2

## En bref

- **Objectif** : mesurer la qualité des tests, pas seulement leur présence.
- **Apport au projet** : `./mvnw verify` échoue si la couverture (JaCoCo) ou le score de mutation (PIT) passe sous un seuil. Les tests de `Quantity.parse` sont complétés là où PIT montre un trou.
- **Durée indicative** : une demi-journée (environ 3 h), en deux cycles. Vous pouvez vous arrêter à chaque "point d'arrêt".
- **Branche** : `ch02/02-coverage-mutation`.

Avancement :

- [ ] A1. Créer la branche
- [ ] A2. JaCoCo : agent et rapport
- [ ] A3. Lire le rapport
- [ ] A4. Un seuil de couverture (avec une erreur volontaire)
- [ ] B1. PIT : premier lancement
- [ ] B2. Lire le rapport et tuer les mutants
- [ ] B3. Un cas de test retiré (erreur volontaire)
- [ ] B4. PIT dans le build
- [ ] B5. Committer et ouvrir la PR
- [ ] Validation en session

Astuce : `Ctrl+Shift+O` affiche le plan du document.

Mémos : [Maven](../memo-maven.md) (section "Plugins et phases"), [tests](../memo-tests.md).

---

## Cycle A - La couverture avec JaCoCo

### Théorie

#### Ce que vous savez déjà, ce qui change

En PHP, `phpunit --coverage-html` mesure la couverture, avec Xdebug ou PCOV. Le rapport colore chaque ligne : exécutée ou non par les tests.

**JaCoCo** fait la même chose pour Java.

Ce qui change :

- pas d'extension à installer : JaCoCo est un **agent Java**, une bibliothèque que la JVM charge au démarrage, et qui instrumente le bytecode des classes à leur chargement ;
- le goal `prepare-agent` ne lance rien lui-même : il prépare l'option `-javaagent:...` et la range dans la propriété Maven `argLine`, que Surefire lit pour démarrer la JVM des tests ;
- les tests écrivent leurs mesures dans `target/jacoco.exec` ; le goal `report` en tire un rapport HTML.

Trois goals, trois phases :

1. `prepare-agent` : `initialize`, avant la compilation ;
2. `report` : `verify` ;
3. `check` : `verify`, après `report`. Deux goals d'une même phase s'exécutent dans l'ordre de déclaration.

#### Lignes et branches

JaCoCo compte plusieurs choses. Deux suffisent :

- **ligne** : exécutée au moins une fois par les tests ;
- **branche** : chaque issue d'une condition. Un `if (a)` a deux branches : `a` vrai, `a` faux.

Une ligne peut être exécutée sans que toutes ses branches le soient. `if (a || b || c)` a six branches : vrai et faux pour chacune des trois conditions.

PHPUnit sait aussi mesurer les branches (`--path-coverage`), mais on s'en sert rarement. En Java, c'est le compteur le plus utile.

#### Exemple 1 : agent et rapport

Projet `shop` de l'étape 2.1. Dans `<plugins>`, après Spotless :

```xml
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>0.8.16</version>
                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
```

Lecture :

1. deux `<execution>` dans le même plugin : chacune attache un goal à sa phase par défaut ;
2. les `<id>` sont obligatoires dès qu'il y a plusieurs executions : ils doivent être distincts ;
3. aucune `<configuration>` : les valeurs par défaut conviennent.

Exécution :

```console
$ ./mvnw -ntp clean verify
[INFO] --- enforcer:3.7.0:enforce (enforce-versions) @ shop ---
[INFO] --- jacoco:0.8.16:prepare-agent (prepare-agent) @ shop ---
[INFO] argLine set to -javaagent:/home/vincent/.m2/repository/org/jacoco/org.jacoco.agent/0.8.16/org.jacoco.agent-0.8.16-runtime.jar=destfile=/tmp/claude-2.2/shop/target/jacoco.exec
...
[INFO] --- surefire:3.6.1:test (default-test) @ shop ---
...
[INFO] --- jacoco:0.8.16:report (report) @ shop ---
[INFO] Loading execution data file /tmp/claude-2.2/shop/target/jacoco.exec
[INFO] Analyzed bundle 'shop' with 3 classes
[INFO] BUILD SUCCESS
```

Lecture :

1. `argLine set to -javaagent:...` : la propriété préparée pour Surefire ;
2. `destfile=.../target/jacoco.exec` : le fichier où les tests écrivent leurs mesures ;
3. `Analyzed bundle 'shop' with 3 classes` : le rapport est prêt.

Le rapport : `target/site/jacoco/index.html`, à ouvrir dans un navigateur.

```bash
xdg-open target/site/jacoco/index.html
```

On y descend par package, puis par classe, jusqu'au code source :

- **vert** : ligne exécutée, toutes ses branches aussi ;
- **jaune** : ligne exécutée, mais pas toutes ses branches ;
- **rouge** : ligne jamais exécutée ;
- un **losange** dans la marge signale une condition. Son infobulle donne le détail : `1 of 2 branches missed.`

Dans `Price.java`, deux lignes jaunes, chacune suivie d'une ligne rouge :

- `if (parts.length != 2) {` : `1 of 2 branches missed.` Aucun test ne donne un texte mal formé ; le `throw` qui suit est rouge ;
- `if (currency != other.currency) {` : même chose, aucun test n'additionne deux devises différentes.

#### Exemple 2 : un seuil avec `check`

Troisième execution, après `report` :

```xml
                    <execution>
                        <id>check</id>
                        <goals>
                            <goal>check</goal>
                        </goals>
                        <configuration>
                            <rules>
                                <rule>
                                    <element>BUNDLE</element>
                                    <limits>
                                        <limit>
                                            <counter>LINE</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.90</minimum>
                                        </limit>
                                        <limit>
                                            <counter>BRANCH</counter>
                                            <value>COVEREDRATIO</value>
                                            <minimum>0.90</minimum>
                                        </limit>
                                    </limits>
                                </rule>
                            </rules>
                        </configuration>
                    </execution>
```

Lecture :

1. `<configuration>` dans l'`<execution>` : elle ne concerne que `check`. Personne ne lance `check` à la main, pas besoin de la monter au niveau du plugin ;
2. `<element>BUNDLE</element>` : la règle porte sur **tout le projet**. On pourrait l'appliquer par classe (`CLASS`), plus sévère ;
3. `<counter>` : ce qu'on compte, `LINE` ou `BRANCH` ;
4. `<value>COVEREDRATIO</value>` : la proportion couverte, entre `0` et `1` ;
5. `<minimum>0.90</minimum>` : 90 % au moins.

```console
$ ./mvnw -ntp verify
...
[INFO] --- jacoco:0.8.16:check (check) @ shop ---
[INFO] Loading execution data file /tmp/claude-2.2/shop/target/jacoco.exec
[INFO] Analyzed bundle 'shop' with 3 classes
[WARNING] Rule violated for bundle shop: branches covered ratio is 0.84, but expected minimum is 0.90
[INFO] BUILD FAILURE
[ERROR] Failed to execute goal org.jacoco:jacoco-maven-plugin:0.8.16:check (check) on project shop: Coverage checks have not been met. See log for details.
```

Les lignes passent (environ 92 %), pas les branches (84 %). Le détail est dans le `[WARNING]`, pas dans le `[ERROR]`.

Ajoutons deux tests à `PriceTest`, un par ligne jaune :

```java
    @Test
    void rejectsAdditionOfDifferentCurrencies() {
        assertThatThrownBy(() -> Price.parse("1 EUR").add(Price.parse("1 CHF")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsTextWithoutCurrency() {
        assertThatThrownBy(() -> Price.parse("12.50")).isInstanceOf(IllegalArgumentException.class);
    }
```

`./mvnw -ntp verify` : `BUILD SUCCESS`, 21 tests. `Price` est à 100 %.

#### Une ligne verte n'est pas une ligne vérifiée

Regardez la ligne `Objects.requireNonNull(currency, "currency");` de `Price` : elle est verte. Pourtant, aucun test ne passe une devise `null`.

La couverture dit **ce qui a été exécuté**, pas **ce qui a été vérifié**. Un test sans assertion couvre autant qu'un bon test.

Conséquences :

- une couverture basse est un signal fiable : du code n'est pas testé du tout ;
- une couverture haute ne prouve rien sur la qualité des tests. C'est le rôle du cycle B ;
- viser 100 % pousse à écrire des tests sans valeur. Le seuil sert de **plancher** contre les régressions, pas d'objectif.

### Pièges

- **`<argLine>` dans la configuration de Surefire** : il remplace celui de JaCoCo, et la couverture tombe à zéro sans erreur. Si vous en avez besoin un jour, écrivez `<argLine>@{argLine} ...</argLine>` pour garder celui de JaCoCo.
- **`./mvnw clean verify -DskipTests`** : pas de tests, donc pas de `jacoco.exec`. `report` et `check` sont sautés (`Skipping JaCoCo execution due to missing execution data file.`), sans échec.
- **`-DskipTests` sans `clean`** : l'ancien `target/jacoco.exec` est relu. Rapport et seuil portent sur les tests du build précédent, sans avertissement.

### Vérifiez votre compréhension

1. Comment Surefire reçoit-il l'agent de JaCoCo, alors que vous n'avez rien configuré dans Surefire ?
2. Combien de branches compte `if (a || b)` ?
3. Pourquoi une couverture de 100 % ne garantit-elle pas que les tests sont bons ?

---

### Pratique

#### A1. Créer la branche

Durée : 2 min.

```bash
cd ~/Sites/spring-boot
git switch main
git pull
git switch -c ch02/02-coverage-mutation
```

**Résultat attendu** : `git status` affiche `Sur la branche ch02/02-coverage-mutation`.

#### A2. JaCoCo : agent et rapport

Durée : 15 min. Modèle : exemple 1.

Ajoutez JaCoCo dans `<plugins>`, avec les executions `prepare-agent` et `report`, puis :

```bash
./mvnw -ntp clean verify
```

**Résultat attendu** : la ligne `argLine set to -javaagent:...`, puis `Analyzed bundle 'obi-wan-quinoa-api' with 5 classes` et `BUILD SUCCESS`.

#### A3. Lire le rapport

Durée : 20 min. Modèle : lecture du rapport, exemple 1.

```bash
xdg-open target/site/jacoco/index.html
```

Notez, sans rien corriger :

1. la couverture des lignes et des branches du projet (ligne "Total") ;
2. les deux classes à 0 %, et pourquoi aucun test ne les exécute ;
3. la ligne jaune de `Quantity`, et le texte de l'infobulle de son losange.

Question à vous poser pour le point 2 : ces classes ont-elles un comportement à tester ? Elles seront exécutées quand la recette s'en servira (chapitre 4).

Le point 3 sera traité au cycle B : gardez-le de côté.

#### A4. Un seuil de couverture (avec une erreur volontaire)

Durée : 20 min. Modèle : exemple 2.

1. Ajoutez l'execution `check`, avec deux limites (`LINE` et `BRANCH`) à `1.00`.
2. Lancez `./mvnw -ntp verify`.
3. Lisez les deux `[WARNING] Rule violated`.
4. Fixez chaque minimum juste sous la valeur actuelle : un plancher, pas un objectif.

**Résultat attendu** : d'abord `BUILD FAILURE` avec `lines covered ratio is 0.80` et `branches covered ratio is 0.96` ; puis `BUILD SUCCESS`.

Committez :

```bash
git add pom.xml
git commit -m "build: add JaCoCo coverage report and threshold"
```

> **Point d'arrêt** : la couverture est mesurée et protégée. Le cycle B mesure la qualité des tests.

---

## Cycle B - Les tests de mutation avec PIT

### Théorie

#### Ce que vous savez déjà, ce qui change

Si vous connaissez **Infection**, vous connaissez **PIT** (ou "pitest") : c'est le même principe.

1. PIT modifie un petit bout du code de production : il crée un **mutant**. Exemple : `<` devient `<=`.
2. Il relance les tests qui exécutent ce bout de code.
3. Si un test échoue, le mutant est **tué** (`KILLED`) : les tests ont vu la différence.
4. Si tous passent, le mutant **survit** (`SURVIVED`) : un bug de ce genre passerait inaperçu.

Correspondances avec Infection :

- MSI : **mutation score** (tués / tous les mutants) ;
- Covered Code MSI : **test strength** (tués / mutants exécutés par au moins un test) ;
- `NO_COVERAGE` : un mutant qu'aucun test n'exécute. JaCoCo montre la même ligne en rouge ;
- `--min-msi` : `mutationThreshold`.

Ce qui change : PIT modifie le **bytecode** en mémoire, pas le fichier source. Rien n'est écrit dans `src/`.

#### Les mutateurs

Un **mutateur** est un type de modification. Quelques-uns :

- `ConditionalsBoundary` : `<` devient `<=`, `>` devient `>=` ;
- `RemoveConditional` : une condition est remplacée par `true` ou `false` (`removed conditional - replaced equality check with true`) ;
- `BooleanTrueReturnVals` : une méthode qui renvoie un `boolean` renvoie toujours `true` ;
- `EmptyObjectReturnVals` : une méthode qui renvoie une `String` renvoie `""`.

PIT les range en groupes :

- `DEFAULTS` : le groupe par défaut ;
- `STRONGER` : `DEFAULTS` plus `RemoveConditional`, qui teste chaque condition d'un `||` ou d'un `&&` séparément ;
- `ALL` : tout, y compris des mutants **équivalents** (qui ne changent pas le comportement, donc impossibles à tuer). Du bruit.

Choix du cours : **`STRONGER`**. Sur le projet, `DEFAULTS` tue 100 % des mutants sans rien révéler ; `STRONGER` trouve un vrai trou.

#### Exemple 3 : premier lancement

PIT ne connaît pas JUnit 5 (ni 6) de lui-même : il lui faut un plugin, `pitest-junit5-plugin`, déclaré comme **dépendance du plugin**.

Dans `<plugins>`, après JaCoCo :

```xml
            <plugin>
                <groupId>org.pitest</groupId>
                <artifactId>pitest-maven</artifactId>
                <version>1.30.2</version>
                <dependencies>
                    <dependency>
                        <groupId>org.pitest</groupId>
                        <artifactId>pitest-junit5-plugin</artifactId>
                        <version>1.2.3</version>
                    </dependency>
                </dependencies>
                <configuration>
                    <mutators>
                        <mutator>STRONGER</mutator>
                    </mutators>
                </configuration>
            </plugin>
```

Lecture :

1. `<dependencies>` **dans** `<plugin>` : une dépendance du plugin, pas du projet. Comme `annotationProcessorPaths` à l'étape 2.1, rien n'arrive dans votre code ni dans le `.jar` ;
2. `pitest-junit5-plugin` 1.2.3 fonctionne avec JUnit 6 : il ajoute lui-même le lanceur JUnit de la bonne version (`Auto adding org.junit.platform:junit-platform-launcher:jar:6.1.3`) ;
3. `<configuration>` au niveau du plugin : on lance d'abord PIT à la main, comme `spotless:apply` ;
4. pas encore d'`<execution>` : PIT n'est pas dans le build.

PIT a besoin des classes compilées, tests compris :

```bash
./mvnw -ntp test-compile pitest:mutationCoverage
```

La sortie est longue (une cinquantaine de lignes `Found plugin`, puis le détail par mutateur). La fin :

```console
================================================================================
- Statistics
================================================================================
>> Line Coverage (for mutated classes only): 49/51 (96%)
>> 6 tests examined
>> Generated 35 mutations Killed 25 (71%)
>> Mutations with no coverage 1. Test strength 74%
>> Ran 73 tests (2.09 tests per mutation)
```

Lecture :

1. `Generated 35 mutations Killed 25 (71%)` : le mutation score ;
2. `Mutations with no coverage 1` : un mutant qu'aucun test n'exécute ;
3. `Test strength 74%` : 25 tués sur 34 exécutés ;
4. `Ran 73 tests` : PIT relance seulement les tests qui exécutent la ligne mutée, pas toute la suite.

Le rapport : `target/pit-reports/index.html`. Par classe, le code source est coloré : vert clair si les mutants de la ligne sont tués, rose sinon. Sous le code, chaque mutant est listé avec son numéro de ligne et son statut.

#### Exemple 4 : lire les survivants

Les 10 mutants non tués de `shop`, regroupés par cause :

1. **Bornes non testées**, dans `Price` :
   - `if (amount.signum() < 0)` : `changed conditional boundary`. Avec `<= 0`, zéro serait refusé. Aucun test ne vérifie qu'un prix de `0` est accepté ;
   - `if (amount.stripTrailingZeros().scale() > 2)` : même chose avec `>= 2`. Aucun test de `PriceTest` ne vérifie qu'exactement deux décimales sont acceptées.

2. **Règle non testée**, dans `Product` :
   - `if (name.isBlank())` : `replaced equality check with false`. On peut supprimer la règle "nom vide refusé" : aucun test ne s'en aperçoit.

3. **Cas non testé**, dans `Product`, quatre mutants pour une seule cause :
   - aucun test ne crée, **dans le test lui-même**, un produit avec une marque. Ni le `strip` de la marque, ni le `toString` avec parenthèses ne sont vérifiés. JaCoCo montrait déjà le `return` avec la marque en rouge (`NO_COVERAGE`).

4. **Données de test qui ne départagent rien**, dans `Product.namesCheaperThan`, trois mutants :
   - le filtre sur la devise peut renvoyer toujours `true` : le seul produit en CHF (3.90) est de toute façon plus cher que la limite (2 EUR), donc écarté par le filtre suivant ;
   - `< 0` peut devenir `<= 0` : aucun produit ne coûte exactement la limite.

Quatre tests tuent les sept premiers :

```java
    // PriceTest
    @Test
    void acceptsZero() {
        assertThat(Price.parse("0 EUR")).hasToString("0.00 EUR");
    }

    @Test
    void acceptsTwoDecimals() {
        assertThat(Price.parse("1.25 EUR")).hasToString("1.25 EUR");
    }
```

```java
    // ProductTest
    @Test
    void rejectsBlankName() {
        assertThatThrownBy(() -> new Product(" ", null, Price.parse("1.20 EUR")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void printsBrandBetweenParentheses() {
        Product coffee = new Product("Café", " Arabica & Co ", Price.parse("4.50 EUR"));

        assertThat(coffee).hasToString("Café (Arabica & Co) 4.50 EUR");
    }
```

Ce qui est nouveau :

- `acceptsZero` et `acceptsTwoDecimals` testent **la borne elle-même**, la valeur exacte où la règle bascule. C'est là que vivent les erreurs `<` / `<=` ;
- `printsBrandBetweenParentheses` tue quatre mutants d'un coup : marque non vide acceptée, marque normalisée, format du `toString`.

Résultat :

```console
>> Line Coverage (for mutated classes only): 51/51 (100%)
>> Generated 35 mutations Killed 32 (91%)
>> Mutations with no coverage 0. Test strength 91%
```

Les trois survivants restants sont ceux du point 4 : il faudrait un produit en CHF moins cher que la limite, et un produit au prix exact de la limite.

#### PIT dans le build

Comme JaCoCo, PIT peut faire échouer `verify` sous un seuil. Une `<execution>` et une option suffisent :

```xml
                <configuration>
                    <mutators>
                        <mutator>STRONGER</mutator>
                    </mutators>
                    <mutationThreshold>85</mutationThreshold>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>mutationCoverage</goal>
                        </goals>
                    </execution>
                </executions>
```

- `mutationCoverage` est attaché par défaut à `verify`, après les tests ;
- `mutationThreshold` : un pourcentage entier, de `0` à `100`.

Avec ce seuil, sur `shop` avant les quatre tests :

```console
[ERROR] Failed to execute goal org.pitest:pitest-maven:1.30.2:mutationCoverage (default) on project shop: Mutation score of 71 is below threshold of 85
```

Coût : PIT relance des tests pour chaque mutant. Sur `shop`, environ 4 secondes. Avec les tests Spring du chapitre 3, ce sera plus long : on restreindra alors PIT aux tests unitaires.

Pour un build local rapide, sans PIT :

```bash
./mvnw -ntp verify -DskipPitest
```

#### Surefire et Failsafe

Vous n'avez écrit jusqu'ici que des **tests unitaires** : rapides, sans base de données ni serveur. Surefire les lance à la phase `test`.

À partir du chapitre 4, des **tests d'intégration** démarreront un vrai PostgreSQL (Testcontainers). Maven a un plugin dédié : **Failsafe**.

- Surefire : phase `test`, classes `*Test` ; un échec arrête le build tout de suite ;
- Failsafe : phase `integration-test`, classes `*IT` ; un échec est signalé à la phase `verify`, après la phase `post-integration-test` qui sert à arrêter proprement ce qui a été démarré (base, serveur).

En PHPUnit, l'équivalent serait deux `testsuite` dans `phpunit.xml.dist`, lancées par deux commandes.

Pour l'instant, rien à configurer : on y reviendra quand le premier test `*IT` arrivera.

### Pièges

- **`./mvnw pitest:mutationCoverage` seul après un `clean`** : rien à muter, il faut compiler d'abord. `test-compile pitest:mutationCoverage`.
- **Une donnée construite dans une constante `static final` du test** : constaté sur `shop`, les produits de `CATALOG` ne tuent aucun mutant (le `0.95 EUR` aurait dû tuer celui de la borne des décimales). Pour tuer un mutant, construisez la valeur dans le test lui-même.
- **Survivant sans test possible** : un mutant peut être équivalent (aucun comportement observable ne change). Rare avec `STRONGER`, fréquent avec `ALL`. On le note, on ne le poursuit pas.
- **Viser 100 %** : comme pour la couverture, le seuil est un plancher. Un survivant se lit d'abord comme une question : "ce cas mérite-t-il un test ?"

### Vérifiez votre compréhension

1. Que signifie un mutant qui survit ?
2. Pourquoi une ligne 100 % couverte par JaCoCo peut-elle encore avoir des mutants survivants ?
3. Pourquoi tester la valeur exacte de la borne (`0`, deux décimales) plutôt qu'une valeur "au milieu" ?

---

### Pratique

#### B1. PIT : premier lancement

Durée : 15 min. Modèle : exemple 3.

1. Ajoutez PIT dans `<plugins>`, sans `<execution>` ni seuil.
2. Lancez :

   ```bash
   ./mvnw -ntp test-compile pitest:mutationCoverage
   ```

**Résultat attendu** : un bloc `Statistics` avec `Generated 32 mutations Killed 30 (94%)`.

#### B2. Lire le rapport et tuer les mutants

Durée : 30 min. Modèle : exemple 4.

```bash
xdg-open target/pit-reports/index.html
```

Deux mutants survivent, sur **la même ligne** de `Quantity` : la condition de format écrite à l'étape 2.1.

1. Lisez la description de chaque survivant.
2. Comparez avec A3 : JaCoCo ne signalait qu'**une** branche manquante sur cette ligne. PIT en trouve plus.
3. Trouvez les entrées de `parse` qui manquent au test des formats invalides. Ajoutez-les au `@ValueSource` existant.

Indice : une condition `a || b || c` n'est vraiment testée que si, pour chacune, un cas de test est refusé **par elle seule**. Pour chaque partie du `||`, cherchez une entrée qui ne soit refusée que par elle.

**Résultat attendu** : `Generated 32 mutations Killed 32 (100%)`, et dans le rapport JaCoCo, plus de ligne jaune sur `Quantity`.

```bash
git add src
git commit -m "test: cover each Quantity format rule"
```

> **Point d'arrêt** : tous les mutants sont tués.

#### B3. Un cas de test retiré (erreur volontaire)

Durée : 10 min. Modèle : exemple 4, point 1.

1. Dans `QuantityTest`, retirez le cas `"0 g, 0"` du test des valeurs non positives.
2. Prédisez : quel mutant va survivre, et avec quel mutateur ?
3. Lancez `./mvnw -ntp test-compile pitest:mutationCoverage`.
4. Remettez le cas.

**Résultat attendu** : un survivant `changed conditional boundary` sur la condition du signe dans le constructeur de `Quantity`.

#### B4. PIT dans le build

Durée : 15 min. Modèle : section "PIT dans le build".

1. Ajoutez l'`<execution>` et un `mutationThreshold`.
2. Choisissez le seuil comme en A4 : un plancher sous la valeur actuelle, en laissant une marge pour les mutants difficiles à venir.
3. Lancez `./mvnw -ntp clean verify`.

**Résultat attendu** : `BUILD SUCCESS`, avec la ligne `--- pitest:1.30.2:mutationCoverage (default)` après `jacoco:check`.

```bash
git add pom.xml
git commit -m "build: add PIT mutation testing with threshold"
```

#### B5. Committer et ouvrir la PR

Durée : 15 min.

```bash
./mvnw -ntp spotless:apply
./mvnw -ntp clean verify
git push -u origin ch02/02-coverage-mutation
gh pr create
```

**Résultat attendu** : `BUILD SUCCESS`, puis la PR ouverte.

Pour la description, appliquez la règle de la PR #20 :

- "Quoi" : une ligne par changement, dont les cas de test ajoutés et les seuils choisis ;
- "Pourquoi" : ce que JaCoCo et PIT apportent, et ce que PIT a trouvé ;
- "Comment c'est testé" : `clean verify`, puis les erreurs volontaires (seuils à `1.00`, cas `"0 g"` retiré) avec leur résultat attendu.

> **Point d'arrêt** : la pratique est terminée. Il reste la validation, en session.

---

## Validation

En session, Claude :

- relit la PR ;
- vérifie les points ci-dessous ;
- vous pose les questions "expliquez".

Points vérifiables :

- [ ] `./mvnw clean verify` produit les rapports `target/site/jacoco/index.html` et `target/pit-reports/index.html`.
- [ ] Les seuils de JaCoCo et de PIT sont des planchers justifiés, pas des valeurs au hasard.
- [ ] Le score de mutation de `Quantity` est de 100 %, grâce à des cas ajoutés au test paramétré existant.
- [ ] Retirer un cas de borne fait survivre un mutant (démontré en B3).

Questions "expliquez" :

1. Quelle différence entre "couvert" (JaCoCo) et "tué" (PIT) ?
2. Pourquoi JaCoCo ne voyait-il qu'une branche manquante sur la condition de `parse`, quand PIT trouvait deux mutants ?
3. Pourquoi ne pas écrire de test pour `RecipeType` et `Diet`, alors qu'ils font baisser la couverture ?

---

## Documentation

- JaCoCo, plugin Maven : https://www.jacoco.org/jacoco/trunk/doc/maven.html
- JaCoCo, goal `check` et règles : https://www.jacoco.org/jacoco/trunk/doc/check-mojo.html
- JaCoCo, compteurs (lignes, branches) : https://www.jacoco.org/jacoco/trunk/doc/counters.html
- PIT, démarrage avec Maven : https://pitest.org/quickstart/maven/
- PIT, mutateurs et groupes : https://pitest.org/quickstart/mutators/
- PIT, plugin JUnit 5 : https://github.com/pitest/pitest-junit5-plugin
- Failsafe : https://maven.apache.org/surefire/maven-failsafe-plugin/

## Pour aller plus loin (facultatif)

- `<withHistory>true</withHistory>` : PIT ne réanalyse que ce qui a changé depuis le dernier lancement.
- Règles JaCoCo par classe (`<element>CLASS</element>`) et exclusions (`<excludes>`).
- La couverture MC/DC (aviation, automobile) : chaque condition d'une décision doit, seule, en changer l'issue. C'est exactement ce que `RemoveConditional` vérifie.
