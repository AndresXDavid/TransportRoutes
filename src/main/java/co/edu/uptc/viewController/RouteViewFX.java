package co.edu.uptc.viewController;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import co.edu.uptc.controller.RouteController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class RouteViewFX extends Application {

    private TreeView<String> treeView;
    private TextArea output;
    private RouteController controller;
    private ResourceBundle bundle;

    // Botones para actualizarlos cuando cambie el idioma
    private Button addButton;
    private Button showButton;
    private Button searchButton;
    private Button exitButton;

    private ComboBox<String> languageCombo;

    @Override
    public void start(Stage primaryStage) {
        // Idioma inicial
        setLanguage(new Locale("es"));

        primaryStage.setTitle(bundle.getString("app.title"));

        // Instanciar el controlador
        controller = new RouteController(this);

        // Árbol de rutas
        treeView = new TreeView<>();
        treeView.setPrefWidth(300);

        // Consola de mensajes
        output = new TextArea();
        output.setEditable(false);

        // Botones
        addButton = new Button();
        showButton = new Button();
        searchButton = new Button();
        exitButton = new Button();

        updateTexts(); // Inicializar textos según idioma

        // Acción: agregar ruta
        addButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText(bundle.getString("dialog.add.header"));
            dialog.setContentText(bundle.getString("dialog.add.content"));
            dialog.showAndWait().ifPresent(data -> {
                String[] parts = data.split(",");
                if (parts.length == 3) {
                    controller.addRoute(parts[0].trim(), parts[1].trim(), parts[2].trim());
                } else {
                    showMessage(bundle.getString("error.format.add"));
                }
            });
        });

        // Acción: mostrar jerarquía
        showButton.setOnAction(e -> controller.updateHierarchy());

        // Acción: buscar ruta más corta
        searchButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setHeaderText(bundle.getString("dialog.search.header"));
            dialog.setContentText(bundle.getString("dialog.search.content"));
            dialog.showAndWait().ifPresent(data -> {
                String[] parts = data.split(",");
                if (parts.length == 2) {
                    controller.searchShortestRoute(parts[0].trim(), parts[1].trim());
                } else {
                    showMessage(bundle.getString("error.format.search"));
                }
            });
        });

        // Acción: salir
        exitButton.setOnAction(e -> primaryStage.close());

        ToolBar toolBar = new ToolBar(addButton, showButton, searchButton, exitButton);

        // ComboBox de idiomas
        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Español", "English");
        languageCombo.setValue("Español"); // por defecto
        languageCombo.setOnAction(e -> {
            if (languageCombo.getValue().equals("English")) {
                setLanguage(Locale.ENGLISH);
            } else {
                setLanguage(new Locale("es"));
            }
            primaryStage.setTitle(bundle.getString("app.title"));
            updateTexts();
        });

        HBox topBar = new HBox(10, toolBar, new Label("Idioma:"), languageCombo);
        topBar.setPadding(new Insets(5));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(treeView);
        root.setBottom(output);

        Scene scene = new Scene(root, 750, 480);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    // Cambiar idioma
    private void setLanguage(Locale locale) {
        bundle = ResourceBundle.getBundle("co.edu.uptc.i18n.messages", locale);
    }

    // Actualizar textos dinámicamente
    private void updateTexts() {
        addButton.setText(bundle.getString("button.add"));
        showButton.setText(bundle.getString("button.show"));
        searchButton.setText(bundle.getString("button.search"));
        exitButton.setText(bundle.getString("button.exit"));
    }

    // Mostrar jerarquía en el TreeView
    public void showHierarchy(TreeItem rootItem) {
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
    }

    // Mostrar mensajes
    public void showMessage(String message) {
        output.appendText(message + "\n");
    }

    // Mostrar rutas específicas
    public void showRoutes(List<?> stations) {
        output.appendText(bundle.getString("route.found") + "\n");
        for (Object station : stations) {
            output.appendText(" -> " + station.toString());
        }
        output.appendText("\n");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
