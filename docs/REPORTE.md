# Reporte — Reto 5: Docker, Docker Hub y Despliegue en Minikube sobre Windows

> **Estado: en progreso.** Este documento se actualiza a medida que avanzan las fases pendientes (publicación en Docker Hub, push a GitHub, despliegue en Minikube). Es la base para el PDF final con capturas, comandos y explicación de resultados.

## 1. Descripción del reto

Tomar el microservicio Spring Boot base [`gmacastil/micro-calc`](https://github.com/gmacastil/micro-calc), containerizarlo, publicarlo en Docker Hub, crear manifiestos de Kubernetes y desplegarlo en Minikube sobre Windows, dejando evidencia de cada paso.

## 2. Requerimientos y estado

| # | Entregable (según `final.md`) | Estado |
|---|---|---|
| 1 | URL del repositorio usado o fork del repositorio base | Repo privado creado: `https://github.com/Jhont3/micro-calc-k8s` (no es un fork nativo — ver §4). Pendiente `git push`. |
| 2 | Dockerfile funcional | Hecho y validado localmente. |
| 3 | Imagen publicada en Docker Hub | Pendiente — bloqueado en `docker login` del usuario. |
| 4 | Carpeta `k8s/` con manifiestos YAML | Hecho (raíz del repo, no anidada). |
| 5 | Evidencias de ejecución local con Docker | Hecho — ver §5. |
| 6 | Evidencias de despliegue en Minikube | Pendiente. |
| 7 | Evidencias de prueba del microservicio | Parcial (local); falta la prueba post-despliegue en Minikube. |
| 8 | PDF final con capturas, comandos y explicación | Pendiente — este documento es su borrador base. |

## 3. Requisitos previos verificados en el entorno

- **Docker Desktop** 4.46.0, backend WSL2 (distro `docker-desktop`) — instalado, se inició durante la ejecución.
- **kubectl** v1.32.2 y **Minikube** v1.38.1 (CLI) ya estaban instalados; existía un perfil de Minikube previo (`driver=docker`, contenedor `kicbase:v0.0.50`, detenido).
- **JDK**: solo había JDK 26 (instalado por IntelliJ, en `.jdks\openjdk-26*`). La guía pide explícitamente JDK 21, así que se instaló **Eclipse Temurin 21.0.11** vía `winget` en paralelo al resto del trabajo, en vez de usar el 26.
- Maven no fue necesario instalarlo aparte — se usó el wrapper (`mvnw.cmd`) incluido en el proyecto.
- Puerto 8080 verificado libre antes de correr contenedores (se revisó `docker ps -a` por posibles contenedores de la clase anterior — no había ninguno ocupándolo).

## 4. Qué se hizo

### 4.1 Metodología
Se siguió un flujo de *spec-driven development* ligero: los requisitos y criterios de aceptación se formalizaron primero en `docs/spec.md` (derivados 1:1 del checklist de `final.md`), con un `docs/plan.md` de implementación, antes de escribir código o manifiestos. Las decisiones no evidentes quedaron documentadas como Architecture Decision Records en `docs/adr/` (generados con el skill `create-architectural-decision-record`).

### 4.2 Repositorio
GitHub no permite forks privados de un repositorio público. Se creó en su lugar un repositorio **privado** nuevo (`Jhont3/micro-calc-k8s`), con atribución al proyecto original en el README, para poder revisar el trabajo localmente antes de publicarlo (ver `adr/adr-0001-private-repo-instead-of-fork.md`).

### 4.3 Traducción del código a inglés
Por pedido explícito, todo el código fuente (clases, métodos, variables, endpoints) se tradujo al inglés; el README se dejó en español. Detalle completo en `adr/adr-0003-translate-codebase-to-english.md`. Cambios principales:

| Original (español) | Traducido (inglés) |
|---|---|
| `com.mauricio.clase` | `com.mauricio.calculator` |
| `ControlCalculadora` | `CalculatorController` |
| `Respuesta` (campo `resultado`) | `CalculationResponse` (campo `result`) |
| `/suma`, `/resta`, `/div` | `/add`, `/subtract`, `/divide` |

De paso, se corrigió el orden de parámetros del método `subtract` (en el original, `restar(b, a)` para la ruta `/resta/{a}/{b}` funcionaba correctamente solo por el binding de Spring por nombre — se dejó explícito y en el mismo orden que la URL, ya que el archivo se estaba reescribiendo de todas formas).

### 4.4 Actuator
Se agregó `spring-boot-starter-actuator` para tener un `/actuator/health` real (usado en los probes de Kubernetes). Se confirmó que no representa riesgo: el proyecto no tiene JPA/JDBC en el classpath, así que el healthcheck no intenta abrir ninguna conexión real.

### 4.5 Build y prueba local (jar directo)
```powershell
.\mvnw.cmd clean package -DskipTests
java -jar target\demo-micro-0.0.1-SNAPSHOT.jar
```
Resultado de las pruebas (JDK 21, antes de containerizar):
```
/add/5/3        -> {"a":5,"b":3,"error":"NO","result":8}
/subtract/10/4  -> {"a":10,"b":4,"error":"NO","result":6}
/divide/8/2     -> {"a":8,"b":2,"error":"NO","result":4}
/divide/8/0     -> {"a":8,"b":0,"error":"cannot divide by zero, result is infinite","result":0}
/               -> admin,localhost
/actuator/health -> {"groups":["liveness","readiness"],"status":"UP"}
```

### 4.6 Dockerfile y validación local con Docker
`Dockerfile` de una sola etapa (single-stage), igual al patrón usado en el proyecto de referencia `equipos` del mismo diplomado — no se usó multi-stage porque la guía ya exige JDK/Maven locales, así que no aporta beneficio real aquí.

```powershell
docker build . -t demo-micro:1
docker run --name demo-micro1 -d -p 8080:8080 demo-micro:1
```
Se repitieron las mismas pruebas contra el contenedor con resultados idénticos, y se confirmó que el proceso corre como usuario no-root:
```
docker exec demo-micro1 whoami
-> appuser
```

### 4.7 Manifiestos de Kubernetes
Carpeta `k8s/` en la **raíz** del repo (no anidada en `deploy/k8s/` como en `equipos`), porque el entregable #4 dice literalmente "Carpeta `k8s/`" (ver `adr/adr-0004-root-level-k8s-directory.md`):

- `configmap.yaml` — valores `DB_SERVER`/`DB_USER` **deliberadamente distintos** a los defaults de `application.properties`, para que el endpoint `/` sirva como prueba visual de que el ConfigMap realmente se está usando una vez desplegado.
- `deployment.yaml` — imagen `jhont3/demo-micro:1`, probes de liveness/readiness contra `/actuator/health`, namespace dedicado `reto5`.
- `service.yaml` — tipo **NodePort** (no `ClusterIP` como en `equipos`): `minikube service` no puede tunelizar contra ClusterIP, y bajo el driver Docker en Windows la IP de Minikube no es alcanzable directamente desde el host (ver `adr/adr-0002-nodeport-service-for-minikube.md`).
- `hpa.yaml` — opcional, réplica de 1 a 3, CPU 70%.

## 5. Evidencias recolectadas hasta ahora

- Salida completa de `docker build` (capas descargadas, imagen `demo-micro:1` creada).
- Salida de `docker run` + `docker ps` (contenedor `demo-micro1` corriendo, puerto `0.0.0.0:8080->8080/tcp`).
- Respuestas de los 5 endpoints, en local (jar) y en contenedor Docker (ver §4.5–4.6).
- `docker exec demo-micro1 whoami` → `appuser` (confirma usuario no-root).

*(Nota de formato: estas son transcripciones de comandos+resultados, no capturas de pantalla nativas de la terminal. Sirven como evidencia técnica real, pero si se requieren capturas de pantalla literales de la ventana de PowerShell, hay que re-ejecutar estos mismos comandos manualmente para fotografiarlas.)*

## 6. Pendiente

1. `docker login` (el usuario debe ejecutarlo — no se manejan credenciales por este medio) → `docker tag`/`push` de `demo-micro:1` a `jhont3/demo-micro:1` en Docker Hub.
2. Revisión del usuario de los 5 commits locales → `git push` a `Jhont3/micro-calc-k8s`.
3. `minikube start --driver=docker --cpus=2 --memory=4000` → `kubectl create namespace reto5` → `kubectl apply -f k8s/`.
4. Prueba post-despliegue vía `kubectl port-forward` (y bono `minikube service --url`, `minikube dashboard`), confirmando que `/` muestra los valores del ConfigMap y no los defaults locales.
5. Captura final de evidencias y ensamblado del PDF (skill `pdf`).

## 7. Observaciones generales

- El comando `winget install EclipseAdoptium.Temurin.21.JDK` requiere abrir una consola nueva o fijar `JAVA_HOME`/`PATH` manualmente en la sesión actual — el `PATH` de Windows no se refresca en una sesión ya abierta.
- El perfil de Minikube (`driver=docker`) ya existía de una sesión anterior (contenedor `kicbase:v0.0.50`, detenido) — confirma que "miniduke instalado en una imagen de docker hub" se refería a Minikube corriendo sobre el driver Docker.
- El plugin ECC (`affaan-m/ECC`) se evaluó pero no se instaló globalmente: es un bundle de 67 agentes/277 skills para 8 frameworks distintos, y por diseño propio no sigue spec-driven development (es "research-first" + verificación/evals). Su agente `architect` (dentro del sub-framework Kiro que empaqueta) es funcionalmente equivalente a combinar las herramientas nativas de Claude Code ya usadas en este proyecto (agente `Plan` + skills de arquitectura/ADR). La instalación del plugin en sí (`/plugin marketplace add` / `/plugin install`) requiere que el usuario ejecute esos comandos directamente.
