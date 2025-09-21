package co.edu.uptc.controller;

import java.util.List;

import co.edu.uptc.model.Station;
import co.edu.uptc.utils.RouteConverter;
import co.edu.uptc.viewController.RouteViewFX;

public class RouteController {
    private RouteViewFX view;
    private RouteTree logic;

    public RouteController(RouteViewFX view) {
        this.view = view;
        this.logic = new RouteTree();
    }

    // Acción: agregar ruta
    public void addRoute(String code, String location, String parentCode) {
        boolean inserted = logic.insert(new Station(code, RouteConverter.capitalizeWords(location)), parentCode);
        if (inserted) {
            view.showInfo("route.added.success");
            updateHierarchy(); // refrescar árbol
        } else {
            view.showError("route.added.failure");
        }
    }

    // Acción: mostrar todas las rutas
    public void updateHierarchy() {
        view.showHierarchy(RouteConverter.buildTree(logic.getRoot()));
    }

    // Acción: buscar ruta más corta
    public void searchShortestRoute(String from, String to) {
        List<Station> shortestRoute = logic.searchShortRoute(
                RouteConverter.capitalizeWords(from),
                RouteConverter.capitalizeWords(to)
        );

        if (shortestRoute != null) {
            view.showRoutes(shortestRoute);
        } else {
            view.showError("error.station.notfound",  
                RouteConverter.capitalizeWords(from),  RouteConverter.capitalizeWords(to));
        }
    }

    // Acción: editar ruta
    public void editStation(String code, String newLocation, String newCode) {
        boolean edited = logic.editStation(code, RouteConverter.capitalizeWords(newLocation), newCode);
        if (edited) {
            view.showInfo("route.edit.success");
            updateHierarchy(); // refrescar árbol
        } else {
            view.showError("route.edit.failure");
        }
    }

    // Acción: eliminar ruta
    public void deleteStation(String code) {
        boolean deleted = logic.deleteStation(code.trim());
        if (deleted) {
            view.showInfo("route.deleted.success");
            updateHierarchy(); // refrescar árbol
        } else {
            view.showError("route.deleted.failure");
        }
    }
}