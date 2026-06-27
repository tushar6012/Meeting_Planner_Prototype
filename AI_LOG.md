# AI Prompt Log & Development Steps

This log tracks the key prompts, decisions, and development iterations performed during the creation of the Meeting Planner application.

---

### Step 1: Environmental Research & Tool Verification
- **AI Rationale:** The system workspace was empty. First checked global versions of Java, Maven, Node, and npm.
- **Prompt / Action:** Checked tool command availability. Discovered Java 21 and Node 26 were present, but Maven (`mvn`) was missing and PowerShell execution policy restricted standard `npm`.
- **Decision:** Used `npm.cmd` and `npx.cmd` to bypass PowerShell script execution restrictions, and downloaded/extracted Apache Maven locally to `.maven/` to ensure a self-contained and portable environment.

---

### Step 2: Backend Archetype & Configuration
- **AI Action:** Created `backend/pom.xml` and `backend/src/main/resources/application.properties` from scratch.
- **Design Choices:** Integrated Spring Web, JPA, Security, H2 Database, and JJWT. Configured H2 in-memory mode, H2 console endpoint, and local multipart file upload size limits.

---

### Step 3: Security Architecture & Loop Resolution
- **AI Action:** Implemented `JwtTokenProvider`, `JwtAuthenticationFilter`, and `SecurityConfig`.
- **Intervention:** The initial integration tests failed with `java.lang.StackOverflowError` due to a circular dependency in Spring Security bean references (Filter -> UserRepository -> PasswordEncoder -> Config -> Filter).
- **Resolution Prompt/Action:** Refactored `JwtAuthenticationFilter` out of `@Component` auto-discovery. Added a constructor, instantiated it manually in `SecurityConfig`, and exposed `UserDetailsService` as a clean lambda bean. This broke the injection loop and resolved the failures.

---

### Step 4: Core Domain Business Logic & Controllers
- **AI Action:** Created entity classes (`User`, `Meeting`), repositories, and services.
- **Premium Detail Prompt:** Implemented initials fallback rendering. Designed `UserController` to dynamically generate a clean vector SVG graphic (initials inside an indigo circular badge) if the user did not upload a profile avatar. This prevents layout breakage in the client.

---

### Step 5: Test Verification
- **AI Action:** Wrote unit tests (`UserServiceTest`, `MeetingServiceTest`) and integration tests (`AuthControllerIntegrationTest`, `MeetingControllerIntegrationTest`).
- **Debugging Prompt:** Wrote try-catch handling in `AuthController` for bad credentials and duplicate email exceptions to guarantee tests return `401 Unauthorized` and `400 Bad Request` instead of unhandled Servlet exceptions. Wrote `mvn test` which successfully passed all 13 tests.

---

### Step 6: Frontend App Generation & Services Configuration
- **AI Action:** Initialized Angular frontend utilizing modern standalone schema (`--file-name-style-guide=2016` to keep standard component structures).
- **Wired Services:** Added `AuthService` with multipart registration, `MeetingService` with dynamic URL avatar serving, and functional `authInterceptor` / `authGuard` configurations.

---

### Step 7: Premium Styling & Component Implementation
- **AI Action:** Created CSS variables, responsive tables, meeting details view, and user selection checkboxes.
- **Styling Details:** Implemented a modern dark-indigo theme using slate colors, transition transforms on cards, and overlapping profile avatar stacks.
- **Verification:** Ran `npm run build` which compiled successfully without error.

---

### Step 8: Multi-Layered Validation Constraints & Build Optimization
- **AI Action:** Added robust input validations on both the frontend and backend, and updated build configurations.
- **Validations Implemented:**
  - **Future Meetings:** Programmatically enforced that meeting dates/times must be in the future (backend validation in `MeetingService` and client-side validation in `CreateMeetingComponent`), while adding a `[min]` attribute to disable past dates in the calendar picker.
  - **Avatar Size Limits:** Restricted user profile avatars to a maximum size of 1 MB (validated in `UserService` and checked on file select/form submit in `SignupComponent`).
  - **Email Format Validation:** Added standard email regex pattern validation to prevent invalid email registration (enforced in `UserService` and checked using Angular form directives and onSubmit regex tests).
  - **Mandatory Participants:** Required at least one valid participant when scheduling a meeting (enforced in `MeetingService` and verified in `CreateMeetingComponent`).
- **Build Intervention:** Fixed a compilation issue where font optimization attempted to download external Google Fonts over the internet by disabling font inlining (`"fonts": { "inline": false }`) inside `angular.json`. This allowed the application to compile successfully in offline/sandboxed developer environments.

## 📋 Summary of Project Settings Changes
* **Page Title:** Set browser document title to `"Meeting Planner"`.
* **Favicon:** Generated a custom meeting-planner-themed PNG favicon containing a clock, calendar, and speech bubbles to replace the generic icon.
* **Vite Safe Serving:** Patched Vite's `fs.deny` security settings to support local serving from folders containing `.git` inside their paths.

