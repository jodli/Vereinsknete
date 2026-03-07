# Architecture Guidelines — YogaKnete

Overview of existing patterns and conventions in the codebase.

---

## 1. Layered Architecture

MVVM + Clean Architecture in three layers:

- **presentation/** — UI (Compose screens) + ViewModels
- **domain/** — Models, repository interfaces, use cases, services
- **data/** — Room DAOs, repository implementations

### Dependency Rule
- presentation may import domain, but never data
- domain imports nothing from presentation or data — pure Kotlin interfaces and models
- data implements the domain interfaces

### Feature Organization
Each feature gets its own directory under `presentation/screens/` with its own ViewModel, screen composable, and optional dialogs. Shared UI building blocks live in `presentation/components/`.

---

## 2. State Management

### Pattern
- ViewModels hold state as `MutableStateFlow` (private) + `StateFlow` (public)
- Screens collect state via `collectAsState()`
- State updates always via `_state.update { it.copy(...) }`

### Naming Conventions
- State class: `<Feature>UiState` or `<Feature>ViewState`
- Private flow: `_state` or `_uiState`
- Public flow: `state` or `uiState`

---

## 3. Data Flow

```
UI tap → Screen → ViewModel → Repository (interface) → RepositoryImpl → Room DAO
         ← StateFlow ← Flow ← DAO query
```

### Repository Pattern
- domain/ defines repository interfaces (pure Kotlin)
- data/ implements these interfaces, delegating to Room DAOs
- Reactive reads as Flow, one-shot reads and writes as suspend
- New features should go through repository interfaces, not directly to DAOs

---

## 4. Dependency Injection (Hilt)

- **DatabaseModule** — provides Room database and DAOs
- **RepositoryModule** — binds repository interfaces to implementations
- Both as singletons (SingletonComponent)
- ViewModels with `@HiltViewModel` + `@Inject constructor`
- In Compose via `hiltViewModel()`
- ViewModels receive interfaces, never implementations

---

## 5. Dialog State: ViewModel vs. Local

| ViewModel state | Local composable state |
|----------------|------------------------|
| Dialog visibility | Confirmation dialogs ("Are you sure?") |
| Selected element | Dropdown menu expanded/collapsed |
| Form dialogs | Temporary UI toggles |

**Rule of thumb:** Does the state affect business logic or need to be shared across composables? → ViewModel. Is it a pure UI intermediate step? → Local state.

---

## 6. Navigation

Single-activity app with Jetpack Compose Navigation:
- String-based routes in MainActivity
- Navigation callbacks passed as lambdas to screens
- Back navigation via `navController.navigateUp()`

---

## 7. Date & Time

Exclusively **kotlinx-datetime**, never java.util.Date or java.time:

| Type | Usage |
|------|-------|
| LocalDate | Days, weekdays, invoice periods |
| LocalDateTime | Class times (start, end) |
| LocalTime | Times in templates |

Helper class `DateUtils` in core/utils/ for formatting and calculations.

---

## 8. Serialization

- kotlinx-serialization-json for backup/restore
- Domain models annotated with @Serializable
- Purely offline, no networking

---

## 9. Test Strategy

- **Unit tests only** — no UI/instrumented tests
- Framework: JUnit 4 + MockK + kotlinx-coroutines-test
- ViewModels constructed manually (no Hilt in tests)
- StandardTestDispatcher + Dispatchers.setMain() for coroutine tests
- Test names as backtick strings

### What is tested
ViewModels (state changes, actions), repositories (DAO delegation), models (calculations), utils (formatting), services (HTML/PDF/QR), use cases (auto-scheduling)

---

## 10. Core Rules

1. New features → own directory under presentation/screens/, own ViewModel
2. Data flow → always through repository interface
3. State → MutableStateFlow + update { it.copy(...) }
4. DI → @HiltViewModel, bind interfaces in RepositoryModule
5. Dialogs → visibility in ViewModel state, confirmations as local state
6. Date/time → kotlinx-datetime, never java.util
7. Tests → JUnit 4, MockK, construct ViewModels manually
8. Language → German UI labels, English code comments
