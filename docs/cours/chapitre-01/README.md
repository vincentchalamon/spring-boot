# Chapitre 1 - Java moderne, sans framework

[Plan du cours](../PLAN.md) > Chapitre 1

## En bref

- **Objectif** : écrire du Java moderne sans framework, puis le structurer en projet Maven testé.
- **Apport au projet** : le projet Maven et les types de valeur de la recette (type, régime, unité, quantité, ingrédient), réutilisés tels quels au chapitre 4.
- **Phase** : guidée. Chaque exercice a un modèle complet, sur un autre sujet, à adapter.
- **Durée indicative** : deux jours d'effort.

## Fil rouge : `Quantity`

La même valeur, "250 g", est réécrite à chaque étape avec un meilleur outil :

1. étape 1.0 : classe classique, valeur `double`, unité `String`, dans `sandbox/1.0/` ;
2. étape 1.1 : la même classe dans le projet Maven, avec ses premiers tests ;
3. étape 1.2 : `record` avec `BigDecimal` et enum `Unit` ;
4. étape 1.3 : composant de `Ingredient`.

Comparer les versions successives fait partie de l'apprentissage : gardez votre `sandbox/1.0/`.

## Étapes

1. [Étape 1.0 - Syntaxe de base](etape-1.0.md)
2. [Étape 1.1 - Projet Maven à la main](etape-1.1.md)
3. [Étape 1.2 - Enums et records](etape-1.2.md)
4. [Étape 1.3 - Ingrédients, collections et streams](etape-1.3.md)

## Mémo

La syntaxe vue dans ce chapitre est consignée dans les mémos :

- [Java pour PHP](../memo-java-pour-php.md) ;
- [Maven](../memo-maven.md) ;
- [tests](../memo-tests.md).
