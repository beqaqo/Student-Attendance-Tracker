package com.undefinedvars.attendance.persistence;

import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.Student;

import java.util.Objects;

public final class AttendanceRecordMapper {
    private AttendanceRecordMapper() {}

    public static AttendanceRecordDto toDto(AttendanceRecord r) {
        AttendanceRecordDto dto = new AttendanceRecordDto();
        dto.setId(r.getId());
        dto.setStudentId(r.getStudent().getId());
        dto.setDate(r.getDate());
        dto.setStatus(r.getStatus());
        dto.setMarkedAt(r.getMarkedAt());
        return dto;
    }

    public static AttendanceRecord toDomain(AttendanceRecordDto dto, Student student) {
        Objects.requireNonNull(student, "student cannot be null");

        return AttendanceRecord.builder()
                .id(dto.getId())
                .student(student)
                .date(dto.getDate())
                .status(dto.getStatus())
                .markedAt(dto.getMarkedAt())
                .build();
    }
}
