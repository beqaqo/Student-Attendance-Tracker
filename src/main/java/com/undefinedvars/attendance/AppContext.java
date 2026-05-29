package com.undefinedvars.attendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.repository.AttendanceRepository;
import com.undefinedvars.attendance.repository.JsonAttendanceRepository;
import com.undefinedvars.attendance.repository.JsonStudentRepository;
import com.undefinedvars.attendance.repository.StudentRepository;
import com.undefinedvars.attendance.service.AttendanceService;
import com.undefinedvars.attendance.service.StudentService;
import com.undefinedvars.attendance.util.IdGenerator;
import com.undefinedvars.attendance.util.UuidGenerator;

import java.nio.file.Path;

/*
    AppContext is the central composition root: it constructs the full object
    graph and exposes the top-level services to the rest of the app. Wiring
    repositories, generators, mappers, and services in one place keeps
    dependency injection explicit and the rest of the codebase loosely coupled.
 */
public final class AppContext {
    private static final String DEFAULT_GROUP_ID = "default-group";
    private static final Path STUDENTS_FILE = Path.of("data", "students.json");
    private static final Path ATTENDANCE_FILE = Path.of("data", "attendance.json");

    private final StudentService studentService;
    private final AttendanceService attendanceService;

    private AppContext(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }

    /**
     * Constructs the full object graph for the application.
     * <p>
     * Load order is significant: the student repository must be constructed
     * (and thus loaded) before the attendance repository, because attendance
     * records hold studentId references that get resolved against the loaded
     * students. See {@link JsonAttendanceRepository} for details.
     */
    public static AppContext bootstrap() {
        // Shared infrastructure: one ObjectMapper, configured with JavaTimeModule
        // so LocalDate and LocalDateTime fields (enrolledOn, date, markedAt) are
        // serialized as ISO-8601 strings rather than numeric timestamps.
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Group group = Group.builder().id(DEFAULT_GROUP_ID).name("Group A").build();
        IdGenerator idGenerator = new UuidGenerator();

        // Load order matters: students must be fully loaded before attendance,
        // because JsonAttendanceRepository's constructor resolves studentId
        // references against the student repo.
        StudentRepository studentRepo = new JsonStudentRepository(STUDENTS_FILE, objectMapper, group);
        AttendanceRepository attendanceRepo = new JsonAttendanceRepository(ATTENDANCE_FILE, objectMapper, studentRepo);

        // Services on top of the repositories.
        StudentService studentService = new StudentService(studentRepo, idGenerator, group);
        AttendanceService attendanceService = new AttendanceService(attendanceRepo, studentService, idGenerator);

        return new AppContext(studentService, attendanceService);
    }

    public StudentService getStudentService() {
        return studentService;
    }

    public AttendanceService getAttendanceService() {
        return attendanceService;
    }
}