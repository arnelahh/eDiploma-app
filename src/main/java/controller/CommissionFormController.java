package controller;

import dao.AcademicStaffDAO;
import dao.AppUserDAO;
import dao.CommissionDAO;
import dao.CommissionRoleDAO;
import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.AcademicStaff;
import model.AppUser;
import model.Commission;
import model.CommissionRole;
import model.Thesis;
import utils.SceneManager;

import java.util.List;

public class CommissionFormController {

    @FXML private Text formTitle;
    @FXML private Text formSubtitle;

    @FXML private ComboBox<AcademicStaff> chairmanComboBox;
    @FXML private ComboBox<AcademicStaff> mentorComboBox;
    @FXML private ComboBox<AcademicStaff> memberComboBox;
    @FXML private ComboBox<AppUser> secretaryComboBox;
    @FXML private ComboBox<AcademicStaff> substituteComboBox;

    @FXML private ProgressIndicator loader;

    private final AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();
    private final AppUserDAO appUserDAO = new AppUserDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private final CommissionRoleDAO commissionRoleDAO = new CommissionRoleDAO();
    private final ThesisDAO thesisDAO = new ThesisDAO();

    private int thesisId;
    private ThesisDetailsDTO thesisDetails;
    private Commission existingCommission;
    private boolean isEditMode = false;

    private List<CommissionRole> roles;

    private static final int ROLE_PRESIDENT = 1;
    private static final int ROLE_MEMBER = 2;

    @FXML
    public void initialize() {
        setupComboBoxConverters();
        loadRoles();
        loadAcademicStaff();
        loadSecretaries();
    }

    private void loadRoles() {
        try {
            roles = commissionRoleDAO.getCommissionRoles();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupComboBoxConverters() {
        javafx.util.StringConverter<AcademicStaff> staffConverter = new javafx.util.StringConverter<>() {
            @Override
            public String toString(AcademicStaff staff) {
                if (staff == null) return "";
                String title = staff.getTitle() != null ? staff.getTitle() + " " : "";
                return title + staff.getFirstName() + " " + staff.getLastName();
            }

            @Override
            public AcademicStaff fromString(String s) {
                return null;
            }
        };

        chairmanComboBox.setConverter(staffConverter);
        mentorComboBox.setConverter(staffConverter);
        memberComboBox.setConverter(staffConverter);
        substituteComboBox.setConverter(staffConverter);

        secretaryComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(AppUser user) {
                if (user == null) return "";
                return user.getUsername() + " (" + user.getEmail() + ")";
            }

            @Override
            public AppUser fromString(String s) {
                return null;
            }
        });
    }

    private void loadAcademicStaff() {
        Task<List<AcademicStaff>> task = new Task<>() {
            @Override
            protected List<AcademicStaff> call() throws Exception {
                return academicStaffDAO.getAllActiveAcademicStaff();
            }
        };

        if (loader != null) {
            loader.visibleProperty().bind(task.runningProperty());
        }

        task.setOnSucceeded(e -> {
            List<AcademicStaff> staffList = task.getValue();

            chairmanComboBox.getItems().clear();
            mentorComboBox.getItems().clear();
            memberComboBox.getItems().clear();
            substituteComboBox.getItems().clear();

            chairmanComboBox.getItems().addAll(staffList);
            mentorComboBox.getItems().addAll(staffList);
            memberComboBox.getItems().addAll(staffList);
            substituteComboBox.getItems().addAll(staffList);

            if (isEditMode && existingCommission != null) {
                fillExistingCommission();
            } else if (thesisDetails != null && thesisDetails.getMentor() != null) {
                // Auto-select mentor from thesis
                mentorComboBox.getItems().stream()
                        .filter(s -> s.getId() == thesisDetails.getMentor().getId())
                        .findFirst()
                        .ifPresent(mentorComboBox::setValue);
            }
        });

        task.setOnFailed(e -> {
            showError("Greška pri učitavanju osoblja: " + task.getException().getMessage());
        });

        new Thread(task, "load-academic-staff").start();
    }

    private void loadSecretaries() {
        Task<List<AppUser>> task = new Task<>() {
            @Override
            protected List<AppUser> call() throws Exception {
                return appUserDAO.getAllAppUsers();
            }
        };

        task.setOnSucceeded(e -> {
            List<AppUser> users = task.getValue();

            secretaryComboBox.getItems().clear();
            secretaryComboBox.getItems().addAll(users);

            if (thesisDetails != null && thesisDetails.getSecretary() != null) {
                // Auto-select secretary from thesis
                secretaryComboBox.getItems().stream()
                        .filter(u -> u.getId() == thesisDetails.getSecretary().getId())
                        .findFirst()
                        .ifPresent(secretaryComboBox::setValue);
            }
        });

        new Thread(task, "load-secretaries").start();
    }

    public void initWithThesis(int thesisId, ThesisDetailsDTO details) {
        this.thesisId = thesisId;
        this.thesisDetails = details;
        this.isEditMode = false;

        if (formTitle != null) {
            formTitle.setText("Dodavanje komisije");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Unesite komisiju za završni rad");
        }
    }

    public void initEditCommission(int thesisId, ThesisDetailsDTO details, Commission commission) {
        this.thesisId = thesisId;
        this.thesisDetails = details;
        this.existingCommission = commission;
        this.isEditMode = true;

        if (formTitle != null) {
            formTitle.setText("Uredi komisiju");
        }
        if (formSubtitle != null) {
            formSubtitle.setText("Uredite komisiju za završni rad (uključujući mentora i sekretara)");
        }
    }

    private void fillExistingCommission() {
        if (existingCommission == null) return;


        // Chairman
        if (existingCommission.getMember1() != null) {
            chairmanComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.getMember1().getId())
                    .findFirst()
                    .ifPresent(chairmanComboBox::setValue);
        }

        // Member
        if (existingCommission.getMember2() != null) {
            memberComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.getMember2().getId())
                    .findFirst()
                    .ifPresent(memberComboBox::setValue);
        }

        // Substitute
        if (existingCommission.getMember3() != null) {
            substituteComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.getMember3().getId())
                    .findFirst()
                    .ifPresent(substituteComboBox::setValue);
        }

        //Mentor
        if (thesisDetails != null && thesisDetails.getMentor() != null) {
            mentorComboBox.getItems().stream()
                    .filter(s -> s.getId() == thesisDetails.getMentor().getId())
                    .findFirst()
                    .ifPresent(mentorComboBox::setValue);
        }

        // Secretary (iz Thesis)
        if (thesisDetails != null && thesisDetails.getSecretary() != null) {
            secretaryComboBox.getItems().stream()
                    .filter(u -> u.getId() == thesisDetails.getSecretary().getId())
                    .findFirst()
                    .ifPresent(secretaryComboBox::setValue);
        }
    }

    @FXML
    private void handleSave() {
        // Validation
        if (chairmanComboBox.getValue() == null) {
            showWarning("Morate izabrati predsjednika komisije!");
            return;
        }
        if (mentorComboBox.getValue() == null) {
            showWarning("Morate izabrati mentora!");
            return;
        }
        if (memberComboBox.getValue() == null) {
            showWarning("Morate izabrati člana komisije!");
            return;
        }
        if (secretaryComboBox.getValue() == null) {
            showWarning("Morate izabrati sekretara!");
            return;
        }

        Commission commission = new Commission();
        commission.setThesisId(thesisId);

        // Member 1 - Chairman
        AcademicStaff chairman = chairmanComboBox.getValue();
        commission.setMember1(chairman);
        commission.setMember1Role(getRoleById(ROLE_PRESIDENT));

        // Member 2 - Member
        AcademicStaff member = memberComboBox.getValue();
        commission.setMember2(member);
        commission.setMember2Role(getRoleById(ROLE_MEMBER));

        // Member 3 - Substitute (optional)
        if (substituteComboBox.getValue() != null) {
            AcademicStaff substitute = substituteComboBox.getValue();
            commission.setMember3(substitute);
            commission.setMember3Role(getRoleById(ROLE_MEMBER));
        }

        try {
            // Save commission
            if (isEditMode) {
                commissionDAO.updateCommission(commission);
            } else {
                commissionDAO.insertCommission(commission);
            }
            // Update Mentor and Secretary in Thesis table
            Thesis thesis = thesisDAO.getThesisById(thesisId);
            if (thesis != null) {
                thesis.setAcademicStaffId(mentorComboBox.getValue().getId());
                thesis.setSecretaryId(secretaryComboBox.getValue().getId());
                thesisDAO.updateThesis(thesis);
            }

            showInfo(isEditMode ? "Komisija je uspješno ažurirana!" : "Komisija je uspješno kreirana!");

            // Return to details
            SceneManager.showWithData(
                    "/app/thesisDetails.fxml",
                    "Detalji završnog rada",
                    (ThesisDetailsController controller) -> {
                        controller.initWithThesisId(thesisId);
                    }
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private CommissionRole getRoleById(int id) {
        if (roles == null) return null;
        return roles.stream()
                .filter(r -> r.getId() == id)
                .findFirst()
                .orElse(null);
    }

    @FXML
    private void handleCancel() {
        SceneManager.showWithData(
                "/app/thesisDetails.fxml",
                "Detalji završnog rada",
                (ThesisDetailsController controller) -> {
                    controller.initWithThesisId(thesisId);
                }
        );
    }

    private void showError(String message) {
        javafx.application.Platform.runLater(() -> {
            new Alert(Alert.AlertType.ERROR, message).showAndWait();
        });
    }

    private void showWarning(String message) {
        javafx.application.Platform.runLater(() -> {
            new Alert(Alert.AlertType.WARNING, message).showAndWait();
        });
    }

    private void showInfo(String message) {
        javafx.application.Platform.runLater(() -> {
            new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
        });
    }
}