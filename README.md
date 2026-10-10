# Analyseur statique de programmes Java

Outil en ligne de commande qui lit le code source d'un projet Java, en extrait la structure
(types, attributs, méthodes) et les appels de méthodes, calcule des métriques et construit
le graphe d'appel. Il repose sur Eclipse JDT Core.

Contexte : TP 1 « Analyse statique » du module HAI913I (Université de Montpellier, 2026–2027).

## Sommaire

1. [Projet](#1-projet)
2. [Environnement](#2-environnement)
3. [Construction](#3-construction)
4. [Exécution](#4-exécution)
5. [Validation](#5-validation)
6. [Dépannage](#6-dépannage)

## 1. Projet

### Ce que fait l'outil

- **Structure** : pour chaque type (classe, interface, énumération, record, y compris les types
  imbriqués) : nom qualifié, genre, paquetage, chaîne complète des superclasses, interfaces,
  attributs (nom, type, visibilité) et méthodes (nom, nombre de paramètres, constructeur ou non).
- **Appels** : pour chaque méthode, ses appels avec le type statique du receveur, la méthode cible
  résolue et la ligne.
- **Métriques** : 13 métriques (nombre de classes, de lignes, de méthodes, moyennes, classes et
  méthodes les plus grosses, etc.).
- **Graphe d'appel** : arcs pondérés entre méthodes du projet, et requête « qui appelle cette
  méthode, et qu'appelle-t-elle ? ».

### Entrée et sorties

- **Entrée** : le dossier d'un projet Java. La racine des sources est `src/main/java` si ce dossier
  existe, sinon `src`, sinon le dossier lui-même.
- **Sortie** : un rapport texte sur la sortie standard. Les erreurs de compilation du projet analysé
  et les messages d'erreur de l'outil vont sur la sortie d'erreur.

### Architecture

L'analyse se fait en chaîne : les fichiers sont transformés en arbres syntaxiques (AST) avec leurs
informations de types (bindings), des visiteurs en extraient des faits, puis les métriques, le graphe
et les rapports travaillent uniquement sur ces faits. Seuls les paquetages `parse` et `extract`
dépendent de JDT.

```text
src/main/java/hai913i/tp1/
├── Main.java        point d'entrée : lit les options, enchaîne les étapes, fixe le code de sortie
├── cli/             CommandLine (analyse des arguments), Options, UsageException
├── parse/           ProjectSources (fichiers .java), JdtParser (configuration d'ASTParser)
├── extract/         StructureVisitor, CallVisitor, FactsBuilder, FactsExtractor
├── model/           faits indépendants de JDT : ProjectFacts, TypeFact, FieldFact,
│                    MethodFact, CallFact, SourceFileFact
├── metrics/         les métriques
├── callgraph/       CallGraph et Arc : graphe d'appel et requêtes
└── report/          mise en forme texte : TextReport, MetricsReport, CallGraphReport
```

### Dépendances

- Eclipse JDT Core `org.eclipse.jdt:org.eclipse.jdt.core:3.46.0`, téléchargé par Maven avec ses
  dépendances Eclipse (aucun JAR à installer à la main) ;
- JUnit Jupiter 5.14.4, pour les tests.

### Approche

L'analyse utilise **Eclipse JDT Core** ; Spoon n'est pas utilisé. Tous les fichiers sont analysés en
un seul lot par `ASTParser.createASTs`, avec résolution des bindings (`setResolveBindings(true)`,
`setBindingsRecovery(true)`) et un environnement qui contient la racine des sources et le JDK courant
(`setEnvironment`). Le niveau de langage analysé est Java 17.

### Limites connues

- Les appels virtuels ne sont pas étendus : un appel via une interface vise la méthode de
  l'interface, pas ses implémentations. Les appels par réflexion et l'injection de dépendances ne
  sont pas vus.
- Les appels vers les constructeurs (`new`, `this(...)`, `super(...)`) ne produisent pas d'arcs.
- Les appels écrits hors de toute méthode (initialiseurs de champ) sont ignorés. Les appels écrits
  dans une lambda, une classe anonyme ou une classe locale sont attribués à la méthode englobante.
- Dans un fichier qui ne compile pas, l'instruction en erreur est absente de l'AST : ses appels ne
  sont pas comptés. Les autres fichiers sont analysés normalement.
- Les appels vers une bibliothèque absente du classpath sont comptés comme non résolus.
- Sortie texte uniquement (pas d'export DOT ni d'interface graphique).

## 2. Environnement

### Prérequis

- Git
- JDK 17 ou plus récent
- Maven 3.9 ou plus récent

`java` et `mvn` doivent être accessibles dans le `PATH` :

```text
java -version
mvn -v
```

### Versions utilisées pour construire et vérifier l'outil

| Élément          | Version |
|------------------|---------|
| JDK              | OpenJDK 21.0.12.1 (Eclipse Adoptium) |
| Maven            | Apache Maven 3.9.16 |
| Eclipse JDT Core | 3.46.0 |
| JUnit Jupiter    | 5.14.4 |
| Système          | Windows 11 Pro (version 10.0.26200) |
| Éditeur          | Visual Studio Code 1.141.0 (Windows x64) ; construction et exécution en ligne de commande, sans fonction de l'IDE |

## 3. Construction

Depuis une copie fraîche du dépôt, sans IDE :

```text
git clone https://github.com/aymenbekk/java-static-analyzer.git
cd java-static-analyzer
mvn clean package
```

`mvn clean package` compile le projet, exécute les tests et produit le JAR exécutable
`target/hai913i-tp1-analyzer.jar`, qui contient ses dépendances.

## 4. Exécution

```text
java -jar target/hai913i-tp1-analyzer.jar <projet> --action <action> [--x <N>] [--method "<id>"]
java -jar target/hai913i-tp1-analyzer.jar --help
```

| Action      | Produit | Option requise |
|-------------|---------|----------------|
| `structure` | types, attributs et méthodes | — |
| `calls`     | appels de chaque méthode : ligne, type du receveur, cible | — |
| `metrics`   | les 13 métriques | `--x <N>` |
| `graph`     | arcs pondérés du graphe d'appel et ses comptes | — |
| `query`     | méthodes appelées par une méthode, et méthodes qui l'appellent | `--method "<id>"` |
| `all`       | structure, appels, métriques et graphe | `--x <N>` |

- `--x <N>` : seuil de la métrique « classes ayant strictement plus de N méthodes » (entier positif
  ou nul). Obligatoire pour `metrics` et `all`, refusé pour les autres actions.
- `--method "<id>"` : la méthode à interroger. Trois formes sont acceptées :
  - `Catalog#add` : toutes les surcharges de `add` dans `Catalog` ;
  - `Catalog#add(Item)` : une surcharge précise, types des paramètres en nom simple ;
  - `library.service.Catalog#add(library.model.Item)` : l'identifiant complet.

Les chemins qui contiennent des espaces doivent être entre guillemets.

### Essai rapide sur le projet d'exemple inclus

Le dépôt contient un petit projet de deux fichiers, `src/test/resources/sample`
(`demo.App` appelle `demo.Greeter#greet`) :

```text
java -jar target/hai913i-tp1-analyzer.jar src/test/resources/sample --action graph
```

```text
Racine des sources     : ...\src\test\resources\sample\src
Unites de compilation  : 2
Erreurs de compilation : 0

Call Graph Report
=================
demo.App#main(java.lang.String[]) -> demo.Greeter#greet(java.lang.String) (1)

Number of nodes: 2
Number of arcs: 1
internal sites: 1
Number of external calls: 1
Number of unresolved calls: 0
```

```text
java -jar target/hai913i-tp1-analyzer.jar src/test/resources/sample --action query --method "Greeter#greet"
```

```text
Methode : demo.Greeter#greet(java.lang.String)
  Appelees :
    (aucune)
  Appelantes :
    demo.App#main(java.lang.String[]) (1)
```

L'outil s'applique de la même façon à n'importe quel projet Java.

### Exemples sur le projet de validation du cours

```text
java -jar target/hai913i-tp1-analyzer.jar "C:\chemin\vers\validation" --action metrics --x 5
```

```text
Racine des sources     : C:\chemin\vers\validation\src
Unites de compilation  : 14
Erreurs de compilation : 0

Q1  Nombre de types : 15
Q2  Somme des lignes : 493
Q3  Nombre de methodes : 59
Q4  Nombre de packages distincts : 4
Q5  Moyenne de methodes par type : 3.93
Q6  Moyenne de lignes par methode : 4.64
Q7  Moyenne de champs par type : 1.40
Q8  Top 10% des types par nombre de methodes :
  library.model.Item (10 methodes)
  library.model.Member (6 methodes)
  library.service.LoanService (6 methodes)
...
```

```text
java -jar target/hai913i-tp1-analyzer.jar "C:\chemin\vers\validation" --action query --method "Loanable#checkOut"
```

```text
Methode : library.model.Loanable#checkOut(library.model.Member)
  Appelees :
    (aucune)
  Appelantes :
    library.service.LoanService#borrow(library.model.Member, java.lang.String, int) (1)
```

Autres commandes :

```text
java -jar target/hai913i-tp1-analyzer.jar "C:\chemin\vers\validation" --action all --x 5
java -jar target/hai913i-tp1-analyzer.jar "C:\chemin\vers\validation" --action structure
java -jar target/hai913i-tp1-analyzer.jar "C:\chemin\vers\validation" --action query --method "Catalog#add(Item, Item)"
```

### Codes de sortie

| Code | Signification |
|------|---------------|
| 0    | succès, ou `--help` |
| 1    | erreur interne inattendue (message d'une ligne, sans pile d'exception) |
| 2    | arguments invalides : option ou action inconnue, X absent ou mal formé, option non autorisée pour l'action |
| 3    | projet inexploitable : dossier inexistant ou sans fichier `.java` |
| 4    | fichier source illisible |

## 5. Validation

Le projet de validation (`validation`, 14 fichiers, 4 paquetages) et les échantillons de robustesse
(`robustness`) sont fournis avec le sujet du TP, sur Moodle ; ils ne font pas partie de ce dépôt.

```text
java -jar target/hai913i-tp1-analyzer.jar "<chemin>\validation" --action all --x 5
```

Valeurs attendues sur le projet de validation, toutes obtenues :

| Étape | Valeurs attendues |
|-------|-------------------|
| Analyse | 14 unités de compilation, 0 erreur |
| Structure | 15 types (12 classes, 2 interfaces, 1 énumération), 59 méthodes dont 9 constructeurs, 21 attributs |
| Appels | 94 appels : 46 internes, 48 externes, 0 non résolu ; `loanable.checkOut` ligne 36 : receveur `library.model.Loanable` |
| Modèle | mêmes chiffres calculés depuis le modèle de faits ; deux exécutions donnent une sortie identique |
| Métriques | Q1–Q4 : 15, 493, 59, 4 · Q5–Q7 : 3.93, 4.64, 1.40 · Q8 : Item (10), Member et LoanService (6) · Q9–Q10 : Item et LoanService · Q11 : X = 5 → 3 classes, X = 4 → 6 classes · Q12 : Item : checkOut (7), LoanService : borrow (14) · Q13 : 5 paramètres, `Statistics#report` |
| Graphe | 59 nœuds, 44 arcs, 46 sites d'appel internes, 48 externes, 0 non résolu ; `Catalog#add(Item,Item) → Catalog#add(Item)` de poids 2 ; `TextUtils#repeat` récursif ; appelantes de `Loanable#checkOut(Member)` : `LoanService#borrow` seulement ; `Item#checkOut(Member)` : aucune |

Échantillons de robustesse :

| Commande | Comportement attendu |
|----------|----------------------|
| `... "<chemin>\robustness\missing-dep" --action calls` | `StringUtils.capitalize` non résolu, `formatter.decorate` résolu ; code 0 |
| `... "<chemin>\robustness\syntax-error" --action all --x 0` | erreurs de `Broken.java` signalées, `Helper.java` analysé ; code 0 |
| `... "<chemin>\robustness\no-java-files" --action graph` | `Erreur : aucun fichier Java trouve` ; code 3 |
| `... "<chemin>\inexistant" --action graph` | `Erreur : Pas un repertoire` ; code 3 |
| `... "<chemin>\validation" --action metrics --x abc` | `Erreur : X doit etre un entier positif ou nul` ; code 2 |

Sous PowerShell, le code de sortie de la dernière commande s'affiche avec `$LASTEXITCODE`
(sous cmd : `echo %ERRORLEVEL%`).

## 6. Dépannage

| Problème | Cause | Remède |
|----------|-------|--------|
| `Unresolved compilation problem` à l'exécution | classes compilées par l'IDE restées dans `target/` | `mvn clean package` |
| `UnsupportedClassVersionError` | JDK plus ancien que 17 | vérifier `java -version` et `JAVA_HOME` |
| `'mvn' n'est pas reconnu` | Maven absent du `PATH` | ajouter le dossier `bin` de Maven au `PATH` |
| Chemin introuvable alors qu'il existe | chemin contenant des espaces sans guillemets | entourer le chemin de guillemets |
| `--method` mal interprété sous PowerShell | parenthèses et virgules interprétées par le shell | mettre l'identifiant entre guillemets : `--method "Catalog#add(Item, Item)"` |
| Échec de `mvn exec:java -Dexec.args=...` sur un chemin avec espaces | découpage des arguments par Maven | utiliser `java -jar` |
| `Methode inconnue : ...` | aucune méthode ne correspond à la requête | vérifier l'orthographe ; essayer le nom court `Type#methode` |
| Beaucoup d'appels non résolus sur un autre projet | bibliothèques du projet absentes du classpath | comportement attendu : ces appels sont comptés comme non résolus |


