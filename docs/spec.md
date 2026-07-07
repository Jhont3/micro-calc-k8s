# Specification — Calculator Microservice on Kubernetes (Minikube)

## Overview

This project takes the base Spring Boot service from [`gmacastil/micro-calc`](https://github.com/gmacastil/micro-calc), ports it to English, containerizes it with Docker, publishes the image to Docker Hub, and deploys it to a local Kubernetes cluster (Minikube) on Windows. It is the deliverable for "Reto 5: Docker, Docker Hub y Despliegue en Minikube sobre Windows" (Diplomado, Módulo 5).

This document is the spec-driven-development artifact for the project: requirements and acceptance criteria are defined here first, and implementation is checked against them (see `plan.md` for the task breakdown and `adr/` for the reasoning behind non-obvious decisions).

## Functional requirements

The service exposes the following REST endpoints (all `GET`, path variables are integers):

| Endpoint | Behavior |
|---|---|
| `/add/{a}/{b}` | Returns `{a, b, result: a+b, error: "NO"}` |
| `/subtract/{a}/{b}` | Returns `{a, b, result: a-b, error: "NO"}` |
| `/divide/{a}/{b}` | Returns `{a, b, result: a/b}`, or `{a, b, error: <message>}` when `b == 0` |
| `/` | Returns a plain string built from the externally-configured `db.user` / `db.server` properties — used as a live proof that configuration is coming from the environment (local defaults vs. the Kubernetes ConfigMap) |
| `/actuator/health` | Standard Spring Boot Actuator health check, used by Kubernetes liveness/readiness probes |

## Non-functional requirements

- Java 21, Spring Boot 4.1.x, Maven (wrapper included, no local Maven install required).
- Stateless, single-container service — no database or persistent volume.
- Configuration externalized via environment variables (Spring relaxed binding: `DB_SERVER`, `DB_USER`), sourced from a Kubernetes ConfigMap in-cluster and from `application.properties` defaults locally.
- Runs as a non-root user inside the container.
- Source code, identifiers, and comments in English. README in Spanish (course deliverable audience).

## Deliverables and acceptance criteria

Mapped directly from the assignment's checklist:

1. **Repository URL** — Private GitHub repository `Jhont3/micro-calc-k8s` (GitHub does not allow private forks of a public repo — see `adr/adr-0001-private-repo-instead-of-fork.md`). *Accepted when:* pushed to GitHub and URL recorded in the final PDF.
2. **Functional Dockerfile** — root-level `Dockerfile`, single-stage, non-root user. *Accepted when:* `docker build` succeeds and the resulting image runs the service correctly.
3. **Image published on Docker Hub** — *Accepted when:* `docker push` succeeds and the tag is visible on a public Docker Hub repository page.
4. **`k8s/` folder with YAML manifests** — root-level (not nested), containing Deployment, Service, ConfigMap, and optionally HPA. *Accepted when:* `kubectl apply -f k8s/` succeeds with no errors.
5. **Evidence of local Docker execution** — *Accepted when:* screenshots show `docker run`, `docker ps`, and successful responses from all endpoints via PowerShell/browser.
6. **Evidence of Minikube deployment** — *Accepted when:* screenshots show `minikube start`, `kubectl apply`, and `kubectl get pods,svc` with the pod `Running` and ready.
7. **Evidence of testing the microservice** — *Accepted when:* screenshots show the same endpoint tests succeeding against the Minikube-exposed service, with `/` demonstrably returning the ConfigMap values (not the local defaults) — proof that externalized configuration works end-to-end.
8. **Final PDF** — screenshots, commands used, and a brief explanation of results.

## Out of scope

- `Secret` manifest — no property in this service is sensitive enough to warrant one (unlike the sibling `equipos` project, which has a real DB password).
- `docker-compose` — adds no value for a single container with no dependent services.
- CI/CD pipeline — not required by the assignment (the sibling `equipos` project has one; not reproduced here to keep scope tight).
- Multi-stage Dockerfile — the assignment's prerequisites already mandate a local JDK 21 + Maven, so a self-contained multi-stage build buys nothing here; single-stage mirrors the proven `equipos` convention.
