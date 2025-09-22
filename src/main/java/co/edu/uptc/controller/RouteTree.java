package co.edu.uptc.controller;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.*;

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

    private static final String FILE_PATH = "src/main/resources/co/edu/uptc/routes.xml";

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
            this.root = null;
        }
    }

    public Node getRoot() { return root; }
    public void setRoot(Node root) {this.root = Objects.requireNonNull(root, "El nodo raiz no puede ser null"); }


    /**
     * Inserta una nueva estación como hijo del nodo cuyo código es parentCode.
     * Si parentCode es null y no existe raíz, la nueva estación se convierte en raíz.
     *
     * @return true si insertó, false si ya existe o no se encontró padre.
     */
    public boolean insert(Station newStation, String parentCode) {
        Objects.requireNonNull(newStation, "La estacin no puede ser null");
        Objects.requireNonNull(newStation.getCode(), "El codigo de la estacin no puede ser null");
        Objects.requireNonNull(newStation.getLocation(), "La ubicacion de la estacion no puede ser null");

        String id = newStation.getCode().trim();
        String name = newStation.getLocation().trim();

        if (root == null) {
            if (parentCode == null) {
                root = new Node(newStation);
                save();
                return true;
            } else {
                return false;
            }
        }

        if (existsRec(root, id) || existsLocation(root, name)) return false;

        boolean inserted = insertRec(root, newStation, parentCode);
        if (inserted) {
            save();
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
        Objects.requireNonNull(code, "El codigo no puede ser null");

        if (root == null) return false;

        if (root.getStation() != null && code.equals(root.getStation().getCode())) {
            root = null;
            save();
            return true;
        }
        boolean removed = removeRec(root, code);
        if (removed) save();
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
        Objects.requireNonNull(code, "El codigo no puede ser null");
        Objects.requireNonNull(newLocation, "La nueva ubicacion no puede ser null");
        Objects.requireNonNull(newCode, "El nuevo codigo no puede ser null");

        Node node = findNode(root, code);
        if (node == null || node.getStation() == null) return false;

        if (newCode != null && !newCode.equals(code) && existsRec(root, newCode)) return false;
        if (existsLocation(root, newLocation)) return false;

        node.getStation().setLocation(newLocation.trim());
        node.getStation().setCode(newCode != null ? newCode.trim() : code);

        save();
        return true;
    }

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
    Objects.requireNonNull(startLocation, "El origen no puede ser null");
    Objects.requireNonNull(endLocation, "El destino no puede ser null");

    if (root == null) return null;

    List<Node> p1 = searchRec(root, startLocation.trim());
    List<Node> p2 = searchRec(root, endLocation.trim());
    if (p1 == null || p2 == null) return null;

    // encontrar último índice común (LCA index)
    int max = Math.min(p1.size(), p2.size());
    int lastCommonIndex = -1;
    for (int idx = 0; idx < max; idx++) {
        Node n1 = p1.get(idx);
        Node n2 = p2.get(idx);
        if (n1 == null || n2 == null) break;
        Station s1 = n1.getStation();
        Station s2 = n2.getStation();
        if (s1 == null || s2 == null) break;

        // comparar de forma consistente: primero code si existe, si no location
        String code1 = s1.getCode();
        String code2 = s2.getCode();
        boolean same;
        if (code1 != null || code2 != null) {
            same = Objects.equals(code1, code2);
        } else {
            same = Objects.equals(s1.getLocation(), s2.getLocation());
        }

        if (!same) break;
        lastCommonIndex = idx;
    }

    if (lastCommonIndex < 0) return null; // no comparten ancestro común (o no se puede identificar)

    // construir la ruta desde start hasta end pasando por el LCA
    List<Station> route = new ArrayList<>();

    // Desde el nodo 'start' ascendiendo hasta el hijo directo del LCA
    for (int j = p1.size() - 1; j > lastCommonIndex; j--) {
        route.add(p1.get(j).getStation());
    }

    // Añadir LCA
    route.add(p1.get(lastCommonIndex).getStation());

    // Desde el hijo directo del LCA hacia el nodo 'end'
    for (int j = lastCommonIndex + 1; j < p2.size(); j++) {
        route.add(p2.get(j).getStation());
    }

    return route;
}


    /**
     * Por implementar dentro de RouteTree.
     * Busca la ruta más corta entre dos estaciones considerando el grafo implícito (padre-hijo bidireccional).
     * Usa BFS y devuelve la lista de Station desde origen hasta destino (inclusive).
     * Esta función es la que garantiza la ruta con menor número de aristas en grafos no dirigidos.
     */
    public List<Station> searchShortestPathBFS(String fromLocation, String toLocation) {
        Objects.requireNonNull(fromLocation, "El origen no puede ser null");
        Objects.requireNonNull(toLocation, "El destino no puede ser null");

        if (root == null) return null;

        // Construir mapa: location -> Node
        Map<String, Node> locationMap = new HashMap<>();
        buildLocationMap(root, locationMap);

        Node start = locationMap.get(fromLocation.trim());
        Node goal = locationMap.get(toLocation.trim());
        if (start == null || goal == null) return null; 

        // BFS Clasico
        Queue<Node> q = new ArrayDeque<>();
        Map<Node, Node> parent = new HashMap<>();
        q.add(start);
        parent.put(start, null);

        while (!q.isEmpty()) {
            Node cur = q.poll();
            if (cur.equals(goal)) break;

            // Vecinos: padre + hijos
            List<Node> neighbors = new ArrayList<>(cur.getChildren());
            if (cur.getParent() != null) neighbors.add(cur.getParent());

            for (Node nb : neighbors) {
                if (!parent.containsKey(nb)) {
                    parent.put(nb, cur);
                    q.add(nb);
                }
            }
        }

        // Si no hay ruta
        if (!parent.containsKey(goal)) return null;

        // Reconstruir ruta desde el destino hasta el origen
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
            throw new RuntimeException("Error al guardar el árbol de rutas.", e);
        }
    }
}