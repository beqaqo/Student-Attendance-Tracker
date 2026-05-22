# Student Attendance Tracker

A Java desktop application for managing and monitoring student attendance
for a single course group. Built by team **Undefined Variables**.

## Status

In development. Current functionality (Homework 2):

- Add a student via a form (full name and email)
- View all added students in a list

Planned (later milestones): attendance marking, absence statistics,
and persistent storage.

## Technology Stack

- **Language:** Java 17
- **UI:** JavaFX 17 (FXML)
- **Build:** Maven
- **Testing:** JUnit 5

## Project Structure

```
src/main/java/com/undefinedvars/attendance/
├── model/         Domain entities (Person, Student, Group, ...)
├── repository/    Data access — Repository interface + in-memory implementation
├── service/       Business logic and validation
├── controller/    JavaFX controllers (UI <-> service)
├── util/          Shared helpers (Preconditions, IdGenerator)
├── AppContext.java     Composition root — wires the object graph
└── AttendanceApp.java  Application entry point

src/main/resources/fxml/   FXML layout files
src/test/java/             Unit tests
```

The application follows a layered architecture: UI (FXML + controller),
then service, then repository. Each layer depends on the one below
through interfaces, so implementations can be swapped without affecting
callers.
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