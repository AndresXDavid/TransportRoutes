module co.edu.uptc {
    requires javafx.controls;
    requires javafx.fxml;
    requires jakarta.xml.bind;

    opens co.edu.uptc to javafx.fxml;
    opens co.edu.uptc.logic to jakarta.xml.bind;
    opens co.edu.uptc.persistance to jakarta.xml.bind;

    exports co.edu.uptc;
    exports co.edu.uptc.logic;
    exports co.edu.uptc.persistance;
}
