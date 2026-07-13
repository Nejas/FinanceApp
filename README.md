# FinanceApp
## Что есть в приложении

- экран расходов за выбранную дату;
- экран доходов за выбранную дату;
- экран счетов с общим балансом;
- нижняя навигация между основными разделами;
- свайпы между основными экранами;
- общий top bar с выбранной датой;
- splash screen с Lottie-анимацией;
- светлая и тёмная тема;
- mock-источники данных вместо backend/API;
- unit-тесты для доменных моделей, use case'ов и mock data sources.

## Архитектура

Проект разделён на несколько основных слоёв:

```text
presentation -> domain -> data
       ^          ^        ^
       |          |        |
      UI       use cases  repositories/data sources
```

### Presentation layer

Пакет:

```text
app/src/main/java/com/example/financeapp/presentation
```
Этот слой отвечает за UI и состояние экранов.

Основные элементы:

- `FinanceApp` — корневой composable приложения;
- `AppNavGraph`, `AppNavigationBar`, `AppRoute` — простая навигация между основными экранами;
- `AccountsRoute`, `ExpensesRoute`, `IncomeRoute` — entry-point composable'ы экранов;
- `AccountsViewModel`, `ExpensesViewModel`, `IncomeViewModel` — ViewModel'и экранов;
- `State`, `Intent`, `Effect` классы — MVI-подобная организация UI-логики;
- `presentation/common` — переиспользуемые компоненты, форматтеры и UI-модели.

ViewModel получает use case через Hilt, вызывает его в `viewModelScope`, а результат кладёт в `StateFlow`. Composable подписывается на state и перерисовывается при изменениях.

### Запуск приложения и отображение экранов

При старте `MainActivity` сразу создаёт основной composable `FinanceApp`, а `DotLottieSplashScreen` отображается поверх него как временный overlay. Splash screen не блокирует создание основного UI: пока пользователь видит Lottie-анимацию, экраны уже подключают свои ViewModel, запускают `LaunchedEffect` и начинают асинхронную загрузку данных через корутины.

После завершения задержки splash screen скрывается, а под ним уже находится основной экран приложения. Такой подход уменьшает ощущение промежуточного пустого экрана, потому что подготовка данных идёт параллельно с анимацией.

Переключение между основными разделами доступно двумя способами:

- через нижнюю навигацию `AppNavigationBar`;
- горизонтальными свайпами по контентной области между экранами расходов, доходов и счетов.

Текущий раздел хранится в `FinanceApp` через `rememberSaveable`, а `AnimatedContent` отвечает за анимированную смену экрана. `AppNavGraph` получает выбранный `AppRoute` и отображает соответствующий route: `ExpensesRoute`, `IncomeRoute` или `AccountsRoute`.

Пример направления зависимости:

```text
ExpensesRoute
    -> ExpensesViewModel
        -> GetTransactionsForDateUseCase
            -> TransactionsRepository
```

UI не обращается напрямую к data source и не создаёт mock-данные самостоятельно.

### Domain layer

Пакет:

```text
app/src/main/java/com/example/financeapp/domain
```

Domain-слой содержит бизнес-модели, интерфейсы репозиториев и use case'ы. Он не зависит от Android UI и не знает, какая конкретная реализация данных используется.

Основные модели:

- `Money`;
- `Currency`;
- `Transaction`;
- `TransactionType`;
- `Category`;
- `FinancialAccount`;
- `TransactionsOverview`;
- `FinancialAccountsOverview`.

Репозитории в domain — это интерфейсы:

- `TransactionsRepository`;
- `FinancialAccountsRepository`;
- `CategoriesRepository`.

Use case'ы:

- `GetTransactionsForDateUseCase` — получает операции нужного типа и фильтрует их по дате;
- `GetFinancialAccountsUseCase` — получает счета и считает общий баланс;
- `GetCategoriesUseCase` — получает категории;
- `CalculateMoneyTotalUseCase` — чистая доменная операция для суммирования `Money`.

`CalculateMoneyTotalUseCase` не использует репозиторий, потому что не загружает данные. Он выполняет только расчёт над уже переданным списком. Это делает его переиспользуемым и простым для тестирования.

### Data layer

Пакет:

```text
app/src/main/java/com/example/financeapp/data
```

Data-слой содержит конкретные реализации репозиториев и mock data sources.

Сейчас данные приходят из mock-классов:

- `MockExpensesDataSource`;
- `MockIncomeDataSource`;
- `MockFinancialAccountsDataSource`;
- `MockCategoriesDataSource`.

Реализации репозиториев:

- `TransactionsDataRepository`;
- `FinancialAccountsDataRepository`;
- `CategoriesDataRepository`.

Репозитории скрывают источник данных за domain-интерфейсами. Поэтому при появлении реального API можно заменить mock data source на сетевой/локальный источник, не меняя UI и большую часть domain-логики.

## Dependency Injection

DI реализован через Hilt.

Точки входа:

- `FinanceApplication` помечен `@HiltAndroidApp`;
- `MainActivity` помечен `@AndroidEntryPoint`;
- ViewModel'и помечены `@HiltViewModel`;
- зависимости передаются через `@Inject constructor`.

Связывание интерфейсов и реализаций находится в:

```text
app/src/main/java/com/example/financeapp/di/RepositoryModule.kt
```

Например:

```text
TransactionsRepository -> TransactionsDataRepository
FinancialAccountsRepository -> FinancialAccountsDataRepository
CategoriesRepository -> CategoriesDataRepository
```
## Поток данных

Типичный сценарий для экрана расходов:

```text
Пользователь открывает экран расходов
    -> ExpensesRoute отправляет intent во ViewModel
    -> ExpensesViewModel вызывает GetTransactionsForDateUseCase
    -> UseCase обращается к TransactionsRepository
    -> Hilt подставляет TransactionsDataRepository
    -> Repository берёт данные из MockExpensesDataSource
    -> UseCase фильтрует операции по дате и считает total
    -> ViewModel обновляет ExpensesState
    -> Compose перерисовывает экран
```

Такой же принцип используется для доходов и счетов.

## Тесты

В проекте есть unit-тесты для:

- `Money`;
- `CalculateMoneyTotalUseCase`;
- `GetFinancialAccountsUseCase`;
- `GetTransactionsForDateUseCase`;
- mock data sources.

Запуск:

```bash
./gradlew test
```

## Дополнительные архитектурные правила

В проекте есть отдельный документ:

```text
docs/architecture-rules.md
```

Там описано правило единого источника правды для справочных данных: валюты, языки, категории и счета не должны дублироваться списками внутри экранов. UI может делать только маппинг доменных данных в UI-модели.

