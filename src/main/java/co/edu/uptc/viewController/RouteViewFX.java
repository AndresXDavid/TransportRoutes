package co.edu.uptc.viewController;

import java.util.List;

import co.edu.uptc.controller.RouteController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class RouteViewFX extends Application {

    private TreeView<String> treeView;
    private TextArea output;
    private RouteController controller; // 👈 referencia al controlador

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Sistema de Rutas");

        // Instanciar el controlador y pasarle la vista
        controller = new RouteController(this);

        // Árbol donde se mostrarán las rutas
        treeView = new TreeView<>();
        treeView.setPrefWidth(300);

        // Área de mensajes (como consola dentro de la app)
        output = new TextArea();
        output.setEditable(false);

        // Botones
        Button addButton = new Button("Agregar ruta");
        Button showButton = new Button("Mostrar rutas");
        Button searchButton = new Button("Buscar ruta corta");
        Button exitButton = new Button("Salir");

        // Acción: agregar ruta
        addButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Agregar ruta");
            dialog.setContentText("Formato: código, ubicación, padre");
            dialog.showAndWait().ifPresent(data -> {
                String[] parts = data.split(",");
                if (parts.length == 3) {
                    controller.addRoute(
                        parts[0].trim(),
                        parts[1].trim(),
                        parts[2].trim()
                    );
                } else {
                    showMessage("Formato inválido. Ejemplo: tunja1, Tunja, paipa1");
                }
            });
        });

        // Acción: mostrar jerarquía
        showButton.setOnAction(e -> controller.updateHierarchy());

        // Acción: buscar ruta más corta
        searchButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText("Buscar ruta más corta");
            dialog.setContentText("Formato: origen, destino");
            dialog.showAndWait().ifPresent(data -> {
                String[] parts = data.split(",");
                if (parts.length == 2) {
                    controller.searchShortestRoute(parts[0].trim(), parts[1].trim());
                } else {
                    showMessage("Formato inválido. Ejemplo: Duitama, Tunja");
                }
            });
        });

        // Acción: salir
        exitButton.setOnAction(e -> primaryStage.close());

        ToolBar toolBar = new ToolBar(addButton, showButton, searchButton, exitButton);

        BorderPane root = new BorderPane();
        root.setTop(toolBar);
        root.setCenter(treeView);
        root.setBottom(output);

        Scene scene = new Scene(root, 700, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Mostrar jerarquía en el TreeView
    public void showHierarchy(List<String> lines) {
        TreeItem<String> rootItem = new TreeItem<>("Rutas");
        rootItem.setExpanded(true);
        for (String line : lines) {
            rootItem.getChildren().add(new TreeItem<>(line));
        }
        treeView.setRoot(rootItem);
    }

    // Mostrar mensajes en el área inferior
    public void showMessage(String message) {
        output.appendText(message + "\n");
    }

    // Mostrar rutas específicas (ej: ruta más corta)
    public void showRoutes(List<?> stations) {
        output.appendText("Ruta encontrada:\n");
        for (Object station : stations) {
            output.appendText(" -> " + station.toString() + "\n");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
