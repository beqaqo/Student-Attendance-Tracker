package com.undefinedvars.attendance.repository;

import com.undefinedvars.attendance.model.AttendanceRecord;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/*
    InMemoryAttendanceRepository is a concrete implementation of AttendanceRepository.
    It mirrors InMemoryStudentRepository, a LinkedHashMap backing store, insertion-order
    preserved and has no external dependencies.
 */
public final class InMemoryAttendanceRepository implements AttendanceRepository {

    private final Map<String, AttendanceRecord> inMemoryStorage = new LinkedHashMap<>();

    @Override
    public AttendanceRecord save(AttendanceRecord record) {
        inMemoryStorage.put(record.getId(), record);
        return record;
    }

    @Override
    public Optional<AttendanceRecord> findById(String id) {
        return Optional.ofNullable(inMemoryStorage.get(id));
    }

    @Override
    public List<AttendanceRecord> findAll() {
        return new ArrayList<>(inMemoryStorage.values());
    }

    @Override
    public boolean delete(String id) {
        return inMemoryStorage.remove(id) != null;
    }

    @Override
    public List<AttendanceRecord> findByDate(LocalDate date) {
        Objects.requireNonNull(date, "date cannot be null");
        List<AttendanceRecord> result = new ArrayList<>();
        for (AttendanceRecord record : inMemoryStorage.values()) {
            if (date.equals(record.getDate())) {
                result.add(record);
            }
        }
        return result;
    }

    @Override
    public Optional<AttendanceRecord> findByStudentAndDate(String studentId, LocalDate date) {
        Objects.requireNonNull(studentId, "studentId cannot be null");
        Objects.requireNonNull(date, "date cannot be null");
        for (AttendanceRecord record : inMemoryStorage.values()) {
            if (studentId.equals(record.getStudent().getId()) && date.equals(record.getDate())) {
                return Optional.of(record);
            }
        }
        return Optional.empty();
    }
}
