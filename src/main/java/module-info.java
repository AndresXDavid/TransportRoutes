module co.edu.uptc {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires jakarta.xml.bind;

    opens co.edu.uptc to javafx.fxml;
    opens co.edu.uptc.controller to jakarta.xml.bind;
    opens co.edu.uptc.persistence to jakarta.xml.bind;

    // Abre el paquete model a todo el runtime (más simple)
    opens co.edu.uptc.model;

    exports co.edu.uptc;
    exports co.edu.uptc.controller;
    exports co.edu.uptc.persistence;
    exports co.edu.uptc.model;
    exports co.edu.uptc.viewController;
}
