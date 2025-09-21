package co.edu.uptc.viewController;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringJoiner;

import co.edu.uptc.controller.RouteController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

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
    private Button editButton;
    private Button deleteButton;

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
        editButton = new Button();
        deleteButton = new Button();
        exitButton = new Button();

        updateTexts(); // Inicializar textos según idioma

        // Acción: agregar ruta
        addButton.setOnAction(e -> {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle(bundle.getString("dialog.add.header"));
            dialog.setHeaderText(bundle.getString("dialog.add.header"));

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            // Crear tres cuadros de texto
            TextField codeField = new TextField();
            codeField.setPromptText(bundle.getString("dialog.add.code"));

            TextField nameField = new TextField();
            nameField.setPromptText(bundle.getString("dialog.add.location"));

            TextField routeCodeField = new TextField();
            routeCodeField.setPromptText(bundle.getString("dialog.add.parent"));

            // Layout
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label(bundle.getString("dialog.add.code")), 0, 0);
            grid.add(codeField, 1, 0);
            grid.add(new Label(bundle.getString("dialog.add.location")), 0, 1);
            grid.add(nameField, 1, 1);
            grid.add(new Label(bundle.getString("dialog.add.parent")), 0, 2);
            grid.add(routeCodeField, 1, 2);

            dialog.getDialogPane().setContent(grid);

            // Procesar resultado
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new String[]{
                        codeField.getText(),
                        nameField.getText(),
                        routeCodeField.getText()
                    };
                }
                return null;
            });

            dialog.showAndWait().ifPresent(data -> {
                if (data[0] != null && !data[0].isEmpty()
                        && data[1] != null && !data[1].isEmpty()
                        && data[2] != null && !data[2].isEmpty()) {
                    controller.addRoute(data[0].trim(), data[1].trim(), data[2].trim());
                } else {
                    showError("error.format.add");
                }
            });
        });

        // Acción: mostrar jerarquía
        showButton.setOnAction(e -> controller.updateHierarchy());

        // Acción: buscar ruta más corta
        searchButton.setOnAction(e -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle(bundle.getString("dialog.search.header"));

            // Botones OK / Cancel
            ButtonType searchButtonType = new ButtonType(bundle.getString("dialog.button.search"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(bundle.getString("dialog.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, cancelButtonType);

            // Contenido: dos campos
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);

            TextField originField = new TextField();
            originField.setPromptText(bundle.getString("dialog.search.origin"));
            TextField destField = new TextField();
            destField.setPromptText(bundle.getString("dialog.search.destination"));

            grid.add(new Label(bundle.getString("dialog.search.origin")), 0, 0);
            grid.add(originField, 1, 0);
            grid.add(new Label(bundle.getString("dialog.search.destination")), 0, 1);
            grid.add(destField, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // Convertir resultado
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == searchButtonType) {
                    return new Pair<>(originField.getText().trim(), destField.getText().trim());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                controller.searchShortestRoute(result.getKey(), result.getValue());
            });
        });

        // Acción: editar ruta
        editButton.setOnAction(e -> {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle(bundle.getString("dialog.edit.header"));
            dialog.setHeaderText(bundle.getString("dialog.edit.header"));

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            // Crear los tres cuadros de texto
            TextField currentCodeField = new TextField();
            currentCodeField.setPromptText(bundle.getString("dialog.edit.currentCode"));

            TextField newNameField = new TextField();
            newNameField.setPromptText(bundle.getString("dialog.edit.newLocation"));

            TextField newCodeField = new TextField();
            newCodeField.setPromptText(bundle.getString("dialog.edit.newCode"));

            // Layout para organizarlos
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.add(new Label(bundle.getString("dialog.edit.currentCode")), 0, 0);
            grid.add(currentCodeField, 1, 0);
            grid.add(new Label(bundle.getString("dialog.edit.newLocation")), 0, 1);
            grid.add(newNameField, 1, 1);
            grid.add(new Label(bundle.getString("dialog.edit.newCode")), 0, 2);
            grid.add(newCodeField, 1, 2);

            dialog.getDialogPane().setContent(grid);

            // Procesar resultado
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new String[]{
                        currentCodeField.getText(),
                        newNameField.getText(),
                        newCodeField.getText()
                    };
                }
                return null;
            });

            dialog.showAndWait().ifPresent(data -> {
                if (data[0] != null && !data[0].isEmpty()
                        && data[1] != null && !data[1].isEmpty()
                        && data[2] != null && !data[2].isEmpty()) {
                    controller.editStation(data[0].trim(), data[1].trim(), data[2].trim());
                } else {
                    showError("error.format.search");
                }
            });
        });

        // Acción: eliminar ruta
        deleteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(bundle.getString("dialog.delete.header"));
            dialog.setHeaderText(bundle.getString("dialog.delete.header"));
            dialog.setContentText(bundle.getString("dialog.delete.code"));

            dialog.showAndWait().ifPresent(code -> {
                if (code != null && !code.trim().isEmpty()) {
                    controller.deleteStation(code);
                } else {
                    showError("error.format.search");
                }
            });
        });

        // Acción: salir
        exitButton.setOnAction(e -> primaryStage.close());

        ToolBar toolBar = new ToolBar(addButton, showButton, searchButton, editButton, deleteButton, exitButton);

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

        HBox topBar = new HBox(10, toolBar, new Label(bundle.getString("app.languagetitle")), languageCombo);
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
        editButton.setText(bundle.getString("button.edit"));
        deleteButton.setText(bundle.getString("button.delete"));
        exitButton.setText(bundle.getString("button.exit"));

        // Limpiar TextArea al cambiar idioma
        output.clear();
    }

    // Mostrar jerarquía en el TreeView
    public void showHierarchy(TreeItem rootItem) {
        rootItem.setExpanded(true);
        treeView.setRoot(rootItem);
    }

    // Mostrar mensaje de error en un Alert
    public void showError(String key, Object... args) {
        String message = MessageFormat.format(bundle.getString(key), args);

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("dialog.error.title"));
        alert.setHeaderText(bundle.getString("dialog.error.header"));
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Mostrar mensaje informativo
    public void showInfo(String key, Object... args) {
        String message = MessageFormat.format(bundle.getString(key), args);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("dialog.info.title"));
        alert.setHeaderText(null); // si no quieres encabezado
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Mostrar ruta encontrada
    public void showRoutes(List<?> stations) {
        if (stations == null || stations.isEmpty()) {
            output.clear(); // Borrar si no hay ruta
            return;
        }

        StringJoiner joiner = new StringJoiner(" " + bundle.getString("route.arrow") + " ");
        for (Object station : stations) {
            joiner.add(station.toString());
        }

        output.setText(joiner.toString());
    }
}