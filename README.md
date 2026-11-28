# FitTrack 💪

**A Workout Management REST API built with Java 21 and Spring Boot.**

FitTrack is a backend application designed to help users manage workout plans, organize exercises, and build structured training routines.

The project focuses on secure authentication, role-based authorization, RESTful API design, and relational database management.

---

## 🚀 Features

### Authentication & Authorization
- User registration
- JWT-based authentication
- Stateless authentication with Spring Security
- Role-based access control
- Three user roles: `USER`, `TRAINER`, `ADMIN`

### User Management
- User registration
- Retrieve user profiles
- Retrieve the currently authenticated user
- Update user profiles

### Exercise Management
- Create exercises
- Retrieve all exercises
- Retrieve an exercise by ID
- Update exercises
- Delete exercises
- Role-based permissions for exercise management

### Workout Plan Management
- Create workout plans
- Retrieve personal workout plans
- Retrieve a workout plan by ID
- Update workout plans
- Delete workout plans
- Ownership-based access control

### Workout Plan Exercises
- Add exercises to workout plans
- Retrieve exercises within a workout plan
- Update exercise configuration
- Remove exercises from workout plans
- Configure sets, repetitions, rest time, and exercise order

---

## 🛠 Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 4.1.2 | Backend framework |
| Spring Security | Application security |
| JWT (JJWT) | Authentication |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL 16 | Relational database |
| Flyway | Database migrations |
| Maven | Dependency management |
| Docker Compose | Database environment |
| JUnit 5 | Testing |
| MockMvc | Controller testing |
| Testcontainers | Integration testing |

---

## 🏗 Project Architecture

The backend follows a layered architecture with separation of responsibilities.

```text
FitTrack/
│
├── backend/
│   └── FitTrackBackend/
│       ├── src/
│       │   ├── main/
│       │   │   ├── java/
│       │   │   │   └── org/dewqryp/fittrackbackend/
│       │   │   │       ├── auth/
│       │   │   │       ├── config/
│       │   │   │       ├── controller/
│       │   │   │       ├── exercise/
│       │   │   │       ├── user/
│       │   │   │       ├── workout/
│       │   │   │       └── workoutplanexercises/
│       │   │   │
│       │   │   └── resources/
│       │   │
│       │   └── test/
│       │
│       └── pom.xml
│
├── docker-compose.yml
└── README.md
```

### Architectural Principles

- Layered architecture
- Separation of concerns
- DTO-based request and response handling
- Dependency Injection
- RESTful API conventions
- Stateless security
- Database migrations

---

## 🔐 Security

FitTrack uses Spring Security and JWT to protect its REST API.

Authentication is stateless: clients authenticate using a JWT Bearer token.

Example:

```http
Authorization: Bearer <your_access_token>
```

### User Roles

| Role | Description |
|---|---|
| USER | Standard application user |
| TRAINER | Can manage exercises |
| ADMIN | Administrative access |

Public endpoints:
- `POST /api/v1/users`
- `POST /api/v1/auth/login`
- `GET /api/v1/health`

Other endpoints require authentication, with additional authorization checks where applicable.

---

## 📡 API Overview

Base URL:

```text
http://localhost:8080/api/v1
```

### Authentication

| Method | Endpoint | Description |
|---|---|---|
| POST | `/auth/login` | Authenticate user |

### Users

| Method | Endpoint | Description |
|---|---|---|
| POST | `/users` | Register user |
| GET | `/users/me` | Get current user |
| GET | `/users/{id}` | Get user by ID |
| PUT | `/users/{id}` | Update profile |

### Exercises

| Method | Endpoint | Description |
|---|---|---|
| GET | `/exercises` | Get all exercises |
| GET | `/exercises/{id}` | Get exercise |
| POST | `/exercises` | Create exercise |
| PUT | `/exercises/{id}` | Update exercise |
| DELETE | `/exercises/{id}` | Delete exercise |

### Workout Plans

| Method | Endpoint | Description |
|---|---|---|
| POST | `/workout-plans` | Create workout plan |
| GET | `/workout-plans` | Get personal plans |
| GET | `/workout-plans/{id}` | Get workout plan |
| PUT | `/workout-plans/{id}` | Update workout plan |
| DELETE | `/workout-plans/{id}` | Delete workout plan |

### Workout Plan Exercises

| Method | Endpoint | Description |
|---|---|---|
| POST | `/workout-plans/{workoutPlanId}/exercises` | Add exercise |
| GET | `/workout-plans/{workoutPlanId}/exercises` | Get plan exercises |
| GET | `/workout-plans/{workoutPlanId}/exercises/{id}` | Get plan exercise |
| PUT | `/workout-plans/{workoutPlanId}/exercises/{id}` | Update plan exercise |
| DELETE | `/workout-plans/{workoutPlanId}/exercises/{id}` | Remove plan exercise |

---

## ⚙️ Getting Started

### Prerequisites

Make sure you have installed:

- Java 21+
- Maven
- Docker
- Git

### 1. Clone the repository

```bash
git clone https://github.com/dewqryp/FitTrack.git
cd FitTrack
```

### 2. Configure environment variables

Create a `.env` file in the project root:

```env
POSTGRES_DB=fittrack
POSTGRES_USER=fittrack
POSTGRES_PASSWORD=fittrack
```

Configure the JWT environment variables before starting the backend:

```env
JWT_SECRET=your_base64_encoded_secret
JWT_EXPIRATION_SECONDS=3600
```

Use a secure secret for non-development environments.

### 3. Start PostgreSQL

```bash
docker compose up -d
```

PostgreSQL will be available on port `15432`.

### 4. Run the backend

```bash
cd backend/FitTrackBackend
mvn spring-boot:run
```

The API will be available at:

```text
http://localhost:8080
```

---

## 🧪 Testing

Run the test suite:

```bash
cd backend/FitTrackBackend
mvn test
```

The project includes tests covering:

- Service layer logic
- REST controllers
- Request validation
- Authentication and authorization
- Security integration

---

## 🗺 Roadmap

- [x] User registration
- [x] JWT authentication
- [x] Role-based authorization
- [x] Exercise management
- [x] Workout plan management
- [x] Workout plan exercise management
- [ ] Workout session tracking
- [ ] Workout history
- [ ] Personal progress statistics
- [ ] Automated API testing
- [ ] React frontend
- [ ] CI/CD with GitHub Actions

---

## 👨‍💻 Author

**dewqryp**

GitHub: [@dewqryp](https://github.com/dewqryp)

---

## 📄 License

License information will be added when a project license is selected.

---

**FitTrack — Build your routine. Track your progress. Get stronger.**
