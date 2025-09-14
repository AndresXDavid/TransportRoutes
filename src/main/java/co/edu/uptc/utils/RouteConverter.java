package co.edu.uptc.utils;

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
}
