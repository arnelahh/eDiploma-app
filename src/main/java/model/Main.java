package model;

import dao.AppUserDAO;
import dao.SubjectDAO;

import java.util.List;

public class Main {
    static void main(String[] args) {
        SubjectDAO subjectDao = new SubjectDAO();

        List<Subject> predmeti = subjectDao.getAllSubjects();
        for (Subject subject : predmeti) {
            System.out.println(subject);
        }

        AppUserDAO dao = new AppUserDAO();
        var users = dao.getAllUsers();

        users.forEach(System.out::println);
    }

}
