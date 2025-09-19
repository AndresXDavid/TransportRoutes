package co.edu.uptc.utils;

import co.edu.uptc.model.Node;
import javafx.scene.control.TreeItem;

public class RouteConverter {
    // Convierte un string a "Primera Letra Mayúscula" por cada palabra
    public static String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String[] palabras = input.split("\\s+"); // separar por espacios
        StringBuilder resultado = new StringBuilder();

        for (String palabra : palabras) {
            if (palabra.length() > 0) {
                resultado.append(palabra.substring(0, 1).toUpperCase()); // primera letra mayúscula
                resultado.append(palabra.substring(1).toLowerCase());    // resto minúscula
            }
            resultado.append(" "); // agregar espacio entre palabras
        }

        return resultado.toString().trim(); // quitar espacio final
    }

    public static TreeItem<String> buildTree(Node node) {
        // Mostramos el nombre de la estación como texto
        TreeItem<String> treeItem = new TreeItem<>(node.getStation().getLocation() + " (" + node.getStation().getCode() + ")");

        for (Node child : node.getChildren()) {
            treeItem.getChildren().add(buildTree(child)); // recursión
        }

        return treeItem;
    }
}
