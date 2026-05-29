package com.undefinedvars.attendance.persistence;

import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;

import java.util.Objects;

public final class StudentMapper {
    private StudentMapper() {}

    public static StudentDto toDto(Student s) {
        StudentDto dto = new StudentDto();
        dto.setId(s.getId());
        dto.setFullName(s.getFullName());
        dto.setEmail(s.getEmail());
        dto.setGroupId(s.getGroup().getId());
        dto.setEnrolledOn(s.getEnrolledOn());
        return dto;
    }

    public static Student toDomain(StudentDto dto, Group group) {
        Objects.requireNonNull(group, "group cannot be null");

        return Student.builder()
                .id(dto.getId())
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .group(group)
                .enrolledOn(dto.getEnrolledOn())
                .build();
    }
}
