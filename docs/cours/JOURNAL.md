# Journal du cours

[Plan du cours](PLAN.md) > Journal

Claude complète ce journal en fin de session. L'entrée la plus récente est en haut.

## Reprise

- **Étape en cours** : 0.3 (conventions du dépôt), pas encore rédigée. L'étape 0.2 est validée ; sa branche `ch00/02-vscode` est committée, la PR sera ouverte en 0.3.
- **Prochaine action** : demander "On attaque l'étape 0.3" ; Claude rédige `etape-0.3.md`.
- **Questions en attente** : aucune.

---

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
