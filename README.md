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
    ├── src/
    └── README.md          # Detailed backend documentation
```

---

## Running Locally

**Prerequisites:** Docker and Docker Compose only.

```bash
# Clone the repository
git clone https://github.com/your-name/taskflow
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

## Documentation

- **Backend Details:** See [`backend/README.md`](backend/README.md) for:
  - Complete architecture decisions
  - Tech stack details
  - All API endpoints with examples
  - Database schema and migrations

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

### Core Requirements ✅
- JWT authentication (register/login) with bcrypt cost-12
- Projects API (CRUD with ownership model)
- Tasks API (CRUD with filters and pagination)
- PostgreSQL with Flyway migrations
- Docker Compose setup
- Comprehensive documentation

### Bonus Features ✅
- Pagination on all list endpoints (`?page=&limit=`)
- Stats endpoint (`GET /projects/:id/stats`)
- 3 integration tests covering auth, permissions, and core flows
- Priority filtering on tasks (`?priority=low/medium/high`)

---

## API Endpoints

### Authentication
- `POST /auth/register` - Register new user
- `POST /auth/login` - Login and get JWT token

### Projects
- `GET /projects?page=0&limit=20` - List accessible projects
- `POST /projects` - Create project
- `GET /projects/:id` - Get project with tasks
- `PATCH /projects/:id` - Update project (owner only)
- `DELETE /projects/:id` - Delete project (owner only)
- `GET /projects/:id/stats` - Get task statistics (bonus)

### Tasks
- `GET /projects/:id/tasks?status=&priority=&assignee=&page=&limit=` - List tasks with filters
- `POST /projects/:id/tasks` - Create task
- `PATCH /tasks/:id` - Update task
- `DELETE /tasks/:id` - Delete task (owner/creator only)

See [`backend/README.md`](backend/README.md) for complete API documentation with examples.

---

## Language Choice Note

**Note:** This assignment specifies Go as the preferred backend language. I implemented this in **Java 17 + Spring Boot** because:

- **Expertise**: 2 years of production Java/Spring Boot experience vs beginner-level Go
- **Quality over novelty**: I can demonstrate deeper architectural decisions, testing patterns, and production-ready code in Java
- **Honest assessment**: I believe showing strong engineering in a familiar stack is more valuable than mediocre code in an unfamiliar one

I'm actively learning Go and would be happy to discuss Go-specific patterns (goroutines, channels, interfaces) in the follow-up call.

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
