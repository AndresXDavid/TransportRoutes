// package co.edu.uptc.controller;

// import java.util.List;

// import co.edu.uptc.model.Station;
// import co.edu.uptc.viewController.RouteView;
// import co.edu.uptc.utils.RouteConverter;

// public class RouteController {
//     private RouteView view;
//     private RouteTree logic;

//     public RouteController() {
//         logic = new RouteTree();
//         view = new RouteView();
//     }

//     public void start() {
//         int option;
//         do {
//             option = view.showMenu();
//             switch (option) {
//                 case 1: // Agregar ruta
//                     List<String> routeData = view.requestRouteData();
//                     addRoute(routeData.get(0), RouteConverter.capitalizeWords(routeData.get(1)), routeData.get(2));
//                     break;
//                 case 2: // Mostrar rutas
//                     List<String> lines = logic.getRoutesHierarchySimple(); // o getRoutesHierarchySimple()
//                     if (lines.isEmpty()) {
//                         view.showMessage("No hay rutas registradas.");
//                     } else {
//                         view.showHierarchy(lines);
//                     }
//                     break;
//                 case 3: // Buscar ruta mas corta
//                     List<String> route = view.requestSearchLocation();
//                     List<Station> shortestRoute = logic.searchShortRoute(RouteConverter.capitalizeWords(route.get(0)), RouteConverter.capitalizeWords(route.get(1)));
//                     if (shortestRoute != null) {
//                         view.showRoutes(shortestRoute);
//                     } else {
//                         view.showMessage("No se encontró la estacion: " + RouteConverter.capitalizeWords(route.get(0)) + " o " + RouteConverter.capitalizeWords(route.get(1)));
//                     }
//                     break;
//                 case 4: // Salir
//                     view.showMessage("Saliendo...");
//                     break;
//                 default:
//                     view.showMessage("Opción no válida");
//                     break;
//             }
//         } while (option != 4);
//     }

//     public void addRoute(String code, String location, String stationcode) {
//         boolean inserted = logic.insert(new Station(code, RouteConverter.capitalizeWords(location)), stationcode);
//         if (inserted) {
//             view.showMessage("Ruta agregada correctamente.");
//         } else {
//             view.showMessage("No se pudo agregar la ruta.");
//         }
//     }
// }

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
            view.showMessage("Ruta agregada correctamente.");
            updateHierarchy(); // refrescar árbol
        } else {
            view.showMessage("No se pudo agregar la ruta.");
        }
    }

    // Acción: mostrar todas las rutas
    public void updateHierarchy() {
        List<String> lines = logic.getRoutesHierarchySimple();
        if (lines.isEmpty()) {
            view.showMessage("No hay rutas registradas.");
        } else {
            view.showHierarchy(lines);
        }
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
            view.showMessage("No se encontró la estación: " 
                + RouteConverter.capitalizeWords(from) 
                + " o " + RouteConverter.capitalizeWords(to));
        }
    }
}
