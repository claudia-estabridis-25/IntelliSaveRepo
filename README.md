# IntelliSave
Nuestra startup Lumen Save desarrolla un Sistema Inteligente de Gestión de Energía para Empresas.

IntelliSave es una plataforma web innovadora orientada a ayudar a las organizaciones a monitorear, analizar y optimizar su consumo energético. Su propósito principal es brindar visibilidad sobre el uso de la electricidad dentro de las diferentes sedes, áreas y equipos de una empresa, permitiendo identificar oportunidades de ahorro, reducir costos operativos y promover prácticas sostenibles.

Este repositorio contiene el **API REST (backend)** de IntelliSave.

## Equipo

Curso: Arquitectura de Aplicaciones Web (SI705) — UPC, 2026-20 — Equipo 3

| Integrante | Usuario de GitHub | Rama de trabajo |
|---|---|---|
| Claudia Estabridis | [claudia-estabridis-25](https://github.com/claudia-estabridis-25) | `ClaudiaEstabridis` |
| Juan Sebastián Finetti | [Jsebas05](https://github.com/Jsebas05) | `Juan_Finetti` |
| Kevin Díaz | [Bytesing777](https://github.com/Bytesing777) | `Kevin` |
| Sebastián Ramírez | [sebastianrs13](https://github.com/sebastianrs13) | `Sebastian_Ramirez` |
| Francis *(apellido por completar)* | *(por completar)* | `Francis` |

Cada integrante trabaja en su propia rama y sus cambios se integran a `main` mediante Pull Requests.

## Tecnologías y herramientas

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Spring Security) |
| Seguridad | JWT con OAuth2 Resource Server, contraseñas cifradas con BCrypt, permisos por rol con `@PreAuthorize` |
| Base de datos | PostgreSQL (Hibernate como ORM) |
| Documentación del API | springdoc-openapi 3.1.0 (Swagger UI) |
| Mapeo de DTOs | ModelMapper 3.2.6 |
| Validaciones | Jakarta Validation |
| Reportes PDF | OpenPDF 2.0.3 |
| Correo | Spring Boot Starter Mail |
| API externa de clima | [Open-Meteo](https://open-meteo.com/) |
| Gestión de dependencias | Maven (incluye el wrapper `mvnw`) |
| IDE | IntelliJ IDEA |
| Control de versiones | Git y GitHub |
| Gestión del proyecto | Trello (Product Backlog y Sprint Backlog) |

## Cómo ejecutar el proyecto en IntelliJ IDEA

### Requisitos
- JDK 21
- PostgreSQL con una base de datos llamada `Intelli_Save`
- IntelliJ IDEA (Community o Ultimate)

### Pasos
1. Clonar el repositorio:
   ```bash
   git clone https://github.com/claudia-estabridis-25/IntelliSaveRepo.git
   ```
2. En IntelliJ IDEA, ir a **File → Open** y seleccionar la carpeta `IntelliSaveRepo`. IntelliJ detecta el `pom.xml` y descarga las dependencias de Maven automáticamente.
3. Verificar en **File → Project Structure → Project** que el SDK sea Java 21.
4. Crear la base de datos `Intelli_Save` en PostgreSQL. Las tablas se crean solas al iniciar la aplicación (`spring.jpa.hibernate.ddl-auto=update`).
5. Ejecutar la clase `IntelliSaveAppApplication` con el botón ▶ **Run**.
6. Abrir Swagger UI en: <http://localhost:8080/swagger-ui.html>

### Variables de entorno (opcionales)
La configuración de `application.properties` usa el formato `${VARIABLE:valor por defecto}`. En local no es necesario definir nada: se usan los valores por defecto. Al desplegar en la nube se definen las variables de entorno para no exponer credenciales en el repositorio.

| Variable | Uso | Valor por defecto (local) |
|---|---|---|
| `DB_URL` | URL de conexión a PostgreSQL | `jdbc:postgresql://localhost/Intelli_Save` |
| `DB_USERNAME` | Usuario de la base de datos | `postgres` |
| `DB_PASSWORD` | Contraseña de la base de datos | `root` |
| `JWT_SECRET` | Clave para firmar los tokens JWT | Clave de desarrollo |
| `PORT` | Puerto del servidor | `8080` |

En IntelliJ se configuran en **Run → Edit Configurations → Environment variables**.

## Seguridad y uso del API

1. Al iniciar por primera vez, la aplicación crea un usuario administrador (`DataInitializer`):
   - Correo: `admin@intellisave.pe`
   - Contraseña: `admin123`
2. Iniciar sesión con `POST /login`:
   ```json
   { "username": "admin@intellisave.pe", "password": "admin123" }
   ```
3. Copiar el `token` de la respuesta y, en Swagger UI, presionar **Authorize** y pegarlo. El token dura 5 horas.

Roles del sistema:

| Rol | Descripción |
|---|---|
| `ADMIN` | Administrador de la empresa: gestiona empresa, sedes, áreas y usuarios |
| `SUPERVISOR` | Supervisor energético: gestiona equipos, consumos, tarifas, alertas, reportes y predicciones |
| `EMPLOYEE` | Empleado: registra el consumo de los equipos de su propia área |

## Endpoints principales

| Módulo | Ruta base | Funcionalidades |
|---|---|---|
| Autenticación | `/login` | Inicio de sesión y generación del token JWT |
| Empresas | `/api/companies` | CRUD, búsqueda por RUC, empresas por sector, estructura de sedes y áreas |
| Sedes | `/api/branches` | CRUD, sedes por empresa, estructura, estado de equipos, consumo y potencia instalada |
| Áreas | `/api/departments` | CRUD, áreas por sede, búsqueda por nombre |
| Usuarios | `/api/users` | Registro de supervisores y empleados, actualización, desactivación, perfil propio (`/me`), conteos por rol y área |
| Equipos | `/api/equipments` | CRUD, equipos por estado, dar de baja |
| Registros de consumo | `/api/consumption-records` | CRUD, historial con filtros, consumo por área, sede y categoría, promedio y consumo elevado |
| Tarifas | `/api/tariffs` | Registro, actualización, tarifa vigente por sede, tarifas por proveedor |
| Alertas | `/api/alerts` | CRUD, generación de alertas, historial, conteos por estado, prioridad y área |
| Huella de carbono | `/api/carbon-footprints` | CRUD, cálculo por área, comparación entre periodos, emisiones por área y sede, constancia en PDF |
| Clima | `/api/climate-records` | Consulta del clima por sede y área, actualización desde Open-Meteo |
| Predicciones | `/api/predictions` | Predicción de consumo, historial, evaluación contra el consumo real, predicciones por área |
| Reportes | `/api/reports` | Reporte de consumo en JSON, PDF y CSV |

La documentación completa de cada endpoint, con sus parámetros y permisos, está en Swagger UI.

## Tareas automáticas

La clase `configs/ScheduledTasks` ejecuta procesos sin intervención del usuario (hora de Lima):

| Tarea | Frecuencia | Historia de usuario |
|---|---|---|
| Consultar el clima de todas las sedes | Cada hora | HU17 |
| Detectar consumos anormales de la última semana y generar alertas | Todos los días a las 23:00 | HU21 |

## Estructura del proyecto

```
src/main/java/pe/edu/upc/intellisaveapp
├── clients             # Cliente de la API externa de clima (Open-Meteo)
├── configs             # Configuración general, usuario inicial y tareas programadas
├── controllers         # Endpoints REST
├── dtos                # Objetos de transferencia de datos
├── entities            # Entidades JPA (tablas de la base de datos)
├── exceptions          # Excepciones propias y manejador global de errores
├── repositories        # Repositorios JPA y consultas nativas
├── securities          # Configuración de seguridad, JWT y Swagger
├── servicesimplements  # Implementación de la lógica de negocio
└── servicesinterfaces  # Interfaces de los servicios
```
