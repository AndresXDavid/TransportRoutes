package co.edu.uptc.controller;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lógica del árbol N-ario que modela las estaciones y sus conexiones.
 *
 * <p>
 * Contratos y decisiones de diseño:
 * <ul>
 *   <li>La raíz puede ser {@code null} inicialmente.</li>
 *   <li>Insertar con {@code parentCode == null} crea la raíz si no existe; si la raíz existe,
 *       no se permiten múltiples raíces.</li>
 *   <li>La identidad para evitar duplicados se basa preferentemente en {@code Station.code};
 *       si {@code code} es {@code null}, se considera {@code Station.location}.</li>
 *   <li>Los métodos públicos devuelven {@code boolean} o listas. En caso de error de persistencia
 *       se loguea el problema. No se lanzan RuntimeExceptions desde operaciones de persistencia
 *       para no romper la ejecución en la capa UI.</li>
 * </ul>
 * </p>
 */
@XmlRootElement(name = "routeTree")
@XmlAccessorType(XmlAccessType.FIELD)
public class RouteTree {

    private static final Logger LOGGER = Logger.getLogger(RouteTree.class.getName());

    @XmlElement(name = "root")
    private Node root;

    private static final String FILE_PATH = "src/main/resources-data/routes.xml";

    /**
     * Crea un RouteTree e intenta cargar su estado desde persistencia.
     * Si no existe archivo o ocurre un error, se inicializa con {@code root = null}.
     */
    public RouteTree() {
        try {
            RouteTree loaded = PersistenceManager.getInstance().getRouteDAO().load(FILE_PATH);
            if (loaded != null && loaded.getRoot() != null) {
                this.root = loaded.getRoot();
            } else {
                this.root = null;
            }
        } catch (Exception e) {
            // No rompemos la construcción si hay fallo en persistencia; lo registramos.
            LOGGER.log(Level.WARNING, "No se pudo cargar RouteTree desde persistencia: " + e.getMessage(), e);
            this.root = null;
        }
    }

    /**
     * Inserta una nueva estación en el árbol.
     *
     * @param newStation estación a insertar (no puede ser {@code null})
     * @param parentCode código de la estación padre donde se insertará; si {@code null} y la raíz
     *                   no existe, la nueva estación se convierte en raíz. Si {@code parentCode} es
     *                   {@code null} y ya existe raíz, la operación no inserta y devuelve {@code false}.
     * @return {@code true} si la inserción fue exitosa, {@code false} si ya existe duplicado o no se encontró padre
     */
    public boolean insert(Station newStation, String parentCode) {
        if (newStation == null) {
            return false;
        }

        // Determinar id único: preferir code, sino location
        String newId = newStation.getCode() != null ? newStation.getCode() : newStation.getLocation();
        if (newId == null) {
            // no hay identificador válido
            return false;
        }

        // Si no existe root
        if (root == null) {
            if (parentCode == null) {
                root = new Node(newStation);
                save();
                return true;
            } else {
                // No se puede insertar hijo cuando no existe la raíz
                return false;
            }
        }

        // Evitar duplicados en todo el árbol
        if (existsRec(root, newId)) {
            return false;
        }

        // Buscar padre e insertar
        boolean inserted = insertRec(root, newStation, parentCode);
        if (inserted) {
            save();
        }
        return inserted;
    }

    /**
     * Inserción recursiva: busca el nodo padre por código y añade el hijo.
     *
     * @param current     nodo actual de recorrido
     * @param newStation  estación a insertar
     * @param parentCode  código del padre
     * @return true si se insertó en esta rama
     */
    private boolean insertRec(Node current, Station newStation, String parentCode) {
        if (current == null || current.getStation() == null || parentCode == null) return false;

        String curCode = current.getStation().getCode();
        if (curCode != null && curCode.equals(parentCode)) {
            Node child = new Node(newStation);
            current.addChild(child);
            return true;
        }

        for (Node child : current.getChildren()) {
            if (insertRec(child, newStation, parentCode)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Busca la ruta más corta entre dos ubicaciones (por location).
     * <p>
     * En un árbol, la ruta más corta entre A y B pasa por su ancestro común.
     * </p>
     *
     * @param startLocation ubicación origen (location)
     * @param endLocation   ubicación destino (location)
     * @return lista de {@link Station} desde origen hasta destino (inclusive),
     *         o {@code null} si alguna de las estaciones no existe
     */
    public List<Station> searchShortRoute(String startLocation, String endLocation) {
        if (startLocation == null || endLocation == null || root == null) return null;

        List<Node> pathOrigin = searchRec(root, startLocation);
        List<Node> pathDest = searchRec(root, endLocation);

        if (pathOrigin == null || pathDest == null) {
            return null;
        }

        // Encontrar último ancestro común por comparación de nodos (desde la raíz)
        int i = 0;
        int max = Math.min(pathOrigin.size(), pathDest.size());
        while (i < max) {
            Node a = pathOrigin.get(i);
            Node b = pathDest.get(i);
            if (a == null || b == null) break;
            Station sa = a.getStation();
            Station sb = b.getStation();
            if (sa == null || sb == null) break;
            if (!sa.getCode().equals(sb.getCode())) break;
            i++;
        }
        i--; // retroceder al último común
        if (i < 0) {
            // No se encontró ancestro común válido (caso raro en árbol mal formado)
            return null;
        }

        List<Station> route = new ArrayList<>();

        // Subir desde origen hasta ancestro (sin incluir ancestro)
        for (int j = pathOrigin.size() - 1; j > i; j--) {
            Station s = pathOrigin.get(j).getStation();
            if (s != null) route.add(s);
        }

        // Añadir ancestro común
        Station anc = pathOrigin.get(i).getStation();
        if (anc != null) route.add(anc);

        // Bajar hacia destino (desde i+1 hasta end)
        for (int j = i + 1; j < pathDest.size(); j++) {
            Station s = pathDest.get(j).getStation();
            if (s != null) route.add(s);
        }

        return route;
    }

    /**
     * Busca recursivamente un nodo por su {@code location} y construye la lista desde
     * el nodo de inicio hasta el nodo encontrado (incluyendo ambos).
     *
     * @param current nodo de inicio para la búsqueda (normalmente {@code root})
     * @param location location a buscar
     * @return lista de nodos desde el inicio hasta el encontrado, o {@code null} si no existe
     */
    private List<Node> searchRec(Node current, String location) {
        if (current == null || location == null) return null;
        if (current.getStation() != null && location.equals(current.getStation().getLocation())) {
            List<Node> path = new ArrayList<>();
            path.add(current);
            return path;
        }

        for (Node child : current.getChildren()) {
            List<Node> subPath = searchRec(child, location);
            if (subPath != null) {
                subPath.add(0, current); // insertar al inicio para mantener orden desde la raíz
                return subPath;
            }
        }
        return null;
    }

    /**
     * Comprueba si existe una estación por su código (busca en todo el subárbol).
     *
     * @param current nodo desde donde iniciar (generalmente root)
     * @param code    código a buscar
     * @return {@code true} si existe
     */
    private boolean existsRec(Node current, String code) {
        if (current == null || code == null) return false;
        if (current.getStation() != null && code.equals(current.getStation().getCode())) {
            return true;
        }
        for (Node child : current.getChildren()) {
            if (existsRec(child, code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Devuelve una lista con todas las estaciones del árbol en preorden.
     *
     * @return lista de {@link Station}; lista vacía si el árbol está vacío
     */
    public List<Station> getAllRoutes() {
        List<Station> routes = new ArrayList<>();
        collectRoutes(root, routes);
        return routes;
    }

    private void collectRoutes(Node current, List<Station> routes) {
        if (current == null || current.getStation() == null) {
            return;
        }
        routes.add(current.getStation());
        for (Node child : current.getChildren()) {
            collectRoutes(child, routes);
        }
    }

    /**
     * Elimina una estación por su código.
     *
     * @param code código de la estación a eliminar
     * @return {@code true} si se eliminó correctamente; {@code false} si no se encontró
     */
    public boolean deleteStation(String code) {
        if (code == null || root == null) return false;

        // Si la raíz es la que queremos eliminar
        if (root.getStation() != null && code.equals(root.getStation().getCode())) {
            root = null;
            save();
            return true;
        }

        boolean removed = removeRec(root, code);
        if (removed) save();
        return removed;
    }

    /**
     * Eliminación recursiva: recorre los hijos y usa un iterador para eliminar sin ConcurrentModification.
     *
     * @param parent nodo padre
     * @param code   código a eliminar
     * @return true si se eliminó en esta rama
     */
    private boolean removeRec(Node parent, String code) {
        if (parent == null) return false;
        Iterator<Node> it = parent.getChildren().iterator();
        while (it.hasNext()) {
            Node child = it.next();
            if (child != null && child.getStation() != null && code.equals(child.getStation().getCode())) {
                it.remove();
                return true;
            }
            if (removeRec(child, code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Edita los datos de una estación identificada por su código.
     *
     * @param code               código actual de la estación a editar
     * @param newStationLocation nuevo valor para location
     * @param newStationCode     nuevo código (puede ser igual al anterior)
     * @return {@code true} si la edición fue exitosa; {@code false} si no existe o existe duplicado
     */
    public boolean editStation(String code, String newStationLocation, String newStationCode) {
        if (code == null) return false;
        Node node = findNode(root, code);
        if (node == null) {
            return false;
        }

        // Si cambia el código, verificar duplicado
        if (newStationCode != null && !newStationCode.equals(code) && existsRec(root, newStationCode)) {
            return false;
        }

        if (node.getStation() == null) return false;
        node.getStation().setLocation(newStationLocation);
        node.getStation().setCode(newStationCode);

        save();
        return true;
    }

    /**
     * Busca un nodo por su código (recursivo).
     *
     * @param current nodo donde iniciar
     * @param code    código buscado
     * @return nodo encontrado o {@code null}
     */
    private Node findNode(Node current, String code) {
        if (current == null || code == null) return null;
        if (current.getStation() != null && code.equals(current.getStation().getCode())) {
            return current;
        }
        for (Node child : current.getChildren()) {
            Node result = findNode(child, code);
            if (result != null) return result;
        }
        return null;
    }

    /**
     * Guarda el estado actual del árbol en persistencia.
     * <p>
     * Se invoca internamente tras operaciones que modifican el árbol.
     * Errores de persistencia se registran en el logger.
     * </p>
     */
    public void save() {
        try {
            PersistenceManager.getInstance().getRouteDAO().save(this, FILE_PATH);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error guardando RouteTree en persistencia: " + e.getMessage(), e);
        }
    }

    /* ---------------- Getters / Setters ---------------- */

    /**
     * Devuelve la raíz del árbol (puede ser {@code null}).
     *
     * @return nodo raíz
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Establece la raíz del árbol.
     *
     * @param root nodo raíz
     */
    public void setRoot(Node root) {
        this.root = root;
    }
}