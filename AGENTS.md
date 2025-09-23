# AI Agent Operations Guide (AGENTS.md)

Author: Maintainers + AI Assistants
Last Updated: 2025-09-23
Status: Active

This document defines how automated coding agents (and human contributors) should analyze, change, verify, and ship code in this repository. It codifies project structure, safety rails, quality gates, and repeatable workflows.

---
## 1. Repository Overview
An Android application (package `com.anod.car.home`) composed of modular Kotlin / Gradle subprojects:

Core modules:
- `app/` – Application entry point, wiring, Android components, manifest, signing configs.
- `content/` – Data + domain abstractions (includes SQLDelight integrations and serialization).
- `compose/` – Jetpack Compose UI layer components (reusable UI primitives + surfaces).
- `skins/` – Theming / skin configuration and resources.
- `baselineProfile/` – Baseline Profile generation (macrobenchmark tests for startup perf).
- `lib/` umbrella (internal libraries):
  - `applog/` – Logging & diagnostics.
  - `graphics/` – Image/color/composition utilities.
  - `context/` – Android context helpers & environment abstractions.
  - `framework/` – Framework/service abstractions (system APIs, managers, wrappers).
  - `permissions/` – Permission handling flows.
  - `playservices/` – (If present) Play Services integration wrappers.
  - `notification/` – Notification channel & builder utilities.
  - `ktx/`, `viewmodel/`, `compose/` (under lib) – Targeted extension sets or adapter layers.

Supporting config:
- Gradle version catalogs: `gradle/libs.versions.toml` (single source of truth for dependency & plugin versions).
- Root `build.gradle.kts` sets plugin aliases for subprojects.
- Kotlin JVM target: 11 (per `app/build.gradle.kts`).
- Android compileSdk: 36, minSdk: 29, targetSdk: 34.
- Crash & diagnostics: ACRA dependencies.
- DI: Koin Core.
- Serialization: kotlinx.serialization JSON.
- Performance: Baseline Profile + `androidx.profileinstaller`.

---
## 2. Architectural Notes
- UI: Compose-first (module `compose/` and possible internal bridging). Avoid mixing legacy View code unless required; migrate incremental code to Compose surfaces.
- Modularity: Shared logic resides in internal `lib/*` modules to keep `app` lean. Place new cross-cutting concerns in a dedicated module rather than inflating existing ones.
- Data Access: Use SQLDelight adapters from `content/`; prefer suspension or Flow-based APIs (if present) for asynchronous operations.
- Theming / Skins: Centralized in `skins/` and `BaseProperties` (e.g., internal shortcut resources). Maintain separation between presentation (Compose components) and skin configuration.
- Shortcut & Activity Integration: `WidgetShortcutResource` demonstrates injection of activity targets. Extend by adding new internal shortcuts through properties-based definitions rather than embedding logic in UI modules.

---
## 3. Source Code Conventions
- Language: Kotlin. Follow the official Kotlin style (JetBrains). Keep line length reasonable (~120) unless clarity benefits exceed wrapping cost.
- Nullability: Avoid platform types. Use explicit types for public APIs.
- Immutability: Prefer `val` over `var`; pure functions over side-effect-heavy code.
- Coroutines: Launch in scoped contexts (e.g., ViewModel scope or structured concurrency). Never leak GlobalScope.
- Dependency Injection: Use Koin modules; keep module declarations colocated with the feature or layer they configure.
- Compose: Stateless composables first; state hoisted; previews optional but encouraged.
- Logging: Centralize through `applog` utilities; avoid println / System.out.
- Avoid Magic Numbers: Use constants or configuration objects.
- Public API Surface: Minimize; prefer internal visibility unless cross-module usage is required.

---
## 4. Dependency Management
- All versions pinned in `gradle/libs.versions.toml`. Never hardcode versions elsewhere.
- To add a dependency:
  1. Add `version` (if new) under `[versions]` (sorted alphabetically by key).
  2. Add library entry under `[libraries]` referencing the version.
  3. Reference via `libs.<alias>` in module build file.
- To upgrade dependencies: batch by logical concern (e.g., Compose BOM + related libs). Run a full build and smoke test startup.
- Remove unused dependencies promptly.
- Keep Kotlin & AGP versions mutually compatible (check release notes if updating).

---
## 5. Build & Tooling
Common tasks:
- Assemble debug: `./gradlew :app:assembleDebug`
- Full clean build: `./gradlew clean build`
- Baseline Profile generation (example): `./gradlew :baselineprofile:generateBaselineProfile`
- Lint (if configured): `./gradlew :app:lint`
- Dependency insight: `./gradlew :app:dependencyInsight --configuration debugRuntimeClasspath --dependency <name>`

Agents MUST verify a clean build before proposing refactors.

---
## 6. Testing & Quality Gates
(If dedicated test modules or directories exist, adhere; create as needed.) Minimum expectations:
- Unit tests: Use Kotlin test / JUnit4 style inside `src/test/java`.
- Instrumented tests: `src/androidTest/java` for UI / integration.
- Baseline Performance: Ensure `baselineProfile` remains valid after startup path changes.

Quality Gate Checklist (MANDATORY for agents):
1. Build succeeds (no compilation errors or warnings introduced).
2. All existing tests pass.
3. New logic has at least 1 happy-path + 1 edge-case test (if pure or critical logic).
4. No added TODO/FIXME without linked issue.
5. No secrets or keystore materials committed.
6. No version drift from catalog (use aliases). 
7. Release build still minifies & shrinks without missing classes (run `assembleRelease` when touching ProGuard rules).

---
## 7. Performance & Baseline Profiles
- Modify startup-critical code cautiously; if changed, regenerate baseline profile.
- Avoid heavy I/O or synchronous blocking on main thread.
- Prefer lazy initialization for non-critical services.
- Compose performance: Use `remember {}` and stable parameter types; avoid recomposition thrash.

---
## 8. Security & Privacy
- Never hardcode API keys or credentials. Use Gradle properties (`local.properties` or environment variables) for local dev, and CI secret management for pipelines.
- Signing: Release signing config references properties—do not commit actual keystore unless explicitly intended (current placeholder approach respected).
- Crash Reporting (ACRA): Ensure PII scrubbing in custom log attachments (audit before adding new fields).

---
## 9. Release & Versioning
- App version set in `app/build.gradle.kts` (`versionCode`, `versionName`). Increment:
  - `versionCode`: always +1 per release.
  - `versionName`: semantic (MAJOR.MINOR.PATCH). Patch for bugfix; minor for feature additions; major for breaking API or significant redesign.
- Tag format suggestion: `v<versionName>` (e.g., `v3.2.0`).
- Keep changelog (if added later) in `CHANGELOG.md` using Keep a Changelog format.

---
## 10. Commit & Branch Strategy
Recommended (if not yet documented):
- Main branches:
  - `main` (stable / release-ready)
  - `develop` (optional integration branch)
- Feature branches: `feature/<short-description>`
- Hotfix branches: `hotfix/<issue-id>`
- Use Conventional Commits:
  - `feat: add in-car switching animation`
  - `fix: correct shortcut resource binding`
  - `refactor: split logging provider`
  - `perf: optimize compose recompositions`
  - `chore: update dependencies`
  - `test: add baseline profile smoke test`

---
## 11. Working With Resources & Skins
- All theming alterations should be isolated to `skins/` or Compose theme providers.
- For internal shortcuts (see `BaseProperties.internalShortcutResourcesPrimary`), extend via constants rather than embedding raw IDs in logic.
- Validate any new drawable or color on dark & light backgrounds.

---
## 12. Adding a New Module (Agent Playbook)
1. Create directory (e.g., `lib/analytics`).
2. Add `build.gradle.kts` applying `alias(libs.plugins.android.library)` + Kotlin plugin.
3. Set `namespace = "com.anod.car.home.analytics"` (unique & consistent).
4. Declare minimal dependencies (avoid copying from large modules blindly).
5. Wire into consuming modules with `implementation(project(":lib:analytics"))`.
6. Add at least 1 test if logic > trivial.
7. Run full build.

---
## 13. Refactor Guidelines (Agent Safety Rails)
Before refactor:
- Identify ownership (search usages with grep / semantic). Avoid breaking public APIs used across modules.
- If renaming packages/namespaces: update manifest, Koin modules, and ProGuard keep rules if necessary.
- For large moves: prefer incremental extraction (create new class, migrate usages, remove old).

After refactor:
- Re-run build + tests.
- Confirm no reflection-based lookups broken (auto-service / annotation processors involved).
- Validate app launch still succeeds (if runtime test harness exists).

---
## 14. Handling Annotation Processing & KSP
- Auto-service processors (`auto-service-ksp`, `auto-service`) in use. When adding new services:
  - Annotate interfaces/classes properly.
  - Confirm generated files appear under `build/generated/`.
  - Do not manually edit generated sources.

---
## 15. ProGuard / R8 Considerations
- When adding libraries relying on reflection / serialization: add appropriate `-keep` rules in `proguard-rules.pro` (e.g., for kotlinx.serialization models if necessary).
- Always test `:app:assembleRelease` after altering shrinker rules.

---
## 16. Error Handling & Logging
- Use structured logging (if available) instead of ad-hoc string concatenation.
- Fail fast on programmer errors (throw) but degrade gracefully on recoverable I/O or environment issues.
- Prefer sealed result types or `Result` wrappers for cross-layer boundaries.

---
## 17. Automation & CI (Future Enhancements)
Suggested pipeline (document when implemented):
1. Lint / Static analysis (ktlint, detekt) – to be added.
2. Unit tests.
3. Assemble debug + release.
4. Baseline profile collection on instrumentation device.
5. Artifact signing & upload.

Agents should prepare scripts but not assume presence until verified.

---
## 18. AI Agent Operational Checklist
Before making changes:
- [ ] Read relevant module build file(s).
- [ ] Map symbol ownership (search usages).
- [ ] Confirm no simpler existing abstraction already solves the task.

While editing:
- [ ] Keep diffs minimal & scoped.
- [ ] Update/ add tests for logic changes.
- [ ] Avoid formatting churn.

After editing:
- [ ] Run `./gradlew :app:assembleDebug`.
- [ ] Run tests (if present) `./gradlew test` (and `connectedAndroidTest` if UI impacted).
- [ ] Validate no new dependencies violate constraints.
- [ ] Confirm version catalog untouched unless intentionally modified.
- [ ] Update documentation if public API or behavior changed.

Submission:
- [ ] Provide summary of changes, rationale, and verification steps executed.
- [ ] Note any follow-up tasks (deferred improvements) explicitly.

---
## 19. Documentation Standards
- Keep this file authoritative for agent workflow; update if process evolves.
- Add module-level `README.md` for complex new modules explaining purpose and boundaries.
- Inline KDoc for non-trivial public functions & classes.

---
## 20. Known Gaps / TODO (Track & Remove When Done)
- Add automated static analysis (ktlint/detekt) configuration.
- Introduce CHANGELOG.md with release notes.
- Add sample instrumentation smoke test ensuring app launch path exercised.
- Evaluate dependency update bot strategy (e.g., Renovate) if not present.

---
## 21. Contact & Escalation (Human Handoff)
If an agent detects ambiguous architectural direction, brittle code, or potential data loss scenario, it must flag for human review instead of proceeding. Provide a concise risk summary + suggested mitigation.

---
## 22. Quick Reference Commands
```
# Full clean build
./gradlew clean build

# Assemble debug APK
./gradlew :app:assembleDebug

# Release build (ensure signing props configured)
./gradlew :app:assembleRelease

# Generate baseline profile (example target)
./gradlew :baselineprofile:generateBaselineProfile

# Run unit tests
./gradlew test
```

---
## 23. Amendment Process
1. Propose change (PR updating this file).
2. Justify with concrete need (e.g., new module pattern, new CI stage).
3. Obtain maintainer approval.
4. Keep historical clarity (avoid force-push rewriting history of this file).

---
## 24. Guiding Principles
- Clarity over cleverness.
- Incremental evolution over big-bang rewrites.
- Testable seams over hidden coupling.
- Explicit boundaries over implicit conventions.
- Reliability and startup performance are first-class features.

---
End of AGENTS.md

