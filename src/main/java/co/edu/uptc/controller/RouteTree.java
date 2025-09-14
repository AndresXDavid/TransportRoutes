package co.edu.uptc.controller;

import java.util.ArrayList;
import java.util.List;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistance.RoutePersistance;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "routeTree")
@XmlAccessorType(XmlAccessType.FIELD)
public class RouteTree {

    @XmlElement(name = "root")
    private Node root;
    private static final String FILE_PATH = "src/main/resources-data/routes.xml";
    

    public RouteTree() {
        // Intentar cargar desde XML al crear el árbol
        RouteTree loaded = RoutePersistance.cargar(FILE_PATH);
        if (loaded != null) {
            this.root = loaded.getRoot();
        } else {
            // si no existe el archivo, inicializar con raíz base
            root = null;
        }
    }

    public List<String> getRoutesHierarchySimple() {
        List<String> lines = new ArrayList<>();
        buildSimple(root, 0, lines);
        return lines;
    }

    private void buildSimple(Node node, int depth, List<String> lines) {
        if (node == null || node.getStation() == null) return;
        String indent = "  ".repeat(Math.max(0, depth));
        String line = indent + "Nombre: " + node.getStation().getName()
            + ", Código: " + node.getStation().getCode()
            + ", Ubicación: " + node.getStation().getLocation();
        lines.add(line);
        if (node.getChildren() == null) return;
        for (Node child : node.getChildren()) {
            buildSimple(child, depth + 1, lines);
        }
    }

    // Insertar un nuevo nodo en el árbol
    public boolean insert(Station newStation, String parentCode) {
        if (root == null) {
            root = new Node(newStation);
            save();
            return true;
        } else {
            boolean inserted = insertRec(root, newStation, parentCode);
            if (inserted) {
                save(); 
            }
            return inserted;
        }
    }

    private boolean insertRec(Node current, Station newStation, String parentCode) {
        if (current.getStation().getCode().equals(parentCode)) {
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

    // Buscar estación por código
    public Station search(String code) {
        return searchRec(root, code);
    }

    private Station searchRec(Node current, String code) {
        if (current == null) return null;

        if (current.getStation().getCode().equals(code)) {
            return current.getStation();
        }

        for (Node child : current.getChildren()) {
            Station found = searchRec(child, code);
            if (found != null) return found;
        }
        return null;
    }

    // Obtener lista de rutas
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

    // Guardar árbol en XML
    public void save() {
        RoutePersistance.guardar(this, FILE_PATH);
    }

    // --- Getters y setters ---
    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
