// package co.edu.uptc;

// import co.edu.uptc.controller.RouteController;

// public class App {
//     public static void main(String[] args) {
//         RouteController controller = new RouteController();
//         controller.start();
//     }
// }

package co.edu.uptc;

import co.edu.uptc.viewController.RouteViewFX;
import javafx.application.Application;

public class App {
    public static void main(String[] args) {
        // Lanzamos la aplicación JavaFX
        Application.launch(RouteViewFX.class, args);
    }
}
