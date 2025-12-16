package app;

import dao.*;
import dto.ThesisDTO;
import model.Student;

import java.sql.SQLException;
import java.util.List;

public class Main {
    static void main(String[] args) throws SQLException {
//        SubjectDAO subjectDao = new SubjectDAO();
//
//        List<Subject> predmeti = subjectDao.getAllSubjects();
//        for (Subject subject : predmeti) {
//            System.out.println(subject);
//        }
//        System.out.println("--------------------------");
//        AppUserDAO dao = new AppUserDAO();
//        var users = dao.getAllUsers();
//        users.forEach(System.out::println);
//
//        System.out.println("--------------------------");
//        AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();
//        List<AcademicStaff> staff = academicStaffDAO.getAllAcademicStaff();
//        for(AcademicStaff as : staff){
//            System.out.println(as);
//        }
//
//        System.out.println("--------------------------");
//        CommissionRoleDAO commissionRoleDAO = new CommissionRoleDAO();
//        List<CommissionRole> roles = commissionRoleDAO.getCommissionRoles();
//        for(CommissionRole role : roles){
//            System.out.println(role);
//        }
//
//        System.out.println("--------------------------");
//        //dodavanje predmeta
//        Subject s = new Subject();
//        s.setId((predmeti.getLast().getId())+1);
//        s.setName("DSA");
//        subjectDao.AddSubject(s);
//        predmeti = subjectDao.getAllSubjects();
//        for (Subject subject : predmeti) {
//            System.out.println(subject);
//        }
//        System.out.println("--------------------------");
//        //brisanje predmeta
//        subjectDao.DeleteSubject(s.getId());
//        predmeti = subjectDao.getAllSubjects();
//        for (Subject subject : predmeti) {
//            System.out.println(subject);
//        }
//
//        System.out.println("--------------------------");
//        ThesisDAO thesisDAO = new ThesisDAO();
//        List<ThesisDTO> lista=thesisDAO.getAllThesisBySearch("amar");
//        for(ThesisDTO thesisDTO:lista){
//            System.out.println(thesisDTO);
//        }
//
//        Thesis thesis = Thesis.builder()
//                .title("My Thesis Title")
//                .applicationDate(LocalDate.now())
//                .departmentId(1)
//                .studentId(1)
//                .academicStaffId(1)  // mentor
//                .secretaryId(1)
//                .subjectId(1)
//                .statusId(1)
//                .build();
//
//        ThesisDAO thesisDao = new ThesisDAO();
//        thesisDao.insertThesis(thesis);


//        StudentStatusDAO studentStatusDAO = new StudentStatusDAO();
//        System.out.println(studentStatusDAO.getStatusById(1));
//
//        System.out.println("--------------------------");
//        StudentDAO studentDAO = new StudentDAO();
//        List<Student> students = studentDAO.getAllStudents();
//        for(Student student : students){
//            System.out.println(student);
//        }
        System.out.println("--------------------------");
        ThesisDAO thesisDAO = new ThesisDAO();
        List<ThesisDTO> lista=thesisDAO.getAllThesis();
        for(ThesisDTO thesisDTO:lista){
            System.out.println(thesisDTO);
        }

    }



}
