# Arquitectura — micro-calc-k8s

Vista completa del ciclo: build local → publicación en Docker Hub → despliegue en Minikube → acceso desde el host.

> **Para verlo en VS Code:** abrir este archivo y presionar `Ctrl+Shift+V` (vista previa de Markdown). Requiere la extensión *Markdown Preview Mermaid Support* (`bierner.markdown-mermaid`).

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

## Flujos

1. **Build (azul):** el código se compila con el Maven wrapper sobre JDK 21; el jar resultante se empaqueta en la imagen `demo-micro:1` con usuario no-root.
2. **Publicación (violeta):** la imagen se etiqueta y sube a Docker Hub como `jhont3/demo-micro:1` (pública — el cluster puede jalarla sin `imagePullSecrets`).
3. **Despliegue (verde):** el Deployment en el namespace `reto5` crea el Pod; el kubelet de Minikube jala la imagen **desde Docker Hub**, no desde el daemon local — prueba de que la imagen publicada funciona por sí sola.
4. **Configuración (ámbar):** el ConfigMap inyecta `DB_SERVER`/`DB_USER` como variables de entorno (`envFrom`); el endpoint `/` los expone, demostrando la configuración externalizada frente a los defaults del jar.
5. **Acceso (rosa):** dos rutas desde el host — `kubectl port-forward` (principal, determinística) y el túnel de `minikube service --url` (bono; bajo el driver docker en Windows la IP del nodo no es alcanzable directamente, por eso el Service es NodePort — ver ADR-0002).
