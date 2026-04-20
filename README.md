# Backend Aventureros - Proyecto Web

Este es el repositorio del backend para el proyecto **Aventureros**, desarrollado para la asignatura de Desarrollo Web en la Pontificia Universidad Javeriana. El sistema está diseñado para la gestión y modelado de procesos de negocio (BPMN), permitiendo la administración integral de elementos de flujo de trabajo y seguridad de usuarios.

## 🚀 Descripción
El proyecto es una aplicación robusta construida con **Spring Boot** que implementa una arquitectura de microservicios o monolito modular (según el despliegue) para gestionar:
*   **Modelado de Procesos:** Gestión de Pools, Lanes, Actividades, Gateways y Arcos.
*   **Seguridad:** Autenticación y autorización centralizada.
*   **Notificaciones:** Sistema integrado de envío de correos electrónicos vía SMTP.
*   **Documentación:** Gestión y almacenamiento de documentos asociados a procesos.

## 🛠️ Tecnologías Principales
*   **Java 17**: Lenguaje de programación base.
*   **Spring Boot 4.0.3**: Framework principal para el desarrollo de la API REST.
*   **PostgreSQL**: Sistema de gestión de base de datos relacional.
*   **Spring Data JPA**: Abstracción para el manejo de la persistencia.
*   **Spring Security**: Implementación de seguridad y control de acceso.
*   **Lombok**: Reducción de código repetitivo (Boilerplate).
*   **Docker & Kubernetes**: Soporte para containerización y orquestación.
*   **SonarQube & JaCoCo**: Herramientas para análisis de código estático y cobertura de pruebas.

## 📁 Estructura del Proyecto
```text
src/main/java/com/edu/javeriana/backend/
├── config/       # Configuraciones de Seguridad, CORS y Beans.
├── controller/   # Controladores REST que exponen los endpoints.
├── model/        # Entidades JPA y Modelos de datos.
├── repository/   # Interfaces para operaciones CRUD en la BD.
├── service/      # Capa de lógica de negocio.
└── dto/          # Objetos de Transferencia de Datos (si aplica).
```

## 🚀 Configuración e Instalación

### Requisitos Previos
*   **JDK 17** o superior.
*   **Maven 3.8+**.
*   **PostgreSQL** instalado y configurado.

### Configuración del Entorno
Actualiza las propiedades en `src/main/resources/application.properties` para conectar con tu base de datos:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/nombre_bd
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
```

### Ejecución Local
Para iniciar la aplicación, utiliza el Maven Wrapper:

```bash
./mvnw spring-boot:run
```

La API estará disponible en `http://localhost:8080` (o el puerto configurado).

### Dockerización
Si deseas correr el proyecto en un contenedor:

```bash
docker build -t backend-aventureros .
docker run -p 8080:8080 backend-aventureros
```
O usando Compose:
```bash
docker-compose up -d
```

## 🧪 Pruebas y Calidad
Para asegurar la calidad del código, puedes ejecutar las pruebas y revisiones:

```bash
# Ejecutar pruebas unitarias
./mvnw test

# Generar reporte de JaCoCo
./mvnw jacoco:report
```

## 📧 Contacto y Contribución
Este proyecto es desarrollado por el **Grupo 13** de la clase de Desarrollo Web.
*   **Universidad:** Pontificia Universidad Javeriana.
*   **Proyecto:** Backend Aventureros.

---
*Desarrollado con ❤️ por el equipo de Aventureros.*
