package co.edu.uptc.controller;

import java.util.List;
import java.util.Objects;

import co.edu.uptc.model.Station;
import co.edu.uptc.model.Node;
import co.edu.uptc.utils.RouteConverter;
import co.edu.uptc.viewController.RouteViewFX;
import javafx.scene.control.TreeItem;

public class RouteController {
    private RouteViewFX view;
    private RouteTree logic;

    public RouteController(RouteViewFX view) {
        this.view = Objects.requireNonNull(view, "La vista no puede ser null");
        this.logic = new RouteTree();
    }

    // Acción: agregar ruta
    public void addRoute(String code, String location, String parentCode) {
        Objects.requireNonNull(code, "El codigo no puede ser null");
        Objects.requireNonNull(location, "La ubicacion no puede ser null");

        boolean inserted = logic.insert(
                new Station(code.trim(), RouteConverter.capitalizeWords(location.trim())),
                parentCode != null ? parentCode.trim() : null
        );

        if (inserted) {
            view.showInfo("route.added.success");
            updateHierarchy();
        } else {
            view.showError("route.added.failure");
        }
    }

    // Acción: mostrar todas las rutas
    public void updateHierarchy() {
        Node root = logic.getRoot();
        TreeItem<String> rootItem = buildTreeItem(root);
        view.showHierarchy(rootItem);
    }


    // Acción: buscar ruta más corta
    public void searchShortestRoute(String from, String to) {
        Objects.requireNonNull(from, "El origen no puede ser null");
        Objects.requireNonNull(to, "El destino no puede ser null");

        List<Station> shortestRoute = logic.searchShortestPathBFS(
                RouteConverter.capitalizeWords(from.trim()),
                RouteConverter.capitalizeWords(to.trim())
        );

        if (shortestRoute != null) {
            view.showRoutes(shortestRoute);
        } else {
            view.showError("error.station.notfound",
                    RouteConverter.capitalizeWords(from), RouteConverter.capitalizeWords(to));
        }
    }

    // Acción: editar ruta
    public void editStation(String code, String newLocation, String newCode) {
        Objects.requireNonNull(code, "El codigo no puede ser null");
        Objects.requireNonNull(newLocation, "La nueva ubicacion no puede ser null");
        Objects.requireNonNull(newCode, "El nuevo codigo no puede ser null");

        boolean edited = logic.editStation(
            code.trim(),
            RouteConverter.capitalizeWords(newLocation.trim()), 
            newCode != null ? newCode.trim() : null
        );

        if (edited) {
            view.showInfo("route.edited.success");
            updateHierarchy(); // refrescar arbol
        } else {
            view.showError("route.edited.failure");
        }
    }

    // Acción: eliminar ruta
    public void deleteStation(String code) {
        Objects.requireNonNull(code, "El codigo no puede ser null");

        boolean deleted = logic.deleteStation(code.trim());
        if (deleted) {
            view.showInfo("route.deleted.success");
            updateHierarchy(); // refrescar arbol
        } else {
            view.showError("route.deleted.failure");
        }
    }

    // Método auxiliar para construir TreeItem recursivamente
    private TreeItem<String> buildTreeItem(Node node) {
        if (node == null || node.getStation() == null) return null;
        TreeItem<String> item = new TreeItem<>(node.getStation().getLocation());
        for (Node child : node.getChildren()) {
            TreeItem<String> childItem = buildTreeItem(child);
            if (childItem != null) {
                item.getChildren().add(childItem);
            }
        }
        return item;
    }
}