# ==========================================
# ETAPA 1: Build con Maven
# ==========================================
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar archivos de configuración de Maven primero (para aprovechar caché de capas)
COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .

# Dar permisos de ejecución al wrapper de Maven
RUN chmod +x mvnw

# Descargar dependencias (cacheadas si pom.xml no cambia)
RUN ./mvnw dependency:go-offline -B

# Copiar el código fuente
COPY src/ src/

# Compilar y empaquetar (omitiendo tests)
RUN ./mvnw package -DskipTests -B

# ==========================================
# ETAPA 2: Imagen de producción
# ==========================================
FROM eclipse-temurin:21-jre-alpine AS production

WORKDIR /app

# Instalar curl para el healthcheck
RUN apk add --no-cache curl

# Crear usuario no-root por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copiar el WAR generado desde la etapa de build
COPY --from=builder /app/target/*.war app.war

# Cambiar propietario del archivo
RUN chown appuser:appgroup app.war

# Usar usuario no-root
USER appuser

# Puerto expuesto por Spring Boot (Tomcat embebido)
EXPOSE 8080

# Healthcheck para verificar que la app está corriendo
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1


ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.war"]
