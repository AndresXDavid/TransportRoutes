package co.edu.uptc.viewController;

import java.util.List;
import java.util.Scanner;

import co.edu.uptc.model.Station;

public class RouteView {
    private Scanner scanner;

    public RouteView() {
        scanner = new Scanner(System.in);
    }

    public int showMenu() {
        System.out.println("===== MENÚ PRINCIPAL =====");
        System.out.println("1. Agregar Estacion");
        System.out.println("2. Mostrar Estaciones");
        System.out.println("3. Buscar Ruta Mas Corta");
        System.out.println("4. Salir");
        System.out.print("Seleccione una opción: ");

        int option = scanner.nextInt();
        scanner.nextLine(); // limpiar buffer
        return option;
    }

    public List<String> requestRouteData() {
        System.out.println("Nombre de la Estacion: ");
        String name = scanner.nextLine();

        System.out.println("Código de la Estacion: ");
        String code = scanner.nextLine();

        System.out.println("Ubicación de la Estacion: ");
        String location = scanner.nextLine();

        System.out.println("Código de la Estacion Padre: ");
        String parentCode = scanner.nextLine();

        return List.of(name, code, location, parentCode);
    }

    public void showMessage(String message) {
        System.out.println(message);
    }

    public void showRoutes(List<Station> routes) {
        System.out.println("===== Ruta Mas Corta =====");
        for (Station route : routes) {
            System.out.println(showRoute(route));
        }
    }

    public String showRoute(Station route) {
        return ("Nombre: " + route.getName() + ", Código: " + route.getCode() + ", Ubicación: " + route.getLocation());
    }

    public List<String> requestSearchLocation() {
        System.out.print("Ingrese el Nombre de la Estacion de Partida: ");
        String start = scanner.nextLine();
        System.out.print("Ingrese el Nombre de la Estacion de Llegada: ");
        String end = scanner.nextLine();
        return List.of(start, end);
    }

    public void showHierarchy(List<String> lines) {
        System.out.println("===== Estaciones =====");
        for (String l : lines) {
            System.out.println(l);
        }
    }

}

