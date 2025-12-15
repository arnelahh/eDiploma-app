package controller;

import dao.ThesisDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import model.Thesis;

public class ThesisController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private VBox thesisCardsContainer;
    @FXML private Button btnAddNew;

    private ThesisDAO dao;




}
