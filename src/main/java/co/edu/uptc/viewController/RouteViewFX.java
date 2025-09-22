package co.edu.uptc.viewController;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringJoiner;

import co.edu.uptc.controller.RouteController;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Pair;

/**
 * Vista JavaFX para el sistema de rutas.
 * - Usa recursos (style.css) y soporta logos.
 * - Robusta contra NPE: crea controles antes de actualizar textos.
 */
public class RouteViewFX extends Application {

    private TreeView<String> treeView;
    private TextArea output;
    private RouteController controller;
    private ResourceBundle bundle;

    // Botones
    private Button addButton;
    private Button showButton;
    private Button searchButton;
    private Button exitButton;
    private Button editButton;
    private Button deleteButton;

    private ComboBox<String> languageCombo;
    private Label statusLabel;

    @Override
    public void start(Stage primaryStage) {
        // idioma inicial
        setLanguage(new Locale("es"));

        // Crear controles básicos primero (evita NPE al actualizar textos)
        treeView = new TreeView<>();
        treeView.setPrefWidth(320);

        output = new TextArea();
        output.setEditable(false);
        output.setWrapText(true);

        statusLabel = new Label(bundle.containsKey("status.ready") ? bundle.getString("status.ready") : "Listo");
        statusLabel.getStyleClass().add("status");
        statusLabel.setPadding(new Insets(6));

        // Botones
        addButton = new Button();
        showButton = new Button();
        searchButton = new Button();
        editButton = new Button();
        deleteButton = new Button();
        exitButton = new Button();

        // Combo idiomas
        languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll("Español", "English");
        languageCombo.setValue(bundle.getLocale().getLanguage().equals("en") ? "English" : "Español");

        // Actualizar textos de botones/etiquetas
        updateTexts();

        // Configurar acciones (usa métodos separados)
        configureAddAction();
        // showButton puede servir como 'Refrescar' (pero por omisión la jerarquía se carga automáticamente)
        showButton.setOnAction(e -> {
            if (controller != null) controller.updateHierarchy();
            statusLabel.setText(bundle.containsKey("status.hierarchyShown") ? bundle.getString("status.hierarchyShown") : "Jerarquía actualizada");
        });
        configureSearchAction();
        configureEditAction();
        configureDeleteAction();
        exitButton.setOnAction(e -> primaryStage.close());

        // Language change handling
        languageCombo.setOnAction(e -> {
            if (languageCombo.getValue().equals("English")) {
                setLanguage(Locale.ENGLISH);
            } else {
                setLanguage(new Locale("es"));
            }
            primaryStage.setTitle(bundle.getString("app.title"));
            updateTexts();
            statusLabel.setText(bundle.containsKey("status.ready") ? bundle.getString("status.ready") : "Listo");
        });

        // Construir top bar (logos + toolbar + language control)
        HBox topBar = buildTopBar();

        // Center: SplitPane con árbol (izq) y panel de resultados (der)
        SplitPane centerSplit = new SplitPane();
        centerSplit.setDividerPositions(0.35);

        TitledPane leftPane = new TitledPane(bundle.containsKey("tab.hierarchy") ? bundle.getString("tab.hierarchy") : "Jerarquía", treeView);
        leftPane.setCollapsible(false);

        TabPane rightTabs = new TabPane();
        Tab tabResults = new Tab(bundle.containsKey("tab.results") ? bundle.getString("tab.results") : "Resultados", output);
        tabResults.setClosable(false);
        rightTabs.getTabs().add(tabResults);

        centerSplit.getItems().addAll(leftPane, rightTabs);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(centerSplit);
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 980, 620);

        // Cargar stylesheet (ruta en resources)
        try {
            scene.getStylesheets().add(getClass().getResource("/co/edu/uptc/viewController/style.css").toExternalForm());
        } catch (Exception ignored) {}

        primaryStage.setScene(scene);
        primaryStage.setTitle(bundle.getString("app.title"));
        primaryStage.show();

        // Instanciar controlador *después* de tener la UI lista
        controller = new RouteController(this);

        // Mostrar jerarquía al iniciar (si hay datos)
        controller.updateHierarchy();
    }

    /**
     * Construye la barra superior con logos, toolbar y selector de idioma.
     */
    private HBox buildTopBar() {
        // ToolBar con botones
        ToolBar toolBar = new ToolBar(addButton, /*showButton,*/ searchButton, editButton, deleteButton, new Separator(), exitButton);
        toolBar.getStyleClass().add("tool-bar");
        toolBar.setPadding(new Insets(4));

        // Logos (desde recursos)
        ImageView logoSys = null;
        ImageView logoUptc = null;
        try {
            Image img1 = new Image(getClass().getResourceAsStream("/co/edu/uptc/assets/LogoSistemas.png"));
            logoSys = new ImageView(img1);
            logoSys.setFitHeight(36);
            logoSys.setPreserveRatio(true);
            logoSys.getStyleClass().add("logo");
        } catch (Exception ignored) {}
        try {
            Image img2 = new Image(getClass().getResourceAsStream("/co/edu/uptc/assets/LogoUPTC.png"));
            logoUptc = new ImageView(img2);
            logoUptc.setFitHeight(36);
            logoUptc.setPreserveRatio(true);
            logoUptc.getStyleClass().add("logo");
        } catch (Exception ignored) {}

        HBox logosBox = new HBox(8);
        logosBox.setAlignment(Pos.CENTER_LEFT);
        if (logoSys != null) logosBox.getChildren().add(logoSys);
        if (logoUptc != null) logosBox.getChildren().add(logoUptc);

        Label langLabel = new Label(bundle.containsKey("app.languagetitle") ? bundle.getString("app.languagetitle") : "Idioma:");

        HBox rightBox = new HBox(8, langLabel, languageCombo);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(12, logosBox, toolBar, new Region(), rightBox);
        HBox.setHgrow(topBar.getChildren().get(2), Priority.ALWAYS);
        topBar.setPadding(new Insets(8));
        topBar.setAlignment(Pos.CENTER_LEFT);

        return topBar;
    }

    // ---------- diálogos y acciones ----------
    private void configureAddAction() {
        addButton.setOnAction(e -> {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle(bundle.getString("dialog.add.header"));
            dialog.setHeaderText(bundle.getString("dialog.add.header"));

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            TextField codeField = new TextField();
            codeField.setPromptText(bundle.getString("dialog.add.code"));

            TextField nameField = new TextField();
            nameField.setPromptText(bundle.getString("dialog.add.location"));

            TextField routeCodeField = new TextField();
            routeCodeField.setPromptText(bundle.getString("dialog.add.parent"));

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

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new String[]{ codeField.getText(), nameField.getText(), routeCodeField.getText() };
                }
                return null;
            });

            dialog.showAndWait().ifPresent(data -> {
                if (data[0] != null && !data[0].isEmpty()
                        && data[1] != null && !data[1].isEmpty()) {
                    String parent = (data[2] != null && !data[2].trim().isEmpty()) ? data[2].trim() : null;
                    if (controller != null) controller.addRoute(data[0].trim(), data[1].trim(), parent);
                    statusLabel.setText(bundle.containsKey("status.added") ? bundle.getString("status.added") : "Estación agregada");
                } else {
                    showError("error.format.add");
                }
            });
        });
    }

    private void configureSearchAction() {
        searchButton.setOnAction(e -> {
            Dialog<Pair<String, String>> dialog = new Dialog<>();
            dialog.setTitle(bundle.getString("dialog.search.header"));

            ButtonType searchButtonType = new ButtonType(bundle.getString("dialog.button.search"), ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(bundle.getString("dialog.button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(searchButtonType, cancelButtonType);

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

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == searchButtonType) {
                    return new Pair<>(originField.getText().trim(), destField.getText().trim());
                }
                return null;
            });

            dialog.showAndWait().ifPresent(result -> {
                if (controller != null) controller.searchShortestRoute(result.getKey(), result.getValue());
            });
        });
    }

    private void configureEditAction() {
        editButton.setOnAction(e -> {
            Dialog<String[]> dialog = new Dialog<>();
            dialog.setTitle(bundle.getString("dialog.edit.header"));
            dialog.setHeaderText(bundle.getString("dialog.edit.header"));

            ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

            TextField currentCodeField = new TextField();
            currentCodeField.setPromptText(bundle.getString("dialog.edit.currentCode"));

            TextField newNameField = new TextField();
            newNameField.setPromptText(bundle.getString("dialog.edit.newLocation"));

            TextField newCodeField = new TextField();
            newCodeField.setPromptText(bundle.getString("dialog.edit.newCode"));

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

            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == okButtonType) {
                    return new String[]{ currentCodeField.getText(), newNameField.getText(), newCodeField.getText() };
                }
                return null;
            });

            dialog.showAndWait().ifPresent(data -> {
                if (data[0] != null && !data[0].isEmpty()
                        && data[1] != null && !data[1].isEmpty()
                        && data[2] != null && !data[2].isEmpty()) {
                    if (controller != null) controller.editStation(data[0].trim(), data[1].trim(), data[2].trim());
                    statusLabel.setText(bundle.containsKey("status.edited") ? bundle.getString("status.edited") : "Estación editada");
                } else {
                    showError("error.format.search");
                }
            });
        });
    }

    private void configureDeleteAction() {
        deleteButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(bundle.getString("dialog.delete.header"));
            dialog.setHeaderText(bundle.getString("dialog.delete.header"));
            dialog.setContentText(bundle.getString("dialog.delete.code"));

            dialog.showAndWait().ifPresent(code -> {
                if (code != null && !code.trim().isEmpty()) {
                    if (controller != null) controller.deleteStation(code.trim());
                    statusLabel.setText(bundle.containsKey("status.deleted") ? bundle.getString("status.deleted") : "Estación eliminada");
                } else {
                    showError("error.format.search");
                }
            });
        });
    }

    // Cambiar idioma
    private void setLanguage(Locale locale) {
        bundle = ResourceBundle.getBundle("co.edu.uptc.i18n.messages", locale);
    }

    // Actualizar textos dinámicamente (protege contra campos null)
    private void updateTexts() {
        try {
            if (addButton != null) addButton.setText(bundle.getString("button.add"));
            if (showButton != null) showButton.setText(bundle.getString("button.show"));
            if (searchButton != null) searchButton.setText(bundle.getString("button.search"));
            if (editButton != null) editButton.setText(bundle.getString("button.edit"));
            if (deleteButton != null) deleteButton.setText(bundle.getString("button.delete"));
            if (exitButton != null) exitButton.setText(bundle.getString("button.exit"));

            // Ajustar languageCombo display
            if (languageCombo != null) {
                languageCombo.setValue(bundle.getLocale().getLanguage().equals("en") ? "English" : "Español");
            }

            // Limpiar TextArea si existe
            if (output != null) output.clear();
        } catch (Exception e) {
            // defensivo: si falta alguna key en ResourceBundle, no romper la UI
            System.err.println("Warning updating UI texts: " + e.getMessage());
        }
    }

    // Mostrar jerarquía en el TreeView
    @SuppressWarnings("rawtypes")
    public void showHierarchy(TreeItem rootItem) {
        if (rootItem == null) {
            treeView.setRoot(null);
            return;
        }
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
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Mostrar ruta encontrada
    public void showRoutes(List<?> stations) {
        if (stations == null || stations.isEmpty()) {
            if (output != null) output.clear();
            statusLabel.setText(bundle.containsKey("status.noRoute") ? bundle.getString("status.noRoute") : "No hay ruta");
            return;
        }

        StringJoiner joiner = new StringJoiner(" " + bundle.getString("route.arrow") + " ");
        for (Object station : stations) {
            joiner.add(station.toString());
        }

        if (output != null) output.setText(joiner.toString());
        statusLabel.setText(bundle.containsKey("status.routeFound") ? bundle.getString("status.routeFound") : "Ruta encontrada");
    }

    public static void main(String[] args) {
        launch();
    }
}