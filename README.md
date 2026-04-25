# Proyecto de Gestión de Franquicias - API REST

API RESTful robusta y escalable desarrollada con **Spring Boot** para la gestión completa de franquicias, sucursales y su inventario de productos. Esta solución está diseñada siguiendo las mejores prácticas de desarrollo de software, incluyendo contenedorización con **Docker** y despliegue continuo en la nube con **Railway**.

## ✨ Características Principales

-   **API RESTful Completa:** Endpoints para operaciones CRUD sobre las entidades principales (Franquicias, Sucursales, Productos).
-   **Lógica de Negocio Avanzada:**
    -   Endpoint de análisis para obtener el producto con más stock por sucursal para una franquicia específica.
    -   Endpoints `PLUS` para actualizaciones parciales y eficientes de nombres de entidades y stock de productos.
-   **Arquitectura Limpia:** Separación de responsabilidades en capas (Controller, Service, Repository) y uso de DTOs y Mappers para un flujo de datos limpio y seguro.
-   **Contenedorización:** La aplicación y su base de datos PostgreSQL están completamente dockerizadas usando **Docker Compose**, garantizando un entorno de desarrollo local consistente y reproducible.
-   **Despliegue en la Nube:** La solución está desplegada en **Railway**, con un pipeline de CI/CD que despliega automáticamente los cambios desde la rama `main` del repositorio.

## 🚀 Despliegue y Acceso Público

La API está viva y accesible públicamente a través de la siguiente URL:

-   **URL Base:** `https://franquiciasbackend-production.up.railway.app`

En caso de que no se obtenga respuesta, es posible que el servidor se haya detenido temporalmente por inactividad, como parte de la optimización de recursos. De ser necesario, pueden notificar al área de soporte a través de correo electrónico para reactivar los servicios (soporte: mi persona).

### Colección de Postman

Para facilitar las pruebas, se incluye una colección de Postman (`collection.postman.json`) que contiene todas las peticiones a la API. La colección ya está configurada para apuntar a la URL de producción.

Tambien se incluye la colección (`collection.postman.local.json`) que contiene las mismas peticiones pero para apuntar a la API local.

---

## 🛠️ Stack Tecnológico

-   **Backend:** Java 17, Spring Boot 3
-   **Base de Datos:** PostgreSQL
-   **Contenedorización:** Docker, Docker Compose
-   **Plataforma de Despliegue:** Railway
-   **Build Tool:** Maven

---

## 📂 Estructura del Proyecto

El proyecto sigue una estructura estándar de Maven y Spring Boot, promoviendo la modularidad y mantenibilidad.

```
franquicias/
├── .mvn/
├── src/
│   ├── main/
│   │   ├── java/com/nimbachi/franquicias/
│   │   │   ├── controller/   # Controladores REST (API Endpoints)
│   │   │   ├── dto/          # Data Transfer Objects (Request/Response)
│   │   │   ├── exception/    # Manejo de excepciones globales
│   │   │   ├── mapper/       # Mapeo entre DTOs y Entidades (MapStruct)
│   │   │   ├── model/        # Entidades JPA (Tablas de la BD)
│   │   │   ├── repository/   # Repositorios (Acceso a datos con Spring Data JPA)
│   │   │   └── service/      # Lógica de negocio
│   │   └── resources/
│   │       ├── application.yaml  # Configuración de la aplicación
│   │       └── ...
│   └── test/                 # Pruebas unitarias y de integración
├── Dockerfile                # Receta para construir la imagen de la aplicación
├── docker-compose.yml        # Orquestación de servicios para entorno local
├── pom.xml                   # Dependencias y configuración del proyecto (Maven)
└── README.md                 # Esta documentación
```

---

## 📖 Guía de Endpoints de la API

A continuación se detallan los endpoints disponibles.

### Franquicias

#### `POST /api/franchises`
Crea una nueva franquicia.
-   **Request Body:**
    ```json
    {
        "name": "Franquicia Global"
    }
    ```
-   **Response (201 Created):**
    ```json
    {
        "status": "success",
        "message": "Franquicia creada exitosamente.",
        "data": {
            "id": 1,
            "name": "Franquicia Global"
        }
    }
    ```

#### `PUT /api/franchises/{franchiseId}/name`
Actualiza el nombre de una franquicia existente.
-   **Request Body:**
    ```json
    {
        "newName": "Franquicia Mundial"
    }
    ```

---

### Sucursales

#### `POST /api/franchises/{franchiseId}/branches`
Agrega una nueva sucursal a una franquicia.
-   **Request Body:**
    ```json
    {
        "name": "Sucursal Centro"
    }
    ```
-   **Response (201 Created):**
    ```json
    {
        "status": "success",
        "message": "Sucursal agregada a la franquicia exitosamente.",
        "data": {
            "id": 1,
            "name": "Sucursal Centro"
        }
    }
    ```

#### `PUT /api/branches/{branchId}/name`
Actualiza el nombre de una sucursal existente.
-   **Request Body:**
    ```json
    {
        "newName": "Sucursal Norte Renovada"
    }
    ```

---

### Productos

#### `POST /api/branches/{branchId}/products`
Agrega un nuevo producto a una sucursal.
-   **Request Body:**
    ```json
    {
        "name": "Laptop X1",
        "stock": 50
    }
    ```
-   **Response (201 Created):**
    ```json
    {
        "status": "success",
        "message": "Producto agregado a la sucursal exitosamente.",
        "data": {
            "id": 1,
            "name": "Laptop X1",
            "stock": 50
        }
    }
    ```

#### `DELETE /api/products/{productId}`
Elimina un producto de una sucursal.
-   **Response (200 OK):**
    ```json
    {
        "status": "success",
        "message": "Producto eliminado exitosamente.",
        "data": null
    }
    ```

#### `PATCH /api/products/{productId}/stock`
Modifica el stock de un producto.
-   **Request Body:**
    ```json
    {
        "newStock": 150
    }
    ```

#### `PUT /api/products/{productId}/name`
Actualiza el nombre de un producto.
-   **Request Body:**
    ```json
    {
        "newName": "Laptop Gamer Elite"
    }
    ```

---

### Endpoints de Consulta

#### `GET /api/franchises/{franchiseId}/products/top-stock`
Retorna el producto con más stock para cada sucursal de una franquicia específica.
-   **Response (200 OK):**
    ```json
    {
        "status": "success",
        "message": null,
        "data": [
            {
                "branchName": "Sucursal Centro",
                "productName": "Teclado Mecánico",
                "stock": 120
            },
            {
                "branchName": "Sucursal Norte Renovada",
                "productName": "Auriculares Gaming",
                "stock": 75
            },
            {
                "branchName": "Sucursal Sur",
                "productName": "Cable HDMI 4K",
                "stock": 150
            }
        ]
    }
    ```

---

## 💻 Cómo Ejecutar en Local

Para levantar el proyecto en un entorno local, solo necesitas tener [Docker](https://www.docker.com/products/docker-desktop/) instalado.

1.  **Clona el repositorio:**
    ```bash
    git clone https://github.com/NarenImbachi/franquicias_backend.git
    cd franquicias_backend
    ```

2.  **Levanta los servicios con Docker Compose:**
    Este comando construirá la imagen de la aplicación y levantará los contenedores de la app y la base de datos.
    ```bash
    docker-compose up --build -d
    ```

3.  **¡Listo!** La aplicación estará corriendo en `http://localhost:8080`.

### Comandos Útiles de Docker Compose

-   **Ver estado de los contenedores:** `docker-compose ps`
-   **Ver logs de la aplicación:** `docker-compose logs -f backend`
-   **Detener los servicios:** `docker-compose down`
-   **Detener y borrar datos de la BD:** `docker-compose down -v`
