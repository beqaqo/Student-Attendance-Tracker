package com.undefinedvars.attendance.repository;

import com.undefinedvars.attendance.model.Student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
    InMemoryStudentRepository is a concrete implementation of the Repository interface for Student entities.
    It uses a LinkedHashMap to store students in memory, allowing for efficient retrieval and insertion while maintaining insertion order.
 */
public final class InMemoryStudentRepository implements Repository<Student, String>{
    private final Map<String, Student> inMemoryStorage = new LinkedHashMap<>();

    @Override
    public Student save(Student student) {
        inMemoryStorage.put(student.getId(), student);
        return student;
    }

    @Override
    public Optional<Student> findById(String id) {
        return Optional.ofNullable(inMemoryStorage.get(id));
    }

    @Override
    public List<Student> findAll() {
        return new ArrayList<>(inMemoryStorage.values());
    }

    @Override
    public boolean delete(String id) {
        return inMemoryStorage.remove(id) != null;
    }
}
