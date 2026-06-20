# Student Attendance Tracker

A JavaFX desktop application for managing student attendance for a single course group. The project lets a user register students, mark attendance by date, persist data to JSON files, and view absence statistics.

The application was built as a design-patterns-oriented Java project. Its structure emphasizes clear separation between UI, business logic, persistence, and domain models.

## Project Overview

Student Attendance Tracker solves a small but practical classroom workflow:

- Maintain a list of students.
- Mark each student as `PRESENT`, `ABSENT`, or `LATE` for a selected date.
- Save students and attendance records between application runs.
- Calculate absence and late statistics per student.

The current application targets a single default group and stores data locally under the `data/` directory as JSON.

## Technology Stack

- **Language:** Java 17
- **UI framework:** JavaFX 17 with FXML
- **Build tool:** Maven
- **Persistence:** Jackson JSON serialization
- **Testing:** JUnit 5
- **Architecture style:** Layered architecture with constructor-based dependency injection

## Architecture

The codebase follows a layered design:

```text
FXML Views
    |
Controllers
    |
Services
    |
Repository interfaces
    |
Repository implementations
    |
JSON files / in-memory storage
```

### Main Layers

- `model/` contains domain objects such as `Student`, `Group`, `AttendanceRecord`, `Course`, and `Lecture`.
- `controller/` contains JavaFX controllers that coordinate user actions with services.
- `service/` contains business logic such as student registration, attendance marking, and statistics calculation.
- `repository/` defines data-access interfaces and provides JSON-backed and in-memory implementations.
- `persistence/` contains DTOs and mappers for converting domain objects to JSON-friendly forms.
- `util/` contains shared helpers such as validation and ID generation.
- `AppContext.java` wires the application object graph.
- `AttendanceApp.java` is the JavaFX entry point.

## Build and Run

### Requirements

- JDK 17 or newer
- Maven

### Dependencies

Dependencies are declared in `pom.xml`:

- `javafx-controls`
- `javafx-fxml`
- `jackson-databind`
- `jackson-datatype-jsr310`
- `junit-jupiter`

### Run the Application

From the project root:

```bash
mvn clean javafx:run
```

The application entry point is:

```text
src/main/java/com/undefinedvars/attendance/AttendanceApp.java
```

The Maven configuration points to:

```xml
<main.class>com.undefinedvars.attendance.AttendanceApp</main.class>
```

### Run Tests

```bash
mvn test
```

## Design Patterns Used

### 1. Builder Pattern

The domain model uses builders to construct immutable objects and validate required fields before creation.

Representative example from `src/main/java/com/undefinedvars/attendance/model/AttendanceRecord.java`:

```java
public static Builder builder() {return new Builder();}

public static final class Builder {
    private String id;
    private Student student;
    private LocalDate date;
    private AttendanceStatus status;
    private LocalDateTime markedAt;

    public Builder id(String id) {this.id = id; return this;}
    public Builder student(Student student) {this.student = student; return this;}
    public Builder date(LocalDate date) {this.date = date; return this;}
    public Builder status(AttendanceStatus status) {this.status = status; return this;}
    public Builder markedAt(LocalDateTime markedAt) {this.markedAt = markedAt; return this;}

    public AttendanceRecord build() {
        Preconditions.requireNonBlank(id, "id");
        Objects.requireNonNull(student, "student cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(status, "status cannot be null");

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("date cannot be in the future");
        }

        LocalDateTime currentMarkingDate = LocalDateTime.now();
        LocalDateTime effectiveMarkingDateTime = (markedAt != null) ? markedAt : currentMarkingDate;

        if (effectiveMarkingDateTime.isAfter(currentMarkingDate)) {
            throw new IllegalArgumentException("markedAt cannot be in the future");
        }

        return new AttendanceRecord(id, student, date, status, effectiveMarkingDateTime);
    }
}
```

Purpose:

- Avoid constructors with many parameters.
- Keep domain objects immutable.
- Put construction-time validation in one place.
- Make object creation readable:

```java
AttendanceRecord record = AttendanceRecord.builder()
        .id(recordId)
        .student(student)
        .date(date)
        .status(status)
        .build();
```

Other builder examples appear in `Student`, `Group`, `Course`, `Lecture`, and `Lecturer`.

### 2. Repository Pattern

Repositories hide storage details behind interfaces. Services depend on repository abstractions instead of concrete persistence classes.

From `src/main/java/com/undefinedvars/attendance/repository/Repository.java`:

```java
public interface Repository<T, ID> {
    T save(T entity);
    Optional<T> findById(ID id);
    List<T> findAll();
    boolean delete(ID id);
}
```

Specialized repository from `AttendanceRepository.java`:

```java
public interface AttendanceRepository extends Repository<AttendanceRecord, String> {
    List<AttendanceRecord> findByDate(LocalDate date);
    Optional<AttendanceRecord> findByStudentAndDate(String studentId, LocalDate date);
}
```

Concrete implementations include:

- `InMemoryStudentRepository`
- `InMemoryAttendanceRepository`
- `JsonStudentRepository`
- `JsonAttendanceRepository`

Purpose:

- Isolate persistence logic from business logic.
- Allow JSON-backed storage in production.
- Allow in-memory repositories in tests.
- Make persistence implementations swappable.

### 3. Strategy Pattern

The project uses strategy-like interfaces for behavior that can vary, especially ID generation and persistence implementation.

From `src/main/java/com/undefinedvars/attendance/util/IdGenerator.java`:

```java
public interface IdGenerator {
    String newId();
}
```

Production implementation from `UuidGenerator.java`:

```java
public final class UuidGenerator implements IdGenerator {
    @Override
    public String newId() {
        return java.util.UUID.randomUUID().toString();
    }
}
```

Test implementation from `StudentServiceTest.java`:

```java
private static final class SequentialIdGenerator implements IdGenerator {
    private int counter = 0;

    @Override
    public String newId() {
        counter++;
        return "id-" + counter;
    }
}
```

Purpose:

- Production code can use random UUIDs.
- Tests can use predictable IDs.
- `StudentService` and `AttendanceService` do not need to know how IDs are generated.

### 4. Dependency Injection

Dependencies are passed through constructors instead of being created internally by services and controllers.

From `src/main/java/com/undefinedvars/attendance/service/StudentService.java`:

```java
public StudentService(StudentRepository studentRepository, IdGenerator idGenerator, Group group) {
    this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository cannot be null");
    this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator cannot be null");
    this.group = Objects.requireNonNull(group, "group cannot be null");
}
```

From `src/main/java/com/undefinedvars/attendance/service/AttendanceService.java`:

```java
public AttendanceService(AttendanceRepository attendanceRepository,
                         StudentService studentService,
                         IdGenerator idGenerator) {
    this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository cannot be null");
    this.studentService = Objects.requireNonNull(studentService, "studentService cannot be null");
    this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator cannot be null");
}
```

Purpose:

- Reduce tight coupling.
- Improve testability.
- Make application wiring explicit.
- Allow repositories and generators to be replaced without changing services.

### 5. Factory / Composition Root

`AppContext.bootstrap()` acts as the application's composition root. It creates shared infrastructure, repositories, services, and returns a ready-to-use context.

From `src/main/java/com/undefinedvars/attendance/AppContext.java`:

```java
public static AppContext bootstrap() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());

    Group group = Group.builder().id(DEFAULT_GROUP_ID).name("Group A").build();
    IdGenerator idGenerator = new UuidGenerator();

    StudentRepository studentRepo = new JsonStudentRepository(STUDENTS_FILE, objectMapper, group);
    AttendanceRepository attendanceRepo = new JsonAttendanceRepository(ATTENDANCE_FILE, objectMapper, studentRepo);

    StudentService studentService = new StudentService(studentRepo, idGenerator, group);
    AttendanceService attendanceService = new AttendanceService(attendanceRepo, studentService, idGenerator);

    return new AppContext(studentService, attendanceService);
}
```

Purpose:

- Centralize object creation.
- Keep wiring out of business classes.
- Ensure load order is correct: students are loaded before attendance records because attendance records reference students.

JavaFX also uses a controller factory in `AttendanceApp.java`:

```java
loader.setControllerFactory(type -> {
    if (type == StudentController.class) {
        return new StudentController(context.getStudentService());
    }
    if (type == AttendanceController.class) {
        return new AttendanceController(
                context.getAttendanceService(),
                context.getStudentService());
    }
    if (type == StatisticsController.class) {
        return new StatisticsController(
                context.getAttendanceService(),
                context.getStudentService());
    }
    throw new IllegalArgumentException("Unknown controller type: " + type);
});
```

This allows FXML controllers to receive constructor-injected dependencies.

### 6. Mapper Pattern

Mappers convert between domain objects and DTOs used for persistence.

From `src/main/java/com/undefinedvars/attendance/persistence/StudentMapper.java`:

```java
public static StudentDto toDto(Student s) {
    StudentDto dto = new StudentDto();
    dto.setId(s.getId());
    dto.setFullName(s.getFullName());
    dto.setEmail(s.getEmail());
    dto.setGroupId(s.getGroup().getId());
    dto.setEnrolledOn(s.getEnrolledOn());
    return dto;
}

public static Student toDomain(StudentDto dto, Group group) {
    Objects.requireNonNull(group, "group cannot be null");

    return Student.builder()
            .id(dto.getId())
            .fullName(dto.getFullName())
            .email(dto.getEmail())
            .group(group)
            .enrolledOn(dto.getEnrolledOn())
            .build();
}
```

Purpose:

- Keep JSON structure separate from domain objects.
- Rebuild domain objects through validated builders when loading from disk.
- Avoid exposing persistence-only fields or shapes to business logic.

### 7. Data Transfer Object

DTOs are simple objects used for JSON serialization and deserialization.

From `src/main/java/com/undefinedvars/attendance/persistence/StudentDto.java`:

```java
public final class StudentDto {
    private String id;
    private String fullName;
    private String email;
    private String groupId;
    private LocalDate enrolledOn;

    public StudentDto() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
```

Purpose:

- Provide Jackson-friendly no-argument constructors and setters.
- Store references in a JSON-friendly way, such as `groupId` or `studentId`.
- Keep persistence data passive and separate from domain validation.

### 8. Model-View-Controller

The JavaFX UI follows an MVC-style structure:

- FXML files define views.
- Controller classes handle UI events.
- Services and models contain business behavior.

Example from `src/main/resources/fxml/students_view.fxml`:

```xml
<VBox xmlns:fx="http://javafx.com/fxml"
      fx:controller="com.undefinedvars.attendance.controller.StudentController"
      spacing="10">

    <HBox spacing="8">
        <TextField fx:id="nameField" promptText="Full name"/>
        <TextField fx:id="emailField" promptText="Email"/>
        <Button fx:id="addButton" text="Add" onAction="#handleAdd"/>
    </HBox>

    <TableView fx:id="studentTable" VBox.vgrow="ALWAYS">
        <columns>
            <TableColumn fx:id="nameColumn" text="Name" prefWidth="200"/>
            <TableColumn fx:id="emailColumn" text="Email" prefWidth="280"/>
        </columns>
    </TableView>
</VBox>
```

Matching controller behavior from `StudentController.java`:

```java
@FXML
private void handleAdd() {
    try {
        studentService.register(nameField.getText(), emailField.getText());
        refreshTable();
        clearForm();
        errorLabel.setText("");
    } catch (RuntimeException ex) {
        errorLabel.setText(ex.getMessage());
    }
}
```

Purpose:

- Keep UI layout separate from UI behavior.
- Keep business rules out of controllers.
- Make each layer easier to understand and test.

## Core Functionality

### Student Registration

Users can register students with a full name and email address.

Implementation:

- `StudentController.handleAdd()` reads input from the UI.
- `StudentService.register()` validates input, generates an ID, builds a `Student`, and saves it.
- `StudentRepository.save()` stores the student.

From `StudentService.java`:

```java
public Student register(String fullName, String email) {
    Preconditions.requireNonBlank(fullName, "fullName");
    Preconditions.requireValidEmail(email, "email");
    String id = idGenerator.newId();
    Student student = Student.builder()
            .id(id)
            .fullName(fullName)
            .email(email)
            .group(group)
            .build();
    return studentRepository.save(student);
}
```

### Attendance Marking

Users can select a date and mark each student as present, absent, or late.

Implementation:

- `AttendanceController` loads all students for the selected date.
- Existing attendance records are used to pre-fill statuses.
- Saving calls `AttendanceService.markBulk()`.

From `AttendanceService.java`:

```java
public List<AttendanceRecord> markBulk(LocalDate date, Map<Student, AttendanceStatus> statuses) {
    Objects.requireNonNull(date, "date cannot be null");
    Objects.requireNonNull(statuses, "statuses cannot be null");

    List<AttendanceRecord> saved = new ArrayList<>();
    for (Map.Entry<Student, AttendanceStatus> entry : statuses.entrySet()) {
        saved.add(mark(entry.getKey(), date, entry.getValue()));
    }
    return saved;
}
```

### Attendance Upsert Rule

The application ensures there is at most one attendance record per student per date. If a record already exists, the service reuses its ID and overwrites the status.

From `AttendanceService.java`:

```java
Optional<AttendanceRecord> existing =
        attendanceRepository.findByStudentAndDate(student.getId(), date);

String recordId = existing.map(AttendanceRecord::getId).orElseGet(idGenerator::newId);
```

Purpose:

- Prevent duplicate attendance records.
- Allow users to correct a previously saved status.

### JSON Persistence

Students and attendance records are saved to JSON files under `data/`.

Implementation:

- `JsonStudentRepository` loads and persists students.
- `JsonAttendanceRepository` loads and persists attendance records.
- DTOs and mappers translate between domain objects and JSON shapes.

From `JsonStudentRepository.java`:

```java
private void persist() {
    try {
        Files.createDirectories(path.getParent());

        List<StudentDto> dtos = new ArrayList<>();
        for (Student student : inMemoryStorage.values()) {
            dtos.add(StudentMapper.toDto(student));
        }

        objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(path.toFile(), dtos);
    } catch (IOException e) {
        System.err.println("Failed to persist students to " + path + ": " + e.getMessage());
    }
}
```

### Absence Statistics

The statistics screen calculates totals for:

- Total marked records
- Absences
- Late marks
- Absence percentage

Implementation:

- `StatisticsController.refreshStatistics()` loads students and attendance records from services.
- `AbsenceStatistics.calculate()` computes per-student statistics.

From `AbsenceStatistics.java`:

```java
for (Student student : students) {
    int totalMarked = 0;
    int absentCount = 0;
    int lateCount = 0;

    for (AttendanceRecord record : records) {
        if (!student.equals(record.getStudent())) {
            continue;
        }

        totalMarked++;

        if (record.getStatus() == AttendanceStatus.ABSENT) {
            absentCount++;
        } else if (record.getStatus() == AttendanceStatus.LATE) {
            lateCount++;
        }
    }

    result.add(new StudentAbsenceStats(student, totalMarked, absentCount, lateCount));
}
```

### Validation

Validation is enforced in services and domain builders.

Examples:

- Names and IDs cannot be blank.
- Emails must match a simple email pattern.
- Attendance dates cannot be in the future.
- Attendance status cannot be null.

From `Preconditions.java`:

```java
public static String requireValidEmail(String email, String fieldName) {
    requireNonBlank(email, fieldName);
    if (!EMAIL_PATTERN.matcher(email).matches()) {
        throw new IllegalArgumentException(fieldName + " must be a valid email address");
    }
    return email;
}
```

## Codebase Structure

```text
.
|-- pom.xml
|-- README.md
|-- src
|   |-- main
|   |   |-- java
|   |   |   `-- com/undefinedvars/attendance
|   |   |       |-- AppContext.java
|   |   |       |-- AttendanceApp.java
|   |   |       |-- controller/
|   |   |       |-- model/
|   |   |       |-- persistence/
|   |   |       |-- repository/
|   |   |       |-- service/
|   |   |       `-- util/
|   |   `-- resources
|   |       `-- fxml/
|   `-- test
|       `-- java/com/undefinedvars/attendance/
`-- target/
```

### Key Files

- `AttendanceApp.java`: JavaFX application entry point.
- `AppContext.java`: Composition root that wires repositories, services, and infrastructure.
- `StudentService.java`: Handles student registration and retrieval.
- `AttendanceService.java`: Handles attendance marking and upsert behavior.
- `AbsenceStatistics.java`: Calculates absence-related statistics.
- `Repository.java`: Generic data-access abstraction.
- `JsonStudentRepository.java`: JSON-backed student persistence.
- `JsonAttendanceRepository.java`: JSON-backed attendance persistence.
- `StudentMapper.java`: Converts between `Student` and `StudentDto`.
- `AttendanceRecordMapper.java`: Converts between `AttendanceRecord` and `AttendanceRecordDto`.
- `main_view.fxml`: Main tab layout.
- `students_view.fxml`: Student registration view.
- `attendance_view.fxml`: Attendance marking view.
- `statistics_view.fxml`: Statistics view.

## Testing

The test suite uses JUnit 5 and in-memory repositories to verify business logic without relying on disk persistence.

Examples:

- `StudentServiceTest` verifies student registration and validation.
- `AttendanceServiceTest` verifies attendance marking, validation, and upsert behavior.
- `AbsenceStatisticsTest` verifies absence and late-count calculations.
- JSON repository tests verify persistence behavior.

Run tests with:

```bash
mvn test
```

## Notes and Current Limitations

- The application currently targets one default group.
- JSON persistence rewrites the full file after each mutation.
- Write failures are logged to `stderr`; there is no retry or recovery mechanism.
- Atomic file writes, file locking, export/reporting, and multi-course support are not currently implemented.
