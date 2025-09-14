package co.edu.uptc.controller;

import java.util.List;

import co.edu.uptc.model.Station;
import co.edu.uptc.viewController.RouteView;
import co.edu.uptc.utils.RouteConverter;

public class RouteController {
    private RouteView view;
    private RouteTree logic;

    public RouteController() {
        logic = new RouteTree();
        view = new RouteView();
    }

    public void start() {
        int option;
        do {
            option = view.showMenu();
            switch (option) {
                case 1: // Agregar ruta
                    List<String> routeData = view.requestRouteData();
                    addRoute(routeData.get(0), RouteConverter.capitalizeWords(routeData.get(1)), routeData.get(2));
                    break;
                case 2: // Mostrar rutas
                    List<String> lines = logic.getRoutesHierarchySimple(); // o getRoutesHierarchySimple()
                    if (lines.isEmpty()) {
                        view.showMessage("No hay rutas registradas.");
                    } else {
                        view.showHierarchy(lines);
                    }
                    break;
                case 3: // Buscar ruta mas corta
                    List<String> route = view.requestSearchLocation();
                    List<Station> shortestRoute = logic.searchShortRoute(RouteConverter.capitalizeWords(route.get(0)), RouteConverter.capitalizeWords(route.get(1)));
                    if (shortestRoute != null) {
                        view.showRoutes(shortestRoute);
                    } else {
                        view.showMessage("No se encontró la estacion: " + RouteConverter.capitalizeWords(route.get(0)) + " o " + RouteConverter.capitalizeWords(route.get(1)));
                    }
                    break;
                case 4: // Salir
                    view.showMessage("Saliendo...");
                    break;
                default:
                    view.showMessage("Opción no válida");
                    break;
            }
        } while (option != 4);
    }

    public void addRoute(String code, String location, String stationcode) {
        boolean inserted = logic.insert(new Station(code, RouteConverter.capitalizeWords(location)), stationcode);
        if (inserted) {
            view.showMessage("Ruta agregada correctamente.");
        } else {
            view.showMessage("No se pudo agregar la ruta.");
        }
    }
}
