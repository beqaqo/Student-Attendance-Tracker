package com.undefinedvars.attendance.service;

import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.AttendanceStatus;
import com.undefinedvars.attendance.model.Group;
import com.undefinedvars.attendance.model.Student;
import com.undefinedvars.attendance.repository.InMemoryAttendanceRepository;
import com.undefinedvars.attendance.repository.InMemoryStudentRepository;
import com.undefinedvars.attendance.repository.StudentRepository;
import com.undefinedvars.attendance.util.IdGenerator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttendanceServiceTest {

    /*
        A predictable IdGenerator, returns "id-1", "id-2", ...
        Mirrored from StudentServiceTest so tests can assert exact ids.
     */
    private static final class SequentialIdGenerator implements IdGenerator {
        private int counter = 0;
        @Override
        public String newId() {
            counter++;
            return "id-" + counter;
        }
    }

    private AttendanceService attendanceService;
    private StudentService studentService;
    private Student alice;
    private Student bob;

    /*
        @BeforeEach: fresh service and two pre-registered students before each test.
     */
    @BeforeEach
    void setUp() {
        Group group = Group.builder().id("g1").name("Test Group").build();

        StudentRepository studentRepo = new InMemoryStudentRepository();
        IdGenerator studentIdGen = new SequentialIdGenerator();
        studentService = new StudentService(studentRepo, studentIdGen, group);

        alice = studentService.register("Alice Smith", "alice@example.com");
        bob   = studentService.register("Bob Jones",  "bob@example.com");

        InMemoryAttendanceRepository attendanceRepo = new InMemoryAttendanceRepository();
        IdGenerator attendanceIdGen = new SequentialIdGenerator();
        attendanceService = new AttendanceService(attendanceRepo, studentService, attendanceIdGen);
    }

    // mark() — happy path

    @Test
    void mark_validInput_createsRecord() {
        LocalDate today = LocalDate.now();
        AttendanceRecord record = attendanceService.mark(alice, today, AttendanceStatus.PRESENT);

        assertEquals(alice, record.getStudent());
        assertEquals(today, record.getDate());
        assertEquals(AttendanceStatus.PRESENT, record.getStatus());
    }

    // mark() validation

    @Test
    void mark_futureDate_throws() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        assertThrows(IllegalArgumentException.class,
                () -> attendanceService.mark(alice, tomorrow, AttendanceStatus.PRESENT));
    }

    @Test
    void mark_nullStudent_throws() {
        assertThrows(NullPointerException.class,
                () -> attendanceService.mark(null, LocalDate.now(), AttendanceStatus.PRESENT));
    }

    @Test
    void mark_nullDate_throws() {
        assertThrows(NullPointerException.class,
                () -> attendanceService.mark(alice, null, AttendanceStatus.PRESENT));
    }

    @Test
    void mark_nullStatus_throws() {
        assertThrows(NullPointerException.class,
                () -> attendanceService.mark(alice, LocalDate.now(), null));
    }

    // mark() — upsert behaviour

    @Test
    void mark_upsert_secondMarkOverwritesStatus() {
        LocalDate today = LocalDate.now();
        attendanceService.mark(alice, today, AttendanceStatus.PRESENT);
        attendanceService.mark(alice, today, AttendanceStatus.ABSENT);

        // Still exactly one record for alice on today
        List<AttendanceRecord> records = attendanceService.findByDate(today);
        List<AttendanceRecord> aliceRecords = records.stream()
                .filter(r -> r.getStudent().equals(alice))
                .toList();

        assertEquals(1, aliceRecords.size(), "Upsert must produce exactly one record");
        assertEquals(AttendanceStatus.ABSENT, aliceRecords.get(0).getStatus(),
                "Status must be updated to the latest value");
    }

    @Test
    void mark_upsert_differentDatesAreIndependent() {
        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        attendanceService.mark(alice, today,     AttendanceStatus.PRESENT);
        attendanceService.mark(alice, yesterday, AttendanceStatus.ABSENT);

        assertEquals(1, attendanceService.findByDate(today).size());
        assertEquals(1, attendanceService.findByDate(yesterday).size());
    }

    // markBulk()

    @Test
    void markBulk_populatesAllStudents() {
        LocalDate today = LocalDate.now();
        Map<Student, AttendanceStatus> statuses = Map.of(
                alice, AttendanceStatus.PRESENT,
                bob,   AttendanceStatus.LATE
        );

        List<AttendanceRecord> saved = attendanceService.markBulk(today, statuses);
        assertEquals(2, saved.size());
        assertEquals(2, attendanceService.findByDate(today).size());
    }

    // findByDate()

    @Test
    void findByDate_returnsOnlyMatchingDate() {
        LocalDate today     = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        attendanceService.mark(alice, today,     AttendanceStatus.PRESENT);
        attendanceService.mark(bob,   yesterday, AttendanceStatus.ABSENT);

        assertEquals(1, attendanceService.findByDate(today).size());
        assertEquals(1, attendanceService.findByDate(yesterday).size());
        assertEquals(0, attendanceService.findByDate(today.minusDays(2)).size());
    }
}
