You are a senior Kotlin Multiplatform engineer. Generate a complete, production-ready KMP project following strict architectural and coding guidelines.

## 🏗️ Architecture
- Use **NVI (Model-View-Intent)** architecture
- Separate:
    - Intent
    - State (immutable)
    - ViewModel (state reducer)

## 🧱 Tech Stack (KMP)

- UI: Compose Multiplatform
- Dependency Injection: Koin
- Database: SQLDelight
- Networking: Ktor Client
- Logging: Napier or Kermit (multiplatform logging)

## 🌍 Multiplatform Structure

Project must include:

- `shared/` (commonMain, commonTest)
- `androidApp/`
- `iosApp/` (optional but preferred)

### shared module structure:
shared/
├── src/commonMain/
├── src/commonTest/
├── src/androidMain/
├── src/iosMain/

## 📦 Feature-Based Structure (inside shared/commonMain)

Each feature must follow:

feature_name/
├── presentation/
├── domain/
└── data/

## 🧩 Shared Core Module (MANDATORY)

Inside shared/commonMain create:

shared/
├── ui/
├── domain/
├── data/
├── utils/
├── navigation/

### Rules
- Shared must contain reusable logic
- No duplication across features
- Shared must NOT depend on features
- Features CAN depend on shared

## 📐 Layer Responsibilities

### Presentation
- Compose UI
- ViewModel (platform-independent)
- UI State (separate file)
- UI Intents (sealed class)

### Domain
- UseCases
- Repository interfaces ONLY

### Data
- Repository implementations
- Ktor API
- SQLDelight queries
- Data sources
- DTOs
- Mappers

## ⚙️ Strict Coding Rules

### 1. UseCase per Repository Function
- Every repository function MUST have a UseCase
- ViewModel must ONLY call UseCases

### 2. Interfaces & Implementations
- Repository → interface (domain) + implementation (data)
- UseCase → interface + implementation
- Separate files ALWAYS

### 3. File Separation
- UI State → separate file
- ViewModel → separate file
- Mappers → separate files
- UseCases → separate files
- Repositories → separate files

### 4. Mappers
- DTO → Domain
- Domain → UI
- Separate file per mapper

### 5. State
- Immutable UI state
- StateFlow only

### 6. Clean Code
- SOLID
- Domain must be pure Kotlin (no platform APIs)

## ⚠️ Error Handling
- Use shared sealed class (AppError)
- Map:
    - Network errors
    - Database errors
    - Unknown errors
- No exceptions exposed outside data layer

## 🧪 Coroutines
- UseCases must be suspend
- Inject Dispatchers (no hardcoded Dispatchers)

## 🔤 Naming
- UseCase naming:
    - Verb + Entity + UseCase

## 💉 Dependency Injection (Koin)
- Use modules
- Define all dependencies via Koin DSL
- No manual instantiation

## 🌐 Networking (Ktor)
- Use Ktor Client
- Configure:
    - JSON serialization
    - Logging
    - Error handling
- DTO → Domain via mappers

## 💾 Database (SQLDelight)
- Define .sq files
- Generate typesafe queries
- Use repository to access DB
- Support multiplatform drivers

## 🪵 Logging
- Use Napier or Kermit
- No platform-specific logging

## 🎨 UI (Compose Multiplatform)

### Rules
- Stateless composables
- Driven by UI State

### File Rules
- Each composable in separate file
- No grouping multiple composables

### Preview
- Provide previews where supported

## 🧪 Testing

### Coverage Goal
- 100% line & method coverage

### Must Test
- Repositories
- UseCases
- Mappers
- ViewModels
- Shared logic

### Rules
- Every class must have tests
- Cover success, error, edge cases

### Modification Rule
- Update existing tests
- Fix broken tests
- Never leave failing tests

## ⚙️ Gradle Rules

### Dependency Ordering
- All dependencies must be alphabetically sorted

### Modification
- Keep alphabetical order after changes

### Validation
- No unused dependencies allowed
- Remove unused dependencies
- Re-sort after cleanup

## 🔁 Consistency
- All new code MUST follow rules
- Existing code MUST be updated accordingly

## 🎯 Output Requirements

- Full KMP structure
- One complete feature
- Include:
    - Ktor API
    - SQLDelight DB
    - Repository
    - UseCases
    - ViewModel (NVI)
    - Compose UI
    - Unit tests
    - Shared module usage

IMPORTANT:
- Do NOT use Android-only libraries in shared
- Do NOT bypass UseCases
- Do NOT skip layers
- Do NOT group composables
- Do NOT leave unused dependencies
- Follow ALL rules strictly