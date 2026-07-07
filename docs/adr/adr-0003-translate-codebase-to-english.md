---
title: "ADR-0003: Translate codebase from Spanish to English"
status: "Accepted"
date: "2026-07-07"
authors: "Jhont3 (student)"
tags: ["architecture", "decision", "code-quality"]
supersedes: ""
superseded_by: ""
---

# ADR-0003: Translate codebase from Spanish to English

## Status

**Accepted**

## Context

The base project (`gmacastil/micro-calc`) uses Spanish identifiers throughout: package `com.mauricio.clase`, class `ControlCalculadora`, model `Respuesta`, methods `sumar`/`restar`/`dividir`, and endpoints `/suma`, `/resta`, `/div`. The student explicitly requested that source code and comments be in English for this project, while the README should remain in Spanish (the course's audience/deliverable language).

## Decision

Rename all Spanish identifiers to English equivalents:

- Package: `com.mauricio.clase` → `com.mauricio.calculator`
- `ControlCalculadora` → `CalculatorController`
- `Respuesta` → `CalculationResponse` (field `resultado` → `result`)
- Methods/endpoints: `sumar`/`/suma` → `add`/`/add`, `restar`/`/resta` → `subtract`/`/subtract`, `dividir`/`/div` → `divide`/`/divide`
- `mensajeError` → `errorMessage`; `server`/`user` fields → `dbServer`/`dbUser`

While rewriting `CalculatorController`, the `subtract` method's parameter declaration order was also changed to match the URL path order (`a`, `b`) — the original relied on Spring's `-parameters` compiler flag to bind `@PathVariable` by name rather than position, which produced correct but visually confusing code (parameters declared as `(b, a)` for a `/resta/{a}/{b}` route). Since the file was already being rewritten, this was corrected rather than preserved as a documented quirk.

Property keys (`app.message.error`, `db.server`, `db.user`, `server.port`) were left unchanged, as they are already plain English/neutral words; only the Spanish error message *value* was translated.

## Consequences

### Positive

- **POS-001**: Codebase reads naturally to an English-speaking reviewer/grader without requiring translation.
- **POS-002**: Removes a genuinely confusing (if harmless) parameter-ordering quirk in the same pass, at no extra cost.
- **POS-003**: Consistent with the project's Kubernetes manifests and documentation, which are also in English.

### Negative

- **NEG-001**: Diverges from the upstream `gmacastil/micro-calc` naming, so future upstream changes cannot be merged in without manual translation each time.
- **NEG-002**: The public API surface changed (`/suma` → `/add`, etc.); anyone testing against the old Spanish endpoint names would get a 404.

## Alternatives Considered

### Keep original Spanish identifiers and endpoints unchanged

- **ALT-001**: **Description**: Containerize and deploy the code exactly as-is, treating this as an infrastructure-only exercise.
- **ALT-002**: **Rejection Reason**: Directly contradicts the student's explicit instruction to have code and comments in English.

### Translate comments/identifiers only, keep Spanish endpoint paths

- **ALT-003**: **Description**: Rename Java classes/methods/fields to English but leave the public HTTP routes (`/suma`, `/resta`, `/div`) unchanged for backward compatibility with any existing evidence/screenshots.
- **ALT-004**: **Rejection Reason**: This is a fresh private repository with no existing consumers or prior evidence to preserve compatibility with, so there was no benefit to a half-translated public API.

## Implementation Notes

- **IMP-001**: All test evidence (local Docker run and Minikube deployment) uses the new English endpoint names (`/add`, `/subtract`, `/divide`).
- **IMP-002**: README (Spanish) documents both the new endpoint names and the attribution to the original Spanish-language project.
- **IMP-003**: Success criteria: `grep`-ing the `src/` tree for the original Spanish identifiers (`sumar`, `restar`, `dividir`, `Respuesta`, `ControlCalculadora`) returns no matches.

## References

- **REF-001**: Original source: `gmacastil/micro-calc`, `src/main/java/com/mauricio/clase/`.
- **REF-002**: [[adr-0001-private-repo-instead-of-fork]] — README attribution requirement this ADR's translation note satisfies.
