# Student Attendance Tracker

A Java desktop application for managing and monitoring student attendance
for a single course group. Built by team **Undefined Variables**.

## Status

In development. Current functionality:

- Add a student via a form (full name and email)
- View all added students in a list
- Mark attendance per date for every student (Present / Absent / Late /
  Excused), with bulk save and pre-fill of existing records
- Persist students and attendance records to JSON on disk via Jackson,
  so data survives across application restarts

Planned (later milestones): absence statistics and reporting.

## Technology Stack

- **Language:** Java 17
- **UI:** JavaFX 17 (FXML)
- **Persistence:** Jackson (`jackson-databind` + `jackson-datatype-jsr310`)
  reading/writing JSON files under `data/`
- **Build:** Maven
- **Testing:** JUnit 5

## Project Structure

```
src/main/java/com/undefinedvars/attendance/
├── model/         Domain entities (Person, Student, Group, AttendanceRecord, ...)
├── repository/    Data access — Repository interfaces, in-memory + JSON implementations
├── persistence/   Jackson DTOs and mappers (domain <-> JSON-friendly shape)
├── service/       Business logic and validation (StudentService, AttendanceService)
├── controller/    JavaFX controllers (UI <-> service)
├── util/          Shared helpers (Preconditions, IdGenerator)
├── AppContext.java     Composition root — wires the object graph
└── AttendanceApp.java  Application entry point

src/main/resources/fxml/   FXML layout files (main, students, attendance)
src/test/java/             Unit tests
data/                      JSON persistence files (students.json, attendance.json)
```

The application follows a layered architecture: UI (FXML + controller),
then service, then repository. Each layer depends on the one below
through interfaces, so implementations can be swapped without affecting
callers. The default wiring in `AppContext` uses the JSON-backed
repositories; swap them for the in-memory implementations to get a
non-persistent run.

Persistence is handled by the `persistence/` package: domain objects are
converted to/from DTOs by `StudentMapper` / `AttendanceRecordMapper`,
and the JSON repositories use Jackson to read/write `data/students.json`
and `data/attendance.json`. Load order matters in `AppContext` —
students are loaded before attendance, since attendance records hold
`studentId` references that get resolved against the loaded students.

> **Note:** The JSON repository classes (`JsonStudentRepository`,
> `JsonAttendanceRepository`) require the `ObjectMapper` to have
> Jackson's `JavaTimeModule` registered so `LocalDate` / `LocalDateTime`
> fields (e.g. `enrolledOn`, `date`, `markedAt`) serialize as ISO-8601
> strings rather than numeric timestamps. The in-memory repositories
> are unaffected.

## Running the Application

**With Maven installed:**

```bash
mvn clean javafx:run
```

**In IntelliJ IDEA:**

Open the Maven tool window (View → Tool Windows → Maven), then
expand Plugins → javafx and double-click `javafx:run`.

## Running the Tests

```bash
mvn test
```

Or in IntelliJ, right-click `src/test` and choose Run.

## Team — Undefined Variables

- Dimitri Durmishian
- Nikoloz Beridze
- Aleksandre Maghlakelidze
- Beka Kopadze