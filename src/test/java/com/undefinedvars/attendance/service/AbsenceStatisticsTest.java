package com.undefinedvars.attendance.service;

import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.AttendanceStatus;
import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbsenceStatisticsTest {

    private Group group;
    private Student alice;
    private Student bob;

    @BeforeEach
    void setUp() {
        group = Group.builder()
                .id("g1")
                .name("Test Group")
                .build();

        alice = newStudent("s1", "Alice Smith", "alice@example.com");
        bob = newStudent("s2", "Bob Jones", "bob@example.com");
    }

    private Student newStudent(String id, String fullName, String email) {
        return Student.builder()
                .id(id)
                .fullName(fullName)
                .email(email)
                .group(group)
                .enrolledOn(LocalDate.of(2026, 1, 1))
                .build();
    }

    private AttendanceRecord newRecord(String id, Student student, LocalDate date, AttendanceStatus status) {
        return AttendanceRecord.builder()
                .id(id)
                .student(student)
                .date(date)
                .status(status)
                .build();
    }

    @Test
    void calculate_countsAbsencesAndLateMarksPerStudent() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        List<AttendanceRecord> records = List.of(
                newRecord("r1", alice, today, AttendanceStatus.ABSENT),
                newRecord("r2", alice, yesterday, AttendanceStatus.PRESENT),
                newRecord("r3", bob, today, AttendanceStatus.LATE),
                newRecord("r4", bob, yesterday, AttendanceStatus.ABSENT)
        );

        List<AbsenceStatistics.StudentAbsenceStats> stats =
                AbsenceStatistics.calculate(List.of(alice, bob), records);

        AbsenceStatistics.StudentAbsenceStats aliceStats = stats.stream()
                .filter(stat -> stat.getStudent().equals(alice))
                .findFirst()
                .orElseThrow();

        AbsenceStatistics.StudentAbsenceStats bobStats = stats.stream()
                .filter(stat -> stat.getStudent().equals(bob))
                .findFirst()
                .orElseThrow();

        assertEquals(2, aliceStats.getTotalMarked());
        assertEquals(1, aliceStats.getAbsentCount());
        assertEquals(0, aliceStats.getLateCount());
        assertEquals(50.0, aliceStats.getAbsenceRate());

        assertEquals(2, bobStats.getTotalMarked());
        assertEquals(1, bobStats.getAbsentCount());
        assertEquals(1, bobStats.getLateCount());
        assertEquals(50.0, bobStats.getAbsenceRate());
    }

    @Test
    void calculate_studentWithNoRecordsHasZeroRate() {
        List<AbsenceStatistics.StudentAbsenceStats> stats =
                AbsenceStatistics.calculate(List.of(alice), List.of());

        assertEquals(1, stats.size());
        assertEquals(0, stats.get(0).getTotalMarked());
        assertEquals(0, stats.get(0).getAbsentCount());
        assertEquals(0.0, stats.get(0).getAbsenceRate());
        assertEquals("0.0%", stats.get(0).getAbsenceRateText());
    }
}