package co.edu.uptc.persistence;

import co.edu.uptc.controller.RouteTree;

/**
 * Interfaz que define el contrato para la persistencia de objetos {@link RouteTree}.
 * 
 * <p>Permite guardar y cargar estructuras de rutas en diferentes medios de almacenamiento
 * (archivos XML, CSV, bases de datos, etc.) según la implementación concreta.</p>
 * 
 * <p>El uso de esta interfaz facilita la aplicación del patrón DAO, garantizando
 * bajo acoplamiento entre la lógica de negocio y la capa de persistencia.</p>
 * 
 * @author TuNombre
 */
public interface RouteDAO {

    /**
     * Guarda un objeto {@link RouteTree} en un archivo o medio de almacenamiento.
     *
     * @param tree     Árbol de rutas a guardar.
     * @param filePath Ruta del archivo donde se debe almacenar la información.
     */
    void guardar(RouteTree tree, String filePath);

    /**
     * Carga un objeto {@link RouteTree} desde un archivo o medio de almacenamiento.
     *
     * @param filePath Ruta del archivo desde donde se debe leer la información.
     * @return Objeto {@link RouteTree} cargado, o {@code null} si ocurre un error.
     */
    RouteTree cargar(String filePath);
}
