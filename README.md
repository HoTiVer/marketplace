# Marketplace Platform

A full-stack e-commerce marketplace developed as a Web Technologies and Databases course project.

The platform connects sellers and customers, providing product management, shopping, order processing, and communication functionality.

## Features

### Customer Features
- Browse products and categories.
- Search and explore product listings.
- Shopping cart and wishlist.
- Checkout and order tracking.
- View product promotions and discounted prices.
- Direct messaging with sellers.

### Seller Features
- Seller registration and management.
- Product creation, editing, and deletion.
- Product image management.
- Promotion management.
- Order management.
- Communication with customers.

### Platform Features
- JWT-based authentication and authorization.
- Role-based access control.
- Google OAuth 2.0 authentication.
- Email notifications.
- Product catalog and category management.
- Image storage using MinIO.

## Tech Stack

| Component | Technologies |
|---|---|
| Backend | Java 21, Spring Boot |
| Security | Spring Security, JWT, Google OAuth 2.0 |
| ORM | Spring Data JPA, Hibernate |
| Database | PostgreSQL |
| Caching / Storage | Redis |
| Object Storage | MinIO |
| Frontend | Next.js, React, TypeScript |
| API Documentation | OpenAPI / Swagger |
| Testing | JUnit 5, Mockito |
| Containerization | Docker, Docker Compose |

## Architecture

The project is organized as a monorepository containing two independent applications:

- **Backend** — a multi-module Maven project responsible for business logic, data persistence, security, and REST APIs.
- **Frontend** — a Next.js application responsible for the user interface and communication with the backend.

### Backend Architecture

The backend follows a modular layered architecture, separating application responsibilities and business logic.

Main architectural components:
- REST API layer.
- Service layer containing business logic.
- Repository layer for data access.
- Domain model and persistence entities.
- DTOs for API communication.
- Security configuration and JWT authentication.

### Infrastructure

- **PostgreSQL** — primary relational database.
- **Redis** — caching and token-related data storage.
- **MinIO** — object storage for product images.
- **Docker Compose** — orchestration of application and infrastructure containers.

### Authentication

The application uses JWT-based authentication with access and refresh tokens.

Spring Security handles authentication and role-based authorization. Google OAuth 2.0 provides an additional authentication method.

## Testing & Quality

The backend contains 200+ unit and integration tests covering important application functionality.

Test coverage includes:
- Business logic and service layer.
- REST controllers.
- Authentication and authorization.
- Data validation and application behavior.

Run backend tests:

```bash
cd backend
mvn test
```

## Requirements

### Running with Docker

- Docker Engine / Docker Desktop

### Local Development

- Java Development Kit (JDK) 21.
- Maven.
- Node.js 24+ and npm.
- PostgreSQL 17.
- Redis 7.
- MinIO.

Docker Compose can be used to run infrastructure services during local development.

## Environment Configuration

The project uses environment variables for application configuration.

### 1. Root Environment File

Create a `.env` file in the project root using the provided template:

```bash
cp .env.example .env
```

On Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

Configure the required credentials and application settings.

The root `.env` file is used by Docker Compose for variable substitution.

### 2. Environment Variables

| Variable | Description |
|---|---|
| `SPRING_PROFILES_ACTIVE` | Active Spring Boot profile |
| `DB_URL` | PostgreSQL connection URL for local backend execution |
| `DB_NAME` | PostgreSQL database name |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `JWT_SECRET` | Secret key used for JWT signing |
| `JWT_ACCESS_EXPIRATION` | Access token lifetime in milliseconds |
| `JWT_REFRESH_EXPIRATION` | Refresh token lifetime in milliseconds |
| `REDIS_HOST` | Redis hostname |
| `SERVICE_EMAIL` | Email address used by the application |
| `EMAIL_PASSWORD` | Email provider app password |
| `GOOGLE_CLIENT_ID` | Google OAuth client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth client secret |
| `FRONTEND_URL` | Frontend application URL |
| `IMAGE_STORAGE_HOST` | MinIO endpoint |
| `MINIO_USER` | MinIO username |
| `MINIO_PASSWORD` | MinIO password |
| `MINIO_BUCKET` | MinIO bucket name |
| `MAX_PRODUCT_IMAGES_COUNT` | Maximum number of product images |
| `NEXT_PUBLIC_API_URL` | Public backend URL used by the frontend |
| `API_INTERNAL_URL` | Backend URL used by server-side Next.js requests |

**Important:** Connection addresses depend on where the application runs.

When running services inside Docker Compose, use Docker service names:
- PostgreSQL: `database:5432`
- Redis: `redis:6379`
- MinIO: `minio:9000`
- Backend: `marketplace-backend:8080`

When running an application directly on the host machine, use `localhost` and the corresponding published ports.

Docker Compose overrides the backend connection settings for container-to-container communication.

### 3. Frontend Local Environment

For running Next.js outside Docker, create `frontend/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
API_INTERNAL_URL=http://localhost:8080
```

For Docker deployment, the frontend uses:
- `NEXT_PUBLIC_API_URL` during the image build.
- `API_INTERNAL_URL` at container runtime.

Never commit `.env`, `.env.local`, or files containing actual credentials.

## Running the Application

### Option 1: Run the Entire Application with Docker Compose

This is the recommended way to start the complete application.

1. Configure the root `.env` file.
2. Make sure Docker is running.
3. Open a terminal in the project root.
4. Build and start all services:

```bash
docker compose -f docker-compose.yml up --build -d
```

Or run backend and frontend separately

Backend:
```bash
docker compose -f docker-compose-backend.yml up --build -d
```
Frontend:
```bash
docker compose -f docker-compose-frontend.yml up --build -d
```

### Default Application URLs

| Service | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| MinIO Console | http://localhost:9001 |
| MinIO API | http://localhost:9000 |

PostgreSQL and Redis are accessible to other containers through the internal Docker network.

## Important Notes

- Make sure the required environment variables are configured before starting the application.
- Google OAuth requires valid Google Cloud credentials and a correctly configured redirect URI.
- Email notifications require valid email provider credentials.
- Do not use development credentials or demo account passwords in production.
- Persistent application data is stored in Docker volumes.