package co.edu.uptc.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlTransient;

/**
 * Nodo del árbol N-ario que representa una estación y sus conexiones hijas.
 * <p>
 * - El campo {@code station} se (un)marshallea por JAXB.
 * - {@code children} se envuelve con {@code <children>} para claridad en XML.
 * - {@code parent} está marcado {@code @XmlTransient} para evitar ciclos en la serialización.
 * </p>
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Node {

    @XmlElement(name = "station", required = true)
    private Station station;

    @XmlElementWrapper(name = "children")
    @XmlElement(name = "node")
    private List<Node> children;

    @XmlTransient
    private Node parent;

    /**
     * Constructor sin-args requerido por JAXB.
     */
    public Node() {
        // JAXB necesita constructor público sin argumentos
    }

    /**
     * Crea un nodo con la estación dada.
     *
     * @param station estación que contiene el nodo (no nula idealmente)
     */
    public Node(Station station) {
        this.station = station;
    }

    /**
     * Devuelve la estación asociada a este nodo.
     *
     * @return estación
     */
    public Station getStation() {
        return station;
    }

    /**
     * Establece la estación asociada a este nodo.
     *
     * @param station objeto Station
     */
    public void setStation(Station station) {
        this.station = station;
    }

    /**
     * Devuelve la lista de hijos. Si no existe, se crea una lista vacía.
     *
     * @return lista de nodos hijos (nunca null)
     */
    public List<Node> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    /**
     * Reemplaza la lista de hijos.
     *
     * @param children lista de nodos hijos
     */
    public void setChildren(List<Node> children) {
        this.children = children;
        if (this.children != null) {
            for (Node c : this.children) {
                c.parent = this;
            }
        }
    }

    /**
     * Añade un hijo y establece su referencia a padre.
     *
     * @param child nodo hijo a añadir
     */
    public void addChild(Node child) {
        if (child == null) return;
        getChildren().add(child);
        child.parent = this;
    }

    /**
     * Elimina un hijo (si existe) y limpia su referencia a padre.
     *
     * @param child nodo hijo a eliminar
     * @return {@code true} si se eliminó, {@code false} si no estaba presente
     */
    public boolean removeChild(Node child) {
        if (child == null || children == null) return false;
        boolean removed = children.remove(child);
        if (removed) {
            child.parent = null;
        }
        return removed;
    }

    /**
     * Devuelve el padre de este nodo (puede ser null si es la raíz).
     *
     * @return nodo padre o null
     */
    public Node getParent() {
        return parent;
    }

    /**
     * Establece el padre (uso interno; cuidado si se expone públicamente).
     *
     * @param parent nodo padre
     */
    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        return station == null ? "null" : station.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;
        // si la estación está presente, comparar por su código
        if (station != null && node.station != null) {
            return Objects.equals(station.getCode(), node.station.getCode());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return station == null ? 0 : Objects.hash(station.getCode());
    }
}