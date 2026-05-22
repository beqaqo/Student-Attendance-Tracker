package com.undefinedvars.attendance.service;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.repository.StudentRepository;
import com.undefinedvars.attendance.util.IdGenerator;
import com.undefinedvars.attendance.util.Preconditions;

import java.util.List;
import java.util.Objects;

/*
    Service class responsible for managing student-related operations, such as registration and retrieval.
 */
public final class StudentService {
    private final StudentRepository studentRepository;
    private final IdGenerator idGenerator;
    private final Group group;

    /*
        Dependency Injection
     */
    public StudentService(StudentRepository studentRepository, IdGenerator idGenerator, Group group) {
        this.studentRepository = Objects.requireNonNull(studentRepository, "studentRepository cannot be null");
        this.idGenerator = Objects.requireNonNull(idGenerator, "idGenerator cannot be null");
        this.group = Objects.requireNonNull(group, "group cannot be null");
    }

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

    public List<Student> findAll() {
        return studentRepository.findAll();
    }
}
