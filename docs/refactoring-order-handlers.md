# Refactoring Patroon: Delegatie & Helper Methodes

Om code-duplicatie (geïdentificeerd door SonarQube) te verminderen, hebben we een **Delegatie** patroon toegepast in de Order-handlers.

## Probleem
De klassen `DrugOrderSubclassHandler1_10` en `TestOrderSubclassHandler1_10` hadden bijna identieke implementaties van de methode `getActiveOrders`. Beide voerden de volgende stappen uit:
1.  Uitlezen van request parameters (`careSetting`, `asOfDate`, `sort`).
2.  Converteren van parameters naar OpenMRS objecten.
3.  Aanroepen van `OrderUtil.getOrders`.
4.  Toepassen van sortering en paging.

Dit leidde tot onderhoudsproblemen; een wijziging in de sorteer-logica zou op drie plaatsen (inclusief de hoofd-resource) doorgevoerd moeten worden.

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

## Waarom dit patroon?

-   **DRY (Don't Repeat Yourself):** Logica staat op één plek.
-   **Onderhoudbaarheid:** Correcties in parameter-parsing of paging-logica hoeven slechts in `OrderResource1_10` aangepast te worden.
-   **Consistentie:** Alle order-types (Base, Drug, Test) gedragen zich nu exact hetzelfde wat betreft filters en sortering.
-   **Single Source of Truth:** `OrderResource1_10` beheert hoe orders voor een patiënt worden ontsloten via de REST API.
