# Implementation Plan

Condensed task breakdown for the work described in `spec.md`. See `adr/` for the reasoning behind decisions that deviate from the sibling `equipos` reference project.

## 1. Source port (done)
Copy `gmacastil/micro-calc` into this private repository, translate identifiers/endpoints/comments to English (see ADR 0003), add `spring-boot-starter-actuator`.

## 2. Local build & smoke test (done)
Build with `mvnw.cmd` on JDK 21. Run the jar directly and confirm `/add`, `/subtract`, `/divide`, `/`, and `/actuator/health` all respond correctly before touching Docker.

## 3. Containerize (done)
Root-level `Dockerfile`, single-stage (`eclipse-temurin:21-alpine`, non-root user), mirroring the proven `equipos` pattern — the assignment already mandates a local JDK/Maven, so a multi-stage build adds no benefit here. `docker build`, `docker run`, re-run the same endpoint tests against the container.

## 4. Publish to Docker Hub (done)
Tag, log in, push. Public repository (simplest; no `imagePullSecrets` needed for the cluster to pull it). Published as `jhont3/demo-micro:1`.

## 5. Kubernetes manifests (done)
Root-level `k8s/` (not nested, per the literal deliverable wording — see ADR 0004): `00-namespace.yaml` (sorts first so a bare `kubectl apply -f k8s/` is self-contained), `configmap.yaml` (with `DB_SERVER`/`DB_USER` values deliberately different from the local defaults, to prove externalized config works), `deployment.yaml` (liveness/readiness against `/actuator/health`), `service.yaml` (`NodePort`, not `ClusterIP` — see ADR 0002), optional `hpa.yaml`.

## 6. Deploy & validate on Minikube (done)
Dedicated `reto5` namespace. `minikube start --driver=docker`. `kubectl apply -f k8s/`. Validated via `kubectl port-forward` and `minikube service --url` (PowerShell and browser). Confirmed `/` returns the ConfigMap values in-cluster (`admin-k8s,postgres-k8s.reto5.svc.cluster.local`), not the jar's local defaults.

## 7. Evidence & PDF
Screenshots at each step, commands used, brief explanation of results — assembled into the final PDF deliverable.

## Commit discipline
Work is committed locally in logical, reviewable steps and held back from `git push` until explicitly reviewed by the repository owner (see ADR 0001 for why this repo exists as a fresh private copy rather than a public GitHub fork).
