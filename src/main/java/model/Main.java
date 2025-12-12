package model;

import dao.AcademicStaffDAO;
import dao.AppUserDAO;
import dao.CommissionRoleDAO;
import dao.SubjectDAO;

import java.util.List;

public class Main {
    static void main(String[] args) {
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
    }

}
