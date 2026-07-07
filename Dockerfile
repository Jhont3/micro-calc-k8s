FROM eclipse-temurin:21-alpine-3.21

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

COPY target/*.jar app.jar

RUN chown appuser:appgroup app.jar

USER appuser

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
