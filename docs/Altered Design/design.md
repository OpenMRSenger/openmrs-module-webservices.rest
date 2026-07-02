# Design Comparison: ConversionUtil

This document provides a UML representation, maintainability comparison, and test analysis of the original design of ConversionUtil (located at `omod-common/src/main/java/org/openmrs/module/webservices/rest/web/ConversionUtil.java`) with the new refactored structure.

---

## 1. Original Design (Monolithic)

In the original design, `convert(Object, Type)` is a single, complex method that handles all types of conversion internally, leading to high cognitive complexity and poor readability.

### Class Diagram (Original)
```mermaid
classDiagram
    class ConversionUtil {
        +DATE_FORMAT : String
        +convert(Object object, Type toType) Object
        +convertToRepresentation(Object object, Representation rep) Object
        +convertMap(Map~String, ?~ map, Class~?~ toClass) Object
        +getTypeVariableClass(Class~?~ instanceClass, TypeVariable~?~ typeVariable) Class~?~
        +getCustomRepresentationDescription(CustomRepresentation representation) DelegatingResourceDescription
    }
```

### Flowchart (Original)
```mermaid
flowchart TD
    Start([convert object, toType]) --> ResolveClass[Resolve target toClass]
    ResolveClass --> CheckCollection{Is Collection or Array?}
    
    CheckCollection -- Yes --> CheckArray{Is Array?}
    CheckArray -- Yes --> ConvertArrayLoop[Loop: convert element with reflection and recursive convert call]
    CheckArray -- No --> TypeCollection[Instantiate TreeSet/HashSet/ArrayList]
    TypeCollection --> CheckParam{Has Generic Type Info?}
    CheckParam -- Yes --> LoopGeneric[Loop: convert and add element]
    CheckParam -- No --> AddAll[addAll non-type-safe]
    
    CheckCollection -- No --> CheckAssign{Is Assignable?}
    CheckAssign -- Yes --> RetObj[Return object]
    CheckAssign -- No --> CoerceFloat[Coerce Float/Double]
    CoerceFloat --> CheckStr{Is String?}
    
    CheckStr -- Yes --> FindConverter{Has Converter?}
    FindConverter -- Yes --> GetById[converter.getByUniqueId]
    FindConverter -- No --> CheckDate{Is Date?}
    CheckDate -- Yes --> DateLoop[Loop supported Date Formats, try parsing via DateTime.parse]
    CheckDate -- No --> CheckLocale{Is Locale?}
    CheckLocale -- Yes --> LocaleUtility[LocaleUtility.fromSpecification]
    CheckLocale -- No --> CheckEnum{Is Enum?}
    CheckEnum -- Yes --> EnumValue[Enum.valueOf]
    CheckEnum -- No --> CheckClass{Is Class?}
    CheckClass -- Yes --> ContextLoad[Context.loadClass]
    CheckClass -- No --> ValueOf[Try static valueOf String reflection method]
    
    CheckStr -- No --> CheckMap{Is Map?}
    CheckMap -- Yes --> ConvertMap[convertMap]
    CheckMap -- No --> CoerceNum[Coerce Double/Integer/Boolean]
```

---

## 2. Altered Design (Modularized)

The refactored design decomposes the complex logic into distinct private helper methods, isolating different conversion concerns.

### Class Diagram (Altered)
```mermaid
classDiagram
    class ConversionUtil {
        +DATE_FORMAT : String
        +convert(Object object, Type toType) Object
        +convertToRepresentation(Object object, Representation rep) Object
        +convertMap(Map~String, ?~ map, Class~?~ toClass) Object
        +getTypeVariableClass(Class~?~ instanceClass, TypeVariable~?~ typeVariable) Class~?~
        +getCustomRepresentationDescription(CustomRepresentation representation) DelegatingResourceDescription
        -convertCollectionOrArray(Object object, Type toType, Class~?~ toClass) Object
        -convertToArray(Collection~?~ input, Class~?~ targetElementType) Object
        -convertToCollection(Collection~?~ input, Type toType, Class~?~ toClass) Collection
        -convertSingle(Object object, Type toType, Class~?~ toClass) Object
        -convertFromString(String string, Type toType, Class~?~ toClass) Object
        -convertToDate(String string) Date
    }
```

### Flowchart (Altered)
```mermaid
flowchart TD
    Start([convert object, toType]) --> ResolveClass[Resolve target toClass]
    ResolveClass --> CheckCollection{Is Collection or Array?}
    
    CheckCollection -- Yes --> CallColl[convertCollectionOrArray]
    CallColl --> CheckArray{Is Array?}
    CheckArray -- Yes --> CallArray[convertToArray]
    CheckArray -- No --> CallCollection[convertToCollection]
    
    CheckCollection -- No --> CallSingle[convertSingle]
    CallSingle --> CheckAssign{Is Assignable?}
    CheckAssign -- Yes --> RetObj[Return object]
    CheckAssign -- No --> CheckStr{Is String?}
    
    CheckStr -- Yes --> CallStr[convertFromString]
    CallStr --> CheckDate{Is Date?}
    CheckDate -- Yes --> CallDate[convertToDate]
    CheckDate -- No --> ValueOf[Try static valueOf/converter]
    
    CheckStr -- No --> CheckMap{Is Map?}
    CheckMap -- Yes --> CallMap[convertMap]
    CheckMap -- No --> CoerceNum[Coerce Numbers]
```

---

## 3. Cognitive Complexity Comparison

Cognitive Complexity measures how difficult a method is to understand by looking at nesting, control flow changes, and catch blocks.

### A. Before Refactoring (Complexity: 36)
The original `convert` method is monolithic and deeply nested:
* Base checks (`null` check, type resolution): **+2**
* Collection / Array conversion block: **+11** (Nesting level 3 for element loops, concrete class checks).
* Primitive and Type coercion checks: **+5**
* String-to-type parsing block: **+16** (Nesting level 4 for try-catch inside the date format loop, plus enum/locale/class checks).
* Map/Number coercion checks: **+2**

### B. After Refactoring (Highest Method Complexity: 8)
By decomposing the method, the structural nesting is flattened:

| Method Name | Complexity | Primary Driver |
| :--- | :---: | :--- |
| `convert` | **3** | Type resolution and collection routing. |
| `convertCollectionOrArray` | **2** | Collection validation and array check. |
| `convertToArray` | **1** | Single loop. |
| `convertToCollection` | **6** | Collection instantiation options. |
| `convertSingle` | **7** | Type coercion routing. |
| `convertFromString` | **8** | Date/Locale/Enum/Class/Reflection lookups. |
| `convertToDate` | **3** | Date format loop and try-catch. |

---

## 4. Usage & Verification Analysis

### A. Method Usages (47 References)
* **29 usages** in core logic (converts path variables, request params, and payload fields to domain objects).
* **18 usages** in test suites (converts mock request values or checks assertions).

### B. Verification Strategies
To ensure `ConversionUtil.convert` is called correctly at target places:
1. **Mocking Static References**: In JUnit tests, isolate behavior using Mockito static mocks:
   ```java
   try (MockedStatic<ConversionUtil> mocked = Mockito.mockStatic(ConversionUtil.class)) {
       mocked.when(() -> ConversionUtil.convert(any(), any())).thenReturn(expectedValue);
       // execute and verify...
       mocked.verify(() -> ConversionUtil.convert(inputValue, targetType));
   }
   ```
2. **Integration Verification**: Validate REST requests map strings (UUIDs, ISO-8601 dates) to correct domain models inside active DB transactions.

---

## 5. Coverage Gaps (For 100% Coverage)

To achieve complete test coverage on the refactored code, tests in `omod-common/src/test/java/org/openmrs/module/webservices/rest/web/ConversionUtilTest.java` must cover:

* **Null Input**: `convert(null, toType)` must return `null`.
* **Missing @Test Annotation**: Fix missing annotations on `convert_shouldConvertIntToDouble` and `convert_shouldConvertDoubleToInt`.
* **Collection Format Error**: Passing single object to target collection (should throw `ConversionException`).
* **Unsupported Collection Type**: Trying to convert to `Queue` (should throw `ConversionException`).
* **Raw Collection type**: Verifying execution of `ret.addAll` for raw type targets.
* **Float Primitive Coercion**: Target `Float.class` with `Double` type.
* **Date Parsing Failure**: Invalid date formats (should throw `ConversionException` wrapping `IllegalArgumentException`).
* **Enum Parsing Failure**: Invalid enum string names.
* **Class Loading Failure**: Non-existent class name strings.
* **Boolean to String**: Target `String.class` with `Boolean` type.
* **Static valueOf Fallback**: Custom classes with static `valueOf` methods.
* **Unsupported Conversions**: Standard incompatible types.
