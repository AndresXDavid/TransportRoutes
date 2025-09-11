package co.edu.uptc.interfaz;

import java.util.List;
import java.util.Scanner;

import co.edu.uptc.logic.Station;

public class RouteView {
    private Scanner scanner;

    public RouteView() {
        scanner = new Scanner(System.in);
    }

    public int showMenu() {
        System.out.println("===== MENÚ PRINCIPAL =====");
        System.out.println("1. Agregar Ruta");
        System.out.println("2. Mostrar Rutas");
        System.out.println("3. Buscar Ruta");
        System.out.println("4. Salir");
        System.out.print("Seleccione una opción: ");

        int option = scanner.nextInt();
        scanner.nextLine(); // limpiar buffer
        return option;
    }

    public List<String> requestRouteData() {
        System.out.println("Nombre de la ruta: ");
        String name = scanner.nextLine();

        System.out.println("Código de la ruta: ");
        String code = scanner.nextLine();

        System.out.println("Ubicación de la ruta: ");
        String location = scanner.nextLine();

        System.out.println("Código de la ruta padre (si no tiene, deje vacío): ");
        String parentCode = scanner.nextLine();

        return List.of(name, code, location, parentCode);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showRoutes(List<Station> routes) {
        System.out.println("===== RUTAS =====");
        for (Station route : routes) {
            System.out.println(showRoute(route));
        }
    }

    public String showRoute(Station route) {
        return ("Nombre: " + route.getName() + ", Código: " + route.getCode() + ", Ubicación: " + route.getLocation());
    }

    public String requestSearchCode() {
        System.out.print("Ingrese el código de la ruta a buscar: ");
        return scanner.nextLine();
    }
}

