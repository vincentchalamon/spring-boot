# Journal du cours

[Plan du cours](PLAN.md) > Journal

Claude complète ce journal en fin de session. L'entrée la plus récente est en haut.

## Reprise

- **Étape en cours** : 1.0 (syntaxe de base), rédigée.
- **Prochaine action** : cycle A de l'étape 1.0, sous-étape A1 (prédictions jshell).
- **Questions en attente** : aucune.

---

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
