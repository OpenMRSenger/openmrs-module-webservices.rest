# Refactoring Patroon: Delegatie & Helper Methodes

Om code-duplicatie (geïdentificeerd door SonarQube) te verminderen, hebben we een **Delegatie**-patroon toegepast in de Order-handlers.

## Probleem

De klassen `DrugOrderSubclassHandler1_10` en `TestOrderSubclassHandler1_10` hadden bijna identieke implementaties van de methode `getActiveOrders`. Beide voerden de volgende stappen uit:

1. Uitlezen van request parameters (`careSetting`, `asOfDate`, `sort`).
2. Converteren van parameters naar OpenMRS objecten.
3. Aanroepen van `OrderUtil.getOrders`.
4. Toepassen van sortering en paging.

Dit leidde tot onderhoudsproblemen; een wijziging in de sorteer-logica zou op drie plaatsen (inclusief de hoofd-resource) doorgevoerd moeten worden. SonarQube markeerde dit als duplicatie (code smell).

## Overwogen alternatieven

Voordat we voor delegatie kozen, zijn drie alternatieven afgewogen:

- **Extract Superclass / Pull Up Method:** een nieuwe abstracte tussenklasse introduceren en `getActiveOrders` daarheen optillen. Afgewezen: beide handlers zitten al in de vaste OpenMRS-overervingshiërarchie (`DelegatingSubclassHandler`); een extra abstracte laag werkt tegen het framework in en voegt een kunstmatige klasse toe.
- **Template Method:** een skelet in een basisklasse met hook-methodes per subklasse. Afgewezen: de enige variatie tussen de handlers is de waarde van het `OrderType` ("Drug order" vs "Test order"). Een volledig template-method-patroon is zwaarder dan nodig (YAGNI) voor een enkele variërende parameter.
- **Strategy:** het ophalen van orders in een aparte strategie-klasse plaatsen. Afgewezen: dit voegt indirectie en extra klassen toe zonder meerwaarde, omdat er maar een variatiepunt is.

Delegatie naar de bestaande parent-resource gaf de meeste reductie van duplicatie tegen de laagste structurele kosten, en sluit aan op het GRASP-principe **Information Expert**: `OrderResource1_10` bezit de context en kennis om orders op te halen, dus die klasse hoort die verantwoordelijkheid te dragen.

## Oplossing: Delegatie naar de Parent Resource

We hebben de gedeelde logica gecentraliseerd in `OrderResource1_10.java`.

### 1. Helper Methode in de Parent

In `OrderResource1_10` is een nieuwe publieke methode toegevoegd:

```java
public PageableResult getOrders(Patient patient, OrderType orderType, RequestContext context)
```

Deze methode fungeert als "expert" voor het ophalen van orders op basis van een context.

### 2. Delegatie in de Subclass Handlers

In plaats van de logica te dupliceren, delegeren `DrugOrderSubclassHandler1_10` en `TestOrderSubclassHandler1_10` nu hun werk naar de parent:

```java
public PageableResult getActiveOrders(Patient patient, RequestContext context) {
    OrderType orderType = os.getOrderTypeByName("Drug order"); // Of "Test order"
    return orderResource.getOrders(patient, orderType, context);
}
```

Voor de refactoring bevatten beide handlers een eigen, vrijwel identieke kopie van de ophaal-logica; na de refactoring bevatten ze alleen nog de typebepaling en een delegatie-aanroep.

## Toegepaste patronen en principes

**Refactoringpatronen (Fowler):** Extract Method (de nieuwe `getOrders`-helper), het centraliseren van de logica in de parent (Pull Up / Move Method), en Replace Duplicated Code with Function Call.

**Ontwerpprincipes:**
- **DRY (Don't Repeat Yourself):** logica staat op een plek.
- **Single Source of Truth:** `OrderResource1_10` beheert hoe orders voor een patiënt worden ontsloten via de REST API.
- **SRP (Single Responsibility):** elke handler heeft nog maar een dunne verantwoordelijkheid (typebepaling + delegatie); de ophaal-verantwoordelijkheid ligt op een plek.
- **OCP (Open/Closed):** een nieuw order-type toevoegen vereist alleen een nieuwe, dunne handler die delegeert; de ophaal-logica (`getOrders`) blijft ongewijzigd. Open voor uitbreiding, gesloten voor wijziging.
- **GRASP Information Expert / Low Coupling:** de kennis ligt bij de klasse die de context bezit.

**ISO 25010 (onderhoudbaarheid):** de wijziging verbetert vooral *modificeerbaarheid* (sorteer-/paging-aanpassing op een plek), *herbruikbaarheid* (`getOrders` voor alle order-types), *analyseerbaarheid* (een bron om te begrijpen) en *testbaarheid* (de logica eenmaal grondig testbaar i.p.v. drie keer).

## Trade-offs en afweging

Delegatie naar de parent introduceert een bewuste koppeling: de subklasse-handlers zijn nu afhankelijk van `OrderResource1_10`. Dit is acceptabel omdat die afhankelijkheid al bestond binnen dezelfde resource-familie en de koppeling naar een stabiel, centraal punt loopt in plaats van naar gedupliceerde code. De winst in onderhoudbaarheid en consistentie weegt ruim op tegen de toegenomen koppeling. Bij een grotere variatie tussen order-types (meer dan alleen het `OrderType`) zou een Template Method of Strategy alsnog te verkiezen zijn; dat is nu niet aan de orde.

**Regressie:** de bestaande unit- en integratietests voor de Order-, DrugOrder- en TestOrder-resources slagen na de wijziging; het gedrag (filters, sortering, paging-output) is identiek aan de oude implementatie. De regel-coverage is na de refactoring gelijk gebleven op 69 procent, terwijl de gedupliceerde code is verwijderd. Hiermee is aangetoond dat de onderhoudbaarheid is verbeterd zonder regressie te introduceren.

