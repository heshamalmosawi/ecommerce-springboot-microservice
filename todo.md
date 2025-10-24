## Phase 0 — Foundation
- [ ] Initialize a mono-repo with modules: discovery-server, user-service, product-service, media-service, frontend, and a shared contracts module for DTOs/events [web:33][web:36].
- [ ] Add dependencies to each service: web, security, oauth2-resource-server, spring-kafka, spring-data-mongodb [web:21][web:35].
- [ ] Initialize Angular workspace with routing and feature modules: auth, seller, products, media, shared [web:8][web:1].
- [ ] Create Kafka topics: user.registered, product.created, product.updated, product.deleted, media.uploaded, media.deleted [web:35][web:29].
- [ ] Define MongoDB databases per service and collections with indexes on ownerId, productId, createdAt [web:41][web:54].
- [ ] Choose MongoDB schema patterns: references for media in products and schema versioning in documents [web:41][web:48].

## Phase 1 — Discovery/Kafka/Mongo
- [ ] Implement Eureka server and register all microservices as Eureka clients [web:33][web:36].
- [ ] Configure Kafka producer/consumer factories, KafkaTemplate, and listeners in each microservice [web:35][web:29].
- [ ] Externalize Kafka and Mongo connection properties for local and prod profiles [web:35][web:54].
- [ ] Auto-create topics in dev and validate connectivity with a ping producer/consumer pair [web:35][web:29].
- [ ] Add MongoDB startup index creation on key fields (ownerId, productId, createdAt) [web:54][web:41].

## Phase 2 — Security baseline
- [ ] Configure each microservice as an OAuth2 Resource Server validating JWTs via issuer and JWKS URI [web:21][web:22].
- [ ] Map token claims to ROLE_CLIENT and ROLE_SELLER authorities in the security configuration [web:21][web:22].
- [ ] Set stateless session policy, enable CORS for the Angular origin, and disable CSRF for token-based APIs [web:21][web:22].
- [ ] Protect write endpoints with authorization rules or method security annotations [web:21][web:22].
- [ ] Add security HTTP headers (frame-options, content-type-options, XSS-protection) via configuration [web:21][web:22].

## Phase 3 — User service API
- [ ] Implement POST /auth/register with role selection; validate inputs; hash password before storing [web:50][web:21].
- [ ] Implement POST /auth/login if issuing tokens locally, or integrate with an external IdP and only validate tokens here [web:21][web:22].
- [ ] Implement GET /me and PUT /me restricted to the authenticated subject [web:21][web:22].
- [ ] Implement PUT /me/avatar delegating upload to Media service and storing avatarId [web:57][web:21].
- [ ] Publish user.registered events upon successful registration with minimal metadata [web:35][web:29].

## Phase 4 — Product service API
- [ ] Implement public GET /products and GET /products/{id} returning basic fields and imageIds [web:41][web:54].
- [ ] Implement POST /seller/products (seller-only) using sellerId from JWT; validate DTO [web:21][web:22].
- [ ] Implement PUT /seller/products/{id} with ownership enforcement and version increment [web:21][web:22].
- [ ] Implement DELETE /seller/products/{id} with ownership enforcement and publish product.deleted [web:35][web:29].
- [ ] Implement PATCH /seller/products/{id}/images to attach/detach imageIds owned by the same seller [web:21][web:22].

## Phase 5 — Media service API
- [ ] Configure multipart limits: spring.servlet.multipart.max-file-size=2MB and max-request-size=2MB [web:59][web:57].
- [ ] Implement POST /media with MIME/type checks and signature sniffing; reject >2MB and invalid types with clear 400 errors [web:57][web:59].
- [ ] Persist metadata {id, ownerId, productId?, filename, contentType, size, createdAt}; store bytes via object storage or GridFS [web:54][web:41].
- [ ] Implement GET /media/{id} with caching policy or signed URLs; avoid returning PII [web:57][web:21].
- [ ] Implement DELETE /media/{id} restricted to owner; publish media.deleted and cleanup references as needed [web:21][web:35].

## Phase 6 — Angular auth
- [ ] Build Sign Up and Sign In pages with Reactive Forms and field-level error messages [web:1][web:3].
- [ ] Add JWT storage strategy and an HTTP interceptor to attach Authorization: Bearer headers [web:1][web:8].
- [ ] Add route guards for authenticated routes and seller-only dashboard access [web:1][web:3].
- [ ] Implement profile page for viewing/updating user info and avatar [web:1][web:8].
- [ ] Display server-side error feedback on invalid credentials or validation failures [web:1][web:3].

## Phase 7 — Angular seller dashboard
- [ ] Implement seller product list with create/edit/delete actions wired to Product service [web:1][web:8].
- [ ] Implement product form with validators for title, price, and description; disable submit until valid [web:1][web:3].
- [ ] Implement image manager: file preview, accept=image/*, file.size ≤ 2MB validator, progress UI, and server errors [web:3][web:8].
- [ ] Implement attach/detach images to products via PATCH endpoint with toasts on success/failure [web:1][web:8].
- [ ] Guard all dashboard routes and enforce seller role in the UI and on the backend [web:21][web:1].

## Phase 8 — Public product UI
- [ ] Implement public product listing with simple cards and thumbnail from first imageId [web:1][web:8].
- [ ] Implement product details page with an image gallery resolved via Media service [web:1][web:8].
- [ ] Add loading and empty states for lists and detail pages [web:1][web:8].
- [ ] Handle API errors gracefully with user-friendly messages [web:1][web:3].
- [ ] Ensure no sensitive user data is displayed in public views [web:21][web:22].

## Phase 9 — HTTPS
