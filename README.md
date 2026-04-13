# TaskFlow  Backend API

A task management REST API built with **Java 17 + Spring Boot 3.2**, **PostgreSQL**, and **JWT authentication**.

**Backend Engineer Submission** by Raviteja

**Note:** Built in Java 17 + Spring Boot instead of Go to ensure production-quality code with proper testing and documentation.

---

## Repository Structure

```
taskflow/
├── docker-compose.yml     # Orchestrates PostgreSQL + API
├── .env.example           # Environment variables template  
├── README.md              # This file
└── backend/               # Java Spring Boot API
    ├── Dockerfile
    ├── pom.xml
    ├── db-migrations-down/  # Down migrations for rollback
    └── src/
        ├── main/
        │   ├── java/        # Application code
        │   └── resources/
        │       ├── application.yml
        │       └── db/migration/  # Flyway migrations
        └── test/            # Integration tests
```

---

## Running Locally

**Prerequisites:** Docker and Docker Compose only.

```bash
# Clone the repository
git clone https://github.com/RaviTejaRayavarapu/taskflow.git
cd taskflow

# Copy environment variables
cp .env.example .env

# Start everything (PostgreSQL + API)
docker compose up --build

# App available at http://localhost:3000
```

**What happens automatically:**
- PostgreSQL container starts with health checks
- API container builds using multi-stage Dockerfile
- Flyway migrations run (V1 → V2 → V3 → V4)
- Seed data inserted (1 user, 1 project, 3 tasks)
- Server ready

---

## Running Migrations

**Migrations run automatically on container start** via Flyway. No manual steps required.

When you run `docker compose up`, the following happens:
1. PostgreSQL container starts and becomes healthy
2. API container starts and connects to PostgreSQL
3. Flyway automatically runs pending migrations in order:
   - `V1__create_users.sql` - Creates users table
   - `V2__create_projects.sql` - Creates projects table
   - `V3__create_tasks.sql` - Creates tasks table with triggers
   - `V4__seed_data.sql` - Inserts test user, project, and tasks

**Down migrations** are provided in `backend/db-migrations-down/` for manual rollback if needed.

To reset the database:
```bash
# Stop and remove volumes
docker compose down -v

# Start fresh (migrations run automatically)
docker compose up
```

---

## Test Credentials

```
Email:    test@example.com
Password: password123
```

---

## Quick API Test

```bash
# Health check
curl http://localhost:3000/actuator/health

# Login
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'

# Use the returned token for other endpoints
```

---

## Architecture Decisions

### Why Java + Spring Boot?
While the assignment prefers Go, I chose Java/Spring Boot because:
- **2 years production experience** vs beginner Go knowledge
- **Better code quality** in familiar stack vs learning on the fly
- **Honest tradeoff**: Strong engineering in Java > mediocre Go code

### Layered Architecture
```
Controllers → Services → Repositories → Database
```
- **Controllers**: Handle HTTP, validation, auth
- **Services**: Business logic, authorization checks
- **Repositories**: Data access via Spring Data JPA
- **Clean separation**: Easy to test, maintain, and extend

### Security Design
- **Stateless JWT**: No server-side sessions, scales horizontally
- **Bcrypt cost 12**: Balance between security and performance
- **401 vs 403**: Proper distinction (unauthenticated vs unauthorized)
- **Spring Security**: Industry-standard, battle-tested framework

### Database Design
- **UUID primary keys**: Distributed-friendly, no collision risk
- **Flyway migrations**: Version-controlled schema changes
- **Foreign keys + cascades**: Data integrity at DB level
- **Check constraints**: Enforce enums at DB level (status, priority)
- **Indexes**: On foreign keys and frequently queried fields

### API Design Choices
- **RESTful conventions**: Standard HTTP methods and status codes
- **Pagination**: Prevents large result sets, improves performance
- **Structured errors**: Consistent format, field-level validation errors
- **Filter parameters**: Flexible querying without complex DSL

### Docker Strategy
- **Multi-stage build**: Smaller image (build stage discarded)
- **Health checks**: Ensures DB ready before API starts
- **Non-root user**: Security best practice
- **Compose orchestration**: Single command startup

### What I Left Out (Intentionally)
- **No frontend**: Backend-only role, focused on API quality
- **No WebSockets**: Not required, adds complexity
- **No caching**: Premature optimization for this scale
- **No rate limiting**: Would add in production, not core requirement
- **Simple logging**: Structured but not distributed tracing

---

## What I'd Do With More Time

### Testing & Validation
1. **More test coverage**: Currently 3 integration tests, would add:
   - Edge cases (invalid UUIDs, malformed JSON)
   - Concurrent update scenarios
   - More permission boundary tests
2. **Input validation**: Add @Valid annotations on all DTOs
3. **Better error messages**: Include field names in validation errors
4. **API documentation**: Add Swagger/OpenAPI for interactive testing

### Security Improvements
1. **Rate limiting on login endpoint**: Prevent brute force attacks
2. **Refresh token mechanism**: Currently JWT expires with no refresh
3. **CORS configuration**: For frontend integration
4. **Input sanitization**: Add XSS protection

### Performance Optimizations
1. **Database indexes**: On frequently queried fields
2. **Connection pool tuning**: Better resource management
3. **Caching layer**: For frequently accessed data

### Monitoring & Operations
1. **Structured logging**: With request IDs for tracing
2. **Health check endpoints**: For readiness/liveness probes
3. **Metrics collection**: Response times, error rates
4. **CI/CD pipeline**: Automated testing and deployment

### Feature Enhancements
1. **Task comments**: Add discussion threads on tasks
2. **Email notifications**: Send alerts for task assignments
3. **Audit log**: Track who changed what and when
4. **Bulk operations**: Update multiple tasks at once
5. **Advanced filters**: Date ranges, search by title
6. **File attachments**: Allow uploading files to tasks

### Known Shortcuts Taken
1. **No refresh token rotation**: JWTs expire with no revocation mechanism
2. **No rate limiting on /auth/login**: Open to brute-force attacks
3. **updated_at handled by DB trigger**: Tradeoff vs application-level handling
4. **No input sanitization**: Would add XSS protection
5. **Simple error messages**: Would add error codes for better client handling
6. **No CI/CD pipeline**: Would add automated testing on push
7. **No monitoring**: Would add logging aggregation and alerts
8. **Hardcoded pagination limits**: Should be configurable
9. **No request correlation IDs**: For tracing requests across logs

---

## Tech Stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.2 |
| Security | Spring Security + JJWT 0.12 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Container | Docker (multi-stage) + Compose |
| Tests | JUnit 5 + MockMvc (3 integration tests) |

---

## Features Implemented

### Core Requirements 
- JWT authentication (register/login) with bcrypt cost-12
- Projects API (CRUD with ownership model)
- Tasks API (CRUD with filters and pagination)
- PostgreSQL with Flyway migrations
- Docker Compose setup
- Comprehensive documentation

### Bonus Features 
- Pagination on all list endpoints (`?page=&limit=`)
- Stats endpoint (`GET /projects/:id/stats`)
- 3 integration tests covering auth, permissions, and core flows
- Priority filtering on tasks (`?priority=low/medium/high`)

---

## API Reference

All endpoints require `Content-Type: application/json`. Protected endpoints require `Authorization: Bearer <token>`.

### Authentication

#### `POST /auth/register`
Register a new user.

**Request:**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "password": "password123"
}
```

**Response 201:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "a1b2c3d4-...",
    "name": "Jane Doe",
    "email": "jane@example.com"
  }
}
```

#### `POST /auth/login`
Login and receive JWT token.

**Request:**
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

**Response 200:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": "a0000000-0000-0000-0000-000000000001",
    "name": "Test User",
    "email": "test@example.com"
  }
}
```

---

### Projects

#### `GET /projects?page=0&limit=20`
List projects the current user owns or has tasks in.

**Response 200:**
```json
{
  "content": [
    {
      "id": "b0000000-0000-0000-0000-000000000001",
      "name": "Sample Project",
      "description": "A sample project for testing",
      "ownerId": "a0000000-0000-0000-0000-000000000001",
      "createdAt": "2026-04-13T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

#### `POST /projects`
Create a new project (owner = current user).

**Request:**
```json
{
  "name": "New Project",
  "description": "Optional description"
}
```

**Response 201:**
```json
{
  "id": "uuid",
  "name": "New Project",
  "description": "Optional description",
  "ownerId": "a0000000-0000-0000-0000-000000000001",
  "createdAt": "2026-04-13T10:00:00Z"
}
```

#### `GET /projects/:id`
Get project details with all its tasks.

**Response 200:**
```json
{
  "id": "b0000000-0000-0000-0000-000000000001",
  "name": "Sample Project",
  "description": "A sample project for testing",
  "ownerId": "a0000000-0000-0000-0000-000000000001",
  "createdAt": "2026-04-13T10:00:00Z",
  "tasks": [
    {
      "id": "c0000000-0000-0000-0000-000000000001",
      "title": "Task 1",
      "description": "First task",
      "status": "todo",
      "priority": "high",
      "projectId": "b0000000-0000-0000-0000-000000000001",
      "assigneeId": "a0000000-0000-0000-0000-000000000001",
      "assigneeName": "Test User",
      "creatorId": "a0000000-0000-0000-0000-000000000001",
      "dueDate": "2026-04-18",
      "createdAt": "2026-04-13T10:00:00Z",
      "updatedAt": "2026-04-13T10:00:00Z"
    }
  ]
}
```

#### `PATCH /projects/:id`
Update project name/description (owner only).

**Request (all fields optional):**
```json
{
  "name": "Updated Project Name",
  "description": "Updated description"
}
```

**Response 200:** Returns updated project object.

#### `DELETE /projects/:id`
Delete project and all its tasks (owner only).

**Response 204:** No content.

#### `GET /projects/:id/stats` (Bonus)
Get task statistics for a project.

**Response 200:**
```json
{
  "total": 3,
  "byStatus": {
    "todo": 1,
    "in_progress": 1,
    "done": 1
  },
  "byAssignee": [
    {
      "userId": "a0000000-0000-0000-0000-000000000001",
      "name": "Test User",
      "count": 3
    }
  ]
}
```

---

### Tasks

#### `GET /projects/:id/tasks?status=todo&priority=high&assignee=uuid&page=0&limit=20`
List tasks with optional filters and pagination.

**Query Parameters:**
- `status` (optional): `todo`, `in_progress`, or `done`
- `priority` (optional): `low`, `medium`, or `high`
- `assignee` (optional): User UUID
- `page` (optional): Page number (default: 0)
- `limit` (optional): Items per page (default: 20)

**Response 200:**
```json
{
  "content": [
    {
      "id": "c0000000-0000-0000-0000-000000000002",
      "title": "Task 2",
      "description": "Second task",
      "status": "in_progress",
      "priority": "medium",
      "projectId": "b0000000-0000-0000-0000-000000000001",
      "assigneeId": "a0000000-0000-0000-0000-000000000001",
      "assigneeName": "Test User",
      "creatorId": "a0000000-0000-0000-0000-000000000001",
      "dueDate": "2026-04-23",
      "createdAt": "2026-04-13T10:00:00Z",
      "updatedAt": "2026-04-13T10:00:00Z"
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "last": true
}
```

#### `POST /projects/:id/tasks`
Create a new task in a project.

**Request:**
```json
{
  "title": "Design homepage",
  "description": "Create wireframes and mockups",
  "status": "todo",
  "priority": "high",
  "assigneeId": "a0000000-0000-0000-0000-000000000001",
  "dueDate": "2026-04-20"
}
```

**Response 201:** Returns created task object.

#### `PATCH /tasks/:id`
Update task details.

**Request (all fields optional):**
```json
{
  "title": "Updated title",
  "description": "Updated description",
  "status": "done",
  "priority": "low",
  "assigneeId": "a0000000-0000-0000-0000-000000000001",
  "dueDate": "2026-04-25",
  "clearAssignee": false,
  "clearDueDate": false
}
```

Set `clearAssignee: true` or `clearDueDate: true` to explicitly null those fields.

**Response 200:** Returns updated task object.

#### `DELETE /tasks/:id`
Delete a task (project owner or task creator only).

**Response 204:** No content.

---

### Error Responses

All errors follow a consistent format:

**400 Validation Error:**
```json
{
  "error": "validation failed",
  "fields": {
    "email": "is required",
    "password": "must be at least 8 characters"
  }
}
```

**401 Unauthenticated:**
```json
{
  "error": "unauthorized"
}
```

**403 Forbidden:**
```json
{
  "error": "forbidden"
}
```

**404 Not Found:**
```json
{
  "error": "not found"
}
```

**409 Conflict:**
```json
{
  "error": "email already in use"
}
```

---

## Language Choice Note

**Note:** This assignment specifies Go as the preferred backend language. I implemented this in **Java 17 + Spring Boot** because:

- **Expertise**: 2 years of production Java/Spring Boot experience vs beginner-level Go
- **Quality over novelty**: I can demonstrate deeper architectural decisions, testing patterns, and production-ready code in Java
- **Honest assessment**: I believe showing strong engineering in a familiar stack is more valuable than mediocre code in an unfamiliar one


---

## Stopping the Application

```bash
# Stop containers
docker compose down

# Stop and remove volumes (clean slate)
docker compose down -v
```

---

## Project Highlights

- **Clean Architecture**: Layered structure (Controllers → Services → Repositories)
- **Security**: JWT stateless auth, bcrypt password hashing, proper 401/403 distinction
- **Data Integrity**: Flyway migrations, proper foreign keys, check constraints
- **API Design**: RESTful conventions, structured errors, pagination
- **Testing**: 3 integration tests covering auth, projects, and tasks
- **DevEx**: Single command startup, comprehensive documentation, troubleshooting guides
- **Production Ready**: Multi-stage Docker build, graceful shutdown, structured logging

---

## Contact

For questions about this submission, please reach out via the assignment email.

**Ready for code review call!** I can explain any part of the codebase and discuss architectural decisions.
