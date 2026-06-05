# Skatemap — Backend

Spring Boot REST API for the Skatemap platform. Handles authentication, spot management, media storage, AI-powered chat, and a role-based moderation pipeline.

---

## Tech stack

- Java 21, Spring Boot 4
- Spring Security with JWT (stateless, BCrypt password hashing)
- Spring Data JPA with PostgreSQL
- Spring AI with OpenAI integration
- Cloudinary for media storage
- Thumbnailator for server-side image compression
- Apache Tika for MIME type validation
- Stripe for donation payments
- Deployed on Railway

---

## Authentication

JWT-based stateless authentication. The `TokenFilter` intercepts every request, validates the token, and loads the user into the security context. Roles are stored in a dedicated `UserRole` entity and carried in the JWT payload. Method-level authorization is handled with `@PreAuthorize`, allowing fine-grained access control per endpoint without centralizing all rules in the security config.

---

## Spot filtering with JPA Specifications

The `SpotSpecification` class provides a composable, type-safe filter system built on the JPA Criteria API. Each filter — continent, risk, type, city, country, free-text search — is an independent `Specification<Spot>` predicate that can be combined with `.and()` and `.where()`. Each predicate is null-safe — if a parameter is not provided, the predicate returns null and is ignored by Hibernate.

The type filter performs a double join through `SpotType` and `Type` entities, supporting multi-type queries correctly at the SQL level.

This design is intentional. While the current scale allows client-side filtering for the public globe and grid, the Specification system is already in place and production-ready for server-side filtering. As the database grows into tens or hundreds of thousands of spots, the same controller and service code will scale to backend filtering with zero architectural changes — only the frontend call pattern needs to change. The infrastructure is already there.

The same `SpotSpecification.build()` method is reused by the AI chatbot to translate natural language queries into database filters, which means the filter logic is centralized in one place across both use cases.

---

## AI chatbot

The chatbot uses a two-call pattern with Spring AI and the OpenAI API to handle user messages intelligently.

The first LLM call takes the user message and returns a structured JSON object containing any recognized filter parameters: city, country, continent, risk level, spot types, and an optional error code. The model is instructed to translate location names to English, normalize values to match database enums, and flag edge cases like sub-regional queries or pure skate knowledge questions.

The extracted params are deserialized into a `SpotSearchParamsDTO` and passed through a routing layer. If the error field is `skate_knowledge`, the message is routed to a skateboarding Q&A prompt instead of a database query. If the error is `sub_region`, the user is told that sub-regional filtering is not supported. If no parameters were extracted at all, a lightweight second LLM call classifies whether the message is skate-related before deciding how to respond.

If valid parameters were found, `SpotSpecification.build()` builds a Specification from the extracted DTO and queries the database for up to 10 matching spots. The spots are injected into a second LLM prompt which generates a natural-language response in a casual skate culture tone. If more than 10 results exist, a follow-up note directs the user to explore the grid or map.

The bot active/inactive state is persisted in the database and toggleable by a super admin without redeploying.

---

## Media pipeline

Images and videos are validated with Apache Tika before upload to prevent MIME type spoofing. Images are resized server-side with Thumbnailator to a maximum of 1280x720 at 80% quality before being sent to Cloudinary, reducing storage costs and delivery time. Videos are uploaded directly, with a thumbnail URL derived by transforming the Cloudinary video URL to extract the first frame as a JPEG.

Limits are enforced per spot: up to 5 images and 3 videos. Deletion removes the asset from Cloudinary before removing the database record.

---

## Query optimization

The naive approach of loading spots and then calling `getMedia()` in a loop generates N+1 queries — one per spot. Two strategies are used to avoid this depending on the query context.

For paginated queries with dynamic Specification filters, `@EntityGraph` forces a single `LEFT JOIN` on the media table, loading all media in one query. The first image is then extracted in Java.

For the public list endpoints that return lightweight DTOs, JPQL constructor expressions with correlated subqueries fetch only the first image link per spot directly in SQL, with no entity loading at all. Spot types are fetched in a separate query that returns all type associations in one round trip, then assembled into a map keyed by spot ID in the service layer before building the final DTO list.

---

## Data initialization

Three runners execute at startup: `RoleRunner` seeds the role table if empty, `AdminRunner` creates the default super admin account if it does not exist, and `ChatBotRunner` seeds the chatbot entity. This makes the application self-bootstrapping on a fresh database without requiring manual SQL.

---

## API reference

### Auth — `/auth`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | Public | Register a new user |
| POST | `/auth/login` | Public | Login and receive a JWT token |

### Spots — `/spots`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/spots/all/approved` | Public | All approved spots with filters (continent, risk, type, search) — paginated |
| GET | `/spots/globe/approved/all` | Public | Approved spots for globe view — large page size, same filters |
| GET | `/spots/all/{status}` | Public | Lightweight DTO list by status (APPROVED, PENDING, UNAPPROVED) |
| GET | `/spots/all` | Public | All spots as lightweight DTOs regardless of status |
| GET | `/spots/single/{id}` | Public | Full detail of a single spot |
| GET | `/spots/own` | User | Current user's own spots, filterable by status |
| GET | `/spots/pending` | Admin | Paginated list of all pending spots |
| POST | `/spots` | User | Create a new spot (JSON body) |
| POST | `/spots/upload` | User | Create a spot with media (multipart) |
| POST | `/spots/modify/{id}` | User | Update a spot with optional media replacement (multipart) |
| PUT | `/spots/{id}` | User | Update spot data (JSON body) |
| PATCH | `/spots/status/{id}` | Admin | Change spot status (approved / pending / unapproved) |
| DELETE | `/spots/{id}` | User | Delete a spot |

### Favourites — `/fav`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/fav/all` | User | All favourite spots for the current user |
| GET | `/fav/{spotId}` | User | Single favourite spot detail |
| POST | `/fav/{spotId}` | User | Add a spot to favourites |
| DELETE | `/fav/{spotId}` | User | Remove a spot from favourites |

### Media — `/media`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/media/{id}` | Public | Paginated media for a spot, optionally filtered by type (image / video) |
| POST | `/media/image/{id}` | User | Upload images to a spot (multipart, up to 5) |
| POST | `/media/video/{id}` | User | Upload videos to a spot (multipart, up to 3) |
| DELETE | `/media/{id}` | User | Delete a media item and remove it from Cloudinary |

### Account — `/account`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| GET | `/account` | User | Current user full profile |
| GET | `/account/minimal` | User | Current user minimal info (id, username, role) |
| PUT | `/account` | User | Update current user data |
| DELETE | `/account` | User | Delete current user account |
| GET | `/account/all` | Super admin | All user-role associations, paginated |
| GET | `/account/all/users` | Super admin | All users paginated, sorted by name |
| PUT | `/account/{id}` | Super admin | Change a user's role |
| DELETE | `/account/{id}` | Super admin | Delete a user by id |

### Chatbot — `/bot`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/bot/ask` | Public | Send a message and receive a streamed response (Flux) |
| GET | `/bot/get/status` | Public | Check if the bot is currently active |
| PUT | `/bot/status` | Super admin | Toggle bot active/inactive |

### Donations — `/donations`

| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/donations` | Public | Create a Stripe PaymentIntent and return the client secret |

---

## Repository

https://github.com/DattolaFrancesco/skatemap-be
