# UML Class Diagram — Student Attendance Tracker

Class diagram of the system as implemented (post-cleanup). It reflects the
classes wired into the running application and excludes the unused
`Course`, `Lecture`, `Lecturer`, `DatabaseManager`, and `AttendanceReportService`
that appeared in earlier design drafts.

**Legend:** solid arrow (`-->`) = association / uses · dashed arrow (`..>`) =
dependency · hollow triangle (`<|--`) = inheritance / interface extension ·
dashed triangle (`<|..`) = interface implementation.

```mermaid
classDiagram
    direction TB

    class Person {
        <<abstract>>
        -String id
        -String fullName
        -String email
    }
    class Student {
        -Group group
        -LocalDate enrolledOn
        +builder()
    }
    class Group {
        -String id
        -String name
        +builder()
    }
    class AttendanceRecord {
        -Student student
        -LocalDate date
        -AttendanceStatus status
        -LocalDateTime markedAt
        +builder()
    }
    class AttendanceStatus {
        <<enumeration>>
        PRESENT
        ABSENT
        LATE
    }

    class Repository~T, ID~ {
        <<interface>>
        +save(T) T
        +findById(ID) Optional~T~
        +findAll() List~T~
        +delete(ID) boolean
    }
    class StudentRepository {
        <<interface>>
        +findByGroup(Group) List~Student~
        +search(String) List~Student~
    }
    class AttendanceRepository {
        <<interface>>
        +findByDate(LocalDate) List~AttendanceRecord~
        +findByStudentAndDate(String, LocalDate) Optional~AttendanceRecord~
    }
    class InMemoryStudentRepository
    class JsonStudentRepository
    class InMemoryAttendanceRepository
    class JsonAttendanceRepository

    class StudentService {
        +register(String, String) Student
        +findAll() List~Student~
    }
    class AttendanceService {
        +mark(Student, LocalDate, AttendanceStatus) AttendanceRecord
        +markBulk(LocalDate, Map) List~AttendanceRecord~
        +findByDate(LocalDate) List~AttendanceRecord~
        +findAll() List~AttendanceRecord~
    }
    class AbsenceStatistics {
        <<utility>>
        +calculate(List, List) List
    }

    class StudentDto
    class AttendanceRecordDto
    class StudentMapper {
        +toDto(Student) StudentDto
        +toDomain(StudentDto, Group) Student
    }
    class AttendanceRecordMapper {
        +toDto(AttendanceRecord) AttendanceRecordDto
        +toDomain(AttendanceRecordDto, Student) AttendanceRecord
    }

    class IdGenerator {
        <<interface>>
        +newId() String
    }
    class UuidGenerator
    class Preconditions {
        <<utility>>
    }

    class StudentController {
        +handleAdd()
    }
    class AttendanceController {
        +handleSave()
        +loadForDate(LocalDate)
    }
    class StatisticsController {
        +refreshStatistics()
    }

    class AppContext {
        +bootstrap() AppContext
    }
    class AttendanceApp {
        +start(Stage)
    }

    Person <|-- Student
    Student --> Group
    AttendanceRecord --> Student
    AttendanceRecord --> AttendanceStatus

    Repository~T, ID~ <|-- StudentRepository
    Repository~T, ID~ <|-- AttendanceRepository
    StudentRepository <|.. InMemoryStudentRepository
    StudentRepository <|.. JsonStudentRepository
    AttendanceRepository <|.. InMemoryAttendanceRepository
    AttendanceRepository <|.. JsonAttendanceRepository
    IdGenerator <|.. UuidGenerator

    StudentService --> StudentRepository
    StudentService --> IdGenerator
    StudentService --> Group
    AttendanceService --> AttendanceRepository
    AttendanceService --> StudentService
    AttendanceService --> IdGenerator
    AbsenceStatistics ..> Student
    AbsenceStatistics ..> AttendanceRecord

    StudentMapper ..> Student
    StudentMapper ..> StudentDto
    AttendanceRecordMapper ..> AttendanceRecord
    AttendanceRecordMapper ..> AttendanceRecordDto

    StudentController --> StudentService
    AttendanceController --> AttendanceService
    AttendanceController --> StudentService
    StatisticsController --> AttendanceService
    StatisticsController --> StudentService

    AppContext ..> StudentService
    AppContext ..> AttendanceService
    AttendanceApp ..> AppContext
```
