# Products API — Backend

API REST para la gestión de productos, desarrollada con Spring Boot. Incluye autenticación JWT, persistencia en PostgreSQL y mensajería asíncrona mediante RabbitMQ.

---

## Requisitos previos

| Herramienta     | Versión mínima |
|-----------------|----------------|
| Java (JDK)      | 21             |
| Maven           | 3.9+           |
| PostgreSQL      | 15+            |

---

## Stack tecnológico

| Componente              | Versión   |
|-------------------------|-----------|
| Spring Boot             | 3.3.4     |
| Spring Security         | (Boot BOM)|
| Spring Data JPA         | (Boot BOM)|
| PostgreSQL Driver       | (Boot BOM)|
| JJWT                    | 0.12.3    |
| Lombok                  | (Boot BOM)|

---

## Configuración de la base de datos

### 1. Crear la base de datos

```sql
CREATE DATABASE products_db;
```

### 2. Crear la tabla de productos

```sql
CREATE TABLE public.products (
    id      serial         NOT NULL,
    nombre  varchar(255)   NOT NULL,
    precio  numeric(38, 2) NOT NULL,
    stock   int4           NOT NULL,
    CONSTRAINT products_pkey       PRIMARY KEY (id),
    CONSTRAINT products_stock_check CHECK ((stock >= 0))
);
```

El mismo script crea `users`, `sales` y `sale_items`, y deja el usuario `admin`.

> La aplicación tiene `spring.jpa.hibernate.ddl-auto=none`, por lo que el esquema **debe crearse manualmente** antes de arrancar.

---

## Variables de configuración

Editar `src/main/resources/application.properties` con los valores del entorno:

```properties
# Servidor
server.port=8080

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5432/products_db
spring.datasource.username=<DB_USER>
spring.datasource.password=<DB_PASSWORD>

# JWT
app.jwt.secret=<SECRET_HEX_64_CHARS>
app.jwt.expiration=86400000          # 24 horas en milisegundos


---

## Levantar el proyecto

### Opción A — Maven Wrapper (sin instalar Maven)

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

### Opción B — Maven instalado

```bash
mvn spring-boot:run
```

### Opción C — JAR ejecutable

```bash
mvn clean package -DskipTests
java -jar target/products-api-0.0.1-SNAPSHOT.jar
```

La aplicación estará disponible en `http://localhost:8080`.

---

## Autenticación

La API usa **JWT Bearer Token**. El token no está en el código: cada usuario lo solicita con su usuario y contraseña.

Hay dos roles:

| Rol | Acceso |
|-----|--------|
| `ADMIN` | Productos, usuarios y todas las ventas |
| `VENDEDOR` | Solo sus propias ventas |

### Usuario inicial

El script `product_db.sql` crea un administrador local para desarrollo.

| Campo    | Valor      |
|----------|------------|
| username | `admin`    |
| password | `admin123` |
| rol      | `ADMIN`    |

### 1. Obtener token

**POST** `/api/auth/login`

```json
{
  "username": "admin",
  "password": "admin123"
}
```

Respuesta:

```json
{
  "token": "<JWT>",
  "role": "ADMIN"
}
```

El administrador crea el resto de usuarios. Un vendedor no puede hacerlo.

**POST** `/api/users` — rol `ADMIN`

```json
{
  "username": "vendedor1",
  "password": "vendedor123",
  "role": "VENDEDOR"
}
```

### 2. Usar el token

Incluir en el header de cada petición:

```
Authorization: Bearer <JWT>
```

---

## Endpoints de la API

### Productos — `/api/products`

| Método | Ruta                  | Descripción              | Auth |
|--------|-----------------------|--------------------------|------|
| GET    | `/api/products`       | Listar todos             | ADMIN |
| GET    | `/api/products/{id}`  | Obtener por ID           | ADMIN |
| POST   | `/api/products`       | Crear producto           | ADMIN |
| PUT    | `/api/products/{id}`  | Actualizar producto      | ADMIN |
| DELETE | `/api/products/{id}`  | Eliminar producto        | ADMIN |

### Usuarios — `/api/users`

| Método | Ruta         | Descripción        | Auth  |
|--------|--------------|--------------------|-------|
| POST   | `/api/users` | Crear usuario      | ADMIN |

### Ventas — `/api/sales`

| Método | Ruta             | Descripción                                      | Auth |
|--------|------------------|--------------------------------------------------|------|
| POST   | `/api/sales`     | Registrar venta y descontar stock                | ADMIN, VENDEDOR |
| GET    | `/api/sales`     | Listar ventas. El vendedor solo ve las suyas     | ADMIN, VENDEDOR |
| GET    | `/api/sales/{id}`| Obtener una venta. El vendedor solo ve las suyas | ADMIN, VENDEDOR |

```json
{
  "items": [
    { "productId": 1, "quantity": 2 }
  ]
}
```

El precio se toma del producto al momento de la venta. Si no hay stock suficiente la respuesta es `409`. Si el producto no existe, `404`.

### Autenticación — `/api/auth`

| Método | Ruta             | Descripción    | Auth |
|--------|------------------|----------------|------|
| POST   | `/api/auth/login`| Obtener token  | No   |

---

## Modelo de producto

```json
{
  "id":     1,
  "nombre": "Laptop",
  "precio": 999.99,
  "stock":  50
}
```

### Reglas de validación

| Campo    | Restricción                        |
|----------|------------------------------------|
| `nombre` | Requerido, no vacío                |
| `precio` | Requerido, valor positivo          |
| `stock`  | Requerido, mayor o igual a 0       |

---

## Mensajería RabbitMQ

La aplicación publica eventos de tipo `InvoiceCreatedEvent` al exchange `invoice.exchange` con dos routing keys:

| Routing Key             | Cola destino                  |
|-------------------------|-------------------------------|
| `invoice.stock`         | `invoice.stock.queue`         |
| `invoice.notification`  | `invoice.notification.queue`  |

### Estructura del evento

```json
{
  "invoiceId":   "INV-001",
  "productId":   1,
  "productName": "Laptop",
  "quantity":    2,
  "unitPrice":   999.99
}
```

---

## Documentación Swagger

Con la aplicación en marcha, la documentación de las APIs está en:

`http://localhost:8080/swagger-ui.html`

Ahí se prueban el login, la creación de usuarios, el CRUD de productos y la venta. El botón **Authorize** recibe el token devuelto por `/api/auth/login`.

---

## Estructura del proyecto

```
src/main/java/com/example/demo/
├── config/        SecurityConfig, OpenApiConfig, RabbitMQConfig
├── controller/    AuthController, UserController, ProductController, SaleController
├── dto/
├── entity/        Product, User, Sale, SaleItem
├── exception/
├── messaging/
├── repository/    ProductRepository, UserRepository, SaleRepository
├── security/
└── service/       ProductService, UserService, SaleService
```

---

## Manejo de errores

| Código HTTP | Situación                              |
|-------------|----------------------------------------|
| 400         | Datos de entrada inválidos (validación)|
| 401         | Credenciales incorrectas / token ausente|
| 403         | El rol no puede ejecutar la operación  |
| 404         | Recurso no encontrado                  |
| 409         | Sin stock o usuario duplicado          |
| 500         | Error interno del servidor             |

---

## Notas de seguridad

- El secreto JWT, la URL y la contraseña de base de datos se leen de las variables `JWT_SECRET`, `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`. En Azure deben venir de Key Vault o App Configuration, no del repositorio.
- Los valores de `application.properties` son solo el respaldo local. Hay que cambiarlos fuera de desarrollo.
- Los usuarios viven en la tabla `users`. El token se emite en el login y no queda escrito en el código.
- CORS habilitado únicamente para `http://localhost:4200` (frontend Angular en desarrollo).
