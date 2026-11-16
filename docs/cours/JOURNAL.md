# Journal du cours

[Plan du cours](PLAN.md) > Journal

Claude complète ce journal en fin de session. L'entrée la plus récente est en haut.

## Reprise

- **Étape en cours** : 0.2 (VSCode pour Java), pas encore rédigée.
- **Prochaine action** : demander "On attaque l'étape 0.2" ; Claude rédige `etape-0.2.md`.
- **Questions en attente** : aucune.

---

## 2026-11-16 - Étape 0.1 validée

- **Fait** : JDK 25 via SDKMAN, httpie, Docker vérifié ; `HelloWorld.java`, `Hello.java`, `Weather.java` (forme compacte) dans `sandbox/0.1/` ; prédictions jshell.
- **Initiatives** : message d'erreur sur `System.err` quand l'argument manque.
- **Appris** : `==` compare les références, `equals` le contenu ; `BigDecimal.equals` tient compte de l'échelle (`1.0` différent de `1.00`), `compareTo` non.
- **Flou** : pourquoi `java Weather.class` échoue (nom de classe interprété comme package `Weather` + classe `class`) ; `compareTo` renvoie un signe (-1, 0, 1, comme `<=>` en PHP), pas une différence.
- **Pour aller plus loin** : code de sortie non nul en cas d'erreur (`System.exit`).
- **Cours** : méthode pédagogique définie (théorie puis pratique, accompagnement dégressif, bilans, rituels, mise en forme) ; plan et chapitre 0 restructurés. Étape 0.1 jugée "OK à 75 %", à peaufiner à l'usage.

## 2026-11-16 - Mise en place

- Création du dépôt, du plan et de `CLAUDE.md`.
