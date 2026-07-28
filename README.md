# FinanceApp

## Что есть в приложении

- расходы, доходы, счета и аналитика в едином Compose-интерфейсе;
- добавление и редактирование транзакций и счетов;
- offline-first работа через Room: ранее загруженные данные доступны без интернета;
- создание и редактирование данных offline с последующей синхронизацией;
- автоматическая синхронизация с backend через WorkManager раз в 2 часа;
- интеграция с backend через Retrofit и Bearer token;
- обработка сетевых ошибок, retry для временных сбоев и offline-баннер;
- тёмная и светлая тема, общие UI-компоненты и MVI-подобная организация состояния;
- unit-тесты для domain/use case и sync-логики.


### Часть 1. Оставшиеся функции

Реализованы сценарии, которые раньше были в roadmap:

- добавление расхода и дохода через `TransactionEditorBottomSheet`;
- редактирование существующего расхода или дохода;
- сохранение новой или изменённой транзакции через domain use case и repository;
- обновление списков после успешного сохранения транзакции;
- добавление финансового счёта через `AccountEditorBottomSheet`;
- редактирование счёта: название, валюта и баланс;
- сохранение изменений счёта через backend, если сеть доступна, или через локальную очередь, если приложение offline.

Редакторы находятся в `presentation/bottomSheets/transactionEditor` и `presentation/bottomSheets/accountEditor`. ViewModel'и не обращаются к data-слою напрямую: операции проходят через `TransactionUseCases` и `AccountUseCases`.

### Часть 2. Offline mode

Локальные данные хранятся в Room DB. База описана в `FinanceDatabase` и содержит таблицы:

- `accounts` — финансовые счета;
- `categories` — категории операций;
- `transactions` — расходы и доходы;
- `sync_operations` — очередь операций, которые нужно отправить на backend.

Repository работает по offline-first принципу:

- при успешном сетевом ответе данные обновляются в Room;
- если сеть недоступна или запрос временно не прошёл, приложение пытается показать локальный кэш;
- новые offline-операции сохраняются в Room сразу и получают локальные отрицательные id;
- операции создания, редактирования и удаления попадают в `sync_operations`;
- при следующей синхронизации очередь отправляется на backend.

### Синхронизация

Синхронизация вынесена в пакет `data/sync`.

Основные классы:

- `SyncBootstrapper` — запускается при старте приложения, ставит периодическую синхронизацию и подписывается на появление интернета;
- `WorkManagerSyncWorkScheduler` — планирует one-time и periodic sync;
- `FinanceSyncWorker` — WorkManager worker, который выполняет синхронизацию;
- `SyncCoordinator` — последовательно обрабатывает локальную очередь и обновляет snapshot данных с сервера;
- `TransactionSyncOperationHandler` — синхронизирует создание, изменение и удаление транзакций;
- `AccountSyncOperationHandler` — синхронизирует создание и изменение счетов;
- `ServerSnapshotRefresher` — после успешной отправки локальных изменений перезагружает актуальные данные с backend;
- `SyncConflictResolver` — решает конфликты между локальной и серверной версией данных.

Периодическая синхронизация запускается раз в 2 часа. Для WorkManager задано условие `NetworkType.CONNECTED`, поэтому worker стартует только при наличии подключения.


## Архитектура

Проект разделён на несколько основных слоёв:

```text
presentation -> domain -> data -> local / network / sync
       ^          ^        ^          ^        ^        ^
       |          |        |          |        |        |
      UI       use cases repositories Room   Retrofit WorkManager
```

Отдельно выделен `core`-слой. В нём находятся общие вещи, которые не относятся к конкретному экрану: тема, размеры, отступы, диспетчеры корутин, helper-функции и мониторинг сети.

### Presentation layer

Пакет:

```text
app/src/main/java/com/example/financeapp/presentation
```

### Основные экраны

Расходы, доходы и счета на главных вкладках собираются через `GetFinancialSummaryUseCase`. Он получает счета и транзакции по выбранной дате, а `MainViewModel` уже на `Default` dispatcher раскладывает результат по состояниям расходов, доходов и счетов.

Типичный поток:

```text
FinanceApp
    -> MainViewModel
        -> GetFinancialSummaryUseCase
        -> AccountUseCases
        -> TransactionUseCases
            -> TransactionsRepository
                -> Room cache
                -> FinanceRemoteDataSource / FinanceApiService
                -> sync queue для pending-операций
```

Если сеть недоступна, экран не превращается в пустое состояние: репозитории возвращают данные из Room, а корневой UI дополнительно показывает offline-баннер.

### Analytics

Экран аналитики строится вокруг `GetTransactionAnalysisUseCase`.

Он поддерживает фильтры:

- тип операции: расходы, доходы или всё;
- период: неделя, месяц, квартал, год или произвольный период;
- категории;
- счет.

Для выбора фильтров используются общие bottom sheet-компоненты на базе `FinanceModalBottomSheet`.

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
- `TransactionDraft`;
- `TransactionType`;
- `Category`;
- `Account`;
- `AccountDraft`;
- `TransactionSummary`;
- `AccountSummary`;
- `TransactionAnalysis`.

Репозитории в domain — это интерфейсы:

- `TransactionsRepository`;
- `FinancialAccountsRepository`;
- `CategoriesRepository`.

Use case'ы:

- `TransactionUseCases` — получает, создаёт, обновляет и удаляет транзакции;
- `AccountUseCases` — получает, создаёт, обновляет и удаляет счета;
- `CategoryUseCase` — получает категории операций;
- `GetFinancialSummaryUseCase` — собирает данные для главных экранов расходов, доходов и счетов;
- `GetTransactionAnalysisUseCase` — готовит данные аналитики, категории, проценты и список операций;
- `SynchronizationUseCases` — даёт UI доступ к событиям синхронизации и управлению неудачными операциями;
- `Money.sum` — чистая доменная операция для суммирования денежных значений.

Суммирование денег не использует репозиторий, потому что не загружает данные. Оно выполняет только расчёт над уже переданным списком. Это делает доменную операцию переиспользуемой и простой для тестирования.

## Data, Local, Sync и Network layer

Пакеты:

```text
app/src/main/java/com/example/financeapp/data
app/src/main/java/com/example/financeapp/data/local
app/src/main/java/com/example/financeapp/data/offline
app/src/main/java/com/example/financeapp/data/sync
app/src/main/java/com/example/financeapp/data/network
```

Data-слой содержит реализации репозиториев, локальное хранилище, очередь offline-операций, синхронизацию и мапперы между DTO, Room entity и domain-моделями.

Реализации репозиториев:

- `TransactionsDataRepository`;
- `FinancialAccountsDataRepository`;
- `CategoriesDataRepository`.

Репозитории скрывают от domain-слоя детали того, откуда пришли данные. Для UI и use case'ов сценарий остаётся единым: запросить данные или сохранить изменение. Внутри repository решает, можно ли сходить в сеть, нужно ли вернуть кэш из Room или поставить изменение в очередь синхронизации.

### Local storage

Локальное хранилище построено на Room.

Основные элементы:

- `FinanceDatabase` — Room database;
- `AccountDao` — доступ к счетам;
- `CategoryDao` — доступ к категориям;
- `TransactionDao` — доступ к транзакциям;
- `SyncOperationDao` — доступ к очереди синхронизации;
- `AccountEntity`, `CategoryEntity`, `TransactionEntity`, `SyncOperationEntity` — локальные entity;
- `LocalAccountMapper`, `LocalTransactionMapper` — маппинг Room entity в domain-модели.

Локальные записи имеют `syncState`, чтобы UI и sync-слой могли отличать синхронизированные данные от pending-изменений. Новые offline-сущности получают локальные отрицательные id до тех пор, пока backend не вернёт настоящий id.

### Offline operations

Пакет `data/offline` отвечает за сохранение пользовательских изменений, которые пока нельзя отправить на сервер.

Основные классы:

- `PendingTransactionMutationStore` — создаёт локальные транзакции, обновляет pending-транзакции, ставит в очередь update/delete;
- `PendingAccountMutationStore` — создаёт локальные счета и ставит в очередь изменения счёта.

Такой подход позволяет пользователю продолжать работу без сети: операция сразу появляется в списке, а backend догоняет локальное состояние позже.

### Sync layer

Пакет `data/sync` отвечает за доставку локальных изменений на backend и обновление локального snapshot.

Поток синхронизации:

```text
SyncBootstrapper
    -> SyncWorkScheduler
        -> WorkManager
            -> FinanceSyncWorker
                -> SyncCoordinator
                    -> SyncOperationDao
                    -> TransactionSyncOperationHandler / AccountSyncOperationHandler
                    -> ServerSnapshotRefresher
                    -> SyncEventPublisher
```

`SyncCoordinator` обрабатывает pending-операции последовательно. Retryable-ошибки оставляют задачу на повтор через WorkManager, неретрайбл-ошибки помечаются как `FAILED`, а UI может предложить повторить или отбросить неудачные операции.

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
- `LocalDataModule` — создаёт Room database и DAO;
- `SyncModule` — связывает scheduler, repository событий синхронизации и sync-зависимости;
- `NetworkModule` — создаёт Retrofit, OkHttp, JSON, `RetryPolicy` и network data source;
- `NetworkMonitorModule` — связывает `NetworkMonitor` с `ConnectivityNetworkMonitor`;
- `DispatchersModule` — предоставляет `IoDispatcher` и `DefaultDispatcher`.

## Как запустить после клонирования

После клонирования проекта из удалённого репозитория нужно настроить локальные параметры окружения.
Добавить API-токен для backend:

```properties
SHMR_API_TOKEN=your_token_here
```
Токен читается в `app/build.gradle.kts` и попадает в `BuildConfig.SHMR_API_TOKEN`.
Затем `BuildConfigAuthTokenProvider` передаёт его в `BearerAuthInterceptor`, который добавляет `Authorization: Bearer ...` к запросам.
Если `SHMR_API_TOKEN` не указан или указан неверно, реальные backend-запросы будут получать `401 Unauthorized`.


## Тесты

В проекте есть unit-тесты для:

- `Money`;
- `TransactionUseCases`;
- `AccountUseCases`;
- `GetFinancialSummaryUseCase`;
- `GetTransactionAnalysisUseCase`;
- `TransactionSyncOperationHandler`;
- `AccountSyncOperationHandler`;
- `LastWriteWinsSyncConflictResolver`;
- сетевого integration/smoke-сценария для реального API.

