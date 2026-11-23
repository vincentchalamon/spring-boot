# Chapitre 2 - Qualité et CI

[Plan du cours](../PLAN.md) > Chapitre 2

## En bref

- **Objectif** : confier à l'outillage ce qu'un humain vérifie mal : le formatage, les erreurs courantes, la qualité des tests, et la vérification de chaque PR.
- **Apport au projet** : un `./mvnw verify` qui échoue sur un code mal formaté, une erreur détectable ou des tests trop faibles ; une CI et une revue automatique sur chaque PR.
- **Phase** : guidée. Chaque exercice a un modèle complet, sur le projet `shop`, à adapter.
- **Durée indicative** : un jour et demi d'effort.

## Fil rouge : `./mvnw verify`

Une seule commande, la même en local et en CI. Chaque étape lui ajoute une vérification :

1. étape 2.1 : version de Java, formatage, Error Prone, NullAway ;
2. étape 2.2 : seuil de couverture (JaCoCo), tests de mutation (PIT) ;
3. étape 2.3 : la CI lance `./mvnw verify` sur chaque PR, et `main` exige qu'elle passe ;
4. étape 2.4 : une revue de Claude s'ajoute aux vérifications de la CI.

## Étapes

1. [Étape 2.1 - Formatage et analyse statique](etape-2.1.md)
2. Étape 2.2 - Couverture et tests de mutation
3. Étape 2.3 - CI GitHub Actions et Dependabot
4. Étape 2.4 - Revue de code par Claude

Puis le **bilan 1** (record `Step`), qui clôt la phase guidée.

## Mémo

- [Maven](../memo-maven.md) : section "Plugins et phases", ajoutée avec ce chapitre ;
- [Java pour PHP](../memo-java-pour-php.md) ;
- [tests](../memo-tests.md).
