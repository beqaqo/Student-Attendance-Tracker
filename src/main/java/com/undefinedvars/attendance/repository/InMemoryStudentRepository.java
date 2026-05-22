package com.undefinedvars.attendance.repository;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/*
    InMemoryStudentRepository is a concrete implementation of the StudentRepository interface for Student entities.
    It uses a LinkedHashMap to store students in memory, allowing for efficient retrieval and insertion while maintaining insertion order.
 */
public final class InMemoryStudentRepository implements StudentRepository {
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

    @Override
    public List<Student> findByGroup(Group group) {
        Objects.requireNonNull(group, "group cannot be null");
        List<Student> result = new ArrayList<>();
        for (Student student : inMemoryStorage.values()) {
            if (group.equals(student.getGroup())) {
                result.add(student);
            }
        }
        return result;
    }

    @Override
    public List<Student> search(String query) {
        Objects.requireNonNull(query, "query cannot be null");
        String needle = query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) {
            return new ArrayList<>();
        }
        List<Student> result = new ArrayList<>();
        for (Student student : inMemoryStorage.values()) {
            String name = student.getFullName().toLowerCase(Locale.ROOT);
            String email = student.getEmail().toLowerCase(Locale.ROOT);
            if (name.contains(needle) || email.contains(needle)) {
                result.add(student);
            }
        }
        return result;
    }
}