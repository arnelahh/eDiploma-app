package model;

import dao.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class Main {
    static void main(String[] args) throws SQLException {
        SubjectDAO subjectDao = new SubjectDAO();

        List<Subject> predmeti = subjectDao.getAllSubjects();
        for (Subject subject : predmeti) {
            System.out.println(subject);
        }
        System.out.println("--------------------------");
        AppUserDAO dao = new AppUserDAO();
        var users = dao.getAllUsers();
        users.forEach(System.out::println);

        System.out.println("--------------------------");
        AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();
        List<AcademicStaff> staff = academicStaffDAO.getAllAcademicStaff();
        for(AcademicStaff as : staff){
            System.out.println(as);
        }

        System.out.println("--------------------------");
        CommissionRoleDAO commissionRoleDAO = new CommissionRoleDAO();
        List<CommissionRole> roles = commissionRoleDAO.getCommissionRoles();
        for(CommissionRole role : roles){
            System.out.println(role);
        }

        System.out.println("--------------------------");
        //dodavanje predmeta
        Subject s = new Subject();
        s.setId((predmeti.getLast().getId())+1);
        s.setName("DSA");
        subjectDao.AddSubject(s);
        predmeti = subjectDao.getAllSubjects();
        for (Subject subject : predmeti) {
            System.out.println(subject);
        }
        System.out.println("--------------------------");
        //brisanje predmeta
        subjectDao.DeleteSubject(s.getId());
        predmeti = subjectDao.getAllSubjects();
        for (Subject subject : predmeti) {
            System.out.println(subject);
        }

    }



}
