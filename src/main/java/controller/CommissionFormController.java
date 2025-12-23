package controller;

import dao.AcademicStaffDAO;
import dao.AppUserDAO;
import dao.CommissionDAO;
import dao.CommissionRoleDAO;
import dao.ThesisDAO;
import dto.ThesisDetailsDTO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import model.AcademicStaff;
import model.Commission;
import model.CommissionRole;
import model.Thesis;
import utils.AsyncHelper;
import utils.CommissionValidator;
import utils.SceneManager;
import utils.GlobalErrorHandler;
import utils.ValidationResult;

import java.util.List;

public class CommissionFormController {

    @FXML private Text formTitle;
    @FXML private Text formSubtitle;

    @FXML private ComboBox<AcademicStaff> chairmanComboBox;
    @FXML private ComboBox<AcademicStaff> mentorComboBox;
    @FXML private ComboBox<AcademicStaff> memberComboBox;
    @FXML private ComboBox<AcademicStaff> secretaryComboBox;
    @FXML private ComboBox<AcademicStaff> substituteComboBox;

    @FXML private ProgressIndicator loader;

    private final AcademicStaffDAO academicStaffDAO = new AcademicStaffDAO();
    private final AppUserDAO appUserDAO = new AppUserDAO();
    private final CommissionDAO commissionDAO = new CommissionDAO();
    private final CommissionRoleDAO commissionRoleDAO = new CommissionRoleDAO();
    private final ThesisDAO thesisDAO = new ThesisDAO();
    private final CommissionValidator validator = new CommissionValidator();

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
            GlobalErrorHandler.error("Greška pri učitavanju uloga komisije.", e);
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
        secretaryComboBox.setConverter(staffConverter);
    }

    private void loadAcademicStaff() {
        AsyncHelper.executeAsyncWithLoader(
            () -> academicStaffDAO.getAllActiveAcademicStaff(),
            staffList -> {
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
                    mentorComboBox.getItems().stream()
                            .filter(s -> s.getId() == thesisDetails.getMentor().getId())
                            .findFirst()
                            .ifPresent(mentorComboBox::setValue);
                }
            },
            error -> GlobalErrorHandler.error("Greška pri učitavanju osoblja.", error),
            loader
        );
    }

    private void loadSecretaries() {
        AsyncHelper.executeAsync(
            () -> appUserDAO.getAllSecretariesAsStaff(),
            secretaries -> {
                secretaryComboBox.getItems().clear();
                secretaryComboBox.getItems().addAll(secretaries);

                if (thesisDetails != null && thesisDetails.getSecretary() != null) {
                    secretaryComboBox.getItems().stream()
                            .filter(s -> s.getId() == thesisDetails.getSecretary().getId())
                            .findFirst()
                            .ifPresent(secretaryComboBox::setValue);
                }
            },
            error -> GlobalErrorHandler.error("Greška pri učitavanju sekretara.", error)
        );
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

        if (existingCommission.getMember1() != null) {
            chairmanComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.getMember1().getId())
                    .findFirst()
                    .ifPresent(chairmanComboBox::setValue);
        }

        if (existingCommission.getMember2() != null) {
            memberComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.getMember2().getId())
                    .findFirst()
                    .ifPresent(memberComboBox::setValue);
        }

        if (existingCommission.getMember3() != null) {
            substituteComboBox.getItems().stream()
                    .filter(s -> s.getId() == existingCommission.getMember3().getId())
                    .findFirst()
                    .ifPresent(substituteComboBox::setValue);
        }

        if (thesisDetails != null && thesisDetails.getMentor() != null) {
            mentorComboBox.getItems().stream()
                    .filter(s -> s.getId() == thesisDetails.getMentor().getId())
                    .findFirst()
                    .ifPresent(mentorComboBox::setValue);
        }

        if (thesisDetails != null && thesisDetails.getSecretary() != null) {
            secretaryComboBox.getItems().stream()
                    .filter(s -> s.getId() == thesisDetails.getSecretary().getId())
                    .findFirst()
                    .ifPresent(secretaryComboBox::setValue);
        }
    }

    @FXML
    private void handleSave() {
        // Validacija komisije - provjera da isti član nije na više uloga
        ValidationResult validationResult = validator.validateCommission(
            chairmanComboBox.getValue(),
            mentorComboBox.getValue(),
            memberComboBox.getValue(),
            substituteComboBox.getValue(),
            secretaryComboBox.getValue()
        );

        if (!validationResult.isValid()) {
            showErrorList(validationResult.getErrors());
            return;
        }

        Commission commission = new Commission();
        commission.setThesisId(thesisId);

        AcademicStaff chairman = chairmanComboBox.getValue();
        commission.setMember1(chairman);
        commission.setMember1Role(getRoleById(ROLE_PRESIDENT));

        AcademicStaff member = memberComboBox.getValue();
        commission.setMember2(member);
        commission.setMember2Role(getRoleById(ROLE_MEMBER));

        if (substituteComboBox.getValue() != null) {
            AcademicStaff substitute = substituteComboBox.getValue();
            commission.setMember3(substitute);
            commission.setMember3Role(getRoleById(ROLE_MEMBER));
        }

        try {
            if (isEditMode) {
                commissionDAO.updateCommission(commission);
            } else {
                commissionDAO.insertCommission(commission);
            }

            // PROMJENA: Dobijamo AppUser ID od odabranog sekretara (AcademicStaff)
            AcademicStaff selectedSecretary = secretaryComboBox.getValue();
            int secretaryAppUserId = appUserDAO.getAppUserIdByAcademicStaffId(selectedSecretary.getId());

            // Ažuriramo Thesis sa novim mentorom i sekretarom
            Thesis thesis = thesisDAO.getThesisById(thesisId);
            if (thesis != null) {
                thesis.setAcademicStaffId(mentorComboBox.getValue().getId());
                thesis.setSecretaryId(secretaryAppUserId);
                thesisDAO.updateThesis(thesis);
            }

            GlobalErrorHandler.info(isEditMode ? "Komisija je uspješno ažurirana!" : "Komisija je uspješno kreirana!");

            SceneManager.showWithData(
                    "/app/thesisDetails.fxml",
                    "Detalji završnog rada",
                    (ThesisDetailsController controller) -> {
                        controller.initWithThesisId(thesisId);
                    }
            );
        } catch (Exception e) {
            GlobalErrorHandler.error("Greška pri čuvanju.", e);
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

    private void showErrorList(List<String> errors) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Neispravan unos");
        alert.setHeaderText("Molimo ispravite sljedeće greške:");
        alert.setContentText("• " + String.join("\n• ", errors));
        alert.showAndWait();
    }
}
