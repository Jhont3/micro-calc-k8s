# micro-calc-k8s

Microservicio calculadora (Spring Boot) containerizado y desplegado en Kubernetes vía Minikube. Entregable del **Reto 5 — Docker, Docker Hub y Despliegue en Minikube sobre Windows** (Diplomado, Módulo 5).

Basado en [gmacastil/micro-calc](https://github.com/gmacastil/micro-calc), traducido a inglés (código, clases, endpoints) y adaptado para Docker + Kubernetes + Minikube en Windows.

## Sobre este repositorio

Este repositorio se creó desde cero a partir del proyecto base — no como fork nativo de GitHub, ya que GitHub no permite forks privados de repositorios públicos y el trabajo se desarrolló en privado hasta la entrega (ahora es **público** para su revisión). Ver [`docs/adr/adr-0001-private-repo-instead-of-fork.md`](docs/adr/adr-0001-private-repo-instead-of-fork.md) para el detalle de esta decisión.

## Arquitectura

Ciclo completo: build local → publicación en Docker Hub → despliegue en Minikube → acceso desde el host (detalle y explicación de cada flujo en [`docs/architecture.md`](docs/architecture.md)):

```mermaid
%%{init: {"theme": "base", "themeVariables": {
  "fontFamily": "Segoe UI, Helvetica, sans-serif",
  "fontSize": "14px",
  "lineColor": "#64748B",
  "primaryTextColor": "#1E293B"
}}}%%
flowchart TB

    subgraph HOST["🖥️  Windows 11 · Docker Desktop (WSL2)"]
        direction TB

        subgraph BUILD["Build local — JDK 21 + Maven wrapper"]
            direction LR
            SRC["Código fuente<br/>Spring Boot 4 · Java 21"]
            JAR["demo-micro.jar<br/>mvnw clean package"]
            IMG["Imagen local<br/>demo-micro:1<br/>(usuario no-root)"]
            SRC --> JAR
            JAR --> IMG
        end

        subgraph MK["Minikube — driver docker (contenedor kicbase)"]
            direction TB
            subgraph NS["Namespace reto5"]
                direction TB
                DEP["Deployment<br/>demo-micro · 1 réplica"]
                POD["Pod · Spring Boot :8080<br/>probes /actuator/health"]
                CM["ConfigMap<br/>DB_SERVER · DB_USER"]
                SVC["Service NodePort<br/>9080 → 8080"]
                HPA["HPA · 1–3 réplicas<br/>CPU 70%"]
                DEP --> POD
                CM -- "envFrom" --> POD
                SVC --> POD
                HPA -. "escala" .-> DEP
            end
            MS["metrics-server<br/>(addon)"]
            MS -. "métricas de CPU" .-> HPA
        end
    end

    HUB[("Docker Hub<br/>jhont3/demo-micro:1<br/>repositorio público")]
    GH[("GitHub · público<br/>Jhont3/micro-calc-k8s")]
    CLI["👤 Navegador / PowerShell"]

    IMG -- "docker push" --> HUB
    HUB -- "image pull" --> POD
    SRC -. "git push" .-> GH
    CLI -- "kubectl port-forward<br/>localhost:8080 → 9080" --> SVC
    CLI -- "minikube service --url<br/>127.0.0.1 (puerto aleatorio)" --> SVC

    classDef build fill:#DBEAFE,stroke:#2563EB,stroke-width:1.5px,color:#1E3A8A
    classDef k8s fill:#D1FAE5,stroke:#059669,stroke-width:1.5px,color:#064E3B
    classDef cfg fill:#FEF3C7,stroke:#D97706,stroke-width:1.5px,color:#78350F
    classDef registry fill:#EDE9FE,stroke:#7C3AED,stroke-width:1.5px,color:#4C1D95
    classDef client fill:#FCE7F3,stroke:#DB2777,stroke-width:1.5px,color:#831843
    classDef repo fill:#E2E8F0,stroke:#475569,stroke-width:1.5px,color:#1E293B

    class SRC,JAR,IMG build
    class DEP,POD,SVC,HPA,MS k8s
    class CM cfg
    class HUB registry
    class CLI client
    class GH repo

    style HOST fill:#F8FAFC,stroke:#94A3B8,stroke-width:1px
    style BUILD fill:#EFF6FF,stroke:#93C5FD,stroke-width:1px
    style MK fill:#F0FDFA,stroke:#5EEAD4,stroke-width:1px
    style NS fill:#ECFDF5,stroke:#6EE7B7,stroke-width:1px
```

## Endpoints

| Endpoint | Descripción |
|---|---|
| `GET /add/{a}/{b}` | Suma `a + b` |
| `GET /subtract/{a}/{b}` | Resta `a - b` |
| `GET /divide/{a}/{b}` | División `a / b` (maneja división por cero con un mensaje de error en vez de fallar) |
| `GET /` | Muestra la configuración externa activa (`db.user`, `db.server`) — útil para comprobar que el ConfigMap de Kubernetes está siendo usado en vez de los valores por defecto del jar |
| `GET /actuator/health` | Health check de Spring Boot Actuator, usado por los probes de Kubernetes |

## Metodología

Este proyecto sigue un flujo de *spec-driven development* ligero, sin depender de un framework externo:

- [`docs/spec.md`](docs/spec.md) — requisitos funcionales/no funcionales y criterios de aceptación, derivados 1:1 del checklist del reto.
- [`docs/plan.md`](docs/plan.md) — plan de implementación por fases.
- [`docs/adr/`](docs/adr/) — Architecture Decision Records documentando las decisiones que se desvían del proyecto de referencia `equipos` (mismo diplomado): repositorio privado en vez de fork, Service `NodePort` en vez de `ClusterIP`, traducción del código a inglés, y carpeta `k8s/` en la raíz.

## Requisitos

- JDK 21
- Docker Desktop (con backend WSL2 habilitado)
- kubectl
- Minikube

## Build y ejecución local

```powershell
.\mvnw.cmd clean package -DskipTests
docker build . -t demo-micro:1
docker run --name demo-micro1 -d -p 8080:8080 demo-micro:1
```

Probar:

```powershell
Invoke-RestMethod http://localhost:8080/add/5/3
Invoke-RestMethod http://localhost:8080/subtract/10/4
Invoke-RestMethod http://localhost:8080/divide/8/0
Invoke-RestMethod http://localhost:8080/
Invoke-RestMethod http://localhost:8080/actuator/health
```

## Publicar en Docker Hub

```powershell
docker tag demo-micro:1 <tu-usuario-dockerhub>/demo-micro:1
docker login
docker push <tu-usuario-dockerhub>/demo-micro:1
```

## Desplegar en Minikube

```powershell
minikube start --driver=docker --cpus=2 --memory=4000
kubectl config current-context   # debe decir "minikube"
kubectl apply -f k8s/            # el namespace reto5 se crea automáticamente (00-namespace.yaml aplica primero)
kubectl get pods,svc -n reto5
```

Si se va a usar el HPA opcional, habilitar antes el addon de métricas:

```powershell
minikube addons enable metrics-server
```

Probar (recomendado — funciona sin importar el driver de Minikube):

```powershell
kubectl port-forward svc/demo-micro 8080:9080 -n reto5
```

Y en otra terminal, repetir las mismas pruebas de arriba contra `http://localhost:8080`. El endpoint `/` debería mostrar ahora los valores del ConfigMap (`admin-k8s,postgres-k8s.reto5.svc.cluster.local`), no los valores por defecto del jar (`admin,localhost`) — la prueba más simple de que la configuración externalizada realmente funciona.

Bono:

```powershell
minikube service demo-micro -n reto5 --url
minikube dashboard
```

## Estructura

```
├── Dockerfile
├── .dockerignore
├── micro-calc.http       # set de peticiones de prueba (REST Client / IntelliJ HTTP)
├── k8s/                  # manifiestos de Kubernetes (raíz, no anidados)
│   ├── 00-namespace.yaml # namespace reto5 (aplica primero por orden alfabético)
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   └── hpa.yaml          # opcional, requiere "minikube addons enable metrics-server"
├── docs/
│   ├── spec.md
│   ├── plan.md
│   ├── REPORTE.md        # reporte de avance / base del PDF final
│   ├── architecture.md   # diagrama de arquitectura (Mermaid)
│   ├── capturas/         # capturas de pantalla de las evidencias
│   ├── Reto5_Evidencias_Jhonatan_Escobar.pdf   # PDF final del reto
│   └── adr/
└── src/                  # código fuente (en inglés)
```
