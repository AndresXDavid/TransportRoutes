package co.edu.uptc.controller;

import java.util.List;

import co.edu.uptc.interfaz.RouteView;
import co.edu.uptc.logic.RouteTree;
import co.edu.uptc.logic.Station;

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
                    addRoute(routeData.get(0), routeData.get(1), routeData.get(2), routeData.get(3));
                    break;
                case 2: // Mostrar rutas
                    List<Station> routes = logic.getAllRoutes();
                    if (routes.isEmpty()) {
                        view.showMessage("No hay rutas registradas.");
                    } else {
                        view.showRoutes(routes);
                    }
                    break;
                case 3: // Buscar ruta
                    String code = view.requestSearchCode();
                    Station station = logic.search(code);
                    if (station != null) {
                        view.showMessage("Ruta encontrada: " + station);
                    } else {
                        view.showMessage("No se encontró la ruta con código: " + code);
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

    public void addRoute(String name, String code, String location, String parentCode) {
        boolean inserted = logic.insert(new Station(name, code, location), parentCode);
        if (inserted) {
            view.showMessage("Ruta agregada correctamente.");
        } else {
            view.showMessage("No se pudo agregar la ruta (revise el código padre).");
        }
    }
}
