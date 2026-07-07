---
title: "ADR-0004: Root-level k8s/ directory instead of nested deploy/k8s/"
status: "Accepted"
date: "2026-07-07"
authors: "Jhont3 (student)"
tags: ["architecture", "decision", "kubernetes", "repo-structure"]
supersedes: ""
superseded_by: ""
---

# ADR-0004: Root-level k8s/ directory instead of nested deploy/k8s/

## Status

**Accepted**

## Context

The sibling reference project `equipos` (same course) nests its Kubernetes manifests under `deploy/k8s/`. The assignment's deliverable checklist, however, literally names deliverable #4 as **"Carpeta `k8s/`"** — a folder named `k8s/`, without mentioning any parent directory.

## Decision

Place all Kubernetes manifests directly at the repository root, in `k8s/` (`k8s/configmap.yaml`, `k8s/deployment.yaml`, `k8s/service.yaml`, optionally `k8s/hpa.yaml`), rather than mirroring `equipos`' nested `deploy/k8s/` convention.

## Consequences

### Positive

- **POS-001**: Matches the literal, graded deliverable wording exactly — removes any ambiguity for a grader looking for a folder named `k8s/`.
- **POS-002**: `kubectl apply -f k8s/` is a shorter, more discoverable command from the repository root.

### Negative

- **NEG-001**: Breaks structural consistency with the `equipos` sibling project, so the two course projects are not directly comparable file-for-file.
- **NEG-002**: If this project later grows a `deploy/` directory for other purposes (e.g. Terraform, Compose), the Kubernetes manifests would be the odd one out at the root instead of grouped under it.

## Alternatives Considered

### Mirror equipos' deploy/k8s/ nested convention

- **ALT-001**: **Description**: Place manifests under `deploy/k8s/` for consistency with the other course project.
- **ALT-002**: **Rejection Reason**: Technically satisfies "a folder named k8s exists," but nested one level deeper than the deliverable's literal wording suggests, risking an avoidable grading ambiguity for no real benefit.

## Implementation Notes

- **IMP-001**: `k8s/` sits alongside `Dockerfile`, `pom.xml`, and `src/` at the repository root.
- **IMP-002**: Success criteria: `kubectl apply -f k8s/` run from the repository root applies all manifests with no path adjustment needed.

## References

- **REF-001**: Assignment brief (`reto final/final.md`), deliverable #4: "Carpeta `k8s/` con los manifiestos YAML."
- **REF-002**: `equipos/deploy/k8s/` (reference project, nested convention this ADR deviates from).
