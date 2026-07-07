---
title: "ADR-0001: Private repository instead of a GitHub fork"
status: "Accepted"
date: "2026-07-07"
authors: "Jhont3 (student)"
tags: ["architecture", "decision", "github"]
supersedes: ""
superseded_by: ""
---

# ADR-0001: Private repository instead of a GitHub fork

## Status

**Accepted**

## Context

The assignment requires submitting "the repo used, or a fork of the base repo" (`gmacastil/micro-calc`). The student wants to keep the work private and review it locally before anything is pushed publicly. GitHub's native "Fork" action, however, always creates a **public** copy when the source repository is public — there is no option to fork privately.

## Decision

Create a brand-new, empty **private** repository (`Jhont3/micro-calc-k8s`) instead of using GitHub's Fork button. The original source is copied in manually (not via `git fork`/`git clone --mirror` of history) and clearly attributed to `gmacastil/micro-calc` in the README. This satisfies the assignment's "repo used" wording, which the brief explicitly offers as an alternative to a literal fork.

## Consequences

### Positive

- **POS-001**: Full privacy — the work-in-progress is never visible publicly until the student explicitly decides to push and/or change visibility.
- **POS-002**: Local commits can be made and reviewed step-by-step before any `git push`, matching the student's explicit review-before-publish requirement.
- **POS-003**: No dependency on GitHub's fork network or the upstream repository's settings/availability.

### Negative

- **NEG-001**: Loses GitHub's native "forked from gmacastil/micro-calc" lineage link and the associated network graph/PR-back-to-upstream capability.
- **NEG-002**: Attribution is only informal (a README note), not enforced by GitHub's data model — relies on the student keeping it accurate.

## Alternatives Considered

### Native GitHub fork, kept public

- **ALT-001**: **Description**: Use GitHub's "Fork" button directly on `gmacastil/micro-calc`, which preserves lineage and is the most literal reading of the assignment.
- **ALT-002**: **Rejection Reason**: Would be public immediately upon creation, with no way to review privately first — conflicts with the student's explicit requirement to review before publishing.

### Native GitHub fork, then made private after creation

- **ALT-003**: **Description**: Fork publicly, then attempt to change the fork's visibility to private.
- **ALT-004**: **Rejection Reason**: GitHub does not permit changing a fork's visibility to private while it remains part of the upstream fork network; this is a platform-level restriction, not a settings toggle.

## Implementation Notes

- **IMP-001**: Repository created via the GitHub web UI: owner `Jhont3`, name `micro-calc-k8s`, visibility Private, no README/.gitignore/license pre-seeded (so the first local push is a clean fast-forward).
- **IMP-002**: README includes a line attributing the base project: `Basado en gmacastil/micro-calc`.
- **IMP-003**: Success criteria: repository exists, is private, `origin` remote points to it, and it is not connected to the upstream fork network.

## References

- **REF-001**: Assignment brief (`reto final/final.md`), deliverable #1: "URL del repositorio usado o fork del repositorio base."
- **REF-002**: [[adr-0003-translate-codebase-to-english]] — the README attribution note lives in the same repository this ADR creates.
