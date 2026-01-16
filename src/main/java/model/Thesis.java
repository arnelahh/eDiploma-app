package model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Thesis {
    private int id;
    private int studentId;
    private int academicStaffId;
    private int departmentId;
    private int secretaryId; // Ovo je sada ID iz AcademicStaff tabele (preko AppUser.AcademicStaffId)
    private int subjectId;
    private int statusId;

    private String title;
    private LocalDate applicationDate;
    private LocalDate approvalDate; // Datum sjednice komisije (commission meeting date)
    private LocalDate defenseDate;
    private LocalDate finalThesisApprovalDate; // Mapira se na FinalThesisApprovalDate u bazi
    private LocalDate commisionDate; // Datum komisije - mapira se na CommisionDate u bazi
    private LocalDate noticeDate; // NOVO: Datum rješenja obavijesti - mapira se na NoticeDate u bazi
    private LocalDate writtenReportDate; // NOVO: Datum zapisnika sa pismenog dijela - mapira se na WrittenReportDate u bazi
    private LocalDate defenseReportDate; // NOVO: Datum zapisnika sa odbrane - mapira se na DefenseReportDate u bazi
    private String commisionTime; // NOVO: Vrijeme sjednice komisije - mapira se na CommisionTime u bazi
    private Integer grade;
    private boolean isActive;
    private boolean passedSubjects; // Da li je student položio sve ispite - mapira se na PassedSubjects u bazi
    private Department department;
    private Student student;
    private Subject subject;
    private ThesisStatus status;
    private AcademicStaff secretary; // PROMJENA: Sada je AcademicStaff umjesto AppUser
    private AcademicStaff mentor;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String description;
    private String literature;
    private String structure;
}
