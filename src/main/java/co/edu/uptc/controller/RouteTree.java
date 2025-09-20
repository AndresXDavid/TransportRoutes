package co.edu.uptc.controller;

import java.util.ArrayList;
import java.util.List;

import co.edu.uptc.model.Node;
import co.edu.uptc.model.Station;
import co.edu.uptc.persistence.PersistenceManager;
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
        RouteTree loaded = PersistenceManager.getInstance().getRouteDAO().load(FILE_PATH);
        if (loaded != null) {
            this.root = loaded.getRoot();
        } else {
            // si no existe el archivo, inicializar con raíz base
            root = null;
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
        if (current == null) return false;
        
        if (existsRec(root, newStation.getCode())) {
            return false;
        }
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

    // Buscar la ruta mas corta entre dos ubicaciones
    public List<Station> searchShortRoute(String startLocation, String endLocation) {
        List<Node> pathOrigin = searchRec(root, startLocation); // buscar solo la estación de inicio
        List<Node> pathDest = searchRec(root, endLocation); // buscar solo la estación de fin

        if (pathOrigin == null || pathDest == null) {
            return null; // alguna estación no existe
        }

        // Encontrar ancestro común más cercano
        int i = 0;
        while (i < pathOrigin.size() && i < pathDest.size()
                    && pathOrigin.get(i).getStation().getLocation().equals(pathDest.get(i).getStation().getLocation())) {
                i++;
            }
        i--; // último ancestro común

        // Construir ruta: subir desde origen al ancestro + bajar hacia destino
        List<Station> route = new ArrayList<>();

        // Subir desde origen hasta ancestro (sin incluir ancestro)
        for (int j = pathOrigin.size() - 1; j > i; j--) {
            route.add(pathOrigin.get(j).getStation());
        }

        // Agregar ancestro común
        route.add(pathOrigin.get(i).getStation());

        // Bajar hacia destino (empezando después del ancestro)
        for (int j = i + 1; j < pathDest.size(); j++) {
            route.add(pathDest.get(j).getStation());
        }

        return route;
    }
    

    private List<Node> searchRec(Node current, String location) {
        if (current == null) return null;

        if (current.getStation().getLocation().equals(location)) {
            List<Node> path = new ArrayList<>();
            path.add(current);
            return path;
        }

        for (Node child : current.getChildren()) {
            List<Node> subPath = searchRec(child, location);
            if (subPath != null){
                subPath.add(0, current); // Insertar al inicio para mantener el orden desde la raíz
                return subPath;
            }
        }
        return null;
    }

    private boolean existsRec(Node current, String code) {
        if (current == null) return false;

        if (current.getStation().getCode().equals(code)) {
            return true;
        }

        for (Node child : current.getChildren()) {
            if (existsRec(child, code)) {
                return true;
            }
        }
        return false;
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

    // Eliminar un nodo dado su código
    public boolean deleteStation(String code) {
        if (root == null) return false;

        // Si la raíz es la que queremos eliminar
        if (root.getStation().getCode().equals(code)) {
            root = null;
            save();
            return true;
        }

        boolean removed = removeRec(root, code);
        if (removed) save();
        return removed;
    }

    private boolean removeRec(Node parent, String code) {
        for (Node child : parent.getChildren()) {
            if (child.getStation().getCode().equals(code)) {
                parent.getChildren().remove(child);
                return true;
            }
            if (removeRec(child, code)) {
                return true;
            }
        }
        return false;
    }

    // Editar una estación existente por su código
    public boolean editStation(String code, String newStationLocation, String newStationCode) {
        Node node = findNode(root, code);
        if (node == null) {
            return false; // no existe la estación a editar
        }

        // Verificar duplicado SOLO si el código cambia
        if (!code.equals(newStationCode) && existsRec(root, newStationCode)) {
            return false; // ya existe otra estación con ese código
        }

        // Actualizar los datos
        node.getStation().setLocation(newStationLocation);
        node.getStation().setCode(newStationCode);

        save();
        return true;
    }


    // Buscar nodo por código
    private Node findNode(Node current, String code) {
        if (current == null) return null;
        if (current.getStation().getCode().equals(code)) return current;

        for (Node child : current.getChildren()) {
            Node result = findNode(child, code);
            if (result != null) return result;
        }
        return null;
    }

    // Guardar árbol en XML
    public void save(){
        PersistenceManager.getInstance().getRouteDAO().save(this, FILE_PATH);
    }

    // --- Getters y setters ---
    public Node getRoot() {
        return root;
    }

    public void setRoot(Node root) {
        this.root = root;
    }
}
