package com.undefinedvars.attendance;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.repository.InMemoryStudentRepository;
import com.undefinedvars.attendance.repository.Repository;
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

    private AppContext(StudentService studentService) {
        this.studentService = studentService;
    }

    public static AppContext bootstrap() {
        Group group = Group.builder().id(DEFAULT_GROUP_ID).name("Group A").build();
        IdGenerator idGenerator = new UuidGenerator();
        Repository<Student, String> repo = new InMemoryStudentRepository();
        StudentService service = new StudentService(repo, idGenerator, group);
        return new AppContext(service);
    }

    public StudentService getStudentService() {
        return studentService;
    }
}
