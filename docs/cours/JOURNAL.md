# Journal du cours

[Plan du cours](PLAN.md) > Journal

Claude complète ce journal en fin de session. L'entrée la plus récente est en haut.

## Reprise

- **Étape en cours** : 1.1 validée. Prochaine étape : 1.2 (enums et records), pas encore rédigée.
- **Prochaine action** : demander "On attaque la 1.2" ; Claude rédige `etape-1.2.md`. Vérifier avant que la PR #8 est mergée.
- **Questions en attente** : aucune.

---

## 2026-11-17 - Étape 1.1 validée

- **Fait** : Maven Wrapper (Maven 3.9.16), `pom.xml` (`io.github.vincentchalamon:obi-wan-quinoa-api`, JUnit 6.1.3 via BOM, AssertJ 3.27.7, compiler et Surefire épinglés), `Quantity` dans `io.github.vincentchalamon.quinoa.recipe`, `QuantityTest` (19 tests, dont des tests paramétrés en avance sur la 1.2). PR #8.
- **Initiatives** : `@ParameterizedTest` / `@ValueSource` utilisés dès la 1.1 ; `hasToString`.
- **Corrigé en revue** : règle de l'unité non testée (atteignable seulement par le constructeur) ; `assertThat(...)` au lieu de `assertThatThrownBy` (erreur de compilation) ; `hasCause` (type et message) au lieu de `hasCauseInstanceOf` ; message attendu recalculé dans le test ; noms de tests (`parsesText` qui testait `toString`) ; `isTrue()` sur `equals` ; type de commit `tests:` ; titre et description de PR. Test des `double` corrigé par Claude à la demande de Vincent (limite énoncée par `isNotEqualTo(0.3)`).
- **Incidents** : `pom.xml` vide car non enregistré (`input contained no data`, pris pour un problème de droits) ; VSCode gardait le dépôt en "projet invisible" : nettoyage du workspace Java puis "Reload Window". Les deux ajoutés aux pièges de l'étape.
- **Appris** : GAV, `-SNAPSHOT`, phases du cycle de vie, plugins vs dépendances, `pluginManagement`, scope `test`, BOM (liste de versions importée), médiation des versions "la plus proche gagne" (pas de résolution de contraintes comme Composer), Wrapper (`mvnw`), pas d'alias d'import en Java, JUnit / AssertJ face à PHPUnit, `@CsvSource` (vide = `null`, `''` = chaîne vide).
- **Flou** : le BOM (compris après une explication en trois temps) ; la correspondance par `groupId:artifactId` exact, pas par préfixe.
- **À consolider** : description de PR (incomplète deux PR de suite : "Quoi" partiel, "Comment c'est testé" sans résultat attendu) ; choix du type Conventional Commits (`test:` puis `chore:` avant `build:`) ; un test qui documente une limite doit l'énoncer.

## 2026-11-17 - Étape 1.0 validée

- **Fait** : `ReadQuantity.java` (cycle A) ; `Quantity.java` (classe `final`, champs `private final`, validation dans le constructeur, `parse`, `equals` / `hashCode` / `toString`) et `QuantityDemo.java` (cycle B), dans `sandbox/1.0/`. Toutes les entrées invalides donnent un message et le code `1`.
- **Initiatives** : messages distincts pour "argument manquant" et "trop d'arguments" ; `parse` enveloppe la `NumberFormatException` dans un message plus clair, en conservant la cause.
- **Corrigé en revue** : règle "exactement deux parties" écrite `> 2` (pile d'exception sur `"250 "`) ; `%.1f` arrondissait `0.25` en `0,3` (locale) ; `Double` au lieu de `double` ; `isNaN || isInfinite` au lieu de `isFinite` ; `trim().isEmpty()` puis `isEmpty() || isBlank()` au lieu de `isBlank()` ; cause d'exception perdue ; classe non `final`.
- **Appris** : `instanceof` simple vs avec variable (test + cast) ; constructeur vs méthode statique vs méthode d'instance ; méthode de fabrique (`parse`) ; `this` implicite, obligatoire seulement en cas de masquage ; `hashCode` par défaut lié à l'identité, collisions normales, contrat à sens unique ; buckets d'un `HashSet` ; `List` / `Map` / `Set` face au tableau PHP ; `Math.round` renvoie un `long` et arrondit les moitiés vers plus l'infini.
- **Flou** : correspondance des collections avec le tableau PHP (`HashSet` d'abord pris pour un tableau associatif, qui est une `Map`) ; méthode de fabrique d'abord vue comme l'outil par défaut, puis comme le moyen de l'injection de dépendances (c'est le constructeur classique) ; `final` sur la classe ne rend pas les champs `final`.
- **Javadoc** : `isFinite` trouvée par l'autocomplétion ; `Math.round` trouvée dans la Javadoc, type de retour lu dans "Returns".

## 2026-11-17 - Étape 0.3 validée, chapitre 0 terminé

- **Fait** : `.gitignore` (contenu de `.vscode/` sauf les deux fichiers partagés, `target/`, `sandbox/`, négations de `*.sql` et `*.http` contre le `.gitignore` global) ; `.sdkmanrc` committé ; modèle de PR ; merge en squash seul, titre de PR comme message ; ruleset `main` sans exception. Push direct refusé (`GH013`, règle citée : "merges must occur on the web"). PR #2 (0.3) et #3 (0.2) mergées.
- **Corrigé en revue** : négation sous `.vscode/` (dossier exclu) laissée en l'état après l'erreur volontaire ; `target/` et négations `*.sql` / `*.http` oubliées ; modèle de PR recopié de l'exemple (`composer ci`) ; description de PR : `gh check-ignore` au lieu de `git check-ignore`, "Quoi" incomplet, test limité au cas simple.
- **Incident** : B6 fait avant B5. La branche `ch00/02-vscode`, jamais poussée, a été supprimée par `git branch -D` ; commit retrouvé via le reflog et branche recréée.
- **Appris** : trois niveaux d'ignore (global, projet, `.git/info/exclude`) et leur priorité ; pas de ré-inclusion sous un dossier exclu ; modèle de PR lu sur la branche par défaut ; squash et titre de PR ; `check-ignore` n'affiche pas les fichiers suivis (`--no-index`).
- **Rappel 0.1** : `java Weather.class` presque juste (attribué à tort au mot-clé `class` : c'est la classe `class` du package `Weather`) ; valeur exacte de `compareTo` (`0`) non donnée.
- **Flou** : le squash "réécrit" les commits (non : il en crée un nouveau sur `main`, la branche reste intacte) ; reflog vu en passant seulement.
- **Cours** : relecture du chapitre 0 (PR #1 : B1 en une commande `gh repo edit`, `==` Java vs `===` PHP, quatre réglages en 0.2) ; documents du cours désormais via PR `docs/...`.

## 2026-11-16 - Étape 0.2 validée

- **Fait** : extensions installées ; `.vscode/extensions.json` (9 extensions), `.vscode/settings.json`, `.editorconfig` committés sur `ch00/02-vscode` (`c550ae1`) ; police et confort de lecture ; erreur vue pendant la frappe ; premier débogage avec point d'arrêt et Debug Console.
- **Corrigé en revue** : réglage de portée projet placé dans un bloc `[java]` (ignoré) ; réglage imbriqué `"editor": {...}` ; `files.insertFinalNewline` d'abord mis dans les réglages utilisateur au lieu du dépôt.
- **Appris** : réglages utilisateur (machine, préférences) vs workspace (projet, partagé) vs `.editorconfig` (tous éditeurs) ; le formateur JSON de VSCode ignore `.editorconfig` ; JDT LS compile en continu avec le compilateur d'Eclipse (messages différents de `javac`).
- **Flou** : qui garantit le formatage (le build et la CI au chapitre 2, pas l'IDE) ; comment l'indentation de `.editorconfig` arrive jusqu'au formateur Java (via les réglages de l'éditeur).
- **Cours** : REST Client remplacé par httpYac ; pièges ajoutés à l'étape 0.2. Côté Claude : committer les documents via un worktree, sans changer la branche de Vincent.

## 2026-11-16 - Étape 0.1 validée

- **Fait** : JDK 25 via SDKMAN, httpie, Docker vérifié ; `HelloWorld.java`, `Hello.java`, `Weather.java` (forme compacte) dans `sandbox/0.1/` ; prédictions jshell.
- **Initiatives** : message d'erreur sur `System.err` quand l'argument manque.
- **Appris** : `==` compare les références, `equals` le contenu ; `BigDecimal.equals` tient compte de l'échelle (`1.0` différent de `1.00`), `compareTo` non.
- **Flou** : pourquoi `java Weather.class` échoue (nom de classe interprété comme package `Weather` + classe `class`) ; `compareTo` renvoie un signe (-1, 0, 1, comme `<=>` en PHP), pas une différence.
- **Pour aller plus loin** : code de sortie non nul en cas d'erreur (`System.exit`).
- **Cours** : méthode pédagogique définie (théorie puis pratique, accompagnement dégressif, bilans, rituels, mise en forme) ; plan et chapitre 0 restructurés. Étape 0.1 jugée "OK à 75 %", à peaufiner à l'usage.

## 2026-11-16 - Mise en place

- Création du dépôt, du plan et de `CLAUDE.md`.
