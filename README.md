# FinanceApp

## Что есть в приложении

- основные экраны расходов, доходов и счетов;
- экран аналитики с фильтрами по типу операции, периоду, категориям и счету;
- donut chart для распределения операций по категориям;
- прокручиваемая легенда под donut chart, если категорий становится много;
- детализация аналитики в bottom sheet;
- нижняя навигация между основными разделами;
- горизонтальные свайпы между основными экранами;
- общий top bar с выбранной датой, кнопкой аналитики и кнопкой настроек;
- floating action button для добавления операции;
- pull-to-refresh: экран можно обновить свайпом вниз;
- периодическое silent-обновление активных экранов во время работы приложения;
- splash screen с Lottie-анимацией;
- светлая и тёмная тема;
- загрузка данных с backend через Retrofit;
- централизованная обработка сетевых ошибок;
- отслеживание состояния интернета на устройстве;
- offline-баннер, если на телефоне нет подключения к интернету;
- экран ошибки с кнопкой "Повторить";
- общая политика retry для временных сетевых ошибок;
- unit-тесты для доменных моделей и use case'ов.

## Архитектура

Проект разделён на несколько основных слоёв:

```text
presentation -> domain -> data -> network
       ^          ^        ^        ^
       |          |        |        |
      UI       use cases repositories Retrofit/API
```

Отдельно выделен `core`-слой. В нём находятся общие вещи, которые не относятся к конкретному экрану: тема, размеры, отступы, диспетчеры корутин, helper-функции и мониторинг сети.

### Presentation layer

Пакет:

```text
app/src/main/java/com/example/financeapp/presentation
```

Этот слой отвечает за UI, состояние экранов и обработку пользовательских действий.

Основные элементы:

- `FinanceApp` — корневой composable приложения;
- `AppNavGraph`, `BottomNavigationBar`, `AppRoute` — навигация между экранами;
- `MainViewModel` — общий источник состояния для основных вкладок;
- `AnalyticsViewModel` — ViewModel экрана аналитики;
- `ExpensesRoute`, `IncomeRoute`, `AccountsRoute`, `AnalyticsRoute` — entry-point composable'ы экранов;
- `State`, `Intent`, `Effect` — MVI-подобная организация UI-логики;
- `presentation/common/components/base` — общие UI-компоненты;
- `presentation/common/placeholders` — состояния загрузки, пустого списка и ошибки;
- `presentation/common/network` — lifecycle refresh, offline-баннер и UI-обёртка для состояния сети.

ViewModel получает use case через Hilt, вызывает его в `viewModelScope`, а результат кладёт в `StateFlow`. Composable подписывается на state и перерисовывается при изменениях.

### Основные экраны

Расходы и доходы на главных вкладках загружаются без обязательного периода. Для этого используется `GetTransactionsUseCase`, который получает список транзакций по счетам, а `MainViewModel` уже на `Default` dispatcher разделяет транзакции на доходы и расходы по типу категории.

Типичный поток:

```text
FinanceApp
    -> MainViewModel
        -> GetAllFinancialAccountsOverviewUseCase
        -> GetCategoriesUseCase
        -> GetTransactionsUseCase
            -> TransactionsRepository
                -> FinanceRemoteDataSource
                    -> FinanceApiService
```

После загрузки `MainViewModel` обновляет:

- `ExpensesState`;
- `IncomeState`;
- `AccountsState`.

Пользователь может обновить данные свайпом вниз. Pull-to-refresh вызывает `refreshFromNetwork`, после чего запрос снова проходит через общую сетевую политику.

### Analytics

Экран аналитики строится вокруг `GetAnalyticsOverviewUseCase`.

Он поддерживает фильтры:

- тип операции: расходы, доходы или всё;
- период: неделя, месяц, квартал, год или произвольный период;
- категории;
- счет.

Для выбора фильтров используются общие bottom sheet-компоненты на базе `FinanceModalBottomSheet`.

На экране аналитики есть:

- donut chart с суммой в центре;
- горизонтально прокручиваемая легенда категорий под графиком;
- список транзакций;
- bottom sheet детализации по категориям;
- pull-to-refresh свайпом вниз.

Donut chart рисуется через Compose `Canvas`, потому что в `androidx.compose.material3` нет готового компонента для donut/pie chart. Углы сегментов считаются по реальным суммам категорий, а не по округлённым процентам, поэтому при нескольких категориях в графике не появляется визуальный разрыв.

## Domain layer

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
- `TransactionsOverviewFilter`;
- `AnalyticsOverview`;
- `AnalyticsFilter`;
- `FinancialAccountsOverview`.

Репозитории в domain — это интерфейсы:

- `TransactionsRepository`;
- `FinancialAccountsRepository`;
- `CategoriesRepository`.

Use case'ы:

- `GetTransactionsUseCase` — получает транзакции по фильтру репозитория;
- `GetTransactionsOverviewUseCase` — собирает обзор транзакций для аналитики;
- `GetAnalyticsOverviewUseCase` — готовит данные аналитики, категории, проценты и список операций;
- `GetFinancialAccountsUseCase` — получает счета и считает общий баланс;
- `GetAllFinancialAccountsOverviewUseCase` — собирает общий обзор счетов;
- `GetCategoriesUseCase` — получает категории;
- `CalculateMoneyTotalUseCase` — чистая доменная операция для суммирования `Money`.

`CalculateMoneyTotalUseCase` не использует репозиторий, потому что не загружает данные. Он выполняет только расчёт над уже переданным списком. Это делает его переиспользуемым и простым для тестирования.

## Data и Network layer

Пакеты:

```text
app/src/main/java/com/example/financeapp/data
app/src/main/java/com/example/financeapp/data/network
```

Data-слой содержит реализации репозиториев и мапперы между DTO и domain-моделями.

Реализации репозиториев:

- `TransactionsDataRepository`;
- `FinancialAccountsDataRepository`;
- `CategoriesDataRepository`.

Сетевой слой содержит:

- `FinanceApiService` — Retrofit API;
- `FinanceRemoteDataSource` — контракт удалённого источника;
- `FinanceNetworkDataSource` — реализация удалённого источника;
- `NetworkRequestExecutor` — единая точка выполнения сетевых запросов;
- `RetryPolicy` — параметры повторных запросов;
- `NetworkResult` — типизированный результат сетевого вызова;
- `NetworkDataException` — ошибки data-слоя после маппинга результата.

Все сетевые запросы выполняются асинхронно на `Dispatchers.IO` через `NetworkRequestExecutor`.

## Политика retry

В приложении есть общая политика retry для сетевых запросов. Она применяется централизованно в `NetworkRequestExecutor`, поэтому экраны и use case'ы не дублируют retry-логику.

Правила:

- перед запросом проверяется `NetworkMonitor`;
- если интернет на устройстве выключен, запрос не стартует;
- каждый запрос выполняется с timeout `15 секунд`;
- при временной ошибке запрос повторяется автоматически;
- максимум выполняется 3 повторных запроса после первой попытки;
- интервал между повторами фиксированный: 2 секунды;
- backoff не используется.

Retry применяется для:

- `HTTP 500`;
- `NetworkError`;
- `TimeoutError`.

Retry не применяется для:

- `400`;
- `401`;
- `404`;
- ошибок сериализации;
- неизвестных ошибок, которые нельзя безопасно считать временными.

Автоматический retry не заменяет ручное действие пользователя. Если данные не удалось загрузить, приложение показывает экран ошибки с кнопкой "Повторить". Нажатие на эту кнопку отправляет `Retry` intent во ViewModel, запускает новый `refreshFromNetwork` и снова проходит через общую сетевую политику.

## Отслеживание сети и ошибки

Состояние подключения отслеживается через `ConnectivityManager` в `ConnectivityNetworkMonitor`.

Если на телефоне нет валидного интернет-соединения:

- в корневом UI появляется offline-баннер;
- lifecycle refresh не дёргает сеть;
- сетевой executor сразу возвращает сетевую ошибку;
- экран может показать ошибку "Нет подключения к интернету".

Ошибки для UI классифицируются через `ScreenErrorMapper`.

Возможные экранные состояния:

- `NO_INTERNET`;
- `SERVER_ERROR`;
- `TIMEOUT`;
- `LOAD_FAILED`.

`ErrorContent` показывает текст под конкретный тип ошибки и кнопку "Повторить".

## Dependency Injection

DI реализован через Hilt.

Точки входа:

- `FinanceApplication` помечен `@HiltAndroidApp`;
- `MainActivity` помечен `@AndroidEntryPoint`;
- ViewModel'и помечены `@HiltViewModel`;
- зависимости передаются через `@Inject constructor`.

Основные DI-модули:

- `RepositoryModule` — связывает domain repository interfaces с data implementations;
- `NetworkModule` — создаёт Retrofit, OkHttp, JSON, `RetryPolicy` и network data source;
- `NetworkMonitorModule` — связывает `NetworkMonitor` с `ConnectivityNetworkMonitor`;
- `DispatchersModule` — предоставляет `IoDispatcher` и `DefaultDispatcher`.

## Как запустить после клонирования

После клонирования проекта из удалённого репозитория нужно настроить локальные параметры окружения.

1. Открыть проект в Android Studio.
2. Дождаться первичного Gradle Sync или создать файл вручную.
3. В корне проекта создать файл:

```text
local.properties
```

4. Добавить путь к Android SDK, если Android Studio не добавила его автоматически:

```properties
sdk.dir=C\:\\Users\\User\\AppData\\Local\\Android\\Sdk
```

5. Добавить API-токен для backend:

```properties
SHMR_API_TOKEN=your_token_here
```

Пример полного `local.properties`:

```properties
sdk.dir=C\:\\Users\\User\\AppData\\Local\\Android\\Sdk
SHMR_API_TOKEN=your_token_here
```

`local.properties` хранит локальные настройки разработчика и секреты, поэтому он не должен попадать в git. Файл уже добавлен в `.gitignore`.

Токен читается в `app/build.gradle.kts` и попадает в `BuildConfig.SHMR_API_TOKEN`. Затем `BuildConfigAuthTokenProvider` передаёт его в `BearerAuthInterceptor`, который добавляет `Authorization: Bearer ...` к запросам.

Если `SHMR_API_TOKEN` не указан или указан неверно, реальные backend-запросы будут получать `401 Unauthorized`.

После настройки:

1. выполнить Gradle Sync;
2. выбрать устройство или эмулятор;
3. запустить приложение из Android Studio.

## Поток данных

Главные экраны:

```text
Пользователь открывает расходы / доходы / счета
    -> FinanceApp отображает нужный Route
    -> MainViewModel запускает загрузку
    -> UseCase обращается к repository
    -> Repository обращается к network data source
    -> NetworkRequestExecutor выполняет запрос на IO
    -> MainViewModel готовит UI state на Default
    -> Compose перерисовывает экран
```

Аналитика:

```text
Пользователь открывает аналитику или меняет фильтр
    -> AnalyticsViewModel обновляет AnalyticsFilter
    -> GetAnalyticsOverviewUseCase загружает транзакции и категории
    -> UseCase группирует операции по категориям
    -> ViewModel готовит цвета и UI state
    -> AnalyticsScreen отображает chart, легенду, фильтры и транзакции
```

Ошибки:

```text
NetworkRequestExecutor
    -> NetworkResult
        -> NetworkDataException
            -> ScreenError
                -> ErrorContent / offline banner
```

## Тесты

В проекте есть unit-тесты для:

- `Money`;
- `CalculateMoneyTotalUseCase`;
- `GetFinancialAccountsUseCase`;
- `GetAllFinancialAccountsOverviewUseCase`;
- `GetTransactionsOverviewUseCase`;
- сетевого integration/smoke-сценария для реального API.

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
