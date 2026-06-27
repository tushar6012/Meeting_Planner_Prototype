# Meeting Planner Prototype

This project is a prototype for a Meeting Planner web application. It features a modern, responsive user interface built with **Angular** (using custom CSS variables and glassmorphism styling) and a robust REST API backend powered by **Spring Boot**, with persistent storage handled via an in-memory **H2 Database**.

---

## Technical Stack
- **Backend:** Java 21, Spring Boot 3.2, Spring Data JPA, Spring Security 6.2, H2 Database, io.jsonwebtoken (JWT).
- **Frontend:** Angular 19 (Standalone architecture), RxJS, Vanilla CSS.

---

## How to Run the Application Locally

### Prerequisites
- **Java Development Kit (JDK) 21** or higher.
- **Node.js** v26 or higher and **npm** v11 or higher.

### 1. Starting the Backend (Spring Boot)
Open a terminal in the root workspace(cloned repository path) and run:
```bash
cd backend
# Build and run using the local Maven Wrapper (Windows)
..\.maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
*(On macOS/Linux, if global Maven or Maven Wrapper is present, use `mvn spring-boot:run` or `./mvnw spring-boot:run`)*

The server will start on port **8080**.
- **REST API Base URL:** `http://localhost:8080`
- **H2 Console:** [http://localhost:8080/h2-console]  
  *Use This for connect console (JDBC URL: `jdbc:h2:mem:meetingdb`, Username: `sa`, Password: `password`)*

### 2. Starting the Frontend (Angular)
Open a new terminal window in the root workspace and run:
```bash
cd frontend
# Install dependencies (on Windows PowerShell, use npm.cmd if you see a script execution error)
npm.cmd install
# Start the local development server
npm.cmd start
```
*Note: If you are on macOS/Linux or standard command prompt (cmd.exe), you can use standard `npm install` and `npm start`.*
Open your browser and navigate to **[http://localhost:4200]**.

---

## How to Run the Tests

### Backend (Spring Boot)
The backend contains a combination of mock-based unit tests (`UserServiceTest`, `MeetingServiceTest`) and full-context MockMvc integration tests (`AuthControllerIntegrationTest`, `MeetingControllerIntegrationTest`).

To execute all tests, navigate to the `backend` folder and run:
```bash
..\.maven\apache-maven-3.9.6\bin\mvn.cmd test
```

---

## Important Design Decisions

1. **In-Memory Portability (H2 Database):**  
   We utilized H2 in in-memory mode (`jdbc:h2:mem:meetingdb`) with `DB_CLOSE_DELAY=-1` to ensure that database files do not clutter the host filesystem and the project remains 100% portable. The DB schema is automatically generated and updated via Hibernate on startup.

2. **Avatar Binary DB Storage:**  
   Instead of uploading images to a local folder (which can fail due to folder write permissions or cause environment-specific absolute path issues), I store avatar images as `BLOB` bytes directly in the user record in H2. Avatars are served dynamically using a public GET endpoint (`/api/users/{id}/avatar`).

3. **Dynamic Initials SVG Fallback:**  
   If a user signs up without uploading a profile image, the backend dynamically constructs an SVG graphic containing the user's name initials (e.g., "Tushar Patel" -> "TP") rendered on an Indigo background. This guarantees that every user card or participant badge always displays a clean, professional profile circle.

4. **Lightweight JWT Security:**  
   Instead of session cookies, I use stateless JWT authentication. A JWT is issued upon successful login or registration, stored in `localStorage` in the browser, and appended to all HTTP calls via an Angular `HttpInterceptorFn`.

5. **Loop-Free Spring Security configuration:**  
   To avoid cyclic bean loading issues, I instantiated `JwtAuthenticationFilter` manually within the `SecurityConfig` block instead of registering it as a global `@Component` bean, passing down the required `JwtTokenProvider` and `UserRepository` resources.

---

## Assumptions Made
- The host system has Java 21+ and Node.js 26+ installed.
- Single timezone usage: All dates and times are stored and represented in local time (`LocalDateTime`) without offset conversions for simplicity.
- The default H2 database credentials (`sa`/`password`) and port `8080`/`4200` configurations are free and not in use by other local services.
- Screen design I can use any thing not existing design available
- User session used Jwt token service and front end use loacalStorage of browser
- File size validation less then 1 MB as I store in database
- Application UI not consistent in every browser

---

## Known Limitations
- **Stateless Database:** Since H2 runs in-memory, all registered users and meeting data are cleared whenever the Spring Boot backend server is restarted.
- **Token Expiry Management:** Once the JWT expires, requests will fail with HTTP 401. A production app would implement token auto-refresh (using refresh tokens) or intercept 401s to redirect the user to login.
- **Large Image Blobs:** Storing large images as BLOBs in transactional tables can degrade database performance in large scale operations. Production code should utilize an object store (e.g., AWS S3 or Google Cloud Storage).

---

## What to Improve with More Time
1. **Persistent Database:** Switch to a persistent database like PostgreSQL or MySQL using Docker containers to preserve data across restarts.
2. **Interactive H2 Console Control:** Implement Spring Boot profiles (e.g., `dev` and `prod`) so that H2 console is disabled in production environments.
3. **Advanced Meeting Features:** Add features like calendar integrations (ICS file generation), email notifications when users are invited to meetings, and meeting status updates (e.g., Pending, Confirmed, Cancelled),Scheduling Assistance(Display user availibility on selection) same as outlook or other tools,Give meeting options like occurrance(daily,weekly etc..).
4. **End-to-End Testing:** Incorporate Cypress or Playwright tests to test the Angular user interfaces and login flows end-to-end.
5. **Validation Improvement:** Incorporate more suitable validation like meeting schedule button enable after only select participant,Max length (Character limit on free form text),file type validation etc.
6. **UI Improvement:** Use component that work same in every browser(every browser support),Resposive UI to support mobile,Ipad,Tab
7. **Doker Kubernates:** Create docker Image and push to docker repository you can direct pull and run in docker environment
