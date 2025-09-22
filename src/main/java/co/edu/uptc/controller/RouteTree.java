package co.edu.uptc.controller;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;
import co.edu.uptc.persistence.PersistenceException;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Lógica del árbol N-ario que modela las estaciones y sus conexiones.
 *
 * <p>Soporta:
 * - inserción, edición y borrado de estaciones,
 * - búsqueda por ruta via ancestro común (útil si realmente es un árbol),
 * - búsqueda de ruta más corta usando BFS considerando el grafo implícito (padre-hijo bidireccional).
 * - persistencia vía PersistenceManager/RouteDAO.
 * </p>
 */
@XmlRootElement(name = "routeTree")
@XmlAccessorType(XmlAccessType.FIELD)
public class RouteTree {

    private static final Logger LOGGER = Logger.getLogger(RouteTree.class.getName());
    private static final String FILE_PATH = "src/main/resources-data/routes.xml";

    @XmlElement(name = "root")
    private Node root;

    public RouteTree() {
        try {
            RouteTree loaded = PersistenceManager.getInstance().getRouteDAO().load(FILE_PATH);
            if (loaded != null && loaded.getRoot() != null) {
                this.root = loaded.getRoot();
            } else {
                this.root = null;
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "No se pudo cargar RouteTree desde persistencia: " + e.getMessage(), e);
            this.root = null;
        }
    }

    /* -------------------- CRUD básico -------------------- */

    public Node getRoot() { return root; }
    public void setRoot(Node root) { this.root = root; }

    /**
     * Inserta una nueva estación como hijo del nodo cuyo código es parentCode.
     * Si parentCode es null y no existe raíz, la nueva estación se convierte en raíz.
     *
     * @return true si insertó, false si ya existe o no se encontró padre.
     */
    public boolean insert(Station newStation, String parentCode) {
        if (newStation == null) return false;
        String id = newStation.getCode() != null ? newStation.getCode() : newStation.getLocation();
        String name = newStation.getLocation();
        if (id == null) return false;

        if (root == null) {
            if (parentCode == null) {
                root = new Node(newStation);
                save(); // Propagará la excepción si algo falla
                return true;
            } else {
                return false;
            }
        }

        if (existsRec(root, id)) return false;
        if (existsLocation(root, name)) return false;
        boolean inserted = insertRec(root, newStation, parentCode);
        if (inserted) {
            save(); // Propagar la excepción si algo falla
            return true;
        }
        return inserted;
    }

    private boolean insertRec(Node current, Station newStation, String parentCode) {
        if (current == null || current.getStation() == null || parentCode == null) return false;
        String curCode = current.getStation().getCode();
        if (curCode != null && curCode.equals(parentCode)) {
            Node child = new Node(newStation);
            current.addChild(child);
            return true;
        }
        for (Node child : current.getChildren()) {
            if (insertRec(child, newStation, parentCode)) return true;
        }
        return false;
    }

    public boolean deleteStation(String code) {
        if (code == null || root == null) return false;
        if (root.getStation() != null && code.equals(root.getStation().getCode())) {
            root = null;
            save(); // Propagar la excepción si algo falla
            return true;
        }
        boolean removed = removeRec(root, code);
        if (removed) {
            save(); // Propagar la excepción si algo falla
            return true;
        }
        return removed;
    }

    private boolean removeRec(Node parent, String code) {
        if (parent == null) return false;
        Iterator<Node> it = parent.getChildren().iterator();
        while (it.hasNext()) {
            Node child = it.next();
            if (child != null && child.getStation() != null && code.equals(child.getStation().getCode())) {
                it.remove();
                return true;
            }
            if (removeRec(child, code)) return true;
        }
        return false;
    }

    public boolean editStation(String code, String newLocation, String newCode) {
        if (code == null) return false;
        Node node = findNode(root, code);
        if (node == null) return false;
        if (newCode != null && !newCode.equals(code) && existsRec(root, newCode) && existsLocation(root, newLocation)) return false;
        if (node.getStation() == null) return false;
        node.getStation().setLocation(newLocation);
        node.getStation().setCode(newCode);
        save(); // Propagar la excepción si algo falla
        return true;
    }

    /* -------------------- Búsquedas -------------------- */

    /**
     * Busca recursivamente un nodo por su location (devuelve path desde 'start' hasta el nodo).
     * @return lista de nodos desde 'start' hasta la ubicación (incluyendo ambos), o null si no existe
     */
    public List<Node> searchRec(Node start, String location) {
        if (start == null || location == null) return null;
        if (start.getStation() != null && location.equals(start.getStation().getLocation())) {
            List<Node> p = new ArrayList<>();
            p.add(start);
            return p;
        }
        for (Node child : start.getChildren()) {
            List<Node> sub = searchRec(child, location);
            if (sub != null) {
                sub.add(0, start);
                return sub;
            }
        }
        return null;
    }

    /**
     * Busca la ruta usando ancestro común (útil si la estructura es estrictamente árbol).
     * @return lista de Station desde inicio hasta fin (inclusive) o null si alguna no existe
     */
    public List<Station> searchShortRoute(String startLocation, String endLocation) {
        if (startLocation == null || endLocation == null || root == null) return null;
        List<Node> p1 = searchRec(root, startLocation);
        List<Node> p2 = searchRec(root, endLocation);
        if (p1 == null || p2 == null) return null;

        int i = 0;
        int max = Math.min(p1.size(), p2.size());
        while (i < max) {
            Station s1 = p1.get(i).getStation();
            Station s2 = p2.get(i).getStation();
            if (s1 == null || s2 == null) break;
            if (!Objects.equals(s1.getCode(), s2.getCode())) break;
            i++;
        }
        i--;
        if (i < 0) return null;

        List<Station> route = new ArrayList<>();
        for (int j = p1.size() - 1; j > i; j--) route.add(p1.get(j).getStation());
        route.add(p1.get(i).getStation());
        for (int j = i + 1; j < p2.size(); j++) route.add(p2.get(j).getStation());
        return route;
    }

    /**
     * Busca la ruta más corta entre dos estaciones considerando el grafo implícito (padre-hijo bidireccional).
     * Usa BFS y devuelve la lista de Station desde origen hasta destino (inclusive).
     * Esta función es la que garantiza la ruta con menor número de aristas en grafos no dirigidos.
     */
    public List<Station> searchShortestPathBFS(String fromLocation, String toLocation) {
        if (fromLocation == null || toLocation == null || root == null) return null;
        // Construir mapa location -> Node para acceso rápido
        Map<String, Node> locMap = new HashMap<>();
        buildLocationMap(root, locMap);

        Node start = locMap.get(fromLocation);
        Node goal = locMap.get(toLocation);
        if (start == null || goal == null) return null;

        // BFS con path reconstruction
        Queue<Node> q = new ArrayDeque<>();
        Map<Node, Node> parent = new HashMap<>(); // child -> parent
        q.add(start);
        parent.put(start, null);
        boolean found = false;
        while (!q.isEmpty() && !found) {
            Node cur = q.poll();
            // neighbors: children + parent (if exists)
            List<Node> neighbors = new ArrayList<>(cur.getChildren());
            if (cur.getParent() != null) neighbors.add(cur.getParent());
            for (Node nb : neighbors) {
                if (!parent.containsKey(nb)) {
                    parent.put(nb, cur);
                    if (nb.equals(goal)) { found = true; break; }
                    q.add(nb);
                }
            }
        }
        if (!parent.containsKey(goal)) return null;

        // Reconstruir ruta desde goal a start con parent map
        LinkedList<Station> route = new LinkedList<>();
        Node cur = goal;
        while (cur != null) {
            if (cur.getStation() != null) route.addFirst(cur.getStation());
            cur = parent.get(cur);
        }
        return route;
    }

    private void buildLocationMap(Node current, Map<String, Node> map) {
        if (current == null) return;
        if (current.getStation() != null && current.getStation().getLocation() != null) {
            map.put(current.getStation().getLocation(), current);
        }
        for (Node c : current.getChildren()) {
            // asegurar vínculo padre->hijo consistente
            c.setParent(current);
            buildLocationMap(c, map);
        }
    }

    private Node findNode(Node current, String code) {
        if (current == null || code == null) return null;
        if (current.getStation() != null && code.equals(current.getStation().getCode())) return current;
        for (Node child : current.getChildren()) {
            Node r = findNode(child, code);
            if (r != null) return r;
        }
        return null;
    }

    private boolean existsRec(Node current, String code) {
        if (current == null || code == null) return false;
        if (current.getStation() != null && code.equals(current.getStation().getCode())) return true;
        for (Node child : current.getChildren()) if (existsRec(child, code)) return true;
        return false;
    }

    private boolean existsLocation(Node current, String name) {
        if (current == null || name == null) return false;
        if (current.getStation() != null && name.equals(current.getStation().getLocation())) return true;
        for (Node child : current.getChildren()) if (existsLocation(child, name)) return true;
        return false;
    }

    public List<Station> getAllRoutes() {
        List<Station> routes = new ArrayList<>();
        collectRoutes(root, routes);
        return routes;
    }
    private void collectRoutes(Node cur, List<Station> out) {
        if (cur == null || cur.getStation() == null) return;
        out.add(cur.getStation());
        for (Node c : cur.getChildren()) collectRoutes(c, out);
    }

    public void save() {
        try {
            PersistenceManager.getInstance().getRouteDAO().save(this, FILE_PATH);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error guardando RouteTree: " + e.getMessage(), e);
            throw new RuntimeException("Error al guardar el árbol de rutas.", e);
        }
    }
}