package com.undefinedvars.attendance;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.repository.AttendanceRepository;
import com.undefinedvars.attendance.repository.InMemoryAttendanceRepository;
import com.undefinedvars.attendance.repository.InMemoryStudentRepository;
import com.undefinedvars.attendance.repository.StudentRepository;
import com.undefinedvars.attendance.service.AttendanceService;
import com.undefinedvars.attendance.service.StudentService;
import com.undefinedvars.attendance.util.IdGenerator;
import com.undefinedvars.attendance.util.UuidGenerator;

/*
    AppContext is a central class responsible for initializing and providing access to application-wide services and resources.
    It follows the Dependency Injection pattern to manage dependencies and promote loose coupling between components.
 */
public final class AppContext {
    private static final String DEFAULT_GROUP_ID = "default-group";
    private final StudentService studentService;
    private final AttendanceService attendanceService;

    private AppContext(StudentService studentService, AttendanceService attendanceService) {
        this.studentService = studentService;
        this.attendanceService = attendanceService;
    }

    public static AppContext bootstrap() {
        Group group = Group.builder().id(DEFAULT_GROUP_ID).name("Group A").build();
        IdGenerator idGenerator = new UuidGenerator();
        StudentRepository studentRepo = new InMemoryStudentRepository();
        StudentService studentService = new StudentService(studentRepo, idGenerator, group);

        AttendanceRepository attendanceRepo = new InMemoryAttendanceRepository();
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
