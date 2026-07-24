# Fitness Full-Stack App

A merged Spring Boot microservices backend and Vite React frontend for tracking fitness activities and viewing AI recommendations.

## Services

- Eureka Server
- Config Server
- API Gateway
- User Service
- Activity Service
- AI Service
- React Frontend

## Technologies

- Java
- Spring Boot
- Spring Cloud
- Eureka Discovery
- Spring Cloud Gateway
- Docker
- Maven
- React
- Vite
- Material UI
- Redux Toolkit

## Run

1. Start Docker infrastructure for Kafka and Keycloak:
   ```powershell
   npm run infra:up
   ```
2. Make sure your local databases are running:
   - MongoDB on `localhost:27017`
   - Postgres on `localhost:5432`
   - Postgres database: `fitness-micro-user`
   - Postgres username/password: `postgres` / `postgres`
3. Start backend services in separate terminals:
   ```powershell
   .\eureka\mvnw.cmd -f eureka\pom.xml spring-boot:run
   .\configserver\mvnw.cmd -f configserver\pom.xml spring-boot:run
   .\userservice\mvnw.cmd -f userservice\pom.xml spring-boot:run
   .\activityservice\mvnw.cmd -f activityservice\pom.xml spring-boot:run
   .\aiservice\mvnw.cmd -f aiservice\pom.xml spring-boot:run
   .\gateway\mvnw.cmd -f gateway\pom.xml spring-boot:run
   ```
4. Install frontend dependencies and start Vite from the repository root:
   ```powershell
   npm install
   npm run dev
   ```
5. Open `http://localhost:5173`.

## Default Local Auth

Docker Compose imports a `fitness-app` Keycloak realm with a public PKCE client:

- Client ID: `oauth2-pkce-client`
- Test user: `testuser`
- Test password: `Test@12345`

Keycloak admin is available at `http://localhost:8181` with `admin` / `admin`.

## Configuration

Frontend environment defaults live in `fitness-app-frontend/.env.example`. The frontend calls `/api`, and Vite proxies those requests to the API Gateway at `http://localhost:8080`.

MongoDB and Postgres are not managed by Docker Compose in this project. The backend services connect to local database instances through the existing `localhost` configuration.

The AI service requires Gemini configuration before recommendations can be generated:

- `GEMINI_KEY`
- `GEMINI_URL`
