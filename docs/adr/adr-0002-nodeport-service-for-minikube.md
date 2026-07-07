---
title: "ADR-0002: NodePort Service instead of ClusterIP for Minikube exposure on Windows"
status: "Accepted"
date: "2026-07-07"
authors: "Jhont3 (student)"
tags: ["architecture", "decision", "kubernetes", "minikube"]
supersedes: ""
superseded_by: ""
---

# ADR-0002: NodePort Service instead of ClusterIP for Minikube exposure on Windows

## Status

**Accepted**

## Context

The assignment requires validating the deployed microservice "from the browser or from PowerShell." The sibling reference project from the same course, `equipos`, uses a `ClusterIP` Service. This project runs on Windows with Minikube's **Docker driver** (Docker Desktop + WSL2) — under this driver, the Minikube node's internal IP (e.g. `192.168.49.2`) lives inside Docker Desktop's internal VM network and is **not** directly reachable from the Windows host. Additionally, `minikube service <name> --url` — the idiomatic way to get a browsable URL out of Minikube — only works against `NodePort` or `LoadBalancer` Services; it cannot tunnel to a `ClusterIP` Service.

## Decision

Use `type: NodePort` for the Service, deviating deliberately from the `equipos` convention. Two testing paths follow from this:

1. **Primary/reliable**: `kubectl port-forward svc/demo-micro 8080:9080 -n reto5` — works regardless of Service type or driver, used for the graded evidence.
2. **Bonus**: `minikube service demo-micro -n reto5 --url` — now works because the Service is NodePort, demonstrating idiomatic Minikube usage (an explicit learning objective of the assignment).

## Consequences

### Positive

- **POS-001**: `minikube service --url` becomes usable, satisfying the assignment's explicit learning objective around Minikube service exposure.
- **POS-002**: `kubectl port-forward` remains available regardless, so there is always a deterministic, driver-agnostic way to reach the service for screenshots.
- **POS-003**: No dependency on `minikube tunnel` (which requires an elevated/admin terminal and stays attached in the foreground).

### Negative

- **NEG-001**: Deviates from the `equipos` project's convention, so the two sibling projects are not visually consistent if compared side-by-side.
- **NEG-002**: NodePort opens a port in the 30000-32767 range on the node for the lifetime of the Service — irrelevant for a local, disposable Minikube cluster, but would need reconsideration in a shared/production cluster.

## Alternatives Considered

### ClusterIP + kubectl port-forward only (mirrors equipos exactly)

- **ALT-001**: **Description**: Keep `type: ClusterIP`, rely solely on `kubectl port-forward` for all testing.
- **ALT-002**: **Rejection Reason**: Works, but forfeits the `minikube service --url` demonstration entirely (it errors on ClusterIP with no NodePort to tunnel to), losing an easy piece of evidence for an explicit learning objective.

### minikube tunnel + LoadBalancer Service

- **ALT-003**: **Description**: Use `type: LoadBalancer` and run `minikube tunnel` to get an external IP.
- **ALT-004**: **Rejection Reason**: `minikube tunnel` requires an admin/elevated terminal on Windows and must stay running in the foreground, adding friction for a one-off student demo compared to NodePort + port-forward.

## Implementation Notes

- **IMP-001**: `k8s/service.yaml` sets `type: NodePort`, `port: 9080`, `targetPort: 8080`.
- **IMP-002**: Do not attempt to hit `http://<minikube ip>:<nodePort>` directly from the Windows host — this is the specific dead end this ADR avoids.
- **IMP-003**: Success criteria: both `kubectl port-forward` and `minikube service --url` return successful HTTP responses from the deployed pod.

## References

- **REF-001**: Assignment brief (`reto final/final.md`): "validar su funcionamiento desde el navegador o desde PowerShell."
- **REF-002**: `equipos/deploy/k8s/service.yaml` (reference project, `ClusterIP` convention this ADR deviates from).
