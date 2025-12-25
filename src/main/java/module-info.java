module main.ediploma {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires java.sql;
    requires io.github.cdimascio.dotenv.java;
    requires static lombok;
    requires jbcrypt;
    requires java.desktop;
    requires com.zaxxer.hikari;
    requires openhtmltopdf.pdfbox;


    opens main.ediploma to javafx.fxml;
    opens app to javafx.graphics, javafx.fxml;
    opens controller to javafx.fxml;
    exports main.ediploma;
}