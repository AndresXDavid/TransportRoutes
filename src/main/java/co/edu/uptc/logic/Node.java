package co.edu.uptc.logic;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

@XmlAccessorType(XmlAccessType.FIELD)
public class Node {

    @XmlElement(name = "station")
    private Station station;

    @XmlElementWrapper(name = "children")   // agrupador
    @XmlElement(name = "node")              // cada hijo es un <node>
    private List<Node> children = new ArrayList<>();

    public Node() {
    }

    public Node(Station station) {
        this.station = station;
        this.children = new ArrayList<>();
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public List<Node> getChildren() {
        if (children == null) {
            children = new ArrayList<>();
        }
        return children;
    }

    public void setChildren(List<Node> children) {
        this.children = children;
    }

    public void addChild(Node child) {
        getChildren().add(child);
    }
}
