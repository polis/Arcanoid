# Arcanoid: rules for code changes

## Architecture (Clean Architecture)
- Keep the layers separate: domain (pure Kotlin: models, repository interfaces, use cases; no Android, Play or library types), data (implements the domain interfaces with Room, DataStore, Play Billing, network, system services; implementations `internal`, bound with Hilt/DI), presentation (Compose UI, ViewModels, Android entry points, DI wiring).
- Dependencies point inwards only: presentation → data → domain. Never domain → data or data → presentation. ViewModels talk to use cases or repository interfaces, never to Room/Billing/system services directly.
- Business rules live in the domain and get unit tests there.

## SOLID
- Single responsibility: one class, one reason to change. Split screens, ViewModels and repositories when they grow several jobs.
- Open/closed: add behaviour through new implementations or sealed/enum cases, not by piling flags onto existing classes.
- Liskov: fakes and real implementations honour the same interface contract (tests use fakes of domain interfaces).
- Interface segregation: small, focused interfaces instead of one large service.
- Dependency inversion: depend on domain interfaces and inject them; no service locators or statically reached singletons in UI code.

## Libraries and deprecations
- Use the latest stable versions (no alpha/beta/rc) of Gradle, AGP, Kotlin and all libraries in `gradle/libs.versions.toml`. When touching the build, check for newer stable versions and update.
- No deprecated APIs where a supported replacement exists (Android, AndroidX, Compose, Play Billing, Mobile Ads). Prefer the current recommended API.
- Where a deprecated call is the only option (e.g. an old Android version), keep it behind a version check with `@Suppress("DEPRECATION")` and a comment saying why.
- Before finishing: the release build compiles with no Kotlin warnings, and lint adds no new warnings. Check minified (R8) release builds on a device or emulator; debug builds hide R8 problems.

## Commits
- No Claude signature and no Co-Authored-By lines in commits or PRs.
