# Chapitre 0 - Environnement et dépôt

[Plan du cours](../PLAN.md) > Chapitre 0

## En bref

- **Objectif** : une machine prête et un dépôt aux conventions posées, avant le premier code Java du projet.
- **Apport au projet** : la configuration VSCode partagée, le `.gitignore`, le modèle de PR, la protection de `main`.
- **Phase** : guidée.

## Étapes

1. [Étape 0.1 - JDK et outils en ligne de commande](etape-0.1.md)
2. Étape 0.2 - VSCode pour Java : rédigée au démarrage de l'étape.
3. Étape 0.3 - Conventions du dépôt : rédigée au démarrage de l'étape.

## Avant de lire : confort de lecture

Ces réglages sont personnels : ils vont dans vos réglages utilisateur VSCode, pas dans le dépôt.

Pour lire le cours dans VSCode :

1. ouvrez un fichier `.md` ;
2. `Ctrl+K` puis `V` ouvre l'aperçu à côté du texte source ;
3. `Ctrl+,` ouvre les réglages : cherchez `markdown.preview.fontSize` (essayez 16 à 18) et `markdown.preview.lineHeight` (essayez 1.8).

Autre option : lire le cours sur GitHub avec le mode lecture de Firefox. Il permet de régler la largeur du texte, l'interligne et l'espacement, et propose une lecture à voix haute.

L'étape 0.2 ira plus loin : police de l'éditeur, espacement des lettres, réduction des éléments visuels inutiles.

## Points déjà connus des étapes suivantes

Étape 0.2 :

- L'extension Java de VSCode a besoin d'un JDK. Elle le trouve via `JAVA_HOME` : relancez VSCode depuis un terminal où SDKMAN est actif.
- Réglages de lisibilité personnels : police avec des caractères bien distincts (`l`, `1`, `I`, `0`, `O`), espacement, interligne, correcteur orthographique pour les noms de variables.

Étape 0.3 :

- `.gitignore` : y déplacer la règle `sandbox/` (aujourd'hui dans `.git/info/exclude`).
- Committer `.sdkmanrc`.
- Modèle de PR orienté projet : quoi, pourquoi, comment c'est testé.
- Question à trancher : fusion des PR en "squash and merge", pour pouvoir committer souvent sur la branche (points de sauvegarde) tout en gardant un historique propre sur `main`.
