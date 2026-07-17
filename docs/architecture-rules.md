# Architecture rules

## Single source of truth for reference data

Reference data must have one canonical source.

Examples:

- currencies come from `domain.model.Currency`;
- interface languages come from `core.localization.AppLanguage`;
- transaction categories come from domain/data category models;
- financial accounts come from repository/use-case data, even when it is mock data.

Presentation may create UI models only by mapping the canonical source.

Allowed:

```kotlin
Currency.entries.map { currency -> currency.toUi(...) }
```

Not allowed:

```kotlin
listOf("RUB", "USD", "EUR")
```

Do not duplicate the same option list in several screens. If two screens need visually identical selection UI, create a reusable component in `presentation/common/components`. If two screens need the same option mapping, create a reusable mapper/model in `presentation/common/model`.

Mock data is allowed while there is no backend, but it must live behind data sources/repositories/use cases, not inside screen composables. Screen state may receive already prepared UI state, but should not become the canonical owner of reference dictionaries.

When adding a new reference item:

1. Add it to the canonical source.
2. Update the mapper if the UI needs labels/icons.
3. Use the shared component/mapper from screens.
4. Do not add a second hardcoded list in a screen.

## Общие контракты

Если несколько классов, интерфейсов или моделей имеют общий смысловой набор свойств, методов или поведения, нужно выносить этот общий контракт в отдельный интерфейс, а конкретные реализации должны реализовывать его.

Интерфейсы нужно использовать не только для устранения дублирования, но и для инкапсуляции: внешний код должен зависеть от контракта, а не от конкретной реализации, когда это помогает скрыть детали реализации и сохранить принципы ООП.

При проектировании контрактов, моделей, репозиториев, источников данных, use case и вспомогательных классов нужно учитывать принципы SOLID: классы должны иметь одну понятную ответственность, расширяться без переписывания существующей логики, не заставлять клиентов зависеть от ненужных методов и зависеть от абстракций, а не от конкретных реализаций.

Использовать это правило для основных общих сущностей и повторяющихся контрактов. Например, для response DTO аккаунта с общими полями `id`, `name`, `emoji`, `balance` и `currency`, или для классов, которые выполняют одинаковую роль в разных частях приложения.

Не нужно создавать интерфейс только из-за случайного совпадения одного поля, метода или похожего названия. Общий контракт должен отражать реальную сущность домена, API или архитектуры приложения.
