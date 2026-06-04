package com.undefinedvars.attendance.service;

import com.undefinedvars.attendance.model.AttendanceRecord;
import com.undefinedvars.attendance.model.AttendanceStatus;
import com.undefinedvars.attendance.model.Student;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/*
    Calculates absence statistics from attendance records.
    This class has no JavaFX code and no persistence logic; it only works with
    already-loaded domain objects, which keeps it easy to test.
 */
public final class AbsenceStatistics {

    private AbsenceStatistics() {
        // utility class
    }

    public static List<StudentAbsenceStats> calculate(List<Student> students,
                                                      List<AttendanceRecord> records) {
        Objects.requireNonNull(students, "students cannot be null");
        Objects.requireNonNull(records, "records cannot be null");

        List<StudentAbsenceStats> result = new ArrayList<>();

        for (Student student : students) {
            int totalMarked = 0;
            int absentCount = 0;
            int lateCount = 0;

            for (AttendanceRecord record : records) {
                if (!student.equals(record.getStudent())) {
                    continue;
                }

                totalMarked++;

                if (record.getStatus() == AttendanceStatus.ABSENT) {
                    absentCount++;
                } else if (record.getStatus() == AttendanceStatus.LATE) {
                    lateCount++;
                }
            }

            result.add(new StudentAbsenceStats(student, totalMarked, absentCount, lateCount));
        }

        result.sort(Comparator
                .comparing(StudentAbsenceStats::getAbsenceRate).reversed()
                .thenComparing(stats -> stats.getStudent().getFullName()));

        return result;
    }

    public static final class StudentAbsenceStats {
        private final Student student;
        private final int totalMarked;
        private final int absentCount;
        private final int lateCount;

        public StudentAbsenceStats(Student student, int totalMarked, int absentCount, int lateCount) {
            this.student = Objects.requireNonNull(student, "student cannot be null");
            this.totalMarked = totalMarked;
            this.absentCount = absentCount;
            this.lateCount = lateCount;
        }

        public Student getStudent() {
            return student;
        }

        public int getTotalMarked() {
            return totalMarked;
        }

        public int getAbsentCount() {
            return absentCount;
        }

        public int getLateCount() {
            return lateCount;
        }

        public double getAbsenceRate() {
            if (totalMarked == 0) {
                return 0.0;
            }
            return (absentCount * 100.0) / totalMarked;
        }

        public String getAbsenceRateText() {
            return String.format(Locale.US, "%.1f%%", getAbsenceRate());
        }
    }
}