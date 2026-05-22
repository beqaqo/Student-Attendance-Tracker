package com.undefinedvars.attendance.repository;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;

import java.util.List;

/*
    StudentRepository specializes the generic Repository contract for Student entities,
    adding student-specific lookups. Services depend on this narrower abstraction so the
    extra operations are part of the type, not hidden behind casts.
 */
public interface StudentRepository extends Repository<Student, String> {
    List<Student> findByGroup(Group group);
    List<Student> search(String query);
}