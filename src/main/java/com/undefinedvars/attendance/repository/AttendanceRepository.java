package com.undefinedvars.attendance.repository;

import com.undefinedvars.attendance.model.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/*
    AttendanceRepository specializes the generic Repository contract for AttendanceRecord entities,
    adding attendance-specific lookups needed by the attendance marking screen.
    Services depend on this narrower abstraction so the extra operations are part of the type.
 */
public interface AttendanceRepository extends Repository<AttendanceRecord, String> {

    /*
        Returns all attendance records for the given date.
        Used to populate the attendance table when the user picks a date.
     */
    List<AttendanceRecord> findByDate(LocalDate date);

    /*
        Returns the single attendance record for a student on a given date, if one exists.
        Used by the service to decide whether mark() should insert or update (upsert).
     */
    Optional<AttendanceRecord> findByStudentAndDate(String studentId, LocalDate date);
}
