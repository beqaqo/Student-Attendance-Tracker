package com.undefinedvars.attendance.service;

import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.AttendanceStatus;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.repository.AttendanceRepository;
import com.undefinedvars.attendance.util.IdGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/*
    Service class responsible for attendance-related operations.
    Enforces the business rules: non-future date, valid student and status,
    and the upsert rule, exactly one AttendanceRecord per (student, date) pair.
 */
public final class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentService studentService;
    private final IdGenerator idGenerator;

    /*
        Dependency Injection - mirrors StudentService constructor style.
     */
    public AttendanceService(AttendanceRepository attendanceRepository,
                             StudentService studentService,
                             IdGenerator idGenerator) {
        this.attendanceRepository = Objects.requireNonNull(attendanceRepository, "attendanceRepository cannot be null");
        this.studentService = Objects.requireNonNull(studentService, "studentService cannot be null");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator cannot be null");
    }

    /*
        Marks (or updates) attendance for a single student on a given date.
        Upsert rule: if a record for (student, date) already exists its status
        is overwritten in place — the old record is replaced, not a new one added.
        This guarantees at most one record per student per date.
     */
    public AttendanceRecord mark(Student student, LocalDate date, AttendanceStatus status) {
        Objects.requireNonNull(student, "student cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(status, "status cannot be null");

        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("date cannot be in the future");
        }

        // Upsert: reuse the existing id if a record already exists for this student and date
        Optional<AttendanceRecord> existing =
                attendanceRepository.findByStudentAndDate(student.getId(), date);

        String recordId = existing.map(AttendanceRecord::getId).orElseGet(idGenerator::newId);

        AttendanceRecord record = AttendanceRecord.builder()
                .id(recordId)
                .student(student)
                .date(date)
                .status(status)
                .build();

        return attendanceRepository.save(record);
    }

    /*
        Marks attendance in bulk for an entire class on a given date.
        Calls mark() for each entry, so the upsert rule is applied per student.
        The natural flow is: user picks a date, sees all students, adjusts
        statuses and then clicks Save, which calls markBulk.
     */
    public List<AttendanceRecord> markBulk(LocalDate date, Map<Student, AttendanceStatus> statuses) {
        Objects.requireNonNull(date, "date cannot be null");
        Objects.requireNonNull(statuses, "statuses cannot be null");

        List<AttendanceRecord> saved = new ArrayList<>();
        for (Map.Entry<Student, AttendanceStatus> entry : statuses.entrySet()) {
            saved.add(mark(entry.getKey(), date, entry.getValue()));
        }
        return saved;
    }

    /*
       Returns all attendance records.
       Used by the statistics screen to calculate absence totals and rates.
    */
    public List<AttendanceRecord> findAll() {
        return attendanceRepository.findAll();
    }

    /*
        Returns all attendance records for the given date.
        Used by the controller to pre-fill the table when the user picks a date.
     */
    public List<AttendanceRecord> findByDate(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        return attendanceRepository.findByDate(date);
    }
}
